package clearcontrol.calibrator.modules.impl;

import clearcontrol.core.math.argmax.ArgMaxFinder1DInterface;
import clearcontrol.core.math.argmax.SmartArgMaxFinder;
import clearcontrol.core.math.functions.UnivariateAffineFunction;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface.ChartType;
import clearcontrol.LightSheetMicroscopeQueue;
import clearcontrol.calibrator.CalibrationEngine;
import clearcontrol.calibrator.modules.CalibrationModuleInterface;
import clearcontrol.calibrator.modules.CalibrationPerLightSheetBase;
import clearcontrol.calibrator.utils.ImageAnalysisUtils;
import clearcontrol.component.lightsheet.LightSheetInterface;
import clearcontrol.configurationstate.ConfigurationState;
import clearcontrol.configurationstate.HasStateDescriptionPerLightSheet;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.sourcesink.sink.RawFileStackSink;
import gnu.trove.list.array.TDoubleArrayList;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Math.abs;
import static java.lang.Math.min;

/**
 * Lightsheet A angle calibration module
 *
 * @author royer
 */
public class CalibrationA extends CalibrationPerLightSheetBase implements CalibrationModuleInterface, HasStateDescriptionPerLightSheet
{

  private ArgMaxFinder1DInterface mArgMaxFinder;
  private HashMap<Integer, UnivariateAffineFunction> mModels;

  private BoundedVariable<Double> mAngleOptimisationRangeWidthVariable = new BoundedVariable<Double>("Angle range in degrees", 20.0, 0.0, 40.0, 1.0);

  private BoundedVariable<Double> mExposureTimeInSecondsVariable = new BoundedVariable<Double>("Exposure time in seconds", 0.04, 0.0, Double.MAX_VALUE, 0.001);

  private BoundedVariable<Integer> mNumberOfRepeatsVariable = new BoundedVariable<Integer>("Number of repeats", 4, 1, Integer.MAX_VALUE);

  private BoundedVariable<Integer> mNumberOfAnglesVariable = new BoundedVariable<Integer>("Number of angles", 32, 1, Integer.MAX_VALUE);

  private BoundedVariable<Integer> mMaxIterationsVariable = new BoundedVariable<Integer>("Maximum number of iterations", 3, 0, Integer.MAX_VALUE);

  private BoundedVariable<Double> mStoppingConditionErrorThreshold = new BoundedVariable<Double>("Stopping condition error threshold", 0.5, 0.0, Double.MAX_VALUE, 0.001);

  private BoundedVariable<Double> mLightSheetWidthWhileImaging = new BoundedVariable<Double>("Light sheet width while imaging", 0.25, 0.0, 1.0, 0.01);
  private BoundedVariable<Double> mYRangeVariable = new BoundedVariable<Double>("Y range for testing", 1.0, 0.0, 1.0, 0.01);
  private BoundedVariable<Double> mZRangeVariable = new BoundedVariable<Double>("Z range for testing", 1.0, 0.0, 1.0, 0.01);
  private Variable<String> mDebugPath = new Variable<String>("Debug path", "");

  /**
   * Lightsheet Alpha angle calibration module
   *
   * @param pCalibrator parent calibrator
   */
  public CalibrationA(CalibrationEngine pCalibrator)
  {
    super("A", pCalibrator);
    mModels = new HashMap<>();
  }

  /**
   * Calibrates the Alpha angle for a given lightsheet, number of angles and number of
   * repeats.
   *
   * @param pLightSheetIndex lightsheet index
   */
  public double calibrate(int pLightSheetIndex)
  {
    int lIteration = 0;
    double lError = Double.POSITIVE_INFINITY;
    int lNumberOfDetectionArmDevices = getNumberOfDetectionArms();
    do
    {
      setConfigurationState(pLightSheetIndex, ConfigurationState.fromProgressValue((double) lIteration / mMaxIterationsVariable.get()));

      lError = calibrate(pLightSheetIndex, lNumberOfDetectionArmDevices);
      info("############################################## Error = " + lError);

      if (getCalibrationEngine().isStopRequested())
      {
        setConfigurationState(pLightSheetIndex, ConfigurationState.CANCELLED);
        return Double.NaN;
      }
    } while (lError >= mStoppingConditionErrorThreshold.get() && lIteration++ < mMaxIterationsVariable.get());
    info("############################################## Done ");

    if (Double.isNaN(lError))
    {
      setConfigurationState(pLightSheetIndex, ConfigurationState.FAILED);
    } else if (lError < mStoppingConditionErrorThreshold.get())
    {
      setConfigurationState(pLightSheetIndex, ConfigurationState.SUCCEEDED);
    } else
    {
      setConfigurationState(pLightSheetIndex, ConfigurationState.ACCEPTABLE);
    }

    return lError;
  }

  /**
   * Calibrates the lightsheet alpha angles.
   *
   * @param pLightSheetIndex lightsheet index number of repeats
   * @return true when succeeded
   */
  private double calibrate(int pLightSheetIndex, int pNumberOfDetectionArmDevices)
  {
    mArgMaxFinder = new SmartArgMaxFinder();

    LightSheetInterface lLightSheet = getLightSheetMicroscope().getDeviceLists().getDevice(LightSheetInterface.class, pLightSheetIndex);

    System.out.println("Current Alpha function: " + lLightSheet.getAlphaFunction());

    double lMinA = -mAngleOptimisationRangeWidthVariable.get() / 2.0;
    double lMaxA = mAngleOptimisationRangeWidthVariable.get() / 2.0;

    double lMinIY = lLightSheet.getYVariable().getMin().doubleValue();
    double lMaxIY = lLightSheet.getYVariable().getMax().doubleValue();

    if (Math.abs(mYRangeVariable.get() - 1.0) > 0.001)
    {
      double lDeltaRange = (lMaxIY - lMinIY) * (1.0 - mYRangeVariable.get()) / 2.0;
      lMinIY += lDeltaRange;
      lMaxIY -= lDeltaRange;
    }

    double lMinZ = lLightSheet.getZVariable().getMin().doubleValue();
    double lMaxZ = lLightSheet.getZVariable().getMax().doubleValue();

    if (Math.abs(mZRangeVariable.get() - 1.0) > 0.001)
    {
      double lDeltaRange = (lMaxZ - lMinZ) * (1.0 - mZRangeVariable.get()) / 2.0;
      lMinZ += lDeltaRange;
      lMaxZ -= lDeltaRange;
    }

    double[] angles = new double[pNumberOfDetectionArmDevices];
    int lCount = 0;

    double y = 0.5 * min(abs(lMinIY), abs(lMaxIY));
    double z = 0.5 * (lMaxZ + lMinZ);

    int lNumberOfRepeats = mNumberOfRepeatsVariable.get();
    int lNumberOfAngles = mNumberOfAnglesVariable.get();

    for (int r = 0; r < lNumberOfRepeats; r++)
    {
      System.out.format("Searching for optimal alpha angles for lighsheet at y=+/-%g \n", y);

      final double[] anglesM = focusA(pLightSheetIndex, lMinA, lMaxA, (lMaxA - lMinA) / (lNumberOfAngles - 1), -y, z);

      final double[] anglesP = focusA(pLightSheetIndex, lMinA, lMaxA, (lMaxA - lMinA) / (lNumberOfAngles - 1), +y, z);

      System.out.format("Optimal alpha angles for lighsheet at y=%g: %s \n", -y, Arrays.toString(anglesM));
      System.out.format("Optimal alpha angles for lighsheet at y=%g: %s \n", +y, Arrays.toString(anglesP));

      boolean lValid = true;

      for (int i = 0; i < pNumberOfDetectionArmDevices; i++)
      {
        lValid &= !Double.isNaN(anglesM[i]) && !Double.isNaN(anglesM[i]);
      }

      if (lValid)
      {
        System.out.format("Angle values are valid, we proceed... \n");
        for (int i = 0; i < pNumberOfDetectionArmDevices; i++)
        {
          angles[i] += 0.5 * (anglesM[i] + anglesP[i]);
        }

        lCount++;
      } else System.out.format("Angle are not valid, we continue with next set of y values... \n");

      if (getCalibrationEngine().isStopRequested())
      {
        setConfigurationState(pLightSheetIndex, ConfigurationState.CANCELLED);
        return Double.NaN;
      }
    }

    if (lCount == 0)
    {
      return Double.NaN;
    }

    for (int i = 0; i < pNumberOfDetectionArmDevices; i++)
    {
      angles[i] = angles[i] / lCount;
    }
    System.out.format("Averaged alpha angles: %s \n", Arrays.toString(angles));

    double angle = 0;
    for (int i = 0; i < pNumberOfDetectionArmDevices; i++)
    {
      angle += angles[i];
    }
    angle /= pNumberOfDetectionArmDevices;

    System.out.format("Average alpha angle for all detection arms (assumes that the cameras are well aligned): %s \n", angle);

    UnivariateAffineFunction lUnivariateAffineFunction = new UnivariateAffineFunction(1, angle);
    mModels.put(pLightSheetIndex, lUnivariateAffineFunction);

    System.out.format("Corresponding model: %s \n", lUnivariateAffineFunction);

    return apply(pLightSheetIndex);
  }

  private double[] focusA(int pLightSheetIndex, double pMinA, double pMaxA, double pStep, double pY, double pZ)
  {
    try
    {
      int lNumberOfDetectionArmDevices = getNumberOfDetectionArms();

      LightSheetMicroscopeQueue lQueue = getLightSheetMicroscope().requestQueue();

      final TDoubleArrayList lAList = new TDoubleArrayList();
      double[] angles = new double[lNumberOfDetectionArmDevices];

      lQueue.clearQueue();
      // lQueue.zero();

      lQueue.setFullROI();
      lQueue.setExp(mExposureTimeInSecondsVariable.get());

      lQueue.setI(pLightSheetIndex);
      lQueue.setIX(pLightSheetIndex, 0);
      lQueue.setIY(pLightSheetIndex, pY);
      lQueue.setIZ(pLightSheetIndex, pZ);
      lQueue.setIW(pLightSheetIndex, mLightSheetWidthWhileImaging.get());
      lQueue.setIH(pLightSheetIndex, 0);
      lQueue.setIA(pLightSheetIndex, pMinA);

      for (int i = 0; i < lNumberOfDetectionArmDevices; i++)
      {
        lQueue.setDZ(i, pZ);
        lQueue.setC(i, false);
      }
      lQueue.addCurrentStateToQueue();

      for (int i = 0; i < lNumberOfDetectionArmDevices; i++)
        lQueue.setC(i, true);

      for (double a = pMinA; a <= pMaxA; a += pStep)
      {
        lAList.add(a);
        lQueue.setIA(pLightSheetIndex, a);
        lQueue.addCurrentStateToQueue();
      }

      lQueue.setIA(pLightSheetIndex, pMinA);
      for (int i = 0; i < lNumberOfDetectionArmDevices; i++)
      {
        lQueue.setC(i, false);
      }
      lQueue.addCurrentStateToQueue();

      lQueue.addVoxelDimMetaData(getLightSheetMicroscope(), 10);

      lQueue.finalizeQueue();

      getLightSheetMicroscope().useRecycler("adaptation", 1, 4, 4);
      final Boolean lPlayQueueAndWait = getLightSheetMicroscope().playQueueAndWaitForStacks(lQueue, lQueue.getQueueLength(), TimeUnit.SECONDS);

      if (lPlayQueueAndWait)
      {
        for (int i = 0; i < lNumberOfDetectionArmDevices; i++)
        {
          final OffHeapPlanarStack lStack = (OffHeapPlanarStack) getLightSheetMicroscope().getCameraStackVariable(i).get();

          final double[] lAvgIntensityArray = ImageAnalysisUtils.computeAverageSquareVariationPerPlane(lStack);

          if (mDebugPath.get().length() > 0)
          {
            String timepoint = "" + System.currentTimeMillis();
            RawFileStackSink lRawFileStackSink = new RawFileStackSink();
            lRawFileStackSink.setLocation(new File(mDebugPath.get()), "tempA" + timepoint);
            lRawFileStackSink.appendStack(lStack);
            lRawFileStackSink.close();
            info("Saved as " + timepoint);
          }
          smooth(lAvgIntensityArray, 10);

          String lChartName = String.format("D=%d, I=%d, IY=%g", i, pLightSheetIndex, pY);

          getCalibrationEngine().configureChart(lChartName, "samples", "DZ", "IZ", ChartType.Line);

          for (int j = 0; j < lAvgIntensityArray.length; j++)
          {
            getCalibrationEngine().addPoint(lChartName, "samples", j == 0, lAList.get(j), lAvgIntensityArray[j]);

          }

          final Double lArgMax = mArgMaxFinder.argmax(lAList.toArray(), lAvgIntensityArray);

          if (lArgMax != null)
          {
            TDoubleArrayList lAvgIntensityList = new TDoubleArrayList(lAvgIntensityArray);

            double lAmplitudeRatio = (lAvgIntensityList.max() - lAvgIntensityList.min()) / lAvgIntensityList.max();

            System.out.format("argmax=%s amplratio=%s \n", lArgMax.toString(), lAmplitudeRatio);

            // lPlot.setScatterPlot("argmax");
            // lPlot.addPoint("argmax", lArgMax, 0);

            if (lAmplitudeRatio > 0.1 && lArgMax > lAList.get(0))
            {
              angles[i] = lArgMax;
            } else
            {
              angles[i] = Double.NaN;
            }
            /* if (mArgMaxFinder instanceof Fitting1D)
            {
              Fitting1D lFitting1D = (Fitting1D) mArgMaxFinder;
            
              double[] lFit =
                            lFitting1D.fit(lAList.toArray(),
                                           new double[lAList.size()]);
            
              for (int j = 0; j < lAList.size(); j++)
              {
                //lPlot.setScatterPlot("fit");
                //lPlot.addPoint("fit", lAList.get(j), lFit[j]);
              }
            }/**/

          } else
          {
            angles[i] = Double.NaN;
            System.out.println("Argmax is NULL!");
          }
        }
      }
      return angles;

    } catch (final InterruptedException e)
    {
      e.printStackTrace();
    } catch (final ExecutionException e)
    {
      e.printStackTrace();
    } catch (final TimeoutException e)
    {
      e.printStackTrace();
    } catch (IOException e)
    {
      e.printStackTrace();
    }

    return null;

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
   * Applies the Alpha angle calibration correction to a given lightsheet
   *
   * @param pLightSheetIndex lightsheet index
   * @return residual error
   */
  private double apply(int pLightSheetIndex)
  {
    System.out.println("LightSheet index: " + pLightSheetIndex);

    LightSheetInterface lLightSheetDevice = getLightSheetMicroscope().getDeviceLists().getDevice(LightSheetInterface.class, pLightSheetIndex);

    UnivariateAffineFunction lUnivariateAffineFunction = mModels.get(pLightSheetIndex);

    if (lUnivariateAffineFunction == null)
    {
      System.out.format("No model available! \n");
      setConfigurationState(pLightSheetIndex, ConfigurationState.FAILED);
      return Double.POSITIVE_INFINITY;
    }

    Variable<UnivariateAffineFunction> lFunctionVariable = lLightSheetDevice.getAlphaFunction();

    System.out.format("Correction function: %s \n", lUnivariateAffineFunction);

    lFunctionVariable.get().composeWith(lUnivariateAffineFunction);
    lFunctionVariable.setCurrent();

    System.out.format("New alpha function: %s \n", lFunctionVariable.get());

    double lError = abs(lUnivariateAffineFunction.getSlope() - 1) + abs(lUnivariateAffineFunction.getConstant());

    System.out.format("Error: %g \n", lError);
    setConfigurationState(pLightSheetIndex, ConfigurationState.SUCCEEDED);

    return lError;
  }

  /**
   * Resets the Alpha angle calibration
   */
  @Override
  public void reset()
  {
    super.reset();
    mModels.clear();

    for (int lLightSheetIndex = 0; lLightSheetIndex < this.getLightSheetMicroscope().getNumberOfLightSheets(); lLightSheetIndex++)
    {
      setConfigurationState(lLightSheetIndex, ConfigurationState.UNINITIALIZED);
    }
  }

  public BoundedVariable<Double> getAngleOptimisationRangeWidthVariable()
  {
    return mAngleOptimisationRangeWidthVariable;
  }

  public BoundedVariable<Double> getExposureTimeInSecondsVariable()
  {
    return mExposureTimeInSecondsVariable;
  }

  public BoundedVariable<Integer> getNumberOfRepeatsVariable()
  {
    return mNumberOfRepeatsVariable;
  }

  public BoundedVariable<Integer> getNumberOfAnglesVariable()
  {
    return mNumberOfAnglesVariable;
  }

  public BoundedVariable<Integer> getMaxIterationsVariable()
  {
    return mMaxIterationsVariable;
  }

  public BoundedVariable<Double> getStoppingConditionErrorThreshold()
  {
    return mStoppingConditionErrorThreshold;
  }

  public BoundedVariable<Double> getLightSheetWidthWhileImaging()
  {
    return mLightSheetWidthWhileImaging;
  }

  public BoundedVariable<Double> getYRangeVariable()
  {
    return mYRangeVariable;
  }

  public BoundedVariable<Double> getZRangeVariable()
  {
    return mZRangeVariable;
  }

  @Override
  public String getStateDescription(int pLightSheetIndex)
  {
    final LightSheetInterface lLightSheetDevice = getLightSheetMicroscope().getDeviceLists().getDevice(LightSheetInterface.class, pLightSheetIndex);

    UnivariateAffineFunction lUnivariateAffineFunction = lLightSheetDevice.getAlphaFunction().get();

    return String.format("y = %.3f * x + %.3f", lUnivariateAffineFunction.getSlope(), lUnivariateAffineFunction.getConstant());
  }

  @Override
  public String getStateDescription()
  {
    return "";
  }

  public Variable<String> getDebugPath()
  {
    return mDebugPath;
  }
}
