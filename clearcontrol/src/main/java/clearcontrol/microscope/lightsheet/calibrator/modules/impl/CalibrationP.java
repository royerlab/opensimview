package clearcontrol.microscope.lightsheet.calibrator.modules.impl;

import clearcontrol.core.math.functions.UnivariateAffineFunction;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.calibrator.CalibrationEngine;
import clearcontrol.microscope.lightsheet.calibrator.modules.CalibrationBase;
import clearcontrol.microscope.lightsheet.calibrator.modules.CalibrationModuleInterface;
import clearcontrol.microscope.lightsheet.calibrator.utils.ImageAnalysisUtils;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheetInterface;
import clearcontrol.microscope.lightsheet.configurationstate.ConfigurationState;
import clearcontrol.microscope.lightsheet.configurationstate.HasStateDescriptionPerLightSheet;
import clearcontrol.stack.OffHeapPlanarStack;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Math.abs;
import static java.lang.Math.log;

/**
 * Lightsheets power calibration module
 *
 * @author royer
 */
public class CalibrationP extends CalibrationBase implements CalibrationModuleInterface, HasStateDescriptionPerLightSheet
{

  BoundedVariable<Integer> mNumberOfSamplesVariable = new BoundedVariable<Integer>("Number of samples", 6, 0, Integer.MAX_VALUE);

  private BoundedVariable<Double> mExposureTimeInSecondsVariable = new BoundedVariable<Double>("Exposure time in seconds", 0.5, 0.0, Double.MAX_VALUE, 0.001);

  BoundedVariable<Integer> mDetectionArmVariable;

  private BoundedVariable<Integer> mMaxIterationsVariable = new BoundedVariable<Integer>("Maximum number of iterations", 3, 0, Integer.MAX_VALUE);

  private BoundedVariable<Double> mStoppingConditionErrorThreshold = new BoundedVariable<Double>("Stopping condition error threshold", 0.04, 0.0, Double.MAX_VALUE, 0.001);

  private TDoubleArrayList mRatioList;

  /**
   * Instantiates a lightsheets power calibration module
   *
   * @param pCalibrator parent calibrator
   */
  public CalibrationP(CalibrationEngine pCalibrator)
  {
    super("P", pCalibrator);
    mDetectionArmVariable = new BoundedVariable<Integer>("Detection arm", 0, 0, pCalibrator.getLightSheetMicroscope().getNumberOfDetectionArms());
  }

  public void calibrateAllLightSheets()
  {
    int lIteration = 0;
    double lError = Double.POSITIVE_INFINITY;
    do
    {
      setConfigurationState(ConfigurationState.fromProgressValue((double) lIteration / mMaxIterationsVariable.get()));

      if (!calibrate())
      {
        setConfigurationState(ConfigurationState.FAILED);
        return;
      }
      lError = apply();

      info("############################################## Error = " + lError);

    } while (lError >= mStoppingConditionErrorThreshold.get() && lIteration++ < mMaxIterationsVariable.get());
    info("############################################## Done ");

    if (Double.isNaN(lError))
    {
      setConfigurationState(ConfigurationState.FAILED);
    } else if (lError < mStoppingConditionErrorThreshold.get())
    {
      setConfigurationState(ConfigurationState.SUCCEEDED);
    } else
    {
      setConfigurationState(ConfigurationState.ACCEPTABLE);
    }

  }

  /**
   * Calibrates the lightsheets power
   *
   * @return true for success
   */
  private boolean calibrate()
  {
    int lNumberOfLightSheets = getNumberOfLightSheets();

    TDoubleArrayList lAverageIntensityList = new TDoubleArrayList();
    for (int lLightSheetIndex = 0; lLightSheetIndex < lNumberOfLightSheets; lLightSheetIndex++)
    {
      Double lValue = calibrate(lLightSheetIndex, mDetectionArmVariable.get(), mNumberOfSamplesVariable.get());
      if (lValue == null) return false;
      lAverageIntensityList.add(lValue);
    }

    System.out.format("Average image intensity list: %s \n", lAverageIntensityList);

    double lWeakestLightSheetIntensity = lAverageIntensityList.min();

    System.out.format("Weakest lightsheet intensity: %g \n", lWeakestLightSheetIntensity);

    mRatioList = new TDoubleArrayList();
    for (int l = 0; l < lNumberOfLightSheets; l++)
      mRatioList.add(lWeakestLightSheetIntensity / lAverageIntensityList.get(l));

    System.out.format("Intensity ratios list: %s \n", mRatioList);

    return true;
  }

  /**
   * Calibrates the power of a given lightsheet usinga given detection arm
   *
   * @param pLightSheetIndex   lightsheet index
   * @param pDetectionArmIndex detection arm
   * @param pNumberOfSamples   number of samples
   * @return average intensity
   */
  private Double calibrate(int pLightSheetIndex, int pDetectionArmIndex, int pNumberOfSamples)
  {
    try
    {

      LightSheetInterface lLightSheetDevice = getLightSheetMicroscope().getDeviceLists().getDevice(LightSheetInterface.class, pLightSheetIndex);

      double lMaxHeight = lLightSheetDevice.getHeightVariable().getMax().doubleValue();

      double lMiddleZ = lLightSheetDevice.getZVariable().get().doubleValue();// (lMaxZ - lMinZ) / 2;

      LightSheetMicroscopeQueue lQueue = getLightSheetMicroscope().requestQueue();
      lQueue.clearQueue();
      lQueue.setFullROI();
      lQueue.setExp(mExposureTimeInSecondsVariable.get());
      lQueue.setI(pLightSheetIndex);
      lQueue.setIX(pLightSheetIndex, 0);
      lQueue.setIY(pLightSheetIndex, 0);
      lQueue.setIH(pLightSheetIndex, lMaxHeight);
      lQueue.setIP(pLightSheetIndex, 1);

      lQueue.setDZ(lMiddleZ);
      lQueue.setIZ(pLightSheetIndex, lMiddleZ);

      lQueue.setC(false);
      lQueue.addCurrentStateToQueue();
      lQueue.addCurrentStateToQueue();

      for (int i = 1; i <= pNumberOfSamples; i++)
      {
        lQueue.setC(true);
        double dz = (i - (pNumberOfSamples - 1) / 2);
        lQueue.setDZ(lMiddleZ + dz);
        lQueue.addCurrentStateToQueue();
      }

      lQueue.addVoxelDimMetaData(getLightSheetMicroscope(), 10);

      lQueue.finalizeQueue();
      // Building queue end.

      getLightSheetMicroscope().useRecycler("adaptation", 1, 4, 4);
      final Boolean lPlayQueueAndWait = getLightSheetMicroscope().playQueueAndWaitForStacks(lQueue, lQueue.getQueueLength(), TimeUnit.SECONDS);

      if (!lPlayQueueAndWait) return null;

      final OffHeapPlanarStack lStack = (OffHeapPlanarStack) getLightSheetMicroscope().getCameraStackVariable(pDetectionArmIndex).get();

      long lWidth = lStack.getWidth();
      long lHeight = lStack.getHeight();

      System.out.format("Image: width=%d, height=%d \n", lWidth, lHeight);

      double lAverageIntensity = ImageAnalysisUtils.computeImageAverageIntensity(lStack);

      System.out.format("Image: average intensity: %s \n", lAverageIntensity);

      return lAverageIntensity;
    } catch (InterruptedException | ExecutionException | TimeoutException e)
    {
      e.printStackTrace();
      return null;
    }

  }

  /**
   * Applies correction to the lighsheets power settings
   *
   * @return residual error
   */
  public double apply()
  {
    int lNumberOfLightSheets = getNumberOfLightSheets();

    double lError = 0;

    for (int lLightSheetIndex = 0; lLightSheetIndex < lNumberOfLightSheets; lLightSheetIndex++)
    {
      System.out.format("Light sheet index: %d \n", lLightSheetIndex);

      LightSheetInterface lLightSheetDevice = getLightSheetMicroscope().getDeviceLists().getDevice(LightSheetInterface.class, lLightSheetIndex);

      Variable<UnivariateAffineFunction> lPowerFunctionVariable = lLightSheetDevice.getPowerFunction();

      double lPowerRatio = mRatioList.get(lLightSheetIndex);

      if (lPowerRatio == 0 || Double.isNaN(lPowerRatio))
      {
        warning("Power ratio is null or NaN or infinite (%g)", lPowerRatio);
        setConfigurationState(ConfigurationState.FAILED);
        continue;
      }

      System.out.format("Applying power ratio correction: %g to lightsheet %d \n", lPowerRatio, lLightSheetIndex);

      lPowerFunctionVariable.get().composeWith(UnivariateAffineFunction.axplusb(lPowerRatio, 0));
      lPowerFunctionVariable.setCurrent();

      System.out.format("Power function for lightsheet %d is now: %s \n", lLightSheetIndex, lPowerFunctionVariable.get());

      lError += abs(log(lPowerRatio));

      if (getCalibrationEngine().isStopRequested())
      {
        setConfigurationState(ConfigurationState.CANCELLED);
        return Double.NaN;
      }
    }
    setConfigurationState(ConfigurationState.SUCCEEDED);

    System.out.format("Error after applying power ratio correction: %g \n", lError);

    return lError;
  }

  /**
   * Resets calibration
   */
  @Override
  public void reset()
  {
    int lNumberOfLightSheets = getNumberOfLightSheets();

    for (int lLightSheetIndex = 0; lLightSheetIndex < lNumberOfLightSheets; lLightSheetIndex++)
    {
      getLightSheetMicroscope().getDeviceLists().getDevice(LightSheetInterface.class, lLightSheetIndex).getPowerFunction().set(UnivariateAffineFunction.axplusb(1, 0));
    }

    setConfigurationState(ConfigurationState.UNINITIALIZED);

  }

  public BoundedVariable<Double> getExposureTimeInSecondsVariable()
  {
    return mExposureTimeInSecondsVariable;
  }

  public BoundedVariable<Integer> getNumberOfSamplesVariable()
  {
    return mNumberOfSamplesVariable;
  }

  public BoundedVariable<Integer> getDetectionArmVariable()
  {
    return mDetectionArmVariable;
  }

  public BoundedVariable<Integer> getMaxIterationsVariable()
  {
    return mMaxIterationsVariable;
  }

  public BoundedVariable<Double> getStoppingConditionErrorThreshold()
  {
    return mStoppingConditionErrorThreshold;
  }

  @Override
  public String getStateDescription(int pLightSheetIndex)
  {
    final LightSheetInterface lLightSheetDevice = getLightSheetMicroscope().getDeviceLists().getDevice(LightSheetInterface.class, pLightSheetIndex);

    UnivariateAffineFunction lUnivariateAffineFunction = lLightSheetDevice.getPowerFunction().get();

    return String.format("y = %.3f * x + %.3f", lUnivariateAffineFunction.getSlope(), lUnivariateAffineFunction.getConstant());
  }

  @Override
  public String getStateDescription()
  {
    String result = null;
    int lNumberOfLightSheets = getNumberOfLightSheets();
    for (int lLightSheetIndex = 0; lLightSheetIndex < lNumberOfLightSheets; lLightSheetIndex++)
    {
      if (result == null)
      {
        result = getStateDescription(lLightSheetIndex);
      } else
      {
        result = result + "\n" + getStateDescription(lLightSheetIndex);
      }
    }
    return result;
  }
}
