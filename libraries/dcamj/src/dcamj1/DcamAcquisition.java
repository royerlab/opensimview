package dcamj1;

import dcamapi.DCAMCAP_TRANSFERINFO;
import dcamapi.DcamapiLibrary;
import dcamapi.DcamapiLibrary.DCAMERR;
import org.bridj.BridJ;
import org.bridj.IntValuedEnum;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static org.bridj.Pointer.pointerTo;

public class DcamAcquisition implements AutoCloseable
{

  private final int mDeviceIndex;
  private long mWidth = 2048;
  private long mHeight = 2048;
  private final int mNumberOfBuffersByDefault = 64;
  private double mExposureInSeconds = 0.001;

  public enum TriggerType
  {
    Internal, Software, ExternalEdge, ExternalFastEdge, ExternalLevel
  }

  ;

  private TriggerType mTriggerType = TriggerType.Internal;

  private final boolean mShowErrors = false;
  final boolean mDebug = false;

  DcamDevice mDcamDevice;
  private DcamBufferControl mBufferControl;

  private Thread mAcquisitionThread;
  private DcamAquisitionRunnable mDcamAquisitionRunnable;
  CountDownLatch mAcquisitionStartedSignal;
  CountDownLatch mAcquisitionFinishedSignal;

  public volatile long mAcquiredFrameIndex = 0;

  private final ArrayList<DcamAcquisitionListener> mListenersList = new ArrayList<DcamAcquisitionListener>();
  private DcamProperties mProperties;

  public DcamAcquisition(final int pDeviceIndex)
  {
    super();
    mDeviceIndex = pDeviceIndex;
  }

  public int getDeviceIndex()
  {
    return mDeviceIndex;
  }

  public double setExposureInSeconds(final double exposure)
  {
    if (mDebug) System.out.format("DcamJ: setExposureInSeconds(%g) \n", exposure);
    mExposureInSeconds = exposure;
    if (mProperties != null) mExposureInSeconds = mProperties.setAndGetExposure(mExposureInSeconds);
    return mExposureInSeconds;
  }

  public double getExposureInSeconds()
  {
    return mExposureInSeconds;
  }

  public long setFrameWidth(final long pWidth)
  {
    return mWidth = DcamProperties.roundto4(pWidth);
  }

  public long setFrameHeight(final long pHeight)
  {
    return mHeight = DcamProperties.roundto4(pHeight);
  }

  public void setFrameWidthAndHeight(final long pWidth, final long pHeight)
  {
    mWidth = DcamProperties.roundto4(pWidth);
    mHeight = DcamProperties.roundto4(pHeight);
  }

  public long getWidth()
  {
    return mWidth;
  }

  public long getHeight()
  {
    return mHeight;
  }

  public long getFrameBytesPerPixel()
  {
    return 2;
  }

  public long getFrameSizeInBytes()
  {
    return mWidth * mHeight * 2;
  }

  public void setTriggerType(final TriggerType pTriggerType)
  {
    mTriggerType = pTriggerType;
    if (mProperties != null)
    {
      if (mTriggerType == TriggerType.ExternalEdge)
      {
        mProperties.setInputTriggerToExternalEdge();
      }
      if (mTriggerType == TriggerType.ExternalFastEdge)
      {
        mProperties.setInputTriggerToExternalFastEdge();
      } else if (mTriggerType == TriggerType.ExternalLevel)
      {
        mProperties.setInputTriggerToExternalLevel();
      } else if (mTriggerType == TriggerType.Software)
      {
        mProperties.setInputTriggerToSoftware();
      } else if (mTriggerType == TriggerType.Internal)
      {
        mProperties.setInputTriggerToInternal();
      }
    }
  }

  public boolean isExternalTriggering()
  {
    return mTriggerType == TriggerType.ExternalEdge || mTriggerType == TriggerType.ExternalFastEdge || mTriggerType == TriggerType.ExternalLevel;
  }

  public boolean isSoftwareTriggering()
  {
    return mTriggerType == TriggerType.Software;
  }

  public void addListener(final DcamAcquisitionListener pDcamAcquisitionListener)
  {
    if (!mListenersList.contains(pDcamAcquisitionListener)) mListenersList.add(pDcamAcquisitionListener);
  }

  public boolean open()
  {
    System.out.println("DcamJ: Initializing the DCAM library:");
    DcamLibrary.initialize();

    final int lNumberOfDevices = DcamLibrary.getNumberOfDevices();
    System.out.format("DcamJ: Number of devices connected: %d \n", lNumberOfDevices);

    if (getDeviceIndex() >= lNumberOfDevices)
    {
      System.out.format("DcamJ: device not found for index %d \n", getDeviceIndex());
      return false;
    }

    mDcamDevice = DcamLibrary.getDeviceForId(getDeviceIndex());
    mDcamDevice.mShowErrors = mShowErrors;
    mDcamDevice.mDebug = mDebug;

    mDcamDevice.displayDeviceInfo();

    // lProperties.listAllProperties();

    setCurrentProperties();

    return true;
  }

  public void reopen()
  {
    if (mDebug) System.out.format("DcamJ: reopening device %d begin \n", getDeviceIndex());
    if (mBufferControl != null) mBufferControl.releaseBuffers();
    if (mDcamDevice != null) mDcamDevice.close();
    mDcamDevice = DcamLibrary.getDeviceForId(getDeviceIndex());
    setCurrentProperties();
    if (mDebug) System.out.format("DcamJ: reopening device %d end \n", getDeviceIndex());
  }

  private boolean provideExternalBuffers(final DcamFrame pDcamFrame)
  {
    if (mDebug)
      System.out.format("DcamJ: provide %d external buffers for a total buffer capacity of %d\n", pDcamFrame.getDepth(), pDcamFrame.getTotalSizeInBytesForAllPlanes());/**/
    mBufferControl = getBufferControl();
    mBufferControl.mShowErrors = true;
    mBufferControl.mDebug = false;
    return mBufferControl.attachExternalBuffers(pDcamFrame);
  }

  private boolean allocateInternalBuffers()
  {
    if (mDebug) System.out.format("DcamJ: allocate %d internal buffers \n", mNumberOfBuffersByDefault);
    mBufferControl = getBufferControl();
    mBufferControl.mShowErrors = true;
    mBufferControl.mDebug = false;
    return mBufferControl.allocateInternalBuffers(mNumberOfBuffersByDefault);
  }

  public DcamProperties getProperties()
  {
    return mDcamDevice.getProperties();
  }

  private void setCurrentProperties()
  {
    mProperties = mDcamDevice.getProperties();
    mProperties.mShowErrors = mShowErrors;
    mProperties.mDebug = mDebug;

    final double lExposureRequested = mExposureInSeconds;
    final double lEffectiveExposure = setExposureInSeconds(lExposureRequested);
    if (mDebug)
      System.out.format("DcamJ: exposure requested: %g, exposure set at: %g \n", lExposureRequested, lEffectiveExposure);

    if (mProperties != null) mProperties.setCenteredROI(mWidth, mHeight);
    if (mDebug) System.out.format("DcamJ: Centered ROI set cwidth=%d cheight=%d \n", mWidth, mHeight);

    setTriggerType(mTriggerType);
  }

  public final DcamBufferControl getBufferControl()
  {
    if (mBufferControl == null)
    {
      mBufferControl = new DcamBufferControl(mDcamDevice, this);
    }
    return mBufferControl;
  }

  public final boolean startAcquisition()
  {
    return startAcquisition(true, false, allocateDefaultDcamFrame(mNumberOfBuffersByDefault));
  }

  private DcamFrame allocateDefaultDcamFrame(final int pNumberOfFramesToCapture)
  {
    return DcamFrame.requestFrame(getFrameBytesPerPixel(), getWidth(), getHeight(), pNumberOfFramesToCapture);
  }

  public final boolean startAcquisition(final int pNumberOfFramesToCapture)
  {
    return startAcquisition(false, false, allocateDefaultDcamFrame(pNumberOfFramesToCapture));
  }

  public final boolean startAcquisition(final boolean pContinuousAcquisition, final boolean pStackAcquisition, final DcamFrame pDcamFrame)
  {
    return startAcquisition(pContinuousAcquisition, pStackAcquisition, true, !pContinuousAcquisition, pDcamFrame);
  }

  public final boolean startAcquisition(final boolean pContinuousAcquisition, final boolean pStackAcquisition, final boolean pWaitToStart, final boolean pWaitToFinish, final DcamFrame pDcamFrame)
  {
    if (mDebug) System.out.println("DcamJ: startAcquisition begin!");

    if (!checkDimensions(pDcamFrame)) return false;

    boolean mBufferAllocationSuccess = false;
    if (pDcamFrame != null) mBufferAllocationSuccess = provideExternalBuffers(pDcamFrame);

    if (!mBufferAllocationSuccess)
    {
      getBufferControl().displayErrorList();
      return false;
    }

    mAcquisitionStartedSignal = new CountDownLatch(1);
    mAcquisitionFinishedSignal = new CountDownLatch(1);
    mDcamAquisitionRunnable = new DcamAquisitionRunnable(this, pDcamFrame.getDepth(), pContinuousAcquisition, pStackAcquisition);
    mAcquisitionThread = new Thread(mDcamAquisitionRunnable);
    mAcquisitionThread.setName("DcamAcquisitionThread");
    mAcquisitionThread.setDaemon(true);
    mAcquisitionThread.setPriority(Thread.MAX_PRIORITY);
    mAcquisitionThread.start();

    if (pWaitToStart)
    {
      if (mDebug) System.out.format("DcamJ: wait to start acquisition active! \n");
      waitAcquisitionStarted();
    }

    if (pWaitToFinish)
    {
      if (mDebug) System.out.format("DcamJ: wait to stop acquisition active! \n");

      waitAcquisitionFinishedAndStop();
    }

    if (mDebug) System.out.println("DcamJ: startAcquisition end!");

    return !mDcamAquisitionRunnable.mTrueIfError;
  }

  private boolean checkDimensions(final DcamFrame pDcamFrame)
  {
    final boolean isEverythingFine = pDcamFrame.getWidth() == getWidth() && pDcamFrame.getHeight() == getHeight();
    return isEverythingFine;
  }

  void notifyListeners(final long pAbsoluteFrameIndex, final long pArrivalTimeStampInNanoseconds, final long pFrameIndexInBufferList, final DcamFrame pDcamFrame)
  {
    for (final DcamAcquisitionListener lDcamAcquisitionListener : mListenersList)
    {
      lDcamAcquisitionListener.frameArrived(this, pAbsoluteFrameIndex, pArrivalTimeStampInNanoseconds, pFrameIndexInBufferList, pDcamFrame);
    }

  }

  public final void stopAcquisition()
  {
    if (mDebug) System.out.println("DcamJ: stopAcquisition!");
    if (mDcamAquisitionRunnable == null) return;
    mDcamAquisitionRunnable.mStopIfFalse = false;
    mDcamAquisitionRunnable.mStopContinousIfFalse = false;
    waitAcquisitionFinishedAndStop();
  }

  public void waitAcquisitionStarted()
  {
    try
    {
      mAcquisitionStartedSignal.await();
    } catch (final InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  public void waitAcquisitionFinishedAndStop()
  {
    try
    {
      mAcquisitionFinishedSignal.await();
      mDcamDevice.stop();
    } catch (final InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  public final boolean isAcquiring()
  {
    if (mDcamAquisitionRunnable == null) return false;
    return mDcamAquisitionRunnable.mTrueIfStarted && !mDcamAquisitionRunnable.mTrueIfStopped;
  }

  @Override
  public final void close()
  {
    if (mDebug) System.out.println("mBufferControl.releaseBuffers();");
    try
    {
      Thread.sleep(100);
      if (mBufferControl != null) mBufferControl.releaseBuffers();
      Thread.sleep(100);
      if (mDebug) System.out.println("mDcamDevice.close();");
      if (mDcamDevice != null) mDcamDevice.close();
      Thread.sleep(100);
      if (mDebug) System.out.println("DcamLibrary.uninitialize();");
      DcamLibrary.uninitialize();
      Thread.sleep(100);
    } catch (final InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  DCAMCAP_TRANSFERINFO mDcamCapTransfertInfo;

  DCAMCAP_TRANSFERINFO getTransferinfo()
  {
    mDcamCapTransfertInfo = new DCAMCAP_TRANSFERINFO();
    mDcamCapTransfertInfo.size(BridJ.sizeOf(DCAMCAP_TRANSFERINFO.class));
    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamcapTransferinfo(mDcamDevice.getHDCAMPointer(), pointerTo(mDcamCapTransfertInfo));

    return mDcamCapTransfertInfo;
  }

  public void setDefectCorrection(boolean pDefectCorrection)
  {
    getProperties().setDefectCorectionMode(pDefectCorrection);
  }

  public void trigger()
  {
    DcamapiLibrary.dcamcapFiretrigger(mDcamDevice.getHDCAMPointer(), 0);
  }

}
