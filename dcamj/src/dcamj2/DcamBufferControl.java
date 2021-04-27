package dcamj2;

import static org.bridj.Pointer.pointerTo;

import dcamapi.DCAMBUF_ATTACH;
import dcamapi.DCAM_FRAME;
import dcamapi.DcamapiLibrary;
import dcamapi.DcamapiLibrary.DCAMERR;
import dcamapi.DcamapiLibrary.DCAMIDPROP;
import dcamj2.imgseq.DcamImageSequence;

import org.bridj.BridJ;
import org.bridj.IntValuedEnum;
import org.bridj.Pointer;

/**
 * Dcam buffer control
 *
 * @author royer
 */
public class DcamBufferControl extends DcamBase
{

  private final DcamDevice mDcamDevice;

  private DcamImageSequence mAttachedDcamFrame;
  private DCAMBUF_ATTACH mDCAMBUF_ATTACH;
  private Pointer<Pointer<?>> mPointerToPointerArray;
  private Pointer<DCAM_FRAME> mInternalDcamFramePointer;

  /**
   * Instantiates a Dcam buffer control
   * 
   * @param pDcamDevice
   *          Dcam camera device
   */
  DcamBufferControl(final DcamDevice pDcamDevice)
  {
    mDcamDevice = pDcamDevice;
  }

  /**
   * Allocates internal buffers
   * 
   * @param pNumberOfBuffers
   *          number of internal buffers to allocate
   * @return true: success, false otherwise
   */
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

  /**
   * Locks a frame (whatever that means!)
   * 
   * @return pointer to frame
   */
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

  /**
   * Copies frame.
   * 
   * @return pointer to Dcam frame
   */
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

  /**
   * Attach external buffers provided as a Dcam frame
   * 
   * @param pImageSequence
   *          Dcam frame
   * @return true: success, false otherwise.
   */
  public final boolean attachExternalBuffers(DcamImageSequence pImageSequence)
  {
    // if (mAttachedDcamFrame == pImageSequence)
    // return true;

    mAttachedDcamFrame = pImageSequence;

    final long lNumberOfBuffers = pImageSequence.getDepth();

    if (mPointerToPointerArray == null
        || mPointerToPointerArray.getValidElements() != lNumberOfBuffers)
    {
      if (mPointerToPointerArray != null)
        mPointerToPointerArray.release();

      mPointerToPointerArray =
                             Pointer.allocatePointers((int) lNumberOfBuffers);
    }

    for (int i = 0; i < lNumberOfBuffers; i++)
    {
      Pointer<Byte> lPointerToIndividualBuffer =
                                               pImageSequence.getPointerForPlane(i);
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

    @SuppressWarnings("deprecation")
    final IntValuedEnum<DCAMERR> lError =
                                        DcamapiLibrary.dcambufAttach(mDcamDevice.getHDCAMPointer(),
                                                                     pointerTo(mDCAMBUF_ATTACH));
    final boolean lSuccess =
                           addErrorToListAndCheckHasSucceeded(lError);
    return lSuccess;
  }

  /**
   * Computes total required memory in bytes for the given number of buffers and
   * the current image dimensions.
   * 
   * @param pNumberOfBuffers
   *          number of buffers
   * @return total required memory in bytes
   */
  public long computeTotalRequiredMemoryInBytes(int pNumberOfBuffers)
  {
    final long lImageSizeInBytes =
                                 (long) mDcamDevice.getProperties()
                                                   .getDoublePropertyValue(DCAMIDPROP.DCAM_IDPROP_BUFFER_FRAMEBYTES);

    final long lTotalRequiredmemoryInBytes = pNumberOfBuffers
                                             * lImageSizeInBytes;

    return lTotalRequiredmemoryInBytes;
  }

  /**
   * Returns the Dcam image sequence for a given frame index
   * 
   * @param pFrameIndex
   *          frame index
   * @return Dcam image sequence for single plane
   */
  public DcamImageSequence getDcamFrameForIndex(int pFrameIndex)
  {
    return mAttachedDcamFrame.getSinglePlaneImageSequence(pFrameIndex);
  }

  /**
   * Returns the current attached image sequence depth.
   * 
   * @return number of single plane buffers
   */
  public long getAttachedImageSequenceDepth()
  {
    return mAttachedDcamFrame.getDepth();
  }

  /**
   * Releases buffers
   * 
   * @return true: success, false otherwise
   */
  public final boolean releaseBuffers()
  {
    final IntValuedEnum<DCAMERR> lError =
                                        DcamapiLibrary.dcambufRelease(mDcamDevice.getHDCAMPointer(),
                                                                      0);
    final boolean lSuccess =
                           addErrorToListAndCheckHasSucceeded(lError);

    return lSuccess;
  }

  /**
   * Returns the Dcam frame
   * 
   * @return Dcam frame
   */
  public DcamImageSequence getStackDcamFrame()
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
