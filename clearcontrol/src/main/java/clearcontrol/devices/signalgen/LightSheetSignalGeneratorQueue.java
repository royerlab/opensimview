package clearcontrol.devices.signalgen;

import clearcontrol.component.detection.DetectionArm;
import clearcontrol.component.detection.DetectionArmQueue;
import clearcontrol.component.lightsheet.LightSheet;
import clearcontrol.component.lightsheet.LightSheetQueue;
import clearcontrol.component.opticalswitch.LightSheetOpticalSwitch;
import clearcontrol.component.opticalswitch.LightSheetOpticalSwitchQueue;
import clearcontrol.core.device.queue.QueueInterface;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.devices.signalgen.measure.Measure;
import clearcontrol.devices.signalgen.score.ScoreInterface;
import clearcontrol.devices.signalgen.staves.staves.DetectionArmStaves;
import clearcontrol.devices.signalgen.staves.staves.LightSheetOpticalSwitchStaves;
import clearcontrol.devices.signalgen.staves.staves.LightSheetStaves;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Light sheet signal generator queue
 *
 * @author royer
 */
public class LightSheetSignalGeneratorQueue implements QueueInterface, LoggingFeature

{

  private SignalGeneratorQueue mDelegatedQueue;
  private LightSheetSignalGeneratorDevice mLightSheetSignalGeneratorDevice;

  private Variable<Integer> mSelectedLightSheetIndexVariable = new Variable<Integer>("SelectedLightSheetIndex", 0);

  private Measure mBeforeExposureMeasure, mExposureMeasure, mFinalMeasure;

  private final ConcurrentHashMap<DetectionArm, DetectionArmStaves> mDetectionArmToStavesMap = new ConcurrentHashMap<>();

  private final ArrayList<LightSheet> mLightSheetList = new ArrayList<LightSheet>();

  private final ConcurrentHashMap<LightSheet, LightSheetStaves> mLightSheetToStavesMap = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<LightSheetOpticalSwitch, LightSheetOpticalSwitchStaves> mOpticalSwitchToStavesMap = new ConcurrentHashMap<>();

  private final Variable<Double> mTransitionDurationInSecondsVariable = new Variable<Double>("mTransitionDurationInSeconds", 0d);

  /**
   * Instantiates a lightsheet signal generator queue device
   *
   * @param pLightSheetSignalGeneratorDevice lightsheet signal generator parent
   */
  public LightSheetSignalGeneratorQueue(LightSheetSignalGeneratorDevice pLightSheetSignalGeneratorDevice)
  {
    mLightSheetSignalGeneratorDevice = pLightSheetSignalGeneratorDevice;
    mDelegatedQueue = pLightSheetSignalGeneratorDevice.getDelegatedSignalGenerator().requestQueue();

    setupStagingAndFinalisationScores();
  }

  /**
   * Copy constructors that instantiates a lightsheet signal generator queue from a
   * template.
   *
   * @param pTemplateQueue queue template
   */
  public LightSheetSignalGeneratorQueue(LightSheetSignalGeneratorQueue pTemplateQueue)
  {
    mLightSheetSignalGeneratorDevice = pTemplateQueue.getLightSheetSignalGeneratorDevice();
    mDelegatedQueue = getLightSheetSignalGeneratorDevice().getDelegatedSignalGenerator().requestQueue();

    getSelectedLightSheetIndexVariable().set(pTemplateQueue.getSelectedLightSheetIndexVariable().get());

    setupStagingAndFinalisationScores();
  }

  /**
   * Returns the delegated queue
   *
   * @return delegated queue
   */
  public SignalGeneratorQueue getDelegatedQueue()
  {
    return mDelegatedQueue;
  }

  /**
   * Returns the lightsheet optical switch parent
   *
   * @return optical switch parent
   */
  public LightSheetSignalGeneratorDevice getLightSheetSignalGeneratorDevice()
  {
    return mLightSheetSignalGeneratorDevice;
  }

  /**
   * Setting up the two measures that are necessary for one image acquisition and
   * corresponding lightsheet scanning.
   */
  private void setupStagingAndFinalisationScores()
  {
    mBeforeExposureMeasure = new Measure("BeforeExposure");
    mExposureMeasure = new Measure("Exposure");
    mFinalMeasure = new Measure("Final");

    ScoreInterface lStagingScore = mDelegatedQueue.getStagingScore();

    lStagingScore.addMeasure(mBeforeExposureMeasure);
    lStagingScore.addMeasure(mExposureMeasure);

    ScoreInterface lFinalisationScore = mDelegatedQueue.getFinalizationScore();

    lFinalisationScore.addMeasure(mFinalMeasure);
  }

  /**
   * Returns the staging score
   *
   * @return staging score
   */
  public ScoreInterface getStagingScore()
  {
    return mDelegatedQueue.getStagingScore();
  }

  /**
   * Adds a detection arm.
   *
   * @param pDetectionArmQueue detection arm queue
   */
  public void addDetectionArmQueue(DetectionArmQueue pDetectionArmQueue)
  {
    DetectionArmStaves lDetectionArmStaves = new DetectionArmStaves(pDetectionArmQueue);

    mDetectionArmToStavesMap.put(pDetectionArmQueue.getDetectionArm(), lDetectionArmStaves);

    lDetectionArmStaves.addStavesToMeasures(mBeforeExposureMeasure, mExposureMeasure, mFinalMeasure);

  }

  /**
   * Adds a light sheet
   *
   * @param pLightSheetQueue light sheet queue
   */
  public void addLightSheetQueue(LightSheetQueue pLightSheetQueue)
  {
    LightSheetStaves lLightSheetStaves = new LightSheetStaves(pLightSheetQueue);

    mLightSheetList.add(pLightSheetQueue.getLightSheet());

    mLightSheetToStavesMap.put(pLightSheetQueue.getLightSheet(), lLightSheetStaves);

    lLightSheetStaves.addStavesToMeasures(mBeforeExposureMeasure, mExposureMeasure, mFinalMeasure);

  }

  /**
   * Adds light sheet optical switch
   *
   * @param pLightSheetOpticalSwitchQueue optical switch
   */
  public void addOpticalSwitchQueue(LightSheetOpticalSwitchQueue pLightSheetOpticalSwitchQueue)
  {
    LightSheetOpticalSwitchStaves lLightSheetOpticalSwitchStaves = new LightSheetOpticalSwitchStaves(pLightSheetOpticalSwitchQueue, 0);

    mOpticalSwitchToStavesMap.put(pLightSheetOpticalSwitchQueue.getLightSheetOpticalSwitch(), lLightSheetOpticalSwitchStaves);

    lLightSheetOpticalSwitchStaves.addStavesToMeasures(mBeforeExposureMeasure, mExposureMeasure, mFinalMeasure);

  }

  @Override
  public void clearQueue()
  {
    mDelegatedQueue.clearQueue();
  }

  @Override
  public void addCurrentStateToQueue()
  {
    // first we make sure that the staging score is up-to-date given all the
    // detection and illumination parameters.f
    update();

    /* ScoreVisualizerJFrame.visualize("StagingScore",
                                    mDelegatedQueue.getStagingScore());/**/

    // then add the current state to the queue which corresponds to adding the
    // staging score to the actual measure that represents the queue.
    mDelegatedQueue.addCurrentStateToQueue();
  }

  /**
   * Updates underlying signal generation staves and stack camera configuration.
   */
  private void update()
  {
    synchronized (this)
    {
      // info("Updating: " + mLightSheetSignalGeneratorDevice.getName());

      for (Map.Entry<DetectionArm, DetectionArmStaves> lEntry : mDetectionArmToStavesMap.entrySet())
      {
        DetectionArmStaves lDetectionStaves = lEntry.getValue();

        lDetectionStaves.update(mBeforeExposureMeasure, mExposureMeasure, mFinalMeasure);
      }

      Variable<Boolean> lIsSharedLightSheetControl = getLightSheetSignalGeneratorDevice().getIsSharedLightSheetControlVariable();

      if (lIsSharedLightSheetControl.get())
      {
        int lSelectedLightSheetIndex = getSelectedLightSheetIndexVariable().get();

        if (lSelectedLightSheetIndex < 0 || lSelectedLightSheetIndex >= mLightSheetList.size())
        {
          warning("Selected lightsheet is not valid: %d, using 0 instead", lSelectedLightSheetIndex);
          lSelectedLightSheetIndex = 0;
        }

        LightSheet lSelectedLightSheet = mLightSheetList.get(lSelectedLightSheetIndex);

        LightSheetStaves lLightSheetStaves = mLightSheetToStavesMap.get(lSelectedLightSheet);

        lLightSheetStaves.update(mBeforeExposureMeasure, mExposureMeasure, mFinalMeasure);

      } else for (Map.Entry<LightSheet, LightSheetStaves> lEntry : mLightSheetToStavesMap.entrySet())
      {
        LightSheetStaves lLightSheetStaves = lEntry.getValue();
        lLightSheetStaves.update(mBeforeExposureMeasure, mExposureMeasure, mFinalMeasure);
      }

      for (Entry<LightSheetOpticalSwitch, LightSheetOpticalSwitchStaves> lEntry : mOpticalSwitchToStavesMap.entrySet())
      {
        LightSheetOpticalSwitchStaves lLightSheetOpticalSwitchStaves = lEntry.getValue();
        lLightSheetOpticalSwitchStaves.update(mBeforeExposureMeasure, mExposureMeasure);
      }

    }
  }

  @Override
  public void finalizeQueue()
  {
    update();
    mDelegatedQueue.finalizeQueue();
  }

  @Override
  public int getQueueLength()
  {
    return mDelegatedQueue.getQueueLength();
  }

  /**
   * In the case that we are in a shared lightsheet control situation, this variable holds
   * the index of the lightsheet to use to generate the control signals.
   *
   * @return selected lightsheet variable
   */
  public Variable<Integer> getSelectedLightSheetIndexVariable()
  {
    return mSelectedLightSheetIndexVariable;
  }

  /**
   * Returns the variable holding the transition duration in seconds
   *
   * @return transition duration in seconds variable
   */
  public Variable<Double> getTransitionDurationInSecondsVariable()
  {
    return mTransitionDurationInSecondsVariable;
  }

}
