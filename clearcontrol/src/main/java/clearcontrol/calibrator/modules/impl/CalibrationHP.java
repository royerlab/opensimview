package clearcontrol.calibrator.modules.impl;

import clearcontrol.LightSheetMicroscopeQueue;
import clearcontrol.calibrator.CalibrationEngine;
import clearcontrol.calibrator.modules.CalibrationModuleInterface;
import clearcontrol.calibrator.modules.CalibrationPerLightSheetBase;
import clearcontrol.calibrator.utils.ImageAnalysisUtils;
import clearcontrol.component.lightsheet.LightSheetInterface;
import clearcontrol.configurationstate.ConfigurationState;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface.ChartType;
import clearcontrol.stack.OffHeapPlanarStack;
import gnu.trove.list.array.TDoubleArrayList;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.StatUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Math.abs;

/**
 * Calibrates the lighthseet power versus its height
 *
 * @author royer
 */
public class CalibrationHP extends CalibrationPerLightSheetBase implements CalibrationModuleInterface
{

  BoundedVariable<Integer> mNumberOfPSamplesVariable = new BoundedVariable<Integer>("Number of power samples", 6, 0, Integer.MAX_VALUE);
  BoundedVariable<Integer> mNumberOfHSamplesVariable = new BoundedVariable<Integer>("Number of height samples", 6, 0, Integer.MAX_VALUE);

  BoundedVariable<Integer> mDetectionArmVariable;

  private MultiKeyMap<Integer, PolynomialFunction> mHPFunctions;

  /**
   * Instantiates a height-power calibration module
   *
   * @param pCalibrator parent calibrator
   */
  public CalibrationHP(CalibrationEngine pCalibrator)
  {
    super("HP", pCalibrator);

    mDetectionArmVariable = new BoundedVariable<Integer>("Detection arm", 0, 0, pCalibrator.getLightSheetMicroscope().getNumberOfDetectionArms());

    mHPFunctions = new MultiKeyMap<>();
  }

  public double calibrate(int pLightSheetIndex)
  {
    int lDetectionArmIndex = mDetectionArmVariable.get();
    int lNumberOfSamplesH = mNumberOfHSamplesVariable.get();
    int lNumberOfSamplesP = mNumberOfPSamplesVariable.get();

    LightSheetInterface lLightSheet = getLightSheetMicroscope().getDeviceLists().getDevice(LightSheetInterface.class, pLightSheetIndex);

    lLightSheet.getAdaptPowerToWidthHeightVariable().set(false);

    BoundedVariable<Number> lWidthVariable = lLightSheet.getWidthVariable();
    BoundedVariable<Number> lPowerVariable = lLightSheet.getPowerVariable();

    double lMinP = lPowerVariable.getMin().doubleValue();
    double lMaxP = lPowerVariable.getMax().doubleValue();
    double lReferencePower = lMaxP;

    double lMinH = lWidthVariable.getMin().doubleValue();
    double lMaxH = lWidthVariable.getMax().doubleValue();
    double lStepH = (lMaxH - lMinH) / lNumberOfSamplesH;
    double lReferenceH = lMaxH;

    final double lReferenceIntensity = adjustP(pLightSheetIndex, lDetectionArmIndex, lReferencePower, lReferencePower, lNumberOfSamplesP, lReferenceH, 0, true);

    final WeightedObservedPoints lObservations = new WeightedObservedPoints();
    TDoubleArrayList lHList = new TDoubleArrayList();
    TDoubleArrayList lPRList = new TDoubleArrayList();

    for (double h = lMinH; h <= lMaxH; h += lStepH)
    {
      final double lPower = adjustP(pLightSheetIndex, lDetectionArmIndex, lMinP, lMaxP, lNumberOfSamplesP, h, lReferenceIntensity, false);

      double lPowerRatio = lPower / lReferencePower;

      lHList.add(h);
      lPRList.add(lPowerRatio);
      lObservations.add(h, lPowerRatio);

      if (getCalibrationEngine().isStopRequested())
      {
        setConfigurationState(pLightSheetIndex, ConfigurationState.CANCELLED);
        return Double.NaN;
      }
    }

    final PolynomialCurveFitter lPolynomialCurveFitter = PolynomialCurveFitter.create(2);
    final double[] lCoeficients = lPolynomialCurveFitter.fit(lObservations.toList());
    PolynomialFunction lPowerRatioFunction = new PolynomialFunction(lCoeficients);

    mHPFunctions.put(pLightSheetIndex, lDetectionArmIndex, lPowerRatioFunction);

    String lChartName = String.format(" D=%d, I=%d", lDetectionArmIndex, pLightSheetIndex);

    getCalibrationEngine().configureChart(lChartName, "samples", "IH", "power ratio", ChartType.Line);

    getCalibrationEngine().configureChart(lChartName, "fit", "IH", "power ratio", ChartType.Line);

    for (int j = 0; j < lHList.size(); j++)
    {
      getCalibrationEngine().addPoint(lChartName, "samples", j == 0, lHList.get(j), lPRList.get(j));

      getCalibrationEngine().addPoint(lChartName, "fit", j == 0, lHList.get(j), lPRList.get(j));
    }

    info("############################################## Done ");
    return apply(pLightSheetIndex, lDetectionArmIndex);
  }

  private Double adjustP(int pLightSheetIndex, int pDetectionArmIndex, double pMinP, double pMaxP, int pNumberOfSamples, double pH, double pTargetIntensity, boolean pReturnIntensity)
  {
    try
    {

      LightSheetMicroscopeQueue lQueue = getLightSheetMicroscope().requestQueue();
      lQueue.clearQueue();
      lQueue.zero();

      lQueue.setI(pLightSheetIndex);
      lQueue.setIH(pLightSheetIndex, pH);

      final TDoubleArrayList lPList = new TDoubleArrayList();

      lQueue.setIP(pLightSheetIndex, pMinP);
      lQueue.setC(false);
      lQueue.addCurrentStateToQueue();

      lQueue.setC(true);

      double lStep = (pMaxP - pMinP) / pNumberOfSamples;

      for (double p = pMinP, i = 0; p <= pMaxP && i < pNumberOfSamples; p += lStep, i++)
      {
        lPList.add(p);
        lQueue.setIP(pLightSheetIndex, p);
        lQueue.addCurrentStateToQueue();
      }

      lQueue.setIP(pLightSheetIndex, pMinP);
      lQueue.setC(false);
      lQueue.addCurrentStateToQueue();

      lQueue.addVoxelDimMetaData(getLightSheetMicroscope(), 10);

      lQueue.finalizeQueue();

      getLightSheetMicroscope().useRecycler("adaptation", 1, 4, 4);
      final Boolean lPlayQueueAndWait = getLightSheetMicroscope().playQueueAndWaitForStacks(lQueue, lQueue.getQueueLength(), TimeUnit.SECONDS);

      if (lPlayQueueAndWait)
      {
        final OffHeapPlanarStack lStack = (OffHeapPlanarStack) getLightSheetMicroscope().getCameraStackVariable(pDetectionArmIndex).get();
        // final double[] lDCTSArray =
        // mDCTS2D.computeImageQualityMetric(lImage);
        final double[] lRobustmaxIntensityArray = ImageAnalysisUtils.computePercentileIntensityPerPlane(lStack, 99);

        smooth(lRobustmaxIntensityArray, 1);

        String lChartName = String.format("Mode=%s, D=%d, I=%d, H=%g", pReturnIntensity ? "ret_int" : "ret_pow", pDetectionArmIndex, pLightSheetIndex, pH);

        getCalibrationEngine().configureChart(lChartName, "samples", "IP", "max intensity", ChartType.Line);

        // System.out.format("metric array: \n");
        for (int j = 0; j < lRobustmaxIntensityArray.length; j++)
        {
          getCalibrationEngine().addPoint(lChartName, "samples", j == 0, lPList.get(j), lRobustmaxIntensityArray[j]);

        }

        if (pReturnIntensity)
        {
          double lAvgIntensity = StatUtils.percentile(lRobustmaxIntensityArray, 50);
          return lAvgIntensity;
        } else
        {
          int lIndex = find(lRobustmaxIntensityArray, pTargetIntensity);

          double lPower = lPList.get(lIndex);

          return lPower;
        }

      }

    } catch (final InterruptedException e)
    {
      e.printStackTrace();
    } catch (final ExecutionException e)
    {
      e.printStackTrace();
    } catch (final TimeoutException e)
    {
      e.printStackTrace();
    }

    return null;

  }

  private int find(double[] pArray, double pValueToFind)
  {
    int lIndex = -1;
    double lMinDistance = Double.POSITIVE_INFINITY;
    for (int i = 0; i < pArray.length; i++)
    {
      double lValue = pArray[i];
      double lDistance = abs(lValue - pValueToFind);
      if (lDistance < lMinDistance)
      {
        lMinDistance = lDistance;
        lIndex = i;
      }
    }

    return lIndex;
  }

  private void smooth(double[] pMetricArray, int pIterations)
  {

    for (int j = 0; j < pIterations; j++)
    {
      for (int i = 1; i < pMetricArray.length - 1; i++)
      {
        pMetricArray[i] = (pMetricArray[i - 1] + pMetricArray[i] + pMetricArray[i + 1]) / 3;
      }

      for (int i = pMetricArray.length - 2; i >= 1; i--)
      {
        pMetricArray[i] = (pMetricArray[i - 1] + pMetricArray[i] + pMetricArray[i + 1]) / 3;
      }
    }

  }

  /**
   * Applies the height-power calibration correction for a given lightsheet and detection
   * arm.
   *
   * @param pLightSheetIndex   lightsheet index
   * @param pDetectionArmIndex detection arm
   * @return residual error
   */
  private double apply(int pLightSheetIndex, int pDetectionArmIndex)
  {
    System.out.println("LightSheet index: " + pLightSheetIndex);

    LightSheetInterface lLightSheetDevice = getLightSheetMicroscope().getDeviceLists().getDevice(LightSheetInterface.class, pLightSheetIndex);

    PolynomialFunction lNewWidthPowerFunction = mHPFunctions.get(pLightSheetIndex, pDetectionArmIndex);

    Variable<PolynomialFunction> lCurrentHeightFunctionVariable = lLightSheetDevice.getHeightPowerFunction();

    System.out.format("Current HeightPower function: %s \n", lCurrentHeightFunctionVariable.get());

    lCurrentHeightFunctionVariable.set(lNewWidthPowerFunction);

    System.out.format("New HeightPower function: %s \n", lCurrentHeightFunctionVariable.get());

    setConfigurationState(pLightSheetIndex, ConfigurationState.SUCCEEDED);

    double lError = 0;

    return lError;
  }

  /**
   * Resets the height-power calibration
   */
  @Override
  public void reset()
  {
    super.reset();
    mHPFunctions.clear();

    for (int i = 0; i < this.getLightSheetMicroscope().getNumberOfLightSheets(); i++)
    {
      setConfigurationState(i, ConfigurationState.UNINITIALIZED);
    }
  }

}
