package clearcontrol.timelapse.instructions;

import clearcontrol.LightSheetMicroscope;
import clearcontrol.LightSheetMicroscopeQueue;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.processor.MetaDataFusion;
import clearcontrol.stack.metadata.*;
import clearcontrol.state.AcquisitionType;
import clearcontrol.state.InterpolatedAcquisitionState;
import clearcontrol.state.LightSheetAcquisitionStateInterface;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This instructions acquires an image stack per camera per light sheet. The image stacks
 * are stored in the DataWarehouse in an SequentialImageDataContainer with keys like:
 * <p>
 * C0L0 C1L0 C0L1 C1L1
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) February
 * 2018
 */
public class SequentialAcquisitionInstruction extends AbstractAcquistionInstruction implements InstructionInterface, LoggingFeature
{

  /**
   * INstanciates a virtual device with a given name
   */
  public SequentialAcquisitionInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Acquisition: Sequential", pLightSheetMicroscope);
    mChannelName.set("sequential");
  }

  public SequentialAcquisitionInstruction(String pName, LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pName, pLightSheetMicroscope);
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    mCurrentState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState();

    int lNumberOfDetectionArms = getLightSheetMicroscope().getNumberOfDetectionArms();

    int lNumberOfLightSheets = getLightSheetMicroscope().getNumberOfLightSheets();

    HashMap<Integer, LightSheetMicroscopeQueue> lViewToQueueMap = new HashMap<>();

    // preparing queues:
    for (int l = 0; l < lNumberOfLightSheets; l++)
      if (isLightSheetOn(l))
      {
        LightSheetMicroscopeQueue lQueueForView = getQueueForSingleLightSheet(mCurrentState, l);

        lViewToQueueMap.put(l, lQueueForView);
      }

    // playing the queues in sequence:

    for (int l = 0; l < lNumberOfLightSheets; l++)
    {
      if (isLightSheetOn(l))
      {
        LightSheetMicroscopeQueue lQueueForView = lViewToQueueMap.get(l);

        for (int c = 0; c < lNumberOfDetectionArms; c++)
        {
          if (isCameraOn(c))
          {

            StackMetaData lMetaData = lQueueForView.getCameraDeviceQueue(c).getMetaDataVariable().get();

            lMetaData.addEntry(MetaDataAcquisitionType.AcquisitionType, AcquisitionType.TimelapseSequential);
            lMetaData.addEntry(MetaDataView.Camera, c);
            lMetaData.addEntry(MetaDataView.LightSheet, l);

            String lCxLyString = MetaDataView.getCxLyString(lMetaData);
            lMetaData.addEntry(MetaDataChannel.Channel, lCxLyString);

          }
        }

        mTimeStampBeforeImaging = System.nanoTime();

        try
        {
          getLightSheetMicroscope().playQueueAndWait(lQueueForView, mTimelapse.getTimeOut(), TimeUnit.SECONDS);
        } catch (InterruptedException e)
        {
          e.printStackTrace();
          return false;
        } catch (ExecutionException e)
        {
          e.printStackTrace();
          return false;
        } catch (TimeoutException e)
        {
          e.printStackTrace();
          return false;
        }

//        getLightSheetMicroscope().playQueue(lQueueForView);

      }
    }


    return true;
  }

  protected LightSheetMicroscopeQueue getQueueForSingleLightSheet(LightSheetAcquisitionStateInterface<?> pCurrentState, int pLightSheetIndex)
  {
    int lNumberOfDetectionArms = getLightSheetMicroscope().getNumberOfDetectionArms();

    @SuppressWarnings("unused") int lNumberOfLightSheets = getLightSheetMicroscope().getNumberOfLightSheets();

    int lNumberOfImagesToTake = mCurrentState.getNumberOfZPlanesVariable().get().intValue();

    LightSheetMicroscopeQueue lQueue = getLightSheetMicroscope().requestQueue();
    lQueue.clearQueue();

    int lImageWidth = mCurrentState.getImageWidthVariable().get().intValue();
    int lImageHeight = mCurrentState.getImageHeightVariable().get().intValue();
    double lExposureTimeInSeconds = mCurrentState.getExposureInSecondsVariable().get().doubleValue();

    lQueue.setCenteredROI(lImageWidth, lImageHeight);
    lQueue.setExp(lExposureTimeInSeconds);

    info("acquiring stack from " + mCurrentState);

    // initial position
    goToInitialPosition(getLightSheetMicroscope(), lQueue, mCurrentState.getStackZLowVariable().get().doubleValue(), mCurrentState.getStackZLowVariable().get().doubleValue());

    for (int lImageCounter = 0; lImageCounter < lNumberOfImagesToTake; lImageCounter++)
    {
      mCurrentState.applyAcquisitionStateAtStackPlane(lQueue, lImageCounter);
      for (int k = 0; k < getLightSheetMicroscope().getNumberOfLightSheets(); k++)
      {

        lQueue.setI(k, pLightSheetIndex == k);
        // lQueue.setIH(k,mLightSheetMicroscope.getLightSheet(k).getHeightVariable().get().doubleValue());
      }

      lQueue.addCurrentStateToQueue();
    }
    /*
        pCurrentState.getQueue(0,
                               lNumberOfDetectionArms,
                               pLightSheetIndex,
                               pLightSheetIndex + 1,
                               0,
                               lNumberOfLaserLines,
                               lNumberOfEDFSlices);
    */

    // initial position
    goToInitialPosition(getLightSheetMicroscope(), lQueue, mCurrentState.getStackZLowVariable().get().doubleValue(), mCurrentState.getStackZLowVariable().get().doubleValue());

    /*
    for (int l = 0; l < mLightSheetMicroscope.getNumberOfLightSheets(); l++)
    {
      info("Light sheet " + l + " W: " + lQueue.getIW(l));
    }
    for (int l = 0; l < mLightSheetMicroscope.getNumberOfLightSheets(); l++)
    {
      info("Light sheet " + l + " H: " + lQueue.getIH(l));
    }
    */
    lQueue.addMetaDataEntry(MetaDataOrdinals.TimePoint, mTimelapse.getTimePointCounterVariable().get());

    lQueue.setTransitionTime(0.5);
    lQueue.setFinalisationTime(0.005);
    lQueue.finalizeQueue();

    return lQueue;
  }

  protected boolean isLightSheetOn(int pLightIndex)
  {
    return mCurrentState.getLightSheetOnOffVariable(pLightIndex).get();
  }

  @Override
  public SequentialAcquisitionInstruction copy()
  {
    return new SequentialAcquisitionInstruction(getLightSheetMicroscope());
  }
}
