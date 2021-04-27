package dcamj1;

import dcamapi.DCAMCAP_TRANSFERINFO;
import dcamapi.DcamapiLibrary.DCAMWAIT_EVENT;
import dcamj1.utils.StopWatch;

class DcamAquisitionRunnable implements Runnable
{

  /**
   * 
   */
  private final DcamAcquisition mDcamAcquisition;
  volatile boolean mTrueIfStarted = false;
  volatile boolean mStopContinousIfFalse = true;
  volatile boolean mStopIfFalse = true;
  volatile boolean mTrueIfStopped = false;
  volatile boolean mTrueIfError = false;

  private final long mNumberOfFramesToCapture;
  private final boolean mContinuousAcquisition;
  private final boolean mStackAcquisition;

  public DcamAquisitionRunnable(DcamAcquisition pDcamAcquisition,
                                final long pNumberOfFramesToCapture,
                                final boolean pContinuousAcquisition,
                                final boolean pStackAcquisition)
  {
    mDcamAcquisition = pDcamAcquisition;
    mNumberOfFramesToCapture = pNumberOfFramesToCapture;
    mContinuousAcquisition = pContinuousAcquisition;
    mStackAcquisition = pStackAcquisition;
  }

  @SuppressWarnings("unused")
  @Override
  public void run()
  {

    try
    {
      if (mDcamAcquisition.mDebug)
        System.out.println("DcamJ(Runnable): Starting acquisition:");

      mTrueIfStarted = true;
      mDcamAcquisition.mAcquiredFrameIndex = 0;

      if (mStackAcquisition && mContinuousAcquisition)
        while (mStopContinousIfFalse)
        {
          runOnce();
        }
      else
      {
        runOnce();
      }

    }
    catch (final Throwable e)
    {
      e.printStackTrace();
      mTrueIfError = true;
    }
    finally
    {
      if (!mStackAcquisition && mContinuousAcquisition)
        mDcamAcquisition.mDcamDevice.stop();
      mTrueIfStopped = true;
      if (mDcamAcquisition.mDebug)
        System.out.println("DcamJ(Runnable): stopping acquisition:");
      mDcamAcquisition.mAcquisitionFinishedSignal.countDown();
    }

  }

  private void runOnce()
  {
    mStopIfFalse = true;

    if (mDcamAcquisition.mDebug)
      System.out.println("DcamJ(Runnable): mDcamDevice.getStatus()="
                         + mDcamAcquisition.mDcamDevice.getStatus());

    if (mDcamAcquisition.mDebug)
      System.out.println(mDcamAcquisition.mDcamDevice.getStatus()
                         + " -> "
                         + mDcamAcquisition.mDcamDevice.getStatus()
                                                       .value());
    while (mDcamAcquisition.mDcamDevice.getStatus().value() != 2)
    {
      try
      {
        Thread.sleep(1);
      }
      catch (InterruptedException e)
      {
      }
    } /**/

    if (mContinuousAcquisition && !mStackAcquisition)
    {
      if (mDcamAcquisition.mDebug)
        System.out.format("DcamJ(Runnable): Starting continuous acquisition \n");
      mDcamAcquisition.mDcamDevice.startContinuous();
    }
    else
    {
      if (mDcamAcquisition.mDebug)
        System.out.format("DcamJ(Runnable): Starting acquisition sequence of %d frames \n",
                          mNumberOfFramesToCapture);/**/
      mDcamAcquisition.mDcamDevice.startSequence();
    }

    if (mDcamAcquisition.mDebug)
      System.out.println("DcamJ(Runnable): mDcamDevice.getStatus()="
                         + mDcamAcquisition.mDcamDevice.getStatus());

    /*if (mStackAcquisition)
    	lWaitTimeout = 3000; // + (int) (10 * 1000 * mNumberOfFramesToCapture *
    												// mExposureInSeconds)
    /*else
    {
    	if (mDcamAcquisition.isExternalTriggering())
    		lWaitTimeout = 5000;
    	else
    		lWaitTimeout = 3000;
    }/**/

    // if (mDcamAcquisition.isExternalTriggering())
    // sleep(1000);

    mDcamAcquisition.mAcquisitionStartedSignal.countDown();

    final long lNumberOfBuffers =
                                mDcamAcquisition.getBufferControl()
                                                .getNumberOfSinglePlaneBuffers();

    DCAMCAP_TRANSFERINFO lTransferinfo =
                                       mDcamAcquisition.getTransferinfo();
    mDcamAcquisition.mAcquiredFrameIndex =
                                         lTransferinfo.nFrameCount();

    while (mStopIfFalse)
    {

      // DCAMCAP_EVENT_FRAMEREADYORSTOPPED(2|16),
      final DCAMWAIT_EVENT lDcamcapEventToWaitFor;

      if (mContinuousAcquisition && !mStackAcquisition)
        lDcamcapEventToWaitFor =
                               DCAMWAIT_EVENT.DCAMCAP_EVENT_FRAMEREADYORSTOPPED;
      else if (mStackAcquisition)
        lDcamcapEventToWaitFor =
                               DCAMWAIT_EVENT.DCAMCAP_EVENT_FRAMEREADYORSTOPPED;
      else
        lDcamcapEventToWaitFor =
                               DCAMWAIT_EVENT.DCAMCAP_EVENT_FRAMEREADY;

      if (mDcamAcquisition.mDebug)
        System.out.print("DcamJ(Runnable): waitForEvent: before... ");

      int lWaitTimeout = 5;
      boolean lWaitSuccess = false;
      while (!lWaitSuccess && mStopIfFalse)
      {
        lWaitSuccess =
                     (mDcamAcquisition.mDcamDevice.getDcamWait()
                                                  .waitForEvent(lDcamcapEventToWaitFor,
                                                                lWaitTimeout));

      }
      if (mDcamAcquisition.mDebug)
        System.out.println("DcamJ(Runnable): ...after.");
      final long lAcquisitionTimeStampInNanoseconds =
                                                    StopWatch.absoluteTimeInNanoseconds();
      // System.out.println(System.nanoTime());

      lTransferinfo = mDcamAcquisition.getTransferinfo();

      final long lNumberOfFramesWrittenByDrivertoBuffers =
                                                         lTransferinfo.nFrameCount();
      final long lDriversFrameIndex =
                                    lNumberOfFramesWrittenByDrivertoBuffers
                                      - 1;
      final long lReceivedFrameIndexInBufferList =
                                                 lTransferinfo.nNewestFrameIndex();

      if (mDcamAcquisition.mDebug)
      {
        System.out.println("DcamJ(Runnable): lDriversFrameIndex="
                           + lDriversFrameIndex);
        System.out.println("DcamJ(Runnable): lReceivedFrameIndexInBufferList="
                           + lReceivedFrameIndexInBufferList);
      }

      if (!lWaitSuccess)
      {
        if (!mDcamAcquisition.isExternalTriggering()
            && !mDcamAcquisition.isSoftwareTriggering())
        {
          System.err.println("DcamJ(Runnable): waiting for event failed!!!!");
          System.err.format("DcamJ(Runnable): frame index = %d (local index = %d) out of %d frames to capture (%s acquisition)  \n",
                            mDcamAcquisition.mAcquiredFrameIndex,
                            lReceivedFrameIndexInBufferList,
                            mNumberOfFramesToCapture,
                            mStackAcquisition ? "stack"
                                              : "single plane");
          System.err.println("DcamJ(Runnable): timeout waiting for frame!");
          break;
        }
        continue;
      }

      final long lDcamWaitEvent =
                                mDcamAcquisition.mDcamDevice.getDcamWait()
                                                            .getEvent();
      final boolean lReceivedStopEvent =
                                       lDcamWaitEvent == DCAMWAIT_EVENT.DCAMCAP_EVENT_STOPPED.value;
      final boolean lReceivedFrameReadyEvent =
                                             lDcamWaitEvent == DCAMWAIT_EVENT.DCAMCAP_EVENT_FRAMEREADY.value;

      DcamFrame lDcamFrame = null;

      if (mStackAcquisition && lReceivedStopEvent)
      {
        if (mDcamAcquisition.mDebug)
          System.out.println("DcamJ(Runnable): Received Stop Event");
        if (mStackAcquisition)
        {
          lDcamFrame = mDcamAcquisition.getBufferControl()
                                       .getStackDcamFrame();
          lDcamFrame.setIndex(mDcamAcquisition.mAcquiredFrameIndex);
          lDcamFrame.setTimeStampInNs(lAcquisitionTimeStampInNanoseconds);
          mDcamAcquisition.notifyListeners(mDcamAcquisition.mAcquiredFrameIndex,
                                           lAcquisitionTimeStampInNanoseconds,
                                           0,
                                           lDcamFrame);
          mDcamAcquisition.mAcquiredFrameIndex++;
          mStopIfFalse = false;
        }
      }

      if (!mStackAcquisition && lReceivedFrameReadyEvent)
      {
        long lFirstFrameNotYetAcquired =
                                       mDcamAcquisition.mAcquiredFrameIndex;
        long lNumberOfFramesToAcquire = lDriversFrameIndex
                                        - lFirstFrameNotYetAcquired;
        long lRingBufferFrameIndex = (lReceivedFrameIndexInBufferList
                                      - lNumberOfFramesToAcquire);
        while (lRingBufferFrameIndex < 0)
          lRingBufferFrameIndex += lNumberOfBuffers;
        lRingBufferFrameIndex %= lNumberOfBuffers;
        for (long lFrameIndex =
                              lFirstFrameNotYetAcquired; lFrameIndex <= lDriversFrameIndex; lFrameIndex++)
        {
          lDcamFrame =
                     mDcamAcquisition.getBufferControl()
                                     .getDcamFrameForIndex(lRingBufferFrameIndex);
          lDcamFrame.setIndex(mDcamAcquisition.mAcquiredFrameIndex);
          lDcamFrame.setTimeStampInNs(lAcquisitionTimeStampInNanoseconds);
          mDcamAcquisition.notifyListeners(lFrameIndex,
                                           lAcquisitionTimeStampInNanoseconds,
                                           lRingBufferFrameIndex,
                                           lDcamFrame);
          mDcamAcquisition.mAcquiredFrameIndex++;
          lRingBufferFrameIndex = (lRingBufferFrameIndex + 1)
                                  % lNumberOfBuffers;
        }
      }

      if (lDcamFrame != null)
      {
        if (mDcamAcquisition.mDebug)
          System.out.format("DcamJ(Runnable): true frame index = %d, acquired frame index = %d (local index = %d) \n",
                            lDriversFrameIndex,
                            mDcamAcquisition.mAcquiredFrameIndex,
                            lReceivedFrameIndexInBufferList);

        lDcamFrame.setIndex(lDriversFrameIndex);
        lDcamFrame.setTimeStampInNs(lAcquisitionTimeStampInNanoseconds);
      }

      if (lReceivedFrameReadyEvent)
      {
        if (mDcamAcquisition.mDebug)
          System.out.println("DcamJ(Runnable): Received frame ready Event");

        if (!mContinuousAcquisition && !mStackAcquisition
            && lReceivedFrameIndexInBufferList >= mNumberOfFramesToCapture
                                                  - 1)
        {
          mStopIfFalse = false;
        }

      }

    }

    // if (!mContinuousAcquisition)
    {
      // System.out.println("getTransferinfo.before");
      lTransferinfo = mDcamAcquisition.getTransferinfo();
      // System.out.println("getTransferinfo.after");

      final int lNumberOfFramesWrittenByDrivertoBuffers =
                                                        (int) lTransferinfo.nFrameCount();

      final long lReceivedFrameIndexInBufferList =
                                                 lTransferinfo.nNewestFrameIndex();

      if (mDcamAcquisition.mDebug)
      {
        System.out.println("DcamJ(Runnable):lNumberOfFramesWrittenByDrivertoBuffers="
                           + lNumberOfFramesWrittenByDrivertoBuffers);
        System.out.println("DcamJ(Runnable):lReceivedFrameIndexInBufferList="
                           + lReceivedFrameIndexInBufferList);
        System.out.format("DcamJ(Runnable): Wrote %d frames into external buffers (local frame index=%d) \n",
                          lNumberOfFramesWrittenByDrivertoBuffers,
                          lReceivedFrameIndexInBufferList);/**/
      }

      final boolean lWrongNumberofFramesAcquired =
                                                 lNumberOfFramesWrittenByDrivertoBuffers != mNumberOfFramesToCapture;
      if (!mContinuousAcquisition && lWrongNumberofFramesAcquired)
      {
        System.err.format("DcamJ(Runnable): Wrong number of frames acquired!\n");
        mTrueIfError = true;
      }

    }
  }

  private void sleep(int pMilliseconds)
  {
    try
    {
      Thread.sleep(pMilliseconds);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

}