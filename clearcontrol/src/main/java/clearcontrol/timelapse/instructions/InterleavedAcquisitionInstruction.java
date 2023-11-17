package clearcontrol.timelapse.instructions;

import clearcontrol.LightSheetDOF;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.LightSheetMicroscopeQueue;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.processor.MetaDataFusion;
import clearcontrol.stack.metadata.*;
import clearcontrol.state.AcquisitionType;
import clearcontrol.state.InterpolatedAcquisitionState;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This instructions acquires an image stack per camera where every slice is imaged
 * several times for each light sheet. A stack might contain slices like:
 * <p>
 * C0L0Z0 C0L1Z0 C0L2Z0 C0L3Z0 C0L0Z1 C0L1Z1 C0L2Z1 C0L3Z1 ...
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) February
 * 2018
 */
public class InterleavedAcquisitionInstruction extends AbstractAcquistionInstruction implements InstructionInterface, LoggingFeature
{

  /**
   * INstanciates a virtual device with a given name
   */
  public InterleavedAcquisitionInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Acquisition: Interleaved", pLightSheetMicroscope);
  }

  @Override
  public boolean execute(long pTimePoint)
  {
    mCurrentState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState();

    int lImageWidth = mCurrentState.getImageWidthVariable().get().intValue();
    int lImageHeight = mCurrentState.getImageHeightVariable().get().intValue();
    double lExposureTimeInSeconds = mCurrentState.getExposureInSecondsVariable().get().doubleValue();

    int lNumberOfLaserLines = mCurrentState.getNumberOfLaserLines();
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
      mCurrentState.applyAcquisitionStateAtStackPlane(lQueue, lImageCounter);

      // acquire an image per light sheet + one more
      for (int l = 0; l < getLightSheetMicroscope().getNumberOfLightSheets(); l++)
        if (isLightSheetOn(l))
        {

          // configure light sheets accordingly
          for (int k = 0; k < getLightSheetMicroscope().getNumberOfLightSheets(); k++)
            lQueue.setI(k, k==l);

          // Lightsheet XY position:
          lQueue.setIX(l, mCurrentState.get(LightSheetDOF.IX, lImageCounter, l));
          lQueue.setIY(l, mCurrentState.get(LightSheetDOF.IY, lImageCounter, l));

          lQueue.addCurrentStateToQueue();
        }
    }

    // back to initial position
    goToInitialPosition(getLightSheetMicroscope(), lQueue, mCurrentState.getStackZLowVariable().get().doubleValue(), mCurrentState.getStackZLowVariable().get().doubleValue());

    lQueue.setTransitionTime(0.5);
    lQueue.setFinalisationTime(0.005);

    for (int c = 0; c < getLightSheetMicroscope().getNumberOfDetectionArms(); c++)
      if (isCameraOn(c))
      {
        StackMetaData lMetaData = lQueue.getCameraDeviceQueue(c).getMetaDataVariable().get();

        lMetaData.addEntry(MetaDataAcquisitionType.AcquisitionType, AcquisitionType.TimeLapseInterleaved);
        lMetaData.addEntry(MetaDataView.Camera, c);

        lMetaData.addEntry(MetaDataFusion.RequestFullFusion, true);

        lMetaData.addEntry(MetaDataChannel.Channel, "interleaved");
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

    return true;
  }

  @Override
  public InterleavedAcquisitionInstruction copy()
  {
    return new InterleavedAcquisitionInstruction(getLightSheetMicroscope());
  }
}
