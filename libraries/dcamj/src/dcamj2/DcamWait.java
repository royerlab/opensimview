package dcamj2;

import dcamapi.DCAMWAIT_OPEN;
import dcamapi.DCAMWAIT_START;
import dcamapi.DcamapiLibrary;
import dcamapi.DcamapiLibrary.DCAMERR;
import dcamapi.DcamapiLibrary.DCAMWAIT_EVENT;
import dcamapi.HDCAMWAIT_struct;
import org.bridj.BridJ;
import org.bridj.IntValuedEnum;
import org.bridj.Pointer;

import static org.bridj.Pointer.pointerTo;

/**
 * Dcam wait object
 *
 * @author royer
 */
public class DcamWait extends DcamBase implements AutoCloseable
{
  private static final long cSizeOf_DCAMWAIT_START = BridJ.sizeOf(DCAMWAIT_START.class);
  private final DcamDevice mDcamDevice;
  private Pointer<HDCAMWAIT_struct> mHwaitPointer = null;
  private DCAMWAIT_START mDCAMWAIT_START;
  private Pointer<DCAMWAIT_START> mPointerToDCAMWAIT_START;

  /**
   * Instantiates a Dcam Wait object
   *
   * @param pDcamDevice Dcam device
   */
  DcamWait(final DcamDevice pDcamDevice)
  {
    super();
    mDcamDevice = pDcamDevice;
    open();
  }

  @SuppressWarnings("deprecation")
  private void open()
  {
    final DCAMWAIT_OPEN lDCAMWAIT_OPEN = new DCAMWAIT_OPEN();
    lDCAMWAIT_OPEN.size(BridJ.sizeOf(DCAMWAIT_OPEN.class));
    lDCAMWAIT_OPEN.hdcam(mDcamDevice.getHDCAMPointer());
    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamwaitOpen(pointerTo(lDCAMWAIT_OPEN));

    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);

    if (lSuccess)
    {
      mHwaitPointer = lDCAMWAIT_OPEN.hwait();
      mDCAMWAIT_START = new DCAMWAIT_START();
      //
      mPointerToDCAMWAIT_START = pointerTo(mDCAMWAIT_START);
    }
  }

  /**
   * Waits for a 'stopped' event
   *
   * @param pTimeOut time out in milliseconds
   * @return true: success, false otherwise
   */
  final boolean waitForEventStopped(final long pTimeOut)
  {
    return waitForEvent(DCAMWAIT_EVENT.DCAMCAP_EVENT_STOPPED, pTimeOut);
  }

  /**
   * Waits for a 'ready' event
   *
   * @param pTimeOut timeout
   * @return true: success, false otherwise
   */
  final boolean waitForEventReady(final long pTimeOut)
  {
    return waitForEvent(DCAMWAIT_EVENT.DCAMCAP_EVENT_FRAMEREADY, pTimeOut);
  }

  /**
   * Waits for a 'ready or stopped' event
   *
   * @param pTimeOut timeout
   * @return true: success, false otherwise
   */
  final boolean waitForEventReadyOrStopped(final long pTimeOut)
  {
    return waitForEvent(DCAMWAIT_EVENT.DCAMCAP_EVENT_FRAMEREADYORSTOPPED, pTimeOut);
  }

  /**
   * Waits for a given event
   *
   * @param pDCAMWAIT_EVENT event
   * @param pTimeOut        time out
   * @return
   */
  boolean waitForEvent(final DCAMWAIT_EVENT pDCAMWAIT_EVENT, final long pTimeOut)
  {
    if (mHwaitPointer == null)
    {
      return false;
    }

    mDCAMWAIT_START.size(cSizeOf_DCAMWAIT_START);
    mDCAMWAIT_START.eventmask(pDCAMWAIT_EVENT.value);
    mDCAMWAIT_START.timeout(pTimeOut);

    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamwaitStart(mHwaitPointer, mPointerToDCAMWAIT_START);
    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);
    return lSuccess;
  }

  final long getLastEvent()
  {
    return mDCAMWAIT_START.eventhappened();
  }

  /**
   * Returns true if the last event received is a 'stopped' event
   *
   * @return true if 'stopped' event
   */
  public final boolean isLastEventStopped()
  {
    return isEventBit(DCAMWAIT_EVENT.DCAMCAP_EVENT_STOPPED.value);
  }

  /**
   * Returns true if the last event received is a 'ready' event
   *
   * @return true if 'ready' event
   */
  public final boolean isLastEventReady()
  {
    return isEventBit(DCAMWAIT_EVENT.DCAMCAP_EVENT_FRAMEREADY.value);
  }

  /**
   * Returns true if the last event received is a 'ready or stopped' event
   *
   * @return true if 'ready' event
   */
  public final boolean isLastEventReadyOrStopped()
  {
    return isEventBit(DCAMWAIT_EVENT.DCAMCAP_EVENT_FRAMEREADYORSTOPPED.value);
  }

  private boolean isEventBit(long pValue)
  {
    return (mDCAMWAIT_START.eventhappened() & pValue) == pValue;
  }

  /**
   * Aborts wait.
   *
   * @return true: success, false otherwise
   */
  public final boolean abort()
  {
    if (mHwaitPointer == null)
    {
      return false;
    }

    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamwaitAbort(mHwaitPointer);
    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);
    return lSuccess;
  }

  @Override
  public final void close()
  {
    if (mHwaitPointer == null)
    {
      return;
    }

    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamwaitClose(mHwaitPointer);
    addErrorToListAndCheckHasSucceeded(lError);
    return;
  }

}
