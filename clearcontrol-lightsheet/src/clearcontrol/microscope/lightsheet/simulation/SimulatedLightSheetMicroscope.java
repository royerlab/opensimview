package clearcontrol.microscope.lightsheet.simulation;

import clearcl.ClearCLContext;
import clearcontrol.core.variable.Variable;
import clearcontrol.devices.cameras.devices.sim.StackCameraDeviceSimulator;
import clearcontrol.devices.cameras.devices.sim.StackCameraSimulationProvider;
import clearcontrol.devices.cameras.devices.sim.providers.FractalStackProvider;
import clearcontrol.devices.optomech.filterwheels.instructions.FilterWheelInstruction;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.devices.lasers.devices.sim.LaserDeviceSimulator;
import clearcontrol.devices.lasers.instructions.ChangeLaserPowerInstruction;
import clearcontrol.devices.lasers.instructions.SwitchLaserOnOffInstruction;
import clearcontrol.devices.lasers.instructions.SwitchLaserPowerOnOffInstruction;
import clearcontrol.devices.optomech.filterwheels.FilterWheelDeviceInterface;
import clearcontrol.devices.optomech.filterwheels.devices.sim.FilterWheelDeviceSimulator;
import clearcontrol.devices.signalamp.ScalingAmplifierDeviceInterface;
import clearcontrol.devices.signalamp.devices.sim.ScalingAmplifierSimulator;
import clearcontrol.devices.signalgen.devices.sim.SignalGeneratorSimulatorDevice;
import clearcontrol.devices.stages.StageType;
import clearcontrol.devices.stages.devices.sim.StageDeviceSimulator;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.implementations.MeasureTimeInstruction;
import clearcontrol.instructions.implementations.PauseInstruction;
import clearcontrol.instructions.implementations.PauseUntilTimeAfterMeasuredTimeInstruction;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.adaptive.AdaptationStateEngine;
import clearcontrol.microscope.lightsheet.adaptive.instructions.*;
import clearcontrol.microscope.lightsheet.calibrator.CalibrationEngine;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArm;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheet;
import clearcontrol.microscope.lightsheet.component.lightsheet.instructions.*;
import clearcontrol.microscope.lightsheet.component.opticalswitch.LightSheetOpticalSwitch;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedAcquisitionInstruction;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedFusionInstruction;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedImageDataContainer;
import clearcontrol.microscope.lightsheet.imaging.interleaved.WriteInterleavedRawDataToDiscInstruction;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.OpticsPrefusedAcquisitionInstruction;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.OpticsPrefusedFusionInstruction;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.OpticsPrefusedImageDataContainer;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.WriteOpticsPrefusedRawDataAsRawToDiscInstruction;
import clearcontrol.microscope.lightsheet.imaging.sequential.*;
import clearcontrol.microscope.lightsheet.imaging.singleview.SingleViewAcquisitionInstruction;
import clearcontrol.microscope.lightsheet.imaging.singleview.WriteSingleLightSheetImageAsRawToDiscInstruction;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions.ViewStack2DInstruction;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions.ViewStack3DInstruction;
import clearcontrol.microscope.lightsheet.processor.fusion.FusedImageDataContainer;
import clearcontrol.microscope.lightsheet.processor.fusion.WriteFusedImageAsRawToDiscInstruction;
import clearcontrol.microscope.lightsheet.signalgen.LightSheetSignalGeneratorDevice;
import clearcontrol.microscope.lightsheet.state.ControlPlaneLayout;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.state.LightSheetAcquisitionStateInterface;
import clearcontrol.microscope.lightsheet.state.instructions.*;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.timelapse.instructions.TimelapseStopInstruction;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.ReadStackInterfaceContainerFromDiscInstruction;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteAllStacksAsRawToDiscInstruction;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteSpecificStackToSpecificRawFolderInstruction;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DropOldestStackInterfaceContainerInstruction;
import clearcontrol.microscope.state.AcquisitionStateManager;
import clearcontrol.microscope.timelapse.TimelapseInterface;
import clearcontrol.stack.sourcesink.sink.RawFileStackSink;
import clearcontrol.stack.sourcesink.sink.ZarrStackSink;

import java.util.ArrayList;

/**
 * Simulated lightsheet microscope
 *
 * @author royer
 */
public class SimulatedLightSheetMicroscope extends LightSheetMicroscope
{

  /**
   * Instantiates a simulated lightsheet microscope
   *
   * @param pDeviceName                    device name
   * @param pStackFusionContext            ClearCL context for stack fusion
   * @param pMaxStackProcessingQueueLength max stack processing queue length
   * @param pThreadPoolSize                thread pool size
   */
  public SimulatedLightSheetMicroscope(String pDeviceName,
                                       ClearCLContext pStackFusionContext,
                                       int pMaxStackProcessingQueueLength,
                                       int pThreadPoolSize)
  {
    super(pDeviceName,
          pStackFusionContext,
          pMaxStackProcessingQueueLength,
          pThreadPoolSize);

  }

  /**
   * Assembles the microscope
   *
   * @param pDummySimulation         true-> uses a dummy simulation instead of the embryo
   * @param pXYZRStage               XYZR Stage
   * @param pSharedLightSheetControl true -> shared lightsheet control
   * @param pSimulatorDevice         simulator device
   */
  public void addSimulatedDevices(boolean pDummySimulation,
                                  boolean pXYZRStage,
                                  boolean pSharedLightSheetControl,
                                  LightSheetMicroscopeSimulationDevice pSimulatorDevice)
  {

    int
        lNumberOfDetectionArms =
        pSimulatorDevice.getSimulator().getNumberOfDetectionArms();
    int lNumberOfLightSheets = pSimulatorDevice.getSimulator().getNumberOfLightSheets();

    // Setting up lasers:
    {
      int[] lLaserWavelengths = new int[] { 488, 594 };
      ArrayList<LaserDeviceInterface> lLaserList = new ArrayList<>();
      for (int l = 0; l < lLaserWavelengths.length; l++)
      {
        LaserDeviceInterface
            lLaser =
            new LaserDeviceSimulator("Laser " + lLaserWavelengths[l],
                                     l,
                                     lLaserWavelengths[l],
                                     100);
        lLaserList.add(lLaser);
        addDevice(l, lLaser);

        addDevice(0, new SwitchLaserOnOffInstruction(lLaser, true));
        addDevice(0, new SwitchLaserOnOffInstruction(lLaser, false));
        addDevice(0, new SwitchLaserPowerOnOffInstruction(lLaser, true));
        addDevice(0, new SwitchLaserPowerOnOffInstruction(lLaser, false));
        addDevice(0, new ChangeLaserPowerInstruction(lLaser));

      }
    }

    // Setting up Stage:
    if (pXYZRStage)
    {
      StageDeviceSimulator
          lStageDeviceSimulator =
          new StageDeviceSimulator("Stage", StageType.XYZR, true);
      lStageDeviceSimulator.addXYZRDOFs();
      lStageDeviceSimulator.setSpeed(0.8);

      addDevice(0, lStageDeviceSimulator);
    }

    // Setting up Filterwheel:
    {
      int[] lFilterWheelPositions = new int[] { 0, 1, 2, 3 };
      FilterWheelDeviceInterface
          lFilterWheelDevice =
          new FilterWheelDeviceSimulator("FilterWheel", lFilterWheelPositions);
      lFilterWheelDevice.setPositionName(0, "405 filter");
      lFilterWheelDevice.setPositionName(1, "488 filter");
      lFilterWheelDevice.setPositionName(2, "561 filter");
      lFilterWheelDevice.setPositionName(3, "594 filter");
      getDeviceLists().addDevice(0, lFilterWheelDevice);

      for (int f : lFilterWheelDevice.getValidPositions())
      {
        addDevice(0, new FilterWheelInstruction(lFilterWheelDevice, f));
      }

    }

    // Setting up trigger:

    Variable<Boolean> lTrigger = new Variable<Boolean>("CameraTrigger", false);

    ArrayList<StackCameraDeviceSimulator> lCameraList = new ArrayList<>();

    // Setting up cameras:
    {

      for (int c = 0; c < lNumberOfDetectionArms; c++)
      {
        final StackCameraDeviceSimulator
            lCamera =
            new StackCameraDeviceSimulator("StackCamera" + c, lTrigger);

        long
            lMaxWidth =
            pSimulatorDevice.getSimulator().getCameraRenderer(c).getMaxWidth();

        long
            lMaxHeight =
            pSimulatorDevice.getSimulator().getCameraRenderer(c).getMaxHeight();

        lCamera.getMaxWidthVariable().set(lMaxWidth);
        lCamera.getMaxHeightVariable().set(lMaxHeight);
        lCamera.getStackWidthVariable().set(lMaxWidth / 2);
        lCamera.getStackHeightVariable().set(lMaxHeight);
        lCamera.getExposureInSecondsVariable().set(0.010);

        // lCamera.getStackVariable().addSetListener((o,n)->
        // {System.out.println("camera output:"+n);} );

        addDevice(c, lCamera);

        lCameraList.add(lCamera);
      }
    }

    // Scaling Amplifier:
    {
      ScalingAmplifierDeviceInterface
          lScalingAmplifier1 =
          new ScalingAmplifierSimulator("ScalingAmplifier1");
      addDevice(0, lScalingAmplifier1);

      ScalingAmplifierDeviceInterface
          lScalingAmplifier2 =
          new ScalingAmplifierSimulator("ScalingAmplifier2");
      addDevice(1, lScalingAmplifier2);
    }

    // Signal generator:

    {
      SignalGeneratorSimulatorDevice
          lSignalGeneratorSimulatorDevice =
          new SignalGeneratorSimulatorDevice();

      // addDevice(0, lSignalGeneratorSimulatorDevice);
      lSignalGeneratorSimulatorDevice.getTriggerVariable().sendUpdatesTo(lTrigger);/**/

      final LightSheetSignalGeneratorDevice
          lLightSheetSignalGeneratorDevice =
          LightSheetSignalGeneratorDevice.wrap(lSignalGeneratorSimulatorDevice,
                                               pSharedLightSheetControl);

      addDevice(0, lLightSheetSignalGeneratorDevice);
    }

    // setting up staging score visualization:

    /*final ScoreVisualizerJFrame lVisualizer = ScoreVisualizerJFrame.visualize("LightSheetDemo",
                                                                              lStagingScore);/**/

    // Setting up detection arms:

    {
      for (int c = 0; c < lNumberOfDetectionArms; c++)
      {
        final DetectionArm lDetectionArm = new DetectionArm("D" + c);
        lDetectionArm.getPixelSizeInMicrometerVariable()
                     .set(pSimulatorDevice.getSimulator().getPixelWidth(c));

        addDevice(c, lDetectionArm);
      }
    }

    // Setting up lightsheets:
    {
      for (int l = 0; l < lNumberOfLightSheets; l++)
      {
        final LightSheet
            lLightSheet =
            new LightSheet("I" + l, 9.4, getNumberOfLaserLines());
        addDevice(l, lLightSheet);

      }
    }

    // Setting up lightsheets selector
    {
      LightSheetOpticalSwitch
          lLightSheetOpticalSwitch =
          new LightSheetOpticalSwitch("OpticalSwitch", lNumberOfLightSheets);

      addDevice(0, lLightSheetOpticalSwitch);
    }

    // Setting up simulator:
    {
      // Now that the microscope has been setup, we can connect the simulator to
      // it:

      // first, we connect the devices in the simulator so that parameter
      // changes
      // are forwarded:
      pSimulatorDevice.connectTo(this);

      // second, we make sure that the simulator is used as provider for the
      // simulated cameras:
      for (int c = 0; c < lNumberOfDetectionArms; c++)
      {
        StackCameraSimulationProvider lStackProvider;
        if (pDummySimulation)
          lStackProvider = new FractalStackProvider();
        else
          lStackProvider = pSimulatorDevice.getStackProvider(c);
        lCameraList.get(c).setStackCameraSimulationProvider(lStackProvider);
      }
    }

  }

  public void addDefaultProgram()
  {
    LightSheetTimelapse lTimelapse = getTimelapse();
    if (lTimelapse == null)
    {
      warning("Cannot add default program, because timelapse wasn't initialized yet");
      return;
    }

    ArrayList<InstructionInterface> program = lTimelapse.getCurrentProgram();
    program.clear();
}

  /**
   * Adds standard devices such as the acquisition state manager, calibrator and
   * Timelapse
   */
  @SuppressWarnings("unchecked") public void addStandardDevices(int pNumberOfControlPlanes)
  {

    boolean multiview = getNumberOfDetectionArms() > 1 || getNumberOfLightSheets() > 1;

    // Adding calibrator:
    {
      CalibrationEngine lCalibrator = addCalibrator();
      lCalibrator.load();
    }

    // Setting up acquisition state manager:
    {
      AcquisitionStateManager<LightSheetAcquisitionStateInterface<?>>
          lAcquisitionStateManager;
      lAcquisitionStateManager =
          (AcquisitionStateManager<LightSheetAcquisitionStateInterface<?>>) addAcquisitionStateManager();
      InterpolatedAcquisitionState
          lAcquisitionState =
          new InterpolatedAcquisitionState("default", this);
      lAcquisitionState.setupControlPlanes(pNumberOfControlPlanes,
                                           ControlPlaneLayout.Circular);
      lAcquisitionState.copyCurrentMicroscopeSettings();
      lAcquisitionStateManager.setCurrentState(lAcquisitionState);
      addInteractiveAcquisition();

      addDevice(0, new AcquisitionStateBackupRestoreInstruction(true, this));
      addDevice(0, new AcquisitionStateBackupRestoreInstruction(false, this));

      addDevice(0, new AcquisitionStateResetInstruction(this));

      addDevice(0, new InterpolatedAcquisitionStateLogInstruction(this));

      // Adding adaptive engine device:
      {
        AdaptationStateEngine.setup(this, lAcquisitionState);
      }

      // Setup acquisition state IO
      addDevice(0, new WriteAcquisitionStateToDiscInstruction(this));
      addDevice(0, new ReadAcquisitionStateFromDiscInstruction(this));
    }

    // Adding timelapse device:
    TimelapseInterface lTimelapse = addTimelapse();
    lTimelapse.getAdaptiveEngineOnVariable().set(false);

    lTimelapse.addFileStackSinkType(RawFileStackSink.class);
    lTimelapse.addFileStackSinkType(ZarrStackSink.class);
    // lTimelapse.addFileStackSinkType(SqeazyFileStackSink.class);

    // ------------------------------------------------------------------------
    // setup multiview acquisition and fusion
    if (multiview)
    {
      // ------------------------------------------------------------------------
      // interleaved imaging
      addDevice(0, new InterleavedAcquisitionInstruction(this));
      addDevice(0, new InterleavedFusionInstruction(this));
      addDevice(0, new WriteInterleavedRawDataToDiscInstruction(this));
      addDevice(0, new WriteFusedImageAsRawToDiscInstruction("interleaved", this));
      addDevice(0,
                new DropOldestStackInterfaceContainerInstruction(
                    InterleavedImageDataContainer.class,
                    getDataWarehouse()));


      // ------------------------------------------------------------------------
      // Sequential imaging
      addDevice(0, new SequentialAcquisitionInstruction(this));
      addDevice(0, new SequentialFusionInstruction(this));
      addDevice(0, new WriteSequentialRawDataToDiscInstruction(this));
      addDevice(0, new WriteFusedImageAsRawToDiscInstruction("sequential", this));
      addDevice(0,
                new DropOldestStackInterfaceContainerInstruction(
                    SequentialImageDataContainer.class,
                    getDataWarehouse()));


      // ------------------------------------------------------------------------
      // optics prefused imaging
      addDevice(0, new OpticsPrefusedAcquisitionInstruction(this));
      addDevice(0, new OpticsPrefusedFusionInstruction(this));
      addDevice(0, new WriteOpticsPrefusedRawDataAsRawToDiscInstruction(this));
      addDevice(0, new WriteFusedImageAsRawToDiscInstruction("opticsprefused", this));
      addDevice(0,
                new DropOldestStackInterfaceContainerInstruction(
                    OpticsPrefusedImageDataContainer.class,
                    getDataWarehouse()));


      addDevice(0,
                new DropOldestStackInterfaceContainerInstruction(FusedImageDataContainer.class,
                                                                 getDataWarehouse()));
      addDevice(0,
                new ViewStack3DInstruction<FusedImageDataContainer>(
                    FusedImageDataContainer.class,
                    this));
    }

    String[] lOpticPrefusedStackKeys = new String[getNumberOfDetectionArms()];
    String[] lInterleavedStackKeys = new String[getNumberOfDetectionArms()];
    String[] lInterleavedWaistStackKeys = new String[getNumberOfDetectionArms()];
    String[]
        lHybridInterleavedOpticsPrefusedStackKeys =
        new String[getNumberOfDetectionArms()];
    String[]
        lSequentialStackKeys =
        new String[getNumberOfDetectionArms() * getNumberOfLightSheets()];

    for (int c = 0; c < getNumberOfDetectionArms(); c++)
    {
      for (int l = 0; l < getNumberOfLightSheets(); l++)
      {
        addDevice(0, new SingleViewAcquisitionInstruction(c, l, this));

        addDevice(0, new WriteSingleLightSheetImageAsRawToDiscInstruction(c, l, this));

        lSequentialStackKeys[c * getNumberOfLightSheets() + l] = "C" + c + "L" + l;
        addDevice(0,
                  new ReadStackInterfaceContainerFromDiscInstruction(new String[] { "C"
                                                                                    + c
                                                                                    + "L"
                                                                                    + l },
                                                                     this));
      }
      lOpticPrefusedStackKeys[c] = "C" + c + "opticsprefused";
      lInterleavedStackKeys[c] = "C" + c + "interleaved";
      lHybridInterleavedOpticsPrefusedStackKeys[c] = "hybrid_interleaved_opticsprefused";
      lInterleavedWaistStackKeys[c] = "C" + c + "interleaved_waist";


    }

    // ------------------------------------------------------------------------
    // Simple autofocus
    addDevice(0, new AutoFocusSinglePlaneInstruction(this));

    // ------------------------------------------------------------------------
    // setup writers
    addDevice(0,
              new WriteSpecificStackToSpecificRawFolderInstruction("fused",
                                                                   "default",
                                                                   this));

    addDevice(0,
              new WriteAllStacksAsRawToDiscInstruction(StackInterfaceContainer.class,
                                                       this));

    // ------------------------------------------------------------------------
    // setup reades / simulated acquisition
    addDevice(0,
              new ReadStackInterfaceContainerFromDiscInstruction(new String[] { "default" },
                                                                 this));
    addDevice(0,
              new ReadStackInterfaceContainerFromDiscInstruction(new String[] {
                  "sequential" }, this));
    addDevice(0,
              new ReadStackInterfaceContainerFromDiscInstruction(new String[] {
                  "interleaved" }, this));
    addDevice(0,
              new ReadStackInterfaceContainerFromDiscInstruction(new String[] {
                  "opticsprefused" }, this));
    addDevice(0,
              new ReadStackInterfaceContainerFromDiscInstruction(lOpticPrefusedStackKeys,
                                                                 this));
    addDevice(0,
              new ReadStackInterfaceContainerFromDiscInstruction(lSequentialStackKeys,
                                                                 this));
    addDevice(0,
              new ReadStackInterfaceContainerFromDiscInstruction(lInterleavedStackKeys,
                                                                 this));
    addDevice(0,
              new ReadStackInterfaceContainerFromDiscInstruction(
                  lHybridInterleavedOpticsPrefusedStackKeys,
                  this));
    addDevice(0,
              new ReadStackInterfaceContainerFromDiscInstruction(
                  lInterleavedWaistStackKeys,
                  this));


    // ------------------------------------------------------------------------
    // Setup viewers
    addDevice(0, new ViewStack2DInstruction(0, StackInterfaceContainer.class, this));
    addDevice(0,
              new ViewStack3DInstruction<StackInterfaceContainer>(StackInterfaceContainer.class,
                                                                  this));
    if (getNumberOfLightSheets() > 1 || getNumberOfDetectionArms() > 1)
    {
      addDevice(0, new ViewStack2DInstruction(0, FusedImageDataContainer.class, this));
    }

    // -------------------------------------------------------------------------
    // Setup pauses
    int[] pauseTimes = { 1000, // 1 s
                         10000, // 10 s
                         30000, // 30 s
                         60000, // 1 min
                         300000, // 5 min
                         600000, // 10 min
                         3600000 // 1 h
    };
    String[] timeMeasurementKeys = { "A", "B", "C" };
    for (int i = 0; i < pauseTimes.length; i++)
    {
      addDevice(0, new PauseInstruction(pauseTimes[i]));
    }
    for (int k = 0; k < timeMeasurementKeys.length; k++)
    {
      addDevice(0, new MeasureTimeInstruction(timeMeasurementKeys[k]));
      for (int i = 0; i < pauseTimes.length; i++)
      {
        addDevice(9,
                  new PauseUntilTimeAfterMeasuredTimeInstruction(timeMeasurementKeys[k],
                                                                 pauseTimes[i]));
      }
    }

    // --------------------------------------------------------------------
    // setup focus finders
    int
        lNumberOfControlPlanes =
        ((InterpolatedAcquisitionState) (getAcquisitionStateManager().getCurrentState())).getNumberOfControlPlanes();
    for (int cpi = 0; cpi < lNumberOfControlPlanes; cpi++)
    {
      for (int d = 0; d < getNumberOfDetectionArms(); d++)
      {
        for (int l = 0; l < getNumberOfLightSheets(); l++)
        {
          addDevice(0, new FocusFinderZInstruction(l, d, cpi, this));
          addDevice(0, new FocusFinderAlphaByVariationInstruction(l, d, cpi, this));
        }
        addDevice(0,
                  new ControlPlaneFocusFinderAlphaByVariationInstruction(d, cpi, this));
        addDevice(0, new ControlPlaneFocusFinderZInstruction(d, cpi, this));
      }
    }

    // -------------------------------------------------------------------------
    // setup configuration instructions

    addDevice(0, new ChangeLightSheetXInstruction(this, 0, 0.0));
    addDevice(0, new ChangeLightSheetYInstruction(this, 0, 0.0));

    addDevice(0, new ChangeLightSheetWidthInstruction(this, 0));
    addDevice(0, new ChangeLightSheetHeightInstruction(this, 0, 0.0));
    addDevice(0, new ChangeLaserLineOnOffInstruction(this, 0));
    addDevice(0, new ChangeLightSheetBrightnessInstruction(this, 0, 0));



    addDevice(0, new ChangeImageSizeInstruction(this));
    addDevice(0, new ChangeZRangeInstruction(this));

    addDevice(0, new TimelapseStopInstruction(this));

    addDefaultProgram();
  }

}
