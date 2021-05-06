package clearcontrol.timelapse.instructions;

import clearcontrol.LightSheetMicroscope;
import clearcontrol.LightSheetMicroscopeQueue;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.processor.LightSheetFastFusionProcessor;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.sourcesink.sink.FileStackSinkInterface;
import clearcontrol.state.InterpolatedAcquisitionState;
import clearcontrol.timelapse.LightSheetTimelapse;

/**
 * This class contains generalised methods for all AcquisitionSchedulers
 * <p>
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) February
 * 2018
 */
public abstract class AbstractAcquistionInstruction extends LightSheetMicroscopeInstructionBase implements InstructionInterface, LoggingFeature
{

  protected String mImageKeyToSave = "fused";
  protected Variable<String> mChannelName = new Variable<String>("Dataset name for saving", "default");

  protected StackInterface mLastFusedStack;
  protected StackInterface mLastAcquiredStack;

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pDeviceName device name
   */
  public AbstractAcquistionInstruction(String pDeviceName, LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pDeviceName, pLightSheetMicroscope);
  }

  protected InterpolatedAcquisitionState mCurrentState;
  protected LightSheetTimelapse mTimelapse;
  protected Long mTimeStampBeforeImaging = 0L;

  @Override
  public boolean initialize()
  {

    mCurrentState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState();
    mTimelapse = getLightSheetMicroscope().getDevice(LightSheetTimelapse.class, 0);

    LightSheetFastFusionProcessor lProcessor = getLightSheetMicroscope().getDevice(LightSheetFastFusionProcessor.class, 0);
    if (lProcessor != null)
    {
      lProcessor.initializeEngine();
    }

    return true;
  }

  @Deprecated
  protected void initializeStackSaving(FileStackSinkInterface pFileStackSinkInterface)
  {
    warning("initializeStackSaving is deprecated and will be removed");
  }

  protected void goToInitialPosition(LightSheetMicroscope lLightsheetMicroscope, LightSheetMicroscopeQueue lQueue, double lIlluminationZStart, double lDetectionZZStart)
  {
    ((InterpolatedAcquisitionState) lLightsheetMicroscope.getAcquisitionStateManager().getCurrentState()).applyAcquisitionStateAtZ(lQueue, lIlluminationZStart);
    for (int l = 0; l < lLightsheetMicroscope.getNumberOfLightSheets(); l++)
    {
      lQueue.setI(l, false);
      lQueue.setIZ(lIlluminationZStart);
    }
    for (int d = 0; d < lLightsheetMicroscope.getNumberOfDetectionArms(); d++)
    {
      lQueue.setDZ(d, lDetectionZZStart);
      lQueue.setC(d, false);
    }

    lQueue.addCurrentStateToQueue();
    lQueue.addCurrentStateToQueue();
    lQueue.addVoxelDimMetaData(lLightsheetMicroscope, mCurrentState.getStackZStepVariable().get().doubleValue());
  }


  protected boolean isCameraOn(int pCameraIndex)
  {
    return mCurrentState.getCameraOnOffVariable(pCameraIndex).get();
  }

}
