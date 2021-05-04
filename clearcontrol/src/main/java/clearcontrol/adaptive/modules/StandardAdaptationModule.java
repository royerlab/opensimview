package clearcontrol.adaptive.modules;

import clearcontrol.core.concurrent.executors.AsynchronousExecutorFeature;
import clearcontrol.core.device.name.ReadOnlyNameableInterface;
import clearcontrol.core.math.argmax.SmartArgMaxFinder;
import clearcontrol.core.variable.Variable;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface.ChartType;
import clearcontrol.ip.iqm.DCTS2D;
import clearcontrol.LightSheetDOF;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.LightSheetMicroscopeQueue;
import clearcontrol.adaptive.controlplanestate.ControlPlaneStateListener;
import clearcontrol.adaptive.controlplanestate.HasControlPlaneState;
import clearcontrol.component.detection.DetectionArmInterface;
import clearcontrol.component.lightsheet.LightSheetInterface;
import clearcontrol.configurationstate.CanBeActive;
import clearcontrol.configurationstate.ConfigurationState;
import clearcontrol.configurationstate.ConfigurationStateChangeListener;
import clearcontrol.configurationstate.HasConfigurationState;
import clearcontrol.state.InterpolatedAcquisitionState;
import clearcontrol.state.LightSheetAcquisitionStateInterface;
import clearcontrol.stack.EmptyStack;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.StackInterface;
import clearcontrol.util.NDIterator;
import gnu.trove.list.array.TDoubleArrayList;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ND iterator adaptation module
 *
 * @author royer
 */
public abstract class StandardAdaptationModule extends NDIteratorAdaptationModule<InterpolatedAcquisitionState> implements AdaptationModuleInterface<InterpolatedAcquisitionState>, AsynchronousExecutorFeature, HasConfigurationState, ReadOnlyNameableInterface, HasControlPlaneState, CanBeActive

{

  private final Variable<Integer> mLaserLineVariable = new Variable<Integer>("LaserLine", 0);

  private final Variable<Integer> mNumberOfSamplesVariable = new Variable<Integer>("NumberOfSamples");

  private final Variable<Double> mProbabilityThresholdVariable = new Variable<Double>("ProbabilityThreshold");
  private final Variable<Double> mMaxCorrection = new Variable<Double>("MaxCorrection", 1.0);

  private final Variable<Double> mImageMetricThresholdVariable = new Variable<Double>("MetricThreshold");

  private final Variable<Double> mExposureInSecondsVariable = new Variable<Double>("ExposureInSeconds");

  private final Variable<Double> mLaserPowerVariable = new Variable<Double>("LaserPower");

  private HashMap<Triple<Integer, Integer, Integer>, Result> mResultsMap = new HashMap<>();
  protected LightSheetDOF mLightSheetDOF;

  int[][] mSelectedDetectionArms;
  ConfigurationState[][] mControlPlaneStates = null;
  String[][] mControlPlaneStateDescriptions = null;

  /**
   * Instantiates a ND iterator adaptation module
   *
   * @param pModuleName           module name
   * @param pLightSheetDOF        lightsheet DOF that this module optimizes
   * @param pNumberOfSamples      number of samples
   * @param pProbabilityThreshold probability threshold
   * @param pImageMetricThreshold image metric threshold
   * @param pExposureInSeconds    exposure in seconds
   * @param pLaserPower           laser power
   */
  public StandardAdaptationModule(String pModuleName, LightSheetDOF pLightSheetDOF, int pNumberOfSamples, double pProbabilityThreshold, double pImageMetricThreshold, double pExposureInSeconds, double pLaserPower)
  {
    super(pModuleName);
    mLightSheetDOF = pLightSheetDOF;
    getNumberOfSamplesVariable().set(pNumberOfSamples);
    getProbabilityThresholdVariable().set(pProbabilityThreshold);
    getImageMetricThresholdVariable().set(pImageMetricThreshold);
    getExposureInSecondsVariable().set(pExposureInSeconds);
    getLaserPowerVariable().set(pLaserPower);
  }

  @Override
  public void reset()
  {
    super.reset();

    LightSheetMicroscope lLightsheetMicroscope = (LightSheetMicroscope) getAdaptiveEngine().getMicroscope();

    LightSheetAcquisitionStateInterface<InterpolatedAcquisitionState> lAcquisitionState = getAdaptiveEngine().getAcquisitionStateVariable().get();

    if (lAcquisitionState == null)
    {
      severe("There is no current acquisition state defined!");
      return;
    }

    int lNumberOfControlPlanes = lAcquisitionState.getInterpolationTables().getNumberOfControlPlanes();

    int lNumberOfLighSheets = lLightsheetMicroscope.getDeviceLists().getNumberOfDevices(LightSheetInterface.class);

    setNDIterator(new NDIterator(lNumberOfControlPlanes, lNumberOfLighSheets));

  }

  protected Future<?> findBestDOFValue(int pControlPlaneIndex, int pLightSheetIndex, LightSheetMicroscopeQueue pQueue, InterpolatedAcquisitionState pStackAcquisition, final TDoubleArrayList pDOFValueList)
  {

    try
    {
      LightSheetMicroscope lLightsheetMicroscope = (LightSheetMicroscope) getAdaptiveEngine().getMicroscope();

      if (mControlPlaneStates == null)
      {
        mControlPlaneStates = new ConfigurationState[lLightsheetMicroscope.getNumberOfLightSheets()][pStackAcquisition.getNumberOfControlPlanes()];
        mControlPlaneStateDescriptions = new String[lLightsheetMicroscope.getNumberOfLightSheets()][pStackAcquisition.getNumberOfControlPlanes()];
        mSelectedDetectionArms = new int[lLightsheetMicroscope.getNumberOfLightSheets()][pStackAcquisition.getNumberOfControlPlanes()];
        for (int lLightSheetIndex = 0; lLightSheetIndex < lLightsheetMicroscope.getNumberOfLightSheets(); lLightSheetIndex++)
        {
          for (int lControlPlaneIndex = 0; lControlPlaneIndex < pStackAcquisition.getNumberOfControlPlanes(); lControlPlaneIndex++)
          {
            mControlPlaneStates[lLightSheetIndex][lControlPlaneIndex] = ConfigurationState.UNINITIALIZED;
            mControlPlaneStateDescriptions[lLightSheetIndex][lControlPlaneIndex] = "";
            mSelectedDetectionArms[lLightSheetIndex][lControlPlaneIndex] = -1;
            invokeControlPlaneStateChangeListeners(lLightSheetIndex, lControlPlaneIndex);
          }
        }
      }

      lLightsheetMicroscope.useRecycler("adaptation", 1, 4, 4);
      final Boolean lPlayQueueAndWait = lLightsheetMicroscope.playQueueAndWaitForStacks(pQueue, 10 + pQueue.getQueueLength(), TimeUnit.SECONDS);

      if (!lPlayQueueAndWait)
      {
        mControlPlaneStates[pLightSheetIndex][pControlPlaneIndex] = ConfigurationState.FAILED;
        mControlPlaneStateDescriptions[pLightSheetIndex][pControlPlaneIndex] = "Playing queue failed";

        invokeControlPlaneStateChangeListeners(pLightSheetIndex, pControlPlaneIndex);
        return null;
      }

      final int lNumberOfDetectionArmDevices = lLightsheetMicroscope.getDeviceLists().getNumberOfDevices(DetectionArmInterface.class);

      ArrayList<StackInterface> lStacks = new ArrayList<>();
      for (int d = 0; d < lNumberOfDetectionArmDevices; d++)
      {
        final StackInterface lStackInterface = lLightsheetMicroscope.getCameraStackVariable(d).get();
        lStacks.add(lStackInterface.duplicate());

      }

      Runnable lRunnable = () ->
      {

        try
        {
          SmartArgMaxFinder lSmartArgMaxFinder = new SmartArgMaxFinder();

          String lInfoString = "";

          double lMaxProbability = 0;
          double lMaxMetric = 0;
          double lArgMax = 0;
          int lSelectedDetectionArm = -1;

          for (int pDetectionArmIndex = 0; pDetectionArmIndex < lNumberOfDetectionArmDevices; pDetectionArmIndex++)

          {

            final double[] lMetricArray = computeMetric(pControlPlaneIndex, pLightSheetIndex, pDetectionArmIndex, pDOFValueList, lStacks.get(pDetectionArmIndex));

            if (lMetricArray == null) continue;

            Double lArgmax = lSmartArgMaxFinder.argmax(pDOFValueList.toArray(), lMetricArray);

            Double lFitProbability = lSmartArgMaxFinder.getLastFitProbability();

            if (lArgmax == null || lFitProbability == null)
            {
              lArgmax = 0d;
              lFitProbability = 0d;
            }

            double lMetricMax = Arrays.stream(lMetricArray).max().getAsDouble();

            info("argmax = %s, metric=%s, probability = %s ", lArgmax, lMetricMax, lFitProbability);

            setResult(pControlPlaneIndex, pLightSheetIndex, pDetectionArmIndex, Result.of(lArgmax, lMetricMax, lFitProbability));

            lInfoString += String.format("argmax=%g\nmetricmax=%g\nprob=%g\n", lArgmax, lMetricMax, lFitProbability);

            if (lMaxProbability * lMaxMetric < lFitProbability + lMetricMax)
            {
              lMaxMetric = lMetricMax;
              lMaxProbability = lFitProbability;
              lArgMax = lArgmax;
              lSelectedDetectionArm = pDetectionArmIndex;
            }

          }

          getAdaptiveEngine().addEntry(getName(), false, "LS", "CPI", 9, pLightSheetIndex, pControlPlaneIndex, lInfoString);

          checkAdaptationQuality(pLightSheetIndex, pControlPlaneIndex, lArgMax, lMaxMetric, lMaxProbability, lSelectedDetectionArm);

          for (StackInterface lStack : lStacks)
            lStack.free();

        } catch (Throwable e)
        {
          e.printStackTrace();
        }

      };

      Future<?> lFuture = executeAsynchronously(lRunnable);

      // FORCE SYNC:
      if (!getAdaptiveEngine().getConcurrentExecutionVariable().get())
      {
        try
        {
          lFuture.get();
        } catch (Throwable e)
        {
          e.printStackTrace();
        }
      }

      return lFuture;
    } catch (InterruptedException | ExecutionException | TimeoutException e)
    {
      e.printStackTrace();
    }
    return null;
  }

  protected void setResult(int pControlPlaneIndex, int pLightSheetIndex, int pDetectionArmIndex, Result pResult)
  {
    mResultsMap.put(Triple.of(pControlPlaneIndex, pLightSheetIndex, pDetectionArmIndex), pResult);
  }

  protected Result getResult(int pControlPlaneIndex, int pLightSheetIndex, int d)
  {
    return mResultsMap.get(Triple.of(pControlPlaneIndex, pLightSheetIndex, d));
  }

  protected double[] computeMetric(int pControlPlaneIndex, int pLightSheetIndex, int pDetectionArmIndex, final TDoubleArrayList lDOFValueList, StackInterface lDuplicatedStack)
  {

    if (lDuplicatedStack instanceof EmptyStack) return null;

    DCTS2D lDCTS2D = new DCTS2D();

    // System.out.format("computing DCTS on %s ...\n", lDuplicatedStack);
    final double[] lMetricArray = lDCTS2D.computeImageQualityMetric((OffHeapPlanarStack) lDuplicatedStack);
    lDuplicatedStack.free();

    String lChartName = String.format("CPI=%d|LS=%d|D=%d", pControlPlaneIndex, pLightSheetIndex, pDetectionArmIndex);

    getAdaptiveEngine().configureChart(getName(), lChartName, "ΔZ", "focus metric", ChartType.Line);

    for (int i = 0; i < lMetricArray.length; i++)
    {
      /*System.out.format("%g\t%g \n",
                        lDOFValueList.get(i),
                        lMetricArray[i]);/**/
      if (i < lDOFValueList.size())
      {
        getAdaptiveEngine().addPoint(getName(), lChartName, i == 0,

                lDOFValueList.get(i), lMetricArray[i]);
      } else
      {
        // Todo: this should not happen, but it did on 2017-11-28
      }
    }

    return lMetricArray;
  }

  protected void updateStateInternal(InterpolatedAcquisitionState pStateToUpdate, boolean pRelativeCorrection, boolean pFlipCorrectionSign)
  {
    info("Update new state...");

    int lNumberOfControlPlanes = getAdaptiveEngine().getAcquisitionStateVariable().get().getNumberOfControlPlanes();
    int lNumberOfLightSheets = getAdaptiveEngine().getAcquisitionStateVariable().get().getNumberOfLightSheets();

    int lNumberOfDetectionArms = getAdaptiveEngine().getAcquisitionStateVariable().get().getNumberOfDetectionArms();

    int lMissingInfoCount = 0;
    for (int cpi = 0; cpi < lNumberOfControlPlanes; cpi++)
    {

      for (int l = 0; l < lNumberOfLightSheets; l++)
      {
        int lSelectedDetectionArm = 0;
        Result lResult = getResult(cpi, l, 0);

        if (lResult == null)
        {
          severe("Found null result for cpi=%d, l=%d \n", cpi, l);
          continue;
        }

        for (int d = 1; d < lNumberOfDetectionArms; d++)
        {
          Result lOneResult = getResult(cpi, l, d);

          if (lOneResult != null)
            if (lOneResult.metricmax * lOneResult.probability > lResult.metricmax * lResult.probability)
            {
              lResult = lOneResult;
              lSelectedDetectionArm = d;
            }
        }

        double lCorrection = (pFlipCorrectionSign ? -1 : 1) * lResult.argmax;

        boolean lMissingInfo = checkAdaptationQuality(l, cpi, lCorrection, lResult.metricmax, lResult.probability, lSelectedDetectionArm);

        if (lMissingInfo)
        {
          lCorrection = computeCorrectionBasedOnNeighbooringControlPlanes(pRelativeCorrection, pStateToUpdate, cpi, l);

        }

        info("Correction is: %g \n", lCorrection);

        lCorrection = clampCorrection(lCorrection);

        info("Applying correction: %g \n", lCorrection);

        getAdaptiveEngine().addEntry(getName(), false, "LS", "CPI", 9, l, cpi, String.format("argmax=%g\nmetricmax=%g\nprob=%g\ncorr=%g\nmissing=%s\nselected=%d", lResult.argmax, lResult.metricmax, lResult.probability, lCorrection, lMissingInfo, lSelectedDetectionArm));

        if (pRelativeCorrection) pStateToUpdate.getInterpolationTables().add(mLightSheetDOF, cpi, l, lCorrection);
        else pStateToUpdate.getInterpolationTables().set(mLightSheetDOF, cpi, l, lCorrection);
        invokeControlPlaneStateChangeListeners(l, cpi);
      }
    }
    setConfigurationState(ConfigurationState.fromProgressValue((double) lMissingInfoCount / (lNumberOfControlPlanes * lNumberOfLightSheets)));
  }

  private double clampCorrection(double lCorrection)
  {
    double min = -mMaxCorrection.get().doubleValue();
    double max = +mMaxCorrection.get().doubleValue();
    info("Clamping correction within [%g,%g] \n", min, max);
    lCorrection = Math.max(lCorrection, min);
    lCorrection = Math.min(lCorrection, max);
    return lCorrection;
  }

  protected double computeCorrectionBasedOnNeighbooringControlPlanes(boolean pRelativeCorrection, InterpolatedAcquisitionState pStateToUpdate, int cpi, int l)
  {
    double lValue = pRelativeCorrection ? pStateToUpdate.getInterpolationTables().get(mLightSheetDOF, cpi, l) : 0;

    double lCorrection;
    if (cpi == 0)
    {

      double lValueAfter = pStateToUpdate.getInterpolationTables().get(mLightSheetDOF, cpi + 1, l);

      lCorrection = lValueAfter - lValue;
    } else if (cpi == pStateToUpdate.getNumberOfControlPlanes() - 1)
    {

      double lValueBefore = pStateToUpdate.getInterpolationTables().get(mLightSheetDOF, cpi - 1, l);

      lCorrection = lValueBefore - lValue;
    } else
    {

      double lValueBefore = pStateToUpdate.getInterpolationTables().get(mLightSheetDOF, cpi - 1, l);

      double lValueAfter = pStateToUpdate.getInterpolationTables().get(mLightSheetDOF, cpi + 1, l);

      lCorrection = 0.5 * (lValueAfter + lValueBefore) - lValue;
    }
    return lCorrection;
  }

  protected boolean checkAdaptationQuality(int pLightSheetIndex, int pControlPlaneIndex, double lCorrectionValue, double lMetricValue, double lPropability, int pSelectedDetectionArm)
  {

    boolean lProbabilityInsufficient = lPropability < getProbabilityThresholdVariable().get();

    boolean lMetricMaxInsufficient = lMetricValue < getImageMetricThresholdVariable().get();

    if (lMetricMaxInsufficient)
    {
      warning("Metric maximum too low (%g < %g) for cpi=%d, l=%d using neighbooring values\n", lMetricValue, getImageMetricThresholdVariable().get(), pControlPlaneIndex, pLightSheetIndex);
    }

    if (lProbabilityInsufficient)
    {
      warning("Probability too low (%g < %g) for cpi=%d, l=%d using neighbooring values\n", lPropability, getProbabilityThresholdVariable().get(), pControlPlaneIndex, pLightSheetIndex);
    }

    boolean lMissingInfo = lMetricMaxInsufficient || lProbabilityInsufficient;

    if (lMissingInfo)
    {
      mControlPlaneStates[pLightSheetIndex][pControlPlaneIndex] = ConfigurationState.FAILED;
      mControlPlaneStateDescriptions[pLightSheetIndex][pControlPlaneIndex] = "";

      if (lMetricMaxInsufficient)
      {
        mControlPlaneStateDescriptions[pLightSheetIndex][pControlPlaneIndex] = "Metric max insufficient";
      }
      if (lMetricMaxInsufficient && lProbabilityInsufficient)
      {
        mControlPlaneStateDescriptions[pLightSheetIndex][pControlPlaneIndex] += "\n";
      }
      if (lProbabilityInsufficient)
      {
        mControlPlaneStateDescriptions[pLightSheetIndex][pControlPlaneIndex] += "Probability insufficient";
      }
      mSelectedDetectionArms[pLightSheetIndex][pControlPlaneIndex] = -1;
    } else
    {
      mControlPlaneStates[pLightSheetIndex][pControlPlaneIndex] = ConfigurationState.SUCCEEDED;
      mControlPlaneStateDescriptions[pLightSheetIndex][pControlPlaneIndex] = "" + lCorrectionValue;
      mSelectedDetectionArms[pLightSheetIndex][pControlPlaneIndex] = pSelectedDetectionArm;
    }
    invokeControlPlaneStateChangeListeners(pLightSheetIndex, pControlPlaneIndex);

    return lMissingInfo;
  }

  @Override
  public boolean isReady()
  {
    return getNDIterator() != null && !getNDIterator().hasNext() && super.isReady();
  }

  /**
   * Returns the variable holding the laser line to be used for focussing
   *
   * @return laser line variable
   */
  public Variable<Integer> getLaserLineVariable()
  {
    return mLaserLineVariable;
  }

  /**
   * Returns the variable holding the number of samples
   *
   * @return number of samples variable
   */
  public Variable<Integer> getNumberOfSamplesVariable()
  {
    return mNumberOfSamplesVariable;
  }

  /**
   * Returns the variable holding the probability threshold
   *
   * @return probability threshold variable
   */
  public Variable<Double> getProbabilityThresholdVariable()
  {
    return mProbabilityThresholdVariable;
  }

  /**
   * Returns the variable holding the image metric threshold
   *
   * @return image metric threshold variable
   */
  public Variable<Double> getImageMetricThresholdVariable()
  {
    return mImageMetricThresholdVariable;
  }


  /**
   * Returns the variable holding the max correction value
   *
   * @return max correction value
   */
  public Variable<Double> getMaxCorrectionVariable()
  {
    return mMaxCorrection;
  }

  /**
   * Returns the variable holding the exposure in seconds to be used.
   *
   * @return exposure in seconds variable
   */
  public Variable<Double> getExposureInSecondsVariable()
  {
    return mExposureInSecondsVariable;
  }

  /**
   * Returns the variable holding the laser power to be used.
   *
   * @return laser power variable
   */
  public Variable<Double> getLaserPowerVariable()
  {
    return mLaserPowerVariable;
  }

  ConfigurationState mConfigurationState = ConfigurationState.UNINITIALIZED;

  protected void resetState()
  {
    mConfigurationState = ConfigurationState.UNINITIALIZED;
  }

  protected void setConfigurationState(ConfigurationState pConfigurationState)
  {
    mConfigurationState = pConfigurationState;

    // call listeners
    for (ConfigurationStateChangeListener lConfigurationStateChangeListener : mConfigurationStateChangeListeners)
    {
      lConfigurationStateChangeListener.configurationStateChanged(this);
    }

  }

  public ConfigurationState getConfigurationState()
  {

    return mConfigurationState;
  }

  public ConfigurationState getControlPlaneState(int pLightSheetIndex, int pControlPlaneIndex)
  {
    if (mControlPlaneStates == null)
    {
      return ConfigurationState.UNINITIALIZED;
    }
    return mControlPlaneStates[pLightSheetIndex][pControlPlaneIndex];
  }

  public String getControlPlaneStateDescription(int pLightSheetIndex, int pControlPlaneIndex)
  {
    if (mControlPlaneStateDescriptions == null)
    {
      return "";
    }
    return "D" + mSelectedDetectionArms[pLightSheetIndex][pControlPlaneIndex] + ": " + mControlPlaneStateDescriptions[pLightSheetIndex][pControlPlaneIndex];
  }

  ArrayList<ConfigurationStateChangeListener> mConfigurationStateChangeListeners = new ArrayList<>();

  public void addConfigurationStateChangeListener(ConfigurationStateChangeListener pConfigurationStateChangeListener)
  {
    mConfigurationStateChangeListeners.add(pConfigurationStateChangeListener);

    setConfigurationState(getConfigurationState());
  }

  ArrayList<ControlPlaneStateListener> mControlPlaneStateListenerList = new ArrayList<ControlPlaneStateListener>();

  public void addControlPlaneStateChangeListener(ControlPlaneStateListener pControlPlaneStateListener)
  {
    mControlPlaneStateListenerList.add(pControlPlaneStateListener);
  }

  protected void invokeControlPlaneStateChangeListeners(int pLightSheetIndex, int pControlPlaneIndex)
  {
    for (ControlPlaneStateListener lControlPlaneStateListener : mControlPlaneStateListenerList)
    {
      lControlPlaneStateListener.controlPlaneStateChanged(pLightSheetIndex, pControlPlaneIndex);
    }
  }

  public boolean isActive()
  {
    return super.isActive();
  }
}
