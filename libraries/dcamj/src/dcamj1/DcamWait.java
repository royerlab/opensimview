package dcamj1;

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

public class DcamWait extends DcamBase implements AutoCloseable
{
  private static final long cSizeOf_DCAMWAIT_START = BridJ.sizeOf(DCAMWAIT_START.class);
  private final DcamDevice mDcamDevice;
  private Pointer<HDCAMWAIT_struct> mHwaitPointer = null;
  private DCAMWAIT_START mDCAMWAIT_START;
  private Pointer<DCAMWAIT_START> mPointerToDCAMWAIT_START;

  public DcamWait(final DcamDevice pDcamDevice)
  {
    super();
    mDcamDevice = pDcamDevice;
    open();
  }

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
      mPointerToDCAMWAIT_START = pointerTo(mDCAMWAIT_START);
    }
  }

  public final boolean waitForEvent(final DCAMWAIT_EVENT pDCAMWAIT_EVENT, final long pTimeOut)
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

  public final long getEvent()
  {
    return mDCAMWAIT_START.eventhappened();
  }

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
