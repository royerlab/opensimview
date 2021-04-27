package clearcl;

import java.nio.Buffer;
import java.util.Arrays;

import clearcl.abs.ClearCLMemBase;
import clearcl.enums.HostAccessType;
import clearcl.interfaces.ClearCLImageInterface;
import clearcl.interfaces.ClearCLMemInterface;
import coremem.ContiguousMemoryInterface;
import coremem.enums.NativeTypeEnum;
import coremem.offheap.OffHeapMemory;
import coremem.util.Size;

/**
 * ClearCLHostImageBuffer is the ClearCL abstraction for CPU RAM images.
 *
 * @author royer
 */
public class ClearCLHostImageBuffer extends ClearCLMemBase implements
                                    ClearCLMemInterface,
                                    ClearCLImageInterface
{

  private ContiguousMemoryInterface mContiguousMemory;
  private NativeTypeEnum mNativeType;
  private final long[] mDimensions;
  private long mNumberOfChannels;

  /**
   * Allocates a host image buffer of same dimensions than a given image.
   * 
   * @param pClearCLImage
   *          image template
   * @return newly allocated image.
   */
  public static ClearCLHostImageBuffer allocateSameAs(ClearCLImageInterface pClearCLImage)
  {
    ClearCLHostImageBuffer lClearCLHostImage =
                                             new ClearCLHostImageBuffer(pClearCLImage.getContext(),
                                                                        allocate(pClearCLImage.getSizeInBytes()),
                                                                        pClearCLImage.getNativeType(),
                                                                        pClearCLImage.getNumberOfChannels(),
                                                                        pClearCLImage.getDimensions());
    return lClearCLHostImage;
  }

  /**
   * Internal method to allocate offheap memory.
   * 
   * @param pSizeInBytes
   * @return
   */
  private static OffHeapMemory allocate(long pSizeInBytes)
  {
    return OffHeapMemory.allocatePageAlignedBytes("ClearCLHostImageBuffer",
                                                  pSizeInBytes);
  }

  /**
   * Creates a host image buffer from a context, native type, number of
   * channels, and dimensions.
   * 
   * @param pClearCLContext
   *          context
   * @param pNativeType
   *          native type
   * @param pNumberOfChannels
   *          number of channels
   * @param pDimensions
   *          dimensions
   */
  public ClearCLHostImageBuffer(ClearCLContext pClearCLContext,
                                NativeTypeEnum pNativeType,
                                long pNumberOfChannels,
                                long... pDimensions)
  {
    this(pClearCLContext,
         allocate(pNumberOfChannels * Size.of(pNativeType)
                  * getVolume(pDimensions)),
         pNativeType,
         pNumberOfChannels,
         pDimensions);

  }

  /**
   * Creates a host image buffer from a context, an existing contiguous memory
   * object, native type, number of channels, and dimensions.
   * 
   * @param pClearCLContext
   *          context
   * @param pContiguousMemoryInterface
   *          contiguous memory
   * @param pNativeType
   *          native type
   * @param pNumberOfChannels
   *          number of channels
   * @param pDimensions
   *          dimensions
   */
  public ClearCLHostImageBuffer(ClearCLContext pClearCLContext,
                                ContiguousMemoryInterface pContiguousMemoryInterface,
                                NativeTypeEnum pNativeType,
                                long pNumberOfChannels,
                                long... pDimensions)
  {
    super(pClearCLContext.getBackend(),
          pClearCLContext.getBackend()
                         .wrap(pContiguousMemoryInterface),
          null,
          null,
          null);
    mContiguousMemory = pContiguousMemoryInterface;
    mNativeType = pNativeType;
    mNumberOfChannels = pNumberOfChannels;
    mDimensions = pDimensions;
  }

  @Override
  public ClearCLContext getContext()
  {
    return null;
  }

  /**
   * Returns the contiguous memory object used to store the data.
   * 
   * @return contiguous memory used internally
   */
  public ContiguousMemoryInterface getContiguousMemory()
  {
    return mContiguousMemory;
  }

  @Override
  public HostAccessType getHostAccessType()
  {
    return HostAccessType.ReadWrite;
  }

  @Override
  public long[] getDimensions()
  {
    return mDimensions;
  }

  @Override
  public NativeTypeEnum getNativeType()
  {
    return mNativeType;
  }

  @Override
  public long getPixelSizeInBytes()
  {
    return mNativeType.getSizeInBytes();
  }

  @Override
  public long getNumberOfChannels()
  {
    return mNumberOfChannels;
  }

  private static final long getVolume(long[] pDimensions)
  {
    long lVolume = 1;
    for (int i = 0; i < pDimensions.length; i++)
      lVolume *= pDimensions[i];
    return lVolume;
  }

  @Override
  public long getSizeInBytes()
  {
    return mContiguousMemory.getSizeInBytes();
  }

  @Override
  public void copyTo(ClearCLImage pImage, boolean pBlockingWrite)
  {
    // since this is a host image, we fall-back to a 'read'
    pImage.readFrom(getContiguousMemory(), pBlockingWrite);
  }

  @Override
  public void copyTo(ClearCLBuffer pBuffer, boolean pBlockingWrite)
  {
    // since this is a host image, we fall-back to a 'read'
    pBuffer.readFrom(getContiguousMemory(), pBlockingWrite);
  }

  @Override
  public void writeTo(ContiguousMemoryInterface pContiguousMemory,
                      boolean pBlockingWrite)
  {
    mContiguousMemory.copyTo(pContiguousMemory);
  }

  @Override
  public void writeTo(Buffer pBuffer, boolean pBlockingWrite)
  {
    mContiguousMemory.copyTo(pBuffer);
  }

  @Override
  public void readFrom(ContiguousMemoryInterface pContiguousMemory,
                       boolean pBlockingRead)
  {
    mContiguousMemory.copyFrom(pContiguousMemory);
  }

  @Override
  public void readFrom(Buffer pBuffer, boolean pBlockingRead)
  {
    mContiguousMemory.copyFrom(pBuffer);
  }

  @Override
  public String toString()
  {
    return String.format("ClearCLHostImageBuffer [mContiguousMemory=%s, mNativeType=%s, mDimensions=%s, mNumberOfChannels=%s, getMemAllocMode()=%s, getKernelAccessType()=%s, getBackend()=%s, getPeerPointer()=%s]",
                         mContiguousMemory,
                         mNativeType,
                         Arrays.toString(mDimensions),
                         mNumberOfChannels,
                         getMemAllocMode(),
                         getKernelAccessType(),
                         getBackend(),
                         getPeerPointer());
  }

  @Override
  public void close()
  {
    // No need for rgc here: contig mem is already garbage collected
    if (mContiguousMemory != null)
    {
      mContiguousMemory.free();
      mContiguousMemory = null;
    }
  }

}
