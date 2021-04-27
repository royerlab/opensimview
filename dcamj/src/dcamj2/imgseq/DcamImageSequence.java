package dcamj2.imgseq;

import java.util.ArrayList;

import coremem.ContiguousMemoryInterface;
import coremem.buffers.ContiguousBuffer;
import coremem.exceptions.FreedException;
import coremem.fragmented.FragmentedMemory;
import coremem.fragmented.FragmentedMemoryInterface;
import coremem.interfaces.SizedInBytes;
import coremem.offheap.OffHeapMemory;
import coremem.recycling.RecyclableInterface;
import coremem.recycling.RecyclerInterface;
import coremem.rgc.Freeable;
import dcamj2.DcamDevice;

import org.bridj.Pointer;

/**
 * Dcam image sequence
 *
 * @author royer
 */
public class DcamImageSequence implements
                               RecyclableInterface<DcamImageSequence, DcamImageSequenceRequest>,
                               SizedInBytes,
                               Freeable
{

  private static final int cPageAlignment = 4096;

  private DcamDevice mDcamDevice;
  private FragmentedMemoryInterface mFragmentedMemory;

  private volatile long mBytesPerPixel, mWidth, mHeight, mDepth;
  private volatile long mTimeStampInNs;

  // Recycling stuff:
  private RecyclerInterface<DcamImageSequence, DcamImageSequenceRequest> mRecycler;
  private boolean mIsReleased;

  /**
   * Initializes a Dcam image sequence given the number of bytes per pixel, and
   * image sequence width, height and depth. The memory allocation is handled by
   * this constructor.
   * 
   * @param pDcamDevice
   *          device to use for acquisition, this is used to adjust height and
   *          width
   * 
   * @param pBytesPerPixel
   *          bytes per pixel/voxel
   * @param pWidth
   *          width
   * @param pHeight
   *          height
   * @param pDepth
   *          depth
   */
  public DcamImageSequence(DcamDevice pDcamDevice,
                           final long pBytesPerPixel,
                           final long pWidth,
                           final long pHeight,
                           final long pDepth)
  {
    this(pDcamDevice, pBytesPerPixel, pWidth, pHeight, pDepth, true);
  }

  /**
   * Initialises a Dcam image sequence given the number of bytes per pixel, and
   * image sequence width, height, depth and binning (1,2 or 4). The memory
   * allocation is handled by this constructor.
   * 
   * @param pDcamDevice
   *          device to use for acquisition, this is used to adjust height and
   *          width
   * 
   * @param pBytesPerPixel
   *          bytes per pixel/voxel
   * @param pWidth
   *          width
   * @param pHeight
   *          height
   * @param pDepth
   *          depth
   * @param pFragmented
   *          true: allocates multiple independent buffers, false: allocates a
   *          single contiguous buffer
   */
  public DcamImageSequence(DcamDevice pDcamDevice,
                           final long pBytesPerPixel,
                           final long pWidth,
                           final long pHeight,
                           final long pDepth,
                           boolean pFragmented)
  {
    this(pDcamDevice, null, pBytesPerPixel, pWidth, pHeight, pDepth);

    if (pFragmented)
    {
      mFragmentedMemory = new FragmentedMemory();
      for (int i = 0; i < mDepth; i++)
      {
        long lNumberOfBytes = pBytesPerPixel * mWidth * mHeight;
        OffHeapMemory lAllocatedMemory =
                                       OffHeapMemory.allocateAlignedBytes("DcamImageSequence"
                                                                          + i,
                                                                          lNumberOfBytes,
                                                                          cPageAlignment);
        mFragmentedMemory.add(lAllocatedMemory);
      }
    }
    else
    {
      long lNumberOfBytes =
                          pBytesPerPixel * mWidth * mHeight * mDepth;
      OffHeapMemory lAllocatedMemory =
                                     OffHeapMemory.allocateAlignedBytes("DcamImageSequence",
                                                                        lNumberOfBytes,
                                                                        cPageAlignment);
      mFragmentedMemory = FragmentedMemory.split(lAllocatedMemory,
                                                 mDepth);
    }

  }

  /**
   * Instantiates a Dcam image sequence given a fragmented memory object and
   * corresponding number of bytes per pixel, image width, height and depth.
   * 
   * @param pDcamDevice
   *          device to use for acquisition, this is used to adjust height and
   *          width
   * 
   * @param pFragmentedMemory
   *          fragmented memory object
   * @param pBytesPerPixel
   *          bytes per pixel
   * @param pWidth
   *          width
   * @param pHeight
   *          height
   * @param pDepth
   *          depth
   */
  public DcamImageSequence(DcamDevice pDcamDevice,
                           final FragmentedMemoryInterface pFragmentedMemory,
                           final long pBytesPerPixel,
                           final long pWidth,
                           final long pHeight,
                           final long pDepth)
  {
    mDcamDevice = pDcamDevice;
    mBytesPerPixel = pBytesPerPixel;
    mWidth =
           pDcamDevice.adjustWidthHeight(pWidth,
                                         4 / pDcamDevice.getBinning());
    mHeight =
            pDcamDevice.adjustWidthHeight(pHeight,
                                          4 / pDcamDevice.getBinning());
    mDepth = pDepth;

    mFragmentedMemory = pFragmentedMemory;
  }

  /**
   * Returns the parent Dcam device
   * 
   * @return parent Dcam device
   */
  public DcamDevice getDcamDevice()
  {
    return mDcamDevice;
  }

  /**
   * Returns the number of bytes per pixel
   * 
   * @return number of bytes per pixel
   */
  public final long getBytesPerPixel()
  {
    return mBytesPerPixel;
  }

  /**
   * Returns this image sequence width
   * 
   * @return image sequence width
   */
  public final long getWidth()
  {
    return mWidth;
  }

  /**
   * Returns this image sequence height
   * 
   * @return image sequence height
   */
  public final long getHeight()
  {
    return mHeight;
  }

  /**
   * Returns this image sequence depth
   * 
   * @return image sequence depth
   */
  public final long getDepth()
  {
    return mDepth;
  }

  /**
   * Sets this image sequence time stamp.
   * 
   * @param pTimeStampInNs
   *          image sequence time stamp.
   */
  public void setTimeStampInNs(final long pTimeStampInNs)
  {
    mTimeStampInNs = pTimeStampInNs;
  }

  /**
   * Returns this image sequence time stamp
   * 
   * @return image sequence time stamp
   */
  public final long getTimeStampInNs()
  {
    return mTimeStampInNs;
  }

  /**
   * Returns a BridJ pointer for the plane of given index
   * 
   * @param pIndex
   *          plane index
   * @return BridK pointer
   */
  public Pointer<Byte> getPointerForPlane(final int pIndex)
  {
    ContiguousMemoryInterface lMemoryForPlane =
                                              getMemoryForPlane(pIndex);
    return lMemoryForPlane.getBridJPointer(Byte.class);
  }

  /**
   * Returns memory for a given plane index
   * 
   * @param pIndex
   *          plane index
   * @return memory object for plane
   */
  public ContiguousMemoryInterface getMemoryForPlane(final int pIndex)
  {
    return mFragmentedMemory.get(pIndex);
  }

  /**
   * Returns a Dcam image sequence for a single image of given index from this
   * image sequence
   * 
   * @param pIndex
   *          image index
   * @return Dcam image sequence
   */
  public DcamImageSequence getSinglePlaneImageSequence(final int pIndex)
  {
    ContiguousMemoryInterface lMemory = getMemoryForPlane(pIndex);
    DcamImageSequence lDcamImageSequence =
                                         new DcamImageSequence(mDcamDevice,
                                                               FragmentedMemory.wrap(lMemory),
                                                               getBytesPerPixel(),
                                                               getWidth(),
                                                               getDepth(),
                                                               1L);
    return lDcamImageSequence;
  }

  /**
   * Consolidates (copies) the contents of this image sequence into a
   * 
   * @param pKeepPlaneList
   *          list of boolean flags indicating which planes to keep
   * 
   * @param pDestinationMemory
   *          destination memory
   */
  public void consolidateTo(ArrayList<Boolean> pKeepPlaneList,
                            final ContiguousMemoryInterface pDestinationMemory)
  {
    if (pKeepPlaneList == null || pKeepPlaneList.isEmpty()
        || allPlanesKept(pKeepPlaneList))
      mFragmentedMemory.makeConsolidatedCopy(pDestinationMemory);
    else
    {
      final ContiguousBuffer lContiguousBuffer =
                                               ContiguousBuffer.wrap(pDestinationMemory);

      int lNumberOfFragments =
                             mFragmentedMemory.getNumberOfFragments();

      for (int i = 0; i < lNumberOfFragments; i++)
        if (pKeepPlaneList.get(i))
        {
          ContiguousMemoryInterface lContiguousMemoryInterface =
                                                               mFragmentedMemory.get(i);
          lContiguousBuffer.writeContiguousMemory(lContiguousMemoryInterface);
        }

    }
  }

  private boolean allPlanesKept(ArrayList<Boolean> pKeepPlaneList)
  {
    for (Boolean lKeepPlane : pKeepPlaneList)
      if (!lKeepPlane)
        return false;
    return true;
  }

  /**
   * Returns the number of fragments
   * 
   * @return number of fragments
   */
  public long getNumberOfFragments()
  {
    return mFragmentedMemory.getNumberOfFragments();
  }

  /**
   * Returns whether the data buffer supporting this image sequence is
   * fragmented
   * 
   * @return true: fragmented, false otherwise
   */
  public boolean isFragmented()
  {
    return getNumberOfFragments() > 1;
  }

  @Override
  public long getSizeInBytes()
  {
    return mFragmentedMemory.getSizeInBytes();
  }

  @Override
  public void free()
  {
    mFragmentedMemory.free();
  }

  @Override
  public boolean isFree()
  {
    return mFragmentedMemory.isFree();
  }

  @Override
  public void complainIfFreed() throws FreedException
  {
    mFragmentedMemory.complainIfFreed();
  }

  @Override
  public String toString()
  {
    return String.format("DcamImageSequence [mBytesPerPixel=%d, mWidth=%d, mHeight=%d, mDepth=%d, mTimeStampInNs=%d]",
                         mBytesPerPixel,
                         mWidth,
                         mHeight,
                         mDepth,
                         mTimeStampInNs);
  }

  @Override
  public boolean isCompatible(DcamImageSequenceRequest pRequest)
  {
    return pRequest.isCompatible(this);
  }

  @Override
  public void recycle(DcamImageSequenceRequest pRequest)
  {
    // nothing to do
  }

  @Override
  public void setRecycler(RecyclerInterface<DcamImageSequence, DcamImageSequenceRequest> pRecycler)
  {
    mRecycler = pRecycler;
  }

  @Override
  public void setReleased(boolean pIsReleased)
  {
    mIsReleased = pIsReleased;
  }

  @Override
  public boolean isReleased()
  {
    return mIsReleased;
  }

  @Override
  public void release()
  {
    if (mRecycler != null)
      mRecycler.release(this);
  }

}
