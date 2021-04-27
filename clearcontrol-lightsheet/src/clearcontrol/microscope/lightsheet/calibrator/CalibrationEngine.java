package clearcontrol.microscope.lightsheet.calibrator;

import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.device.task.TaskDevice;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.calibrator.modules.CalibrationModuleInterface;
import clearcontrol.microscope.lightsheet.calibrator.modules.impl.*;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArmInterface;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheetInterface;
import clearcontrol.scripting.engine.ScriptingEngine;
import org.ejml.simple.SimpleMatrix;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Calibration engine
 *
 * @author royer
 */
public class CalibrationEngine extends TaskDevice implements LoggingFeature,
                                                             VisualConsoleInterface
{

  private File mCalibrationFolder = MachineConfiguration.get().getFolder("Calibration");

  private final LightSheetMicroscope mLightSheetMicroscope;
  private CalibrationZ mCalibrationZ;
  private CalibrationZWithSample mCalibrationZWithSample;
  private CalibrationA mCalibrationA;
  private CalibrationXY mCalibrationXY;
  private CalibrationP mCalibrationP;
  private CalibrationW mCalibrationW;
  private CalibrationHP mCalibrationHP;
  private CalibrationWP mCalibrationWP;

  private HashMap<String, LightSheetPositioner> mPositionersMap = new HashMap<>();

  @SuppressWarnings("unused") private int mNumberOfDetectionArmDevices;
  private int mNumberOfLightSheetDevices;

  private final Variable<Boolean>[] mCalibrateLightSheetOnOff;

  private final Variable<Boolean>
      mCalibrateZVariable =
      new Variable<Boolean>("CalibrateZ", true);
  private final Variable<Boolean>
      mCalibrateZWithSampleVariable =
      new Variable<Boolean>("CalibrateZWithSample", false);

  private final Variable<Boolean>
      mCalibrateAVariable =
      new Variable<Boolean>("CalibrateA", false);
  private final Variable<Boolean>
      mCalibrateXYVariable =
      new Variable<Boolean>("CalibrateXY", false);

  private final Variable<Boolean>
      mCalibratePVariable =
      new Variable<Boolean>("CalibrateP", false);
  private final Variable<Boolean>
      mCalibrateWVariable =
      new Variable<Boolean>("CalibrateW", false);
  private final Variable<Boolean>
      mCalibrateWPVariable =
      new Variable<Boolean>("CalibrateWP", false);
  private final Variable<Boolean>
      mCalibrateHPVariable =
      new Variable<Boolean>("CalibrateHP", false);

  private final Variable<String>
      mCalibrationDataName =
      new Variable<String>("CalibrationName", "system");

  private final Variable<Double> mProgressVariable;

  /**
   * Instantiates a calibrator, given a lightsheet microscope
   *
   * @param pLightSheetMicroscope lightsheet microscope
   */
  @SuppressWarnings("unchecked") public CalibrationEngine(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Calibrator");

    mLightSheetMicroscope = pLightSheetMicroscope;
    mCalibrationZ = new CalibrationZ(this);
    mCalibrationZWithSample = new CalibrationZWithSample(this);
    mCalibrationA = new CalibrationA(this);
    mCalibrationXY = new CalibrationXY(this);
    mCalibrationP = new CalibrationP(this);
    mCalibrationW = new CalibrationW(this);
    mCalibrationWP = new CalibrationWP(this);
    mCalibrationHP = new CalibrationHP(this);

    mNumberOfDetectionArmDevices =
        mLightSheetMicroscope.getDeviceLists()
                             .getNumberOfDevices(DetectionArmInterface.class);

    mNumberOfLightSheetDevices =
        mLightSheetMicroscope.getDeviceLists()
                             .getNumberOfDevices(LightSheetInterface.class);

    mProgressVariable = new Variable<Double>(getName() + "Progress", 0.0);

    mCalibrateLightSheetOnOff = new Variable[mNumberOfLightSheetDevices];
    for (int l = 0; l < mNumberOfLightSheetDevices; l++)
    {
      mCalibrateLightSheetOnOff[l] =
          new Variable<Boolean>("CalibrateLightSheet" + l, true);
    }

  }

  /**
   * Returns a lightsheet microscope
   *
   * @return lightsheet microscope
   */
  public LightSheetMicroscope getLightSheetMicroscope()
  {
    return mLightSheetMicroscope;
  }

  @Override public boolean startTask()
  {
    if (getLightSheetMicroscope().getCurrentTask().get() != null)
    {
      warning("Another task (%s) is already running, please stop it first.",
              getLightSheetMicroscope().getCurrentTask());
      return false;
    }
    getLightSheetMicroscope().getCurrentTask().set(this);
    return super.startTask();
  }

  @Override public void run()
  {
    try
    {
      mProgressVariable.set(0.0);
      calibrate();
      mProgressVariable.set(1.0);
      info("############################################## Calibration done");
    }
    finally
    {
      getLightSheetMicroscope().getCurrentTask().set(null);
    }
  }

  /**
   * Performs calibration
   *
   * @return true if foinished normally, false if calibration was canceled or failed
   */
  public boolean calibrate()
  {

    if (getCalibrateZVariable().get() && !calibrateZ())
      return false;

    if (isStopRequested())
      return false;/**/

    if (getCalibrateZWithSampleVariable().get() && !calibrateZWithSample())
      return false;

    if (isStopRequested())
      return false;/**/

    if (getCalibrateAVariable().get() && !calibrateA())
      return false;

    if (isStopRequested())
      return false;/**/

    if (getCalibrateXYVariable().get() && !calibrateXY())
      return false;

    if (isStopRequested())
      return false;/**/

    if (getCalibratePVariable().get() && !calibrateP())
      return false;

    if (isStopRequested())
      return false;

    if (getCalibrateWVariable().get() && !calibrateW())
      return false;

    if ((getCalibrateAVariable().get() || getCalibrateXYVariable().get())
        && getCalibrateZVariable().get()
        && !calibrateZ())
      return false;

    if (isStopRequested())
      return false;/**/

    if (getCalibratePVariable().get() && !calibrateP())
      if (isStopRequested())
        return false;/**/

    return true;
  }

  /**
   * Returns true if calibration should be stopped immediately.
   *
   * @return true for stopping, false otherwise.
   */
  public boolean isStopRequested()
  {
    return ScriptingEngine.isCancelRequestedStatic()
           || !isRunning()
           || getStopSignalVariable().get();
  }

  /**
   * Calibrates the lightsheet and detection arm Z positions.
   *
   * @return true when succeeded
   */
  public boolean calibrateZ()
  {
    for (int l = 0; l < mNumberOfLightSheetDevices && !isStopRequested(); l++)
    {
      if (getCalibrateLightSheetOnOff(l).get())
      {
        mCalibrationZ.calibrateZ(l);
        mProgressVariable.set((1.0 * l) / mNumberOfLightSheetDevices);
      }
    }
    return true;
  }

  /**
   * Calibrates the lightsheet and detection arm Z positions.
   *
   * @return true when succeeded
   */
  public boolean calibrateZWithSample()
  {
    for (int l = 0; l < mNumberOfLightSheetDevices && !isStopRequested(); l++)
    {
      if (getCalibrateLightSheetOnOff(l).get())
      {
        mCalibrationZWithSample.calibrateZ(l);
        mProgressVariable.set((1.0 * l) / mNumberOfLightSheetDevices);
      }
    }
    return true;
  }

  /**
   * Calibrates the alpha angle.
   *
   * @return true when succeeded
   */
  public boolean calibrateA()
  {
    for (int l = 0; l < mNumberOfLightSheetDevices && !isStopRequested(); l++)
      if (getCalibrateLightSheetOnOff(l).get())
      {
        mCalibrationA.calibrate(l);
        mProgressVariable.set((1.0 * l) / mNumberOfLightSheetDevices);
      }
    return true;
  }

  /**
   * Calibrates X and Y lighthseet positions
   *
   * @return true when succeeded
   */
  public boolean calibrateXY()
  {
    for (int l = 0; l < mNumberOfLightSheetDevices && !isStopRequested(); l++)
      if (getCalibrateLightSheetOnOff(l).get())
      {
        mCalibrationXY.calibrate(l);
        mProgressVariable.set((1.0 * l) / mNumberOfLightSheetDevices);
      }

    return true;
  }

  /**
   * @return true when succeeded
   */
  public boolean calibrateP()
  {
    mCalibrationP.reset();
    mCalibrationP.calibrateAllLightSheets();

    return true;
  }

  /**
   * Calibrates the lighthseet laser power versus its height
   *
   * @return true when succeeded
   */
  public boolean calibrateHP()
  {
    for (int l = 0; l < mNumberOfLightSheetDevices && !isStopRequested(); l++)
      if (getCalibrateLightSheetOnOff(l).get())
      {
        mCalibrationHP.calibrate(l);
        mProgressVariable.set((1.0 * l) / mNumberOfLightSheetDevices);
      }
    return true;
  }

  /**
   * Calibrates the lighthseet laser power versus its height
   *
   * @return true when succeeded
   */
  public boolean calibrateWP()
  {
    for (int l = 0; l < mNumberOfLightSheetDevices && !isStopRequested(); l++)
      if (getCalibrateLightSheetOnOff(l).get())
      {
        mCalibrationWP.calibrate(l);
        mProgressVariable.set((1.0 * l) / mNumberOfLightSheetDevices);
      }
    return true;
  }

  /**
   * Calibrates the width (beam NA) of the lighthsheet
   *
   * @return true when succeeded
   */
  public boolean calibrateW()
  {
    mCalibrationW.calibrateAllLightSheets();
    return true;
  }

  // /***************************************************************/ //

  /**
   * Resets the calibration information
   */
  public void reset()
  {
    mCalibrationZ.reset();
    mCalibrationZWithSample.reset();
    mCalibrationA.reset();
    mCalibrationXY.reset();
    mCalibrationP.reset();
    mCalibrationW.reset();

    final int
        lNumberOfDetectionArmDevices =
        mLightSheetMicroscope.getDeviceLists()
                             .getNumberOfDevices(DetectionArmInterface.class);

    for (int i = 0; i < lNumberOfDetectionArmDevices; i++)
    {
      final DetectionArmInterface
          lDetectionArmDevice =
          mLightSheetMicroscope.getDeviceLists()
                               .getDevice(DetectionArmInterface.class, i);
      lDetectionArmDevice.resetFunctions();

    }

    final int
        lNumberOfLightSheetDevices =
        mLightSheetMicroscope.getDeviceLists()
                             .getNumberOfDevices(LightSheetInterface.class);

    for (int i = 0; i < lNumberOfLightSheetDevices; i++)
    {
      final LightSheetInterface
          lLightSheetDevice =
          mLightSheetMicroscope.getDeviceLists().getDevice(LightSheetInterface.class, i);
      if (getCalibrateLightSheetOnOff(i).get())
      {
        lLightSheetDevice.resetFunctions();
      }

    }
  }

  /**
   *
   */
  public void positioners()
  {

    final int
        lNumberOfLightSheetDevices =
        mLightSheetMicroscope.getDeviceLists()
                             .getNumberOfDevices(LightSheetInterface.class);

    final int
        lNumberOfDetectionArmDevices =
        mLightSheetMicroscope.getDeviceLists()
                             .getNumberOfDevices(DetectionArmInterface.class);

    for (int l = 0; l < lNumberOfLightSheetDevices; l++)
      for (int d = 0; d < lNumberOfDetectionArmDevices; d++)
      {

        SimpleMatrix lTransformMatrix = mCalibrationXY.getTransformMatrix(l, d);

        if (lTransformMatrix != null)
        {
          LightSheetPositioner
              lLightSheetPositioner =
              new LightSheetPositioner(lTransformMatrix);

          setPositioner(l, d, lLightSheetPositioner);
        }
      }

  }

  /**
   * Sets the positioner to use for a given lightsheet and detection arm
   *
   * @param pLightSheetIndex      lightsheet index
   * @param pDetectionArmIndex    detection arm index
   * @param pLightSheetPositioner lightsheet positioner
   */
  public void setPositioner(int pLightSheetIndex,
                            int pDetectionArmIndex,
                            LightSheetPositioner pLightSheetPositioner)
  {
    mPositionersMap.put("i" + pLightSheetIndex + "d" + pDetectionArmIndex,
                        pLightSheetPositioner);

  }

  /**
   * Returns the lightsheet positioner for a given lightsheet index and detection arm
   * index
   *
   * @param pLightSheetIndex   light sheet index
   * @param pDetectionArmIndex detection arm index
   * @return positioner
   */
  public LightSheetPositioner getPositioner(int pLightSheetIndex, int pDetectionArmIndex)
  {
    return mPositionersMap.get("i" + pLightSheetIndex + "d" + pDetectionArmIndex);

  }

  /**
   * Saves the calibration information to a file.
   */
  public void save()
  {
    save(mCalibrationDataName.get());
  }

  /**
   * Loads the calibration from a file
   *
   * @return true -> success
   */
  public boolean load()
  {
    return load(mCalibrationDataName.get());
  }

  /**
   * Saves this calibration to a file of given name.
   *
   * @param pName calibration name
   */
  public void save(String pName)
  {
    CalibrationData lCalibrationData = new CalibrationData(mLightSheetMicroscope);

    lCalibrationData.copyFrom(mLightSheetMicroscope);

    lCalibrationData.copyFrom(mPositionersMap);

    lCalibrationData.saveTo(getFile(pName));

  }

  /**
   * Loads calibration from a file of given name
   *
   * @param pName name
   * @return true -> success
   */
  public boolean load(String pName)
  {
    File lFile = getFile(pName);

    if (!lFile.exists())
      return false;

    CalibrationData lCalibrationData = CalibrationData.readFrom(lFile);

    lCalibrationData.applyTo(mLightSheetMicroscope);

    lCalibrationData.copyTo(mPositionersMap);

    return true;
  }

  private File getFile(String pName)
  {
    return new File(mCalibrationFolder, pName + ".json");
  }

  public ArrayList<String> getExistingCalibrationList()
  {
    ArrayList<String> fileList = new ArrayList<String>();
    File folder = mCalibrationFolder;

    for (File file : folder.listFiles())
    {
      if (!file.isDirectory() && file.getAbsolutePath().endsWith(".json"))
      {
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.length() - 5);

        fileList.add(fileName);
      }
    }

    return fileList;
  }

  public ArrayList<CalibrationModuleInterface> getModuleList()
  {
    ArrayList<CalibrationModuleInterface> lModuleList = new ArrayList<>();
    lModuleList.add(mCalibrationZ);
    lModuleList.add(mCalibrationZWithSample);
    lModuleList.add(mCalibrationA);
    lModuleList.add(mCalibrationP);
    lModuleList.add(mCalibrationW);
    lModuleList.add(mCalibrationXY);
    // lModuleList.add(mCalibrationHP);
    // lModuleList.add(mCalibrationWP);
    return lModuleList;
  }

  /**
   * Returns the variable holding the 'calibrate Z' boolean flag.
   *
   * @return calibrate Z variable
   */
  public Variable<Boolean> getCalibrateZVariable()
  {
    return mCalibrateZVariable;
  }

  /**
   * Returns the variable holding the 'calibrate Z' boolean flag.
   *
   * @return calibrate Z variable
   */
  public Variable<Boolean> getCalibrateZWithSampleVariable()
  {
    return mCalibrateZWithSampleVariable;
  }

  /**
   * Returns the variable holding the 'calibrate A' boolean flag.
   *
   * @return calibrate A variable
   */
  public Variable<Boolean> getCalibrateAVariable()
  {
    return mCalibrateAVariable;
  }

  /**
   * Returns the variable holding the 'calibrate XY' boolean flag.
   *
   * @return calibrate XY variable
   */
  public Variable<Boolean> getCalibrateXYVariable()
  {
    return mCalibrateXYVariable;
  }

  /**
   * Returns the variable holding the 'calibrate P' boolean flag.
   *
   * @return calibrate P variable
   */
  public Variable<Boolean> getCalibratePVariable()
  {
    return mCalibratePVariable;
  }

  /**
   * Returns the variable holding the 'calibrate W' boolean flag.
   *
   * @return calibrate W variable
   */
  public Variable<Boolean> getCalibrateWVariable()
  {
    return mCalibrateWVariable;
  }

  /**
   * Returns the variable holding the 'calibrate WP' boolean flag.
   *
   * @return calibrate WP variable
   */
  public Variable<Boolean> getCalibrateWPVariable()
  {
    return mCalibrateWPVariable;
  }

  /**
   * Returns the variable holding the 'calibrate HP' boolean flag.
   *
   * @return calibrate HP variable
   */
  public Variable<Boolean> getCalibrateHPVariable()
  {
    return mCalibrateHPVariable;
  }

  /**
   * Returns the variable holding the 'calibrate W' boolean flag.
   *
   * @return calibrate W variable
   */
  public Variable<Double> getProgressVariable()
  {
    return mProgressVariable;
  }

  /**
   * Returns the calibration data name variable
   *
   * @return calibration data name variable
   */
  public Variable<String> getCalibrationDataNameVariable()
  {
    return mCalibrationDataName;
  }

  /**
   * Returns the variable holding the calibrate on/off flag. This flag decides whether the
   * lightsheet should be calibrated.
   *
   * @param pLightSheetIndex lightsheet index
   * @return calibrate lightsheet variable
   */
  public Variable<Boolean> getCalibrateLightSheetOnOff(int pLightSheetIndex)
  {
    return mCalibrateLightSheetOnOff[pLightSheetIndex];
  }

  /**
   * Returns the variable holding the 'is-running' flag.
   *
   * @return is-running variable
   */
  public boolean isRunning()
  {
    return getIsRunningVariable().get();
  }

}
