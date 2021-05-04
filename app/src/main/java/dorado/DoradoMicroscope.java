package dorado;

import clearcl.ClearCLContext;
import clearcontrol.adaptive.modules.*;
import clearcontrol.devices.cameras.StackCameraDeviceInterface;
import clearcontrol.devices.cameras.devices.hamamatsu.HamStackCamera;
import clearcontrol.devices.lasers.devices.sim.LaserDeviceSimulator;
import clearcontrol.devices.optomech.filterwheels.devices.ludl.LudlFilterWheelDevice;
import clearcontrol.devices.optomech.filterwheels.instructions.FilterWheelInstruction;
import clearcontrol.devices.signalgen.devices.nirio.NIRIOSignalGenerator;
import clearcontrol.component.detection.DetectionArm;
import clearcontrol.component.lightsheet.LightSheet;
import clearcontrol.component.lightsheet.instructions.ChangeLightSheetWidthInstruction;
import clearcontrol.component.opticalswitch.LightSheetOpticalSwitch;
import clearcontrol.signalgen.LightSheetSignalGeneratorDevice;
import clearcontrol.simulation.LightSheetMicroscopeSimulationDevice;
import clearcontrol.simulation.SimulatedLightSheetMicroscope;
import clearcontrol.timelapse.LightSheetTimelapse;
import clearcontrol.component.lightsheet.instructions.MultiChannelInstruction;


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

    {
      MultiChannelInstruction lMultiChannelInstruction = new MultiChannelInstruction(this);
      addDevice(0, lMultiChannelInstruction);
    }

  }

}
