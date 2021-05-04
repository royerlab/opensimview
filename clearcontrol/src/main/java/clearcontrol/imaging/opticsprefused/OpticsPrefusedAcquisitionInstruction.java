package clearcontrol.imaging.opticsprefused;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.LightSheetMicroscopeQueue;
import clearcontrol.imaging.AbstractAcquistionInstruction;
import clearcontrol.processor.MetaDataFusion;
import clearcontrol.stack.MetaDataView;
import clearcontrol.state.InterpolatedAcquisitionState;
import clearcontrol.stack.metadata.MetaDataAcquisitionType;
import clearcontrol.state.AcquisitionType;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.metadata.MetaDataOrdinals;
import clearcontrol.stack.metadata.StackMetaData;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This instructions acquires an image stack per camera where all light sheets are on. The
 * image stacks are stored in the DataWarehouse in an OpticsPrefusedImageDataContainer
 * with keys like CXopticsprefused with X representing the camera number.
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) February
 * 2018
 */
public class OpticsPrefusedAcquisitionInstruction extends AbstractAcquistionInstruction implements InstructionInterface, LoggingFeature
{
  /**
   * INstanciates a virtual device with a given name
   */
  public OpticsPrefusedAcquisitionInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Acquisition: optics-prefused", pLightSheetMicroscope);
    mChannelName.set("opticsprefused");
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    mCurrentState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState();

    int lImageWidth = mCurrentState.getImageWidthVariable().get().intValue();
    int lImageHeight = mCurrentState.getImageHeightVariable().get().intValue();
    double lExposureTimeInSeconds = mCurrentState.getExposureInSecondsVariable().get().doubleValue();

    int lNumberOfImagesToTake = mCurrentState.getNumberOfZPlanesVariable().get().intValue();

    // build a queue
    LightSheetMicroscopeQueue lQueue = getLightSheetMicroscope().requestQueue();

    // initialize queue
    lQueue.clearQueue();
    lQueue.setCenteredROI(lImageWidth, lImageHeight);

    lQueue.setExp(lExposureTimeInSeconds);

    // initial position
    goToInitialPosition(getLightSheetMicroscope(), lQueue, mCurrentState.getStackZLowVariable().get().doubleValue(), mCurrentState.getStackZLowVariable().get().doubleValue());

    // --------------------------------------------------------------------
    // build a queue

    for (int lImageCounter = 0; lImageCounter < lNumberOfImagesToTake; lImageCounter++)
    {
      // acuqire an image with all light sheets on
      mCurrentState.applyAcquisitionStateAtStackPlane(lQueue, lImageCounter);

      for (int k = 0; k < getLightSheetMicroscope().getNumberOfLightSheets(); k++)
      {

        lQueue.setI(k, true);
      }
      lQueue.addCurrentStateToQueue();

    }

    // back to initial position
    goToInitialPosition(getLightSheetMicroscope(), lQueue, mCurrentState.getStackZLowVariable().get().doubleValue(), mCurrentState.getStackZLowVariable().get().doubleValue());

    lQueue.setTransitionTime(0.5);
    lQueue.setFinalisationTime(0.005);

    for (int c = 0; c < getLightSheetMicroscope().getNumberOfDetectionArms(); c++)
    {
      StackMetaData lMetaData = lQueue.getCameraDeviceQueue(c).getMetaDataVariable().get();

      lMetaData.addEntry(MetaDataAcquisitionType.AcquisitionType, AcquisitionType.TimeLapseOpticallyCameraFused);
      lMetaData.addEntry(MetaDataView.Camera, c);

      lMetaData.addEntry(MetaDataFusion.RequestFullFusion, true);

      lMetaData.addEntry(MetaDataChannel.Channel, "opticsprefused");
    }
    lQueue.addVoxelDimMetaData(getLightSheetMicroscope(), mCurrentState.getStackZStepVariable().get().doubleValue());
    lQueue.addMetaDataEntry(MetaDataOrdinals.TimePoint, pTimePoint);

    lQueue.finalizeQueue();

    // programStep!
    boolean lPlayQueueAndWait = false;
    try
    {
      mTimeStampBeforeImaging = System.nanoTime();
      lPlayQueueAndWait = getLightSheetMicroscope().playQueueAndWait(lQueue, 100 + lQueue.getQueueLength(), TimeUnit.SECONDS);
    } catch (InterruptedException e)
    {
      e.printStackTrace();
    } catch (ExecutionException e)
    {
      e.printStackTrace();
    } catch (TimeoutException e)
    {
      e.printStackTrace();
    }

    if (!lPlayQueueAndWait)
    {
      System.out.print("Error while imaging");
      return false;
    }

    // store resulting stacks in the DataWarehouse
    OpticsPrefusedImageDataContainer lContainer = new OpticsPrefusedImageDataContainer(getLightSheetMicroscope());
    for (int d = 0; d < getLightSheetMicroscope().getNumberOfDetectionArms(); d++)
    {
      StackInterface lStack = getLightSheetMicroscope().getCameraStackVariable(d).get();
      putStackInContainer("C" + d + "opticsprefused", lStack, lContainer);
    }
    getLightSheetMicroscope().getDataWarehouse().put("opticsprefused_raw_" + pTimePoint, lContainer);

    return true;
  }

  @Override
  public OpticsPrefusedAcquisitionInstruction copy()
  {
    return new OpticsPrefusedAcquisitionInstruction(getLightSheetMicroscope());
  }
}
