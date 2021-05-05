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

//  protected void putStackInContainer(String pKey, StackInterface pStack, StackInterfaceContainer pContainer)
//  {
//    StackRecyclerManager lStackRecyclerManager = getLightSheetMicroscope().getDevice(StackRecyclerManager.class, 0);
//    RecyclerInterface<StackInterface, StackRequest> lRecycler = lStackRecyclerManager.getRecycler("warehouse", 1024, 1024);
//
//    Variable<StackInterface> lStackCopyVariable = new Variable<StackInterface>("stackcopy", null);
//    ElapsedTime.measureForceOutput("Copy stack (" + pKey + ") for container", () ->
//    {
//      lStackCopyVariable.set(lRecycler.getOrWait(1000, TimeUnit.SECONDS, StackRequest.build(pStack.getDimensions())));
//
//      // we need to copy the data out of the
//      // input-buffer from the camera
//      pStack.getContiguousMemory().copyTo(lStackCopyVariable.get().getContiguousMemory());
//      lStackCopyVariable.get().setMetaData(pStack.getMetaData().clone());
//    });
//    if ((lStackCopyVariable.get().getMetaData().getTimeStampInNanoseconds() - mTimeStampBeforeImaging) < 0)
//    {
//      warning("Error: an acquired image is older than its request!");
//    }
//
//    info(pKey + " (" + lStackCopyVariable.get().getMetaData().getValue(MetaDataOrdinals.TimePoint) + ") in a container " + MetaDataView.getCxLyString(lStackCopyVariable.get().getMetaData()));
//    pContainer.put(pKey, lStackCopyVariable.get());
//
//    mLastAcquiredStack = lStackCopyVariable.get();
//
//  }

  @Deprecated
  protected void initializeStackSaving(FileStackSinkInterface pFileStackSinkInterface)
  {
    warning("initializeStackSaving is deprecated and will be removed");
  }

  protected void goToInitialPosition(LightSheetMicroscope lLightsheetMicroscope, LightSheetMicroscopeQueue lQueue, double lIlluminationZStart, double lDetectionZZStart)
  {
    double widthBefore = lQueue.getIW(0);

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
    double widthAfter = lQueue.getIW(0);

    if (Math.abs(widthAfter - widthBefore) > 0.1)
    {
      // if the width of the light sheets changed significantly, we
      // need to wait a second until the iris has been moved...
      lQueue.setExp(0.5);
    }
    lQueue.addCurrentStateToQueue();
    lQueue.addCurrentStateToQueue();
    lQueue.addVoxelDimMetaData(lLightsheetMicroscope, mCurrentState.getStackZStepVariable().get().doubleValue());
  }

  @Deprecated
  protected void handleImageFromCameras(long pTimepoint)
  {
    warning("handleImagesFromCameras is deprecated and will be removed ");
  }

  /**
   * Deprecated: access resultimg image stacks from the data warehouse
   *
   * @return
   */
  @Deprecated
  public StackInterface getLastAcquiredStack()
  {
    warning("getLastAcquiredStack is deprecated and will be removed. Access acquired images via the DataWarehouse instead!");
    return mLastFusedStack;
  }

  protected boolean isCameraOn(int pCameraIndex)
  {
    return mCurrentState.getCameraOnOffVariable(pCameraIndex).get();
  }

  protected boolean isFused()
  {
    return true;
  }

  public Variable<String> getChannelName()
  {
    return mChannelName;
  }
}
