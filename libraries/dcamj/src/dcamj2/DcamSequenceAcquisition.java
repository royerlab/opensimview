package dcamj2;

import dcamapi.DCAMCAP_TRANSFERINFO;
import dcamj2.imgseq.DcamImageSequence;
import dcamj2.utils.StopWatch;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.max;

/**
 * Dcam sequence acquisition
 *
 * @author royer
 */
public class DcamSequenceAcquisition extends DcamBase
{

  DcamDevice mDcamDevice;

  ExecutorService mSingleThreadExecutor = new ThreadPoolExecutor(1, 1, 1000L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1));

  ExecutorService mSingleThreadExecutorForCallable = new ThreadPoolExecutor(2, 4, 1000L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(4));

  private final ReentrantLock mLock = new ReentrantLock();

  private volatile int mTimeOutCount = 0;

  /**
   * Instantiates a Dcam sequence acquisition given a Dcam device
   *
   * @param pDcamDevice dcam device
   */
  public DcamSequenceAcquisition(DcamDevice pDcamDevice)
  {
    super();
    mDcamDevice = pDcamDevice;

  }

  /**
   * Acquires a sequence of images
   *
   * @param pExposure      exposure
   * @param pImageSequence image sequence to use
   * @return future (true: success)
   */
  public Boolean acquireSequence(double pExposure, DcamImageSequence pImageSequence, Callable<Boolean> pCallable)
  {
    try
    {
      return acquireSequenceAsync(pExposure, null, pImageSequence, pCallable).get();
    } catch (Throwable e)
    {
      throw new RuntimeException("Problem while acquiring image sequence", e);
    }

  }

  /**
   * Acquires a sequence of images
   *
   * @param pExposure      exposure
   * @param pImageSequence image sequence to use
   * @return future (true: success)
   */
  public Future<Boolean> acquireSequenceAsync(double pExposure, DcamImageSequence pImageSequence, Callable<Boolean> pCallable)
  {
    return acquireSequenceAsync(pExposure, null, pImageSequence, pCallable);
  }

  /**
   * Acquires a sequence of images
   *
   * @param pExposureInSeconds exposure
   * @param pTimeOutInSeconds  timeout in seconds, if null then the timeout is computed
   *                           automatically. This value cannot be lower than the default value
   *                           that is automatically computed.
   * @param pImageSequence     image sequence to use
   * @return future (true: success)
   */
  public Future<Boolean> acquireSequenceAsync(double pExposureInSeconds, Double pTimeOutInSeconds, DcamImageSequence pImageSequence, Callable<Boolean> pCallable)
  {
    try
    {
      if (!mLock.tryLock(5, TimeUnit.SECONDS))
      {
        System.out.println("WARNING: already locked!");
        return null;
      }

      //println("Status at start=" + mDcamDevice.getStatus());

      /*if (mDcamDevice.isBusy())
        mDcamDevice.stop();/**/

      if (pImageSequence == null)
      {
        throw new RuntimeException("Critical Error: Image Sequence is null!");
      }

      if (pImageSequence.getDepth() == 0)
      {
        println("WARNING: acquiring empty stack");
        return null;
      }

      System.out.println("setting ROI");
      if (mDcamDevice.getWidth() != pImageSequence.getWidth() * mDcamDevice.getBinning() || mDcamDevice.getHeight() != pImageSequence.getHeight() * mDcamDevice.getBinning())
        mDcamDevice.setCenteredROI(pImageSequence.getWidth() * mDcamDevice.getBinning(), pImageSequence.getHeight() * mDcamDevice.getBinning());

      if (mDcamDevice.getWidth() != pImageSequence.getWidth() * mDcamDevice.getBinning() || mDcamDevice.getHeight() != pImageSequence.getHeight() * mDcamDevice.getBinning())
      {
        System.out.println("WARNING: Can't set ROI!");
        return null;
      }

      System.out.format("set exposure %g seconds \n", pExposureInSeconds);
      mDcamDevice.setExposure(pExposureInSeconds);
      mDcamDevice.setDefectCorectionMode(true);

      //println("Status before attach buffers=" + mDcamDevice.getStatus());
      println("attach buffers");
      mDcamDevice.getBufferControl().attachExternalBuffers(pImageSequence);

      System.out.format("Status before start sequence= %s for device %s \n", mDcamDevice.getStatus(), mDcamDevice);

      // Prepare callable:
      Callable<Boolean> lCallable = () -> asyncSection(pExposureInSeconds, pTimeOutInSeconds, pImageSequence, pCallable);

      Future<Boolean> lAsyncSectionFuture = mSingleThreadExecutor.submit(lCallable);

      System.out.println("start sequence... ");
      mDcamDevice.startSequence();
      System.out.println("AFTER startSequence");

      if (mLock.isHeldByCurrentThread()) mLock.unlock();

      return lAsyncSectionFuture;

    } catch (Throwable e)
    {
      e.printStackTrace();
    }
    return null;
  }

  private boolean asyncSection(double pExposureInSeconds, Double pTimeOutInSeconds, DcamImageSequence pImageSequence, Callable<Boolean> pCallable)
  {
    try
    {
      int lCurrentPriority = Thread.currentThread().getPriority();
      Thread.currentThread().setPriority(Thread.MAX_PRIORITY-1);

      // timeout default value is at least one sec and 4x more than the
      // actual estimated acquisition time
      int lWaitTimeoutInMilliseconds = (int) (4 * 1000 * pImageSequence.getDepth() * pExposureInSeconds);
      lWaitTimeoutInMilliseconds = max(1000, lWaitTimeoutInMilliseconds);
      if (pTimeOutInSeconds != null && pTimeOutInSeconds * 1000 > lWaitTimeoutInMilliseconds)
      {
        lWaitTimeoutInMilliseconds = 1000 * pTimeOutInSeconds.intValue();
      }

      System.out.format("Status before waiting=%s for device %s \n", mDcamDevice.getStatus(), mDcamDevice);

      System.out.println("BEFORE LOCK");
      mLock.lock();
      System.out.println("AFTER LOCK");

      System.out.println("!!Waiting for: "+lWaitTimeoutInMilliseconds+" ms ...");
      boolean lWaitSuccess = mDcamDevice.getDcamWait().waitForEventStopped(lWaitTimeoutInMilliseconds);
      final long lAcquisitionTimeStampInNanoseconds = StopWatch.absoluteTimeInNanoseconds();
      System.out.format("    ...done! Last event: %s for device: %s \n", mDcamDevice.getDcamWait().getLastEvent(), mDcamDevice);

      //format("Status after waiting=%s for device %s. \n", mDcamDevice.getStatus(), mDcamDevice);
      Thread.currentThread().setPriority(lCurrentPriority);

      DCAMCAP_TRANSFERINFO lTransferinfo = mDcamDevice.getTransferInfo();

      long lFrameCount = lTransferinfo.nFrameCount();
      System.out.format("Success: %s with n=%d for device %s \n", lWaitSuccess, lFrameCount, mDcamDevice);

      final long lReceivedFrameIndexInBufferList = lTransferinfo.nNewestFrameIndex();
      System.out.format("DcamJ(Runnable): Wrote %d frames into external buffers (local frame index=%d) \n", lFrameCount, lReceivedFrameIndexInBufferList);/**/

      if (!lWaitSuccess)
      {
        mTimeOutCount +=1;
        System.err.println("DCAMJ2: TIMEOUT! (total number of occurrences: "+mTimeOutCount+" times!)");
        if (lFrameCount != pImageSequence.getDepth())
          System.err.format("DCAMJ2: WRONG NUMBER OF FRAMES: %d instead of %d \n", lFrameCount, pImageSequence.getDepth());
      }

      if (lFrameCount != pImageSequence.getDepth())
      {
        format("Wrong number of images acquired! should be %d but is %d \n", pImageSequence.getDepth(), lFrameCount);
        System.err.format("Wrong number of images acquired! should be %d but is %d \n", pImageSequence.getDepth(), lFrameCount);
      }

      final boolean lReceivedStopEvent = mDcamDevice.getDcamWait().isLastEventStopped();
      final boolean lReceivedFrameReadyEvent = mDcamDevice.getDcamWait().isLastEventReady();

      System.out.format("stop event: %s, ready event: %s, for device: %s\n", lReceivedStopEvent, lReceivedFrameReadyEvent, mDcamDevice);

      DcamImageSequence lDcamFrame = mDcamDevice.getBufferControl().getStackDcamFrame();
      lDcamFrame.setTimeStampInNs(lAcquisitionTimeStampInNanoseconds);

      System.out.format("Stopping acquisition \n");
      mDcamDevice.stop();

      System.out.format("Releasing buffers \n");
      mDcamDevice.getBufferControl().releaseBuffers();

      if (pCallable != null) mSingleThreadExecutorForCallable.submit(pCallable);

      return lWaitSuccess &&  lFrameCount == pImageSequence.getDepth();
    } catch (Throwable e)
    {
      e.printStackTrace();
    } finally
    {
      if (mLock.isHeldByCurrentThread()) mLock.unlock();
    }
    return false;
  }

  /**
   * Ensures that camera is opened with the correct image width and height
   * (centered ROI)
   *
   * @param pRequestedWidth  new requested width
   * @param pRequestedHeight new requested height
   */
  public void ensureOpenedWithCorrectWidthAndHeight(long pRequestedWidth, long pRequestedHeight)
  {
    long lCurrentWidth = mDcamDevice.getWidth();
    long lCurrentHeight = mDcamDevice.getHeight();
    boolean lCurrentExternalTriggering = mDcamDevice.isExternalTriggering();

    if (lCurrentWidth != pRequestedWidth || lCurrentHeight != pRequestedHeight)
    {

      if (mDebug) format("DcamJ: reopening device %d begin \n", mDcamDevice.getDeviceID());

      if (mDcamDevice.mBufferControl != null) mDcamDevice.mBufferControl.releaseBuffers();
      if (mDcamDevice != null) mDcamDevice.close();

      mDcamDevice = DcamLibrary.getDeviceForId(mDcamDevice.getDeviceID(), false, lCurrentExternalTriggering);
      mDcamDevice.setCenteredROI(pRequestedWidth, pRequestedHeight);

      if (mDebug) format("DcamJ: reopening device %d end \n", mDcamDevice.getDeviceID());
    }
  }

}
