package dcamj1;

import static org.bridj.Pointer.pointerTo;

import dcamapi.DCAMBUF_ATTACH;
import dcamapi.DCAM_FRAME;
import dcamapi.DcamapiLibrary;
import dcamapi.DcamapiLibrary.DCAMERR;
import dcamapi.DcamapiLibrary.DCAMIDPROP;

import org.bridj.BridJ;
import org.bridj.IntValuedEnum;
import org.bridj.Pointer;

public class DcamBufferControl extends DcamBase
{

  private final DcamDevice mDcamDevice;
  private DcamAcquisition mDcamAcquisition;

  private DcamFrame mAttachedDcamFrame;
  private DCAMBUF_ATTACH mDCAMBUF_ATTACH;
  private Pointer<Pointer<?>> mPointerToPointerArray;
  private Pointer<DCAM_FRAME> mInternalDcamFramePointer;

  public DcamBufferControl(final DcamDevice pDcamDevice,
                           final DcamAcquisition pDcamAcquisition)
  {
    mDcamDevice = pDcamDevice;
    mDcamAcquisition = pDcamAcquisition;
  }

  public final boolean allocateInternalBuffers(final int pNumberOfBuffers)
  {
    if (pNumberOfBuffers < 1)
    {
      return false;
    }

    final IntValuedEnum<DCAMERR> lError =
                                        DcamapiLibrary.dcambufAlloc(mDcamDevice.getHDCAMPointer(),
                                                                    pNumberOfBuffers);
    final boolean lSuccess =
                           addErrorToListAndCheckHasSucceeded(lError);
    return lSuccess;
  }

  public final Pointer<DCAM_FRAME> lockFrame()
  {
    if (mInternalDcamFramePointer == null)
      mInternalDcamFramePointer = Pointer.allocate(DCAM_FRAME.class);

    final IntValuedEnum<DCAMERR> lError =
                                        DcamapiLibrary.dcambufLockframe(mDcamDevice.getHDCAMPointer(),
                                                                        mInternalDcamFramePointer);
    final boolean lSuccess =
                           addErrorToListAndCheckHasSucceeded(lError);
    if (!lSuccess)
    {
      return null;
    }

    return mInternalDcamFramePointer;
  }

  public final Pointer<DCAM_FRAME> copyFrame()
  {
    if (mInternalDcamFramePointer == null)
      mInternalDcamFramePointer = Pointer.allocate(DCAM_FRAME.class);

    final IntValuedEnum<DCAMERR> lError =
                                        DcamapiLibrary.dcambufCopyframe(mDcamDevice.getHDCAMPointer(),
                                                                        mInternalDcamFramePointer);
    final boolean lSuccess =
                           addErrorToListAndCheckHasSucceeded(lError);
    if (!lSuccess)
    {
      return null;
    }
    return mInternalDcamFramePointer;
  }

  /**/

  public final boolean attachExternalBuffers(DcamFrame pDcamFrame)
  {
    if (mAttachedDcamFrame == pDcamFrame)
      return true;

    mAttachedDcamFrame = pDcamFrame;

    final long lNumberOfBuffers = pDcamFrame.getDepth();

    if (mPointerToPointerArray == null
        || mPointerToPointerArray.getValidElements() != lNumberOfBuffers)
      mPointerToPointerArray =
                             Pointer.allocatePointers((int) lNumberOfBuffers);

    for (int i = 0; i < lNumberOfBuffers; i++)
    {
      @SuppressWarnings("unchecked")
      Pointer<Byte> lPointerToIndividualBuffer =
                                               pDcamFrame.getPointerForSinglePlane(i);
      mPointerToPointerArray.set(i, lPointerToIndividualBuffer);
    }

    final boolean lSuccess = releaseBuffers()
                             && attachBuffersInternal(lNumberOfBuffers);
    return lSuccess;
  }

  private boolean attachBuffersInternal(final long lNumberOfBuffers)
  {
    if (mDCAMBUF_ATTACH == null)
    {
      mDCAMBUF_ATTACH = new DCAMBUF_ATTACH();
      mDCAMBUF_ATTACH.size(BridJ.sizeOf(DCAMBUF_ATTACH.class));
    }
    mDCAMBUF_ATTACH.buffercount(lNumberOfBuffers);
    mDCAMBUF_ATTACH.buffer(mPointerToPointerArray);

    final IntValuedEnum<DCAMERR> lError =
                                        DcamapiLibrary.dcambufAttach(mDcamDevice.getHDCAMPointer(),
                                                                     pointerTo(mDCAMBUF_ATTACH));
    final boolean lSuccess =
                           addErrorToListAndCheckHasSucceeded(lError);
    return lSuccess;
  }

  public long computeTotalRequiredMemoryInBytes(int pNumberOfBuffers)
  {
    final long lImageSizeInBytes =
                                 (long) mDcamDevice.getProperties()
                                                   .getPropertyValue(DCAMIDPROP.DCAM_IDPROP_BUFFER_FRAMEBYTES);

    final long lTotalRequiredmemoryInBytes = pNumberOfBuffers
                                             * lImageSizeInBytes;

    return lTotalRequiredmemoryInBytes;
  }

  public DcamFrame getDcamFrameForIndex(long pFrameIndex)
  {
    return mAttachedDcamFrame.getSinglePlaneDcamFrame(pFrameIndex);
  }

  public long getNumberOfSinglePlaneBuffers()
  {
    return mAttachedDcamFrame.getDepth();
  }

  public final boolean releaseBuffers()
  {
    final IntValuedEnum<DCAMERR> lError =
                                        DcamapiLibrary.dcambufRelease(mDcamDevice.getHDCAMPointer(),
                                                                      0);
    final boolean lSuccess =
                           addErrorToListAndCheckHasSucceeded(lError);

    return lSuccess;
  }

  public DcamFrame getStackDcamFrame()
  {
    return mAttachedDcamFrame;
  }

  // DCAMERR DCAMAPI dcambuf_alloc ( HDCAM h, long framecount ); // call
  // dcambuf_release() to free.
  // DCAMERR DCAMAPI dcambuf_attach ( HDCAM h, const DCAMBUF_ATTACH* param );
  // DCAMERR DCAMAPI dcambuf_release ( HDCAM h, long iKind DCAM_DEFAULT_ARG );
  // DCAMERR DCAMAPI dcambuf_lockframe ( HDCAM h, DCAM_FRAME* pFrame );
  // DCAMERR DCAMAPI dcambuf_copyframe ( HDCAM h, DCAM_FRAME* pFrame );
  // DCAMERR DCAMAPI dcambuf_copymetadata ( HDCAM h, DCAM_METADATAHDR* hdr );

}
