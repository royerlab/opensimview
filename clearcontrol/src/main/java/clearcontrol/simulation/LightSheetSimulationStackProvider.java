package clearcontrol.simulation;

import clearcl.ClearCLImage;
import clearcl.util.ElapsedTime;
import clearcontrol.LightSheetMicroscopeInterface;
import clearcontrol.LightSheetMicroscopeQueue;
import clearcontrol.component.detection.DetectionArmInterface;
import clearcontrol.component.detection.DetectionArmQueue;
import clearcontrol.component.lightsheet.LightSheetInterface;
import clearcontrol.component.lightsheet.LightSheetQueue;
import clearcontrol.component.opticalswitch.LightSheetOpticalSwitch;
import clearcontrol.component.opticalswitch.LightSheetOpticalSwitchQueue;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.devices.cameras.devices.sim.StackCameraSimulationProvider;
import clearcontrol.devices.cameras.devices.sim.StackCameraSimulationProviderBase;
import clearcontrol.devices.cameras.devices.sim.StackCameraSimulationQueue;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.stack.StackInterface;
import coremem.ContiguousMemoryInterface;
import simbryo.synthoscopy.microscope.lightsheet.LightSheetMicroscopeSimulator;
import simbryo.synthoscopy.microscope.parameters.CameraParameter;
import simbryo.synthoscopy.microscope.parameters.DetectionParameter;
import simbryo.synthoscopy.microscope.parameters.IlluminationParameter;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightsheet microscope simulation stack provider
 *
 * @author royer
 */
public class LightSheetSimulationStackProvider extends StackCameraSimulationProviderBase implements StackCameraSimulationProvider, LoggingFeature
{

  private LightSheetMicroscopeInterface mLightSheetMicroscope;
  private LightSheetMicroscopeSimulator mSimbryoLightSheetMicroscopeSimulator;
  private int mCameraIndex;

  private DetectionArmInterface mDetectionArmDevice;
  private LightSheetOpticalSwitch mOpticalSwitch;
  private ArrayList<LightSheetInterface> mLightSheetList = new ArrayList<>();

  private DetectionArmQueue mDetectionStateQueue;
  private LightSheetOpticalSwitchQueue mOpticalSwitchStateQueue;
  private ConcurrentHashMap<Integer, LightSheetQueue> mLightSheetStateQueuesMap = new ConcurrentHashMap<>();

  /**
   * Instanciates a lightsheet simulation stack provider.
   *
   * @param pLightSheetMicroscope          light sheet sample simulation device
   * @param pSimbryoLightSheetMicroscopeSimulator Simbryo light sheet microscope simulator
   * @param pCameraIndex                   camera index
   */
  public LightSheetSimulationStackProvider(LightSheetMicroscopeInterface pLightSheetMicroscope, LightSheetMicroscopeSimulator pSimbryoLightSheetMicroscopeSimulator, int pCameraIndex)
  {
    mLightSheetMicroscope = pLightSheetMicroscope;
    mSimbryoLightSheetMicroscopeSimulator = pSimbryoLightSheetMicroscopeSimulator;
    mCameraIndex = pCameraIndex;

    mDetectionArmDevice = mLightSheetMicroscope.getDevice(DetectionArmInterface.class, mCameraIndex);

    mOpticalSwitch = mLightSheetMicroscope.getDevice(LightSheetOpticalSwitch.class, 0);

    int lNumberOfLightSheets = mLightSheetMicroscope.getNumberOfDevices(LightSheetInterface.class);

    for (int l = 0; l < lNumberOfLightSheets; l++)
      mLightSheetList.add(mLightSheetMicroscope.getDevice(LightSheetInterface.class, l));

  }

  @Override
  protected void fillStackData(StackCameraSimulationQueue pQueue, ArrayList<Boolean> pKeepPlaneList, long pWidth, long pHeight, long pDepth, StackInterface pStack)
  {
    synchronized (mSimbryoLightSheetMicroscopeSimulator)
    {

      // Advance simulation time:
      mSimbryoLightSheetMicroscopeSimulator.advance();

      int lWidth = (int) pStack.getWidth();
      int lHeight = (int) pStack.getHeight();

      float lExposureInSeconds = pQueue.getExposureInSecondsVariable().get().floatValue();

      mSimbryoLightSheetMicroscopeSimulator.setNumberParameter(CameraParameter.Exposure, mCameraIndex, lExposureInSeconds);

      mSimbryoLightSheetMicroscopeSimulator.setNumberParameter(CameraParameter.ROIWidth, mCameraIndex, lWidth);
      mSimbryoLightSheetMicroscopeSimulator.setNumberParameter(CameraParameter.ROIHeight, mCameraIndex, lHeight);

      final ContiguousMemoryInterface lContiguousMemory = pStack.getContiguousMemory();

      int lLastZiKept = getLast(pKeepPlaneList);

      int lQueueLength = pQueue.getQueueLength();

      LightSheetMicroscopeQueue lLightSheetMicroscopeQueue = mLightSheetMicroscope.getPlayedQueueVariable().get();

      boolean lIsSharedLightSheetControl = lLightSheetMicroscopeQueue.getLightSheetSignalGeneratorQueue().getLightSheetSignalGeneratorDevice().getIsSharedLightSheetControlVariable().get();

      int lSelectedLightSheet = lLightSheetMicroscopeQueue.getLightSheetSignalGeneratorQueue().getSelectedLightSheetIndexVariable().get();

      collectDetectionStateQueues(lLightSheetMicroscopeQueue);

      collectOpticalSwitchStateQueues(lLightSheetMicroscopeQueue);

      for (int l = 0; l < mLightSheetList.size(); l++)
        collectIluminationStateQueues(lLightSheetMicroscopeQueue, l);
      /**/

      for (int zi = 0, i = 0; zi < lQueueLength; zi++)
      {

        passDetectionParameters(zi);

        for (int l = 0; l < mLightSheetList.size(); l++)
          if (lIsSharedLightSheetControl)
            passIlluminationParameters(lSelectedLightSheet % mLightSheetMicroscope.getNumberOfLightSheets(), l, zi);
          else passIlluminationParameters(l, l, zi);

        @SuppressWarnings("unused") double lMilliseconds = ElapsedTime.measure("!!renderplane", () -> mSimbryoLightSheetMicroscopeSimulator.render(mCameraIndex, true));
        // info("Rendering plane %d in %g ms \n",zi,lMilliseconds);

        if (pKeepPlaneList.get(zi))
        {
          ClearCLImage lCameraImage = mSimbryoLightSheetMicroscopeSimulator.getCameraImage(mCameraIndex);

          long lOffset = i++ * lCameraImage.getSizeInBytes();

          ContiguousMemoryInterface lImagePlane = lContiguousMemory.subRegion(lOffset, lCameraImage.getSizeInBytes());

          final int fzi = zi;
          lMilliseconds = ElapsedTime.measure("!!copyplane", () -> lCameraImage.writeTo(lImagePlane, fzi == lLastZiKept)); //
          // info("Copying plane %d in %g ms \n",zi,lMilliseconds);

        }
      }
    }
  }

  private void collectDetectionStateQueues(LightSheetMicroscopeQueue pLightSheetMicroscopeQueue)
  {
    mDetectionStateQueue = (DetectionArmQueue) pLightSheetMicroscopeQueue.getDeviceQueue(mDetectionArmDevice);
  }

  private void collectOpticalSwitchStateQueues(LightSheetMicroscopeQueue pLightSheetMicroscopeQueue)
  {
    mOpticalSwitchStateQueue = (LightSheetOpticalSwitchQueue) pLightSheetMicroscopeQueue.getDeviceQueue(mOpticalSwitch);

  }

  private void collectIluminationStateQueues(LightSheetMicroscopeQueue pLightSheetMicroscopeQueue, int pLightSheetIndex)
  {
    LightSheetInterface lLightSheetDevice = mLightSheetList.get(pLightSheetIndex);
    mLightSheetStateQueuesMap.put(pLightSheetIndex, (LightSheetQueue) pLightSheetMicroscopeQueue.getDeviceQueue(lLightSheetDevice));
  }

  private void passDetectionParameters(int zi)
  {

    float z = mDetectionStateQueue.getQueuedValue(mDetectionArmDevice.getZFunction().get(), mDetectionStateQueue.getZVariable(), zi).floatValue();
    mSimbryoLightSheetMicroscopeSimulator.setNumberParameter(DetectionParameter.Z, mCameraIndex, z);

  }

  private void passIlluminationParameters(int pLightSheetIndexSelected, int pLightSheetIndex, int zi)
  {
    LightSheetInterface lLightSheet = mLightSheetList.get(pLightSheetIndexSelected);

    LightSheetQueue lSelectedLightSheetQueue = mLightSheetStateQueuesMap.get(pLightSheetIndexSelected);

    LightSheetQueue lLightSheetQueue = mLightSheetStateQueuesMap.get(pLightSheetIndex);

    float x = lSelectedLightSheetQueue.getQueuedValue(lLightSheet.getXFunction().get(), lSelectedLightSheetQueue.getXVariable(), zi).floatValue();

    mSimbryoLightSheetMicroscopeSimulator.setNumberParameter(IlluminationParameter.X, pLightSheetIndex, x);

    float y = lSelectedLightSheetQueue.getQueuedValue(lLightSheet.getYFunction().get(), lSelectedLightSheetQueue.getYVariable(), zi).floatValue();

    mSimbryoLightSheetMicroscopeSimulator.setNumberParameter(IlluminationParameter.Y, pLightSheetIndex, y);

    float z = lSelectedLightSheetQueue.getQueuedValue(lLightSheet.getZFunction().get(), lSelectedLightSheetQueue.getZVariable(), zi).floatValue();

    mSimbryoLightSheetMicroscopeSimulator.setNumberParameter(IlluminationParameter.Z, pLightSheetIndex, z);

    float alpha = lLightSheetQueue.getQueuedValue(lLightSheet.getAlphaFunction().get(), lLightSheetQueue.getAlphaInDegreesVariable(), zi).floatValue();

    mSimbryoLightSheetMicroscopeSimulator.setNumberParameter(IlluminationParameter.Alpha, pLightSheetIndex, alpha);

    float beta = lLightSheetQueue.getQueuedValue(lLightSheet.getBetaFunction().get(), lLightSheetQueue.getBetaInDegreesVariable(), zi).floatValue();

    mSimbryoLightSheetMicroscopeSimulator.setNumberParameter(IlluminationParameter.Beta, pLightSheetIndex, beta);

    float height = lSelectedLightSheetQueue.getQueuedValue(lLightSheet.getHeightFunction().get(), lSelectedLightSheetQueue.getHeightVariable(), zi).floatValue();

    mSimbryoLightSheetMicroscopeSimulator.setNumberParameter(IlluminationParameter.Height, pLightSheetIndex, height);

    float lLightSheetPower =

            lLightSheetQueue.getQueuedValue(lLightSheet.getPowerFunction().get(), lLightSheetQueue.getPowerVariable(), zi).floatValue();

    boolean lLightSheetSwitchedOn = mOpticalSwitchStateQueue.getQueuedBooleanValue(mOpticalSwitchStateQueue.getSwitchVariable(pLightSheetIndex), zi);

    LaserDeviceInterface lLaserDevice = mLightSheetMicroscope.getDevice(LaserDeviceInterface.class, 0);

    float lLaserPower = lLaserDevice.getCurrentPowerInMilliWattVariable().get().floatValue();

    float lEffectiveIlluminationIntensity = (lLightSheetSwitchedOn ? 1 : 0) * lLightSheetPower * lLaserPower;

    mSimbryoLightSheetMicroscopeSimulator.setNumberParameter(IlluminationParameter.Intensity, pLightSheetIndex, lEffectiveIlluminationIntensity);

  }

  private int getLast(ArrayList<Boolean> pKeepPlaneList)
  {
    int lSize = pKeepPlaneList.size();
    for (int i = lSize - 1; i >= 0; i--)
      if (pKeepPlaneList.get(i)) return i;
    return 0;
  }

}
