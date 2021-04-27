package dorado;

import clearcl.ClearCLContext;
import clearcontrol.devices.cameras.StackCameraDeviceInterface;
import clearcontrol.devices.cameras.devices.hamamatsu.HamStackCamera;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.devices.lasers.devices.sim.LaserDeviceSimulator;
import clearcontrol.devices.lasers.instructions.ChangeLaserPowerInstruction;
import clearcontrol.devices.lasers.instructions.SwitchLaserOnOffInstruction;
import clearcontrol.devices.lasers.instructions.SwitchLaserPowerOnOffInstruction;
import clearcontrol.devices.optomech.filterwheels.devices.ludl.LudlFilterWheelDevice;
import clearcontrol.devices.optomech.filterwheels.instructions.FilterWheelInstruction;
import clearcontrol.devices.signalgen.devices.nirio.NIRIOSignalGenerator;
import clearcontrol.microscope.lightsheet.adaptive.instructions.AdaptationInstruction;
import clearcontrol.microscope.lightsheet.adaptive.modules.*;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArm;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheet;
import clearcontrol.microscope.lightsheet.component.lightsheet.instructions.ChangeLightSheetWidthInstruction;
import clearcontrol.microscope.lightsheet.component.opticalswitch.LightSheetOpticalSwitch;
import clearcontrol.microscope.lightsheet.signalgen.LightSheetSignalGeneratorDevice;
import clearcontrol.microscope.lightsheet.simulation.LightSheetMicroscopeSimulationDevice;
import clearcontrol.microscope.lightsheet.simulation.SimulatedLightSheetMicroscope;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import dorado.adaptive.AdaptiveZInstruction;
import clearcontrol.microscope.lightsheet.component.lightsheet.instructions.MultiChannelInstruction;

import java.util.ArrayList;


/**
 * Dorado microscope
 *
 * @author royer
 */
public class DoradoMicroscope extends SimulatedLightSheetMicroscope
{

  /**
   * Instantiates an Dorado microscope
   * 
   * @param pStackFusionContext
   *          ClearCL context for stack fusion
   * @param pMaxStackProcessingQueueLength
   *          max stack processing queue length
   * @param pThreadPoolSize
   *          thread pool size
   */
  public DoradoMicroscope(ClearCLContext pStackFusionContext,
                          int pMaxStackProcessingQueueLength,
                          int pThreadPoolSize)
  {
    super("Dorado",
          pStackFusionContext,
          pMaxStackProcessingQueueLength,
          pThreadPoolSize);

  }

  /**
   * Assembles the microscope
   * 
   * @param pNumberOfDetectionArms
   *          number of detection arms
   * @param pNumberOfLightSheets
   *          number of lightsheets
   */
  public void addRealHardwareDevices(int pNumberOfDetectionArms,
                                     int pNumberOfLightSheets, boolean pUseStages)
  {
    long lDefaultStackWidth = 2048;
    long lDefaultStackHeight = 2048;

    if (pUseStages)
    {
      // Setup stage here...
    }

    // Setting up lasers:
    {
      addDevice(0,new LaserDeviceSimulator("405",0,405, 100));
      addDevice(1,new LaserDeviceSimulator("488",0,488, 100));
      addDevice(1,new LaserDeviceSimulator("561",0,561, 100));
      addDevice(2,new LaserDeviceSimulator("637",0,637, 100));
    }

    // Setting up cameras and filterwheels:
    if (true)
    {
      for (int c = 0; c < pNumberOfDetectionArms; c++)
      {
        StackCameraDeviceInterface<?> lCamera =
                                              HamStackCamera.buildWithExternalTriggering(c);

        lCamera.getStackWidthVariable().set(lDefaultStackWidth);
        lCamera.getStackHeightVariable().set(lDefaultStackHeight);
        lCamera.getExposureInSecondsVariable().set(0.020);

        // lCamera.getStackVariable().addSetListener((o,n)->
        // {System.out.println("camera output:"+n);} );

        addDevice(c, lCamera);


        LudlFilterWheelDevice lFilterWheel = new LudlFilterWheelDevice(c);
        addDevice(c, lFilterWheel);

        for(int i=0; i<lFilterWheel.getValidPositions().length; i++)
        {
          FilterWheelInstruction lFilterWheelInstruction = new FilterWheelInstruction(lFilterWheel,i);
          addDevice(c, lFilterWheelInstruction);
        }


      }
    }


    // Adding signal Generator:
    LightSheetSignalGeneratorDevice lLSSignalGenerator;
    {
      NIRIOSignalGenerator lNIRIOSignalGenerator =
                                                 new NIRIOSignalGenerator();
      lLSSignalGenerator =
                         LightSheetSignalGeneratorDevice.wrap(lNIRIOSignalGenerator,
                                                              true);
      // addDevice(0, lNIRIOSignalGenerator);
      addDevice(0, lLSSignalGenerator);
    }

    // Setting up detection arms:
    {
      for (int c = 0; c < pNumberOfDetectionArms; c++)
      {
        final DetectionArm lDetectionArm = new DetectionArm("D" + c);
        lDetectionArm.getPixelSizeInMicrometerVariable().set(getDevice(StackCameraDeviceInterface.class, c).getPixelSizeInMicrometersVariable().get());

        addDevice(c, lDetectionArm);
      }
    }

    // Setting up lightsheets:
    {

      for (int l = 0; l < pNumberOfLightSheets; l++)
      {
        final LightSheet lLightSheet =
                                     new LightSheet("I" + l,
                                                    9.4,
                                                    getNumberOfLaserLines());
        addDevice(l, lLightSheet);
      }
    }

    // syncing exposure between cameras and lightsheets, as well as camera image
    // height:
    {
      for (int l = 0; l < pNumberOfLightSheets; l++)
        for (int c = 0; c < pNumberOfDetectionArms; c++)
        {
          StackCameraDeviceInterface<?> lCamera =
                                                getDevice(StackCameraDeviceInterface.class,
                                                          c);
          LightSheet lLightSheet = getDevice(LightSheet.class, l);

          lCamera.getExposureInSecondsVariable()
                 .sendUpdatesTo(lLightSheet.getEffectiveExposureInSecondsVariable());

          lCamera.getStackHeightVariable()
                 .sendUpdatesTo(lLightSheet.getImageHeightVariable());

        }
    }

    // Setting up lightsheets selector
    {
      LightSheetOpticalSwitch lLightSheetOpticalSwitch =
                                                       new LightSheetOpticalSwitch("OpticalSwitch",
                                                                                   pNumberOfLightSheets);

      addDevice(0, lLightSheetOpticalSwitch);
    }

  }

  @Override
  public void addSimulatedDevices(boolean pDummySimulation,
                                  boolean pXYZRStage,
                                  boolean pSharedLightSheetControl,
                                  LightSheetMicroscopeSimulationDevice pSimulatorDevice)
  {
    super.addSimulatedDevices(pDummySimulation, pXYZRStage, pSharedLightSheetControl, pSimulatorDevice);

  }

  @Override
  public void addStandardDevices(int pNumberOfControlPlanes) {
    super.addStandardDevices(pNumberOfControlPlanes);

    LightSheetTimelapse timelapse = getTimelapse();
    timelapse.getListOfActivatedSchedulers().add(0, new ChangeLightSheetWidthInstruction(this, 0.45));


    // setup adaptators/schedulers
    {
      AdaptiveZInstruction lAdaptiveZInstruction = new AdaptiveZInstruction(this);
      addDevice(0, lAdaptiveZInstruction);
    }

    {
      AdaptationInstruction lAdaptationScheduler = new AdaptationInstruction("Adaptation: Focus Z",
                                                                         AdaptationZ.class, this);
      addDevice(0, lAdaptationScheduler);
    }
    {
      AdaptationInstruction lAdaptationScheduler = new AdaptationInstruction("Adaptation: Focus Z with manual detection arm selection",
                                                                         AdaptationZManualDetectionArmSelection.class, this);
      addDevice(0, lAdaptationScheduler);
    }
    {
      AdaptationInstruction lAdaptationScheduler = new AdaptationInstruction("Adaptation: Focus Z with sliding window detection arm selection",
                                                                         AdaptationZSlidingWindowDetectionArmSelection.class, this);
      addDevice(0, lAdaptationScheduler);
    }
    {
      AdaptationInstruction lAdaptationScheduler = new AdaptationInstruction("Adaptation: Lightsheet angle alpha",
                                                                         AdaptationA.class, this);
      addDevice(0, lAdaptationScheduler);
    }
    {
      AdaptationInstruction lAdaptationScheduler = new AdaptationInstruction("Adaptation: Power",
                                                                         AdaptationP.class, this);
      addDevice(0, lAdaptationScheduler);
    }
    {
      AdaptationInstruction lAdaptationScheduler = new AdaptationInstruction("Adaptation: Lightsheet X position",
                                                                         AdaptationX.class, this);
      addDevice(0, lAdaptationScheduler);
    }

    {
      MultiChannelInstruction lMultiChannelInstruction = new MultiChannelInstruction(this);
      addDevice(0, lMultiChannelInstruction);
    }

  }

}
