package clearcontrol.calibrator.modules;

import clearcontrol.LightSheetMicroscope;
import clearcontrol.calibrator.CalibrationEngine;
import clearcontrol.component.detection.DetectionArmInterface;
import clearcontrol.component.lightsheet.LightSheetInterface;
import clearcontrol.configurationstate.ConfigurationState;
import clearcontrol.configurationstate.ConfigurationStateChangeListener;
import clearcontrol.configurationstate.HasConfigurationState;
import clearcontrol.core.device.name.ReadOnlyNameableInterface;
import clearcontrol.core.log.LoggingFeature;

import java.util.ArrayList;

/**
 * Base class providing common fields and methods for all calibration modules
 *
 * @author royer
 */
public abstract class CalibrationBase implements CalibrationModuleInterface, LoggingFeature, HasConfigurationState, ReadOnlyNameableInterface
{
  private final CalibrationEngine mCalibrationEngine;
  private final LightSheetMicroscope mLightSheetMicroscope;

  private volatile int mIteration = 0;

  private String mName;

  /**
   * Instantiates a calibration module given a parent calibrator and lightsheet
   * microscope.
   *
   * @param pName              name of the calibrator
   * @param pCalibrationEngine parent calibrator
   */
  public CalibrationBase(String pName, CalibrationEngine pCalibrationEngine)
  {
    super();
    mName = pName;
    mCalibrationEngine = pCalibrationEngine;
    mLightSheetMicroscope = pCalibrationEngine.getLightSheetMicroscope();
  }

  /**
   * Returns this calibrator's parent lightsheet microscope
   *
   * @return parent lightsheet microscope
   */
  public LightSheetMicroscope getLightSheetMicroscope()
  {
    return mLightSheetMicroscope;
  }

  /**
   * Returns this calibration module parent calibration engine
   *
   * @return parent calibration engine
   */
  public CalibrationEngine getCalibrationEngine()
  {
    return mCalibrationEngine;
  }

  @Override
  public void reset()
  {
    resetState();
    resetIteration();
  }

  /**
   * Returns the iteration counter
   *
   * @return iteration counter value
   */
  public int getIteration()
  {
    return mIteration;
  }

  /**
   * increment iteration counter
   */
  public void incrementIteration()
  {
    mIteration++;
  }

  /**
   * resets iteration counter
   */
  public void resetIteration()
  {
    mIteration = 0;
  }

  /**
   * Returns the number of lightsheets
   *
   * @return number of lightsheets
   */
  public int getNumberOfLightSheets()
  {
    return getLightSheetMicroscope().getDeviceLists().getNumberOfDevices(LightSheetInterface.class);
  }

  /**
   * Returns the number of detection arms
   *
   * @return number of detection arms
   */
  public int getNumberOfDetectionArms()
  {
    return getLightSheetMicroscope().getDeviceLists().getNumberOfDevices(DetectionArmInterface.class);
  }

  public String getName()
  {
    return mName;
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

  ArrayList<ConfigurationStateChangeListener> mConfigurationStateChangeListeners = new ArrayList<>();

  public void addConfigurationStateChangeListener(ConfigurationStateChangeListener pConfigurationStateChangeListener)
  {
    mConfigurationStateChangeListeners.add(pConfigurationStateChangeListener);

    setConfigurationState(getConfigurationState());

  }

}
