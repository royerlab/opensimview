package fastfuse.pool;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.KernelAccessType;
import clearcl.exceptions.OpenCLException;
import coremem.interfaces.SizedInBytes;
import fastfuse.FastFusionException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Pair;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.LongStream;

/**
 * Fast fusion memory pool.
 *
 * @author Uwe Schmidt, Loic Royer
 */
public class FastFusionMemoryPool implements AutoCloseable, SizedInBytes
{
  private final static PrintStream cDebugOut = System.err;

  // TODO: this static shit needs to go...
  private static FastFusionMemoryPool mInstance = null;

  private final boolean mDebug;
  private final ClearCLContext mContext;
  private long mMemorySizeLimitInBytes;

  // TODO replace with atomic long:
  private long mCurrentUsedMemorySizeInBytes = 0;

  // Map of available images
  private final Map<Pair<ImageChannelDataType, List<Long>>, Stack<ClearCLImage>>
      mImagesAvailable =
      new HashMap<>();

  // Set of images in use:
  private final Set<ClearCLImage> mImagesInUseSet = new HashSet<>();

  // This linked hash set is used to record which images where recently accessed:
  private final LinkedHashSet<Pair<ImageChannelDataType, List<Long>>>
      mImageAccess =
      new LinkedHashSet<>();

  /**
   * Returns a fast fusion memory pool instance given a ClearCL context.
   *
   * @param pContext ClearCL context
   * @return pool
   */
  public static FastFusionMemoryPool getInstance(ClearCLContext pContext)
  {
    return getInstance(pContext,
                       pContext.getDevice().getGlobalMemorySizeInBytes(),
                       false);
  }

  /**
   * Returns a fast fusion memory pool instance given a ClearCL context and a prefered
   * pool size.
   *
   * @param pContext                  ClearCL context
   * @param pPreferredPoolSizeInBytes preferred pool size
   * @return pool
   */
  public static FastFusionMemoryPool getInstance(ClearCLContext pContext,
                                                 long pPreferredPoolSizeInBytes)
  {
    if (mInstance == null)
      mInstance = new FastFusionMemoryPool(pContext, pPreferredPoolSizeInBytes, false);
    return mInstance;
  }

  /**
   * Returns a fast fusion memory pool instance given a ClearCL context, a prefered pool
   * size, and a debug flag.
   *
   * @param pContext                  ClearCL context
   * @param pPreferredPoolSizeInBytes preferred pool size
   * @param pDebug                    debug flag
   * @return pool
   */
  public static FastFusionMemoryPool getInstance(ClearCLContext pContext,
                                                 long pPreferredPoolSizeInBytes,
                                                 boolean pDebug)
  {
    if (mInstance == null)
      mInstance = new FastFusionMemoryPool(pContext, pPreferredPoolSizeInBytes, pDebug);
    return mInstance;
  }

  /**
   * TODO: why do we need this?
   *
   * @return
   */
  public static FastFusionMemoryPool get()
  {
    if (mInstance == null)
      throw new FastFusionException(
          "FastFusionMemoryPool.get() can only be called after FastFusionMemoryPool.getInstance()");
    return mInstance;
  }

  /**
   * Constructs a fast fusion memory pool given a ClearCL context, a pool size, and a
   * debug flag.
   *
   * @param pContext         ClearCL context
   * @param pMemorySizeLimitInBytes preferred pool size
   * @param pDebug           debug flag
   */
  private FastFusionMemoryPool(ClearCLContext pContext,
                               long pMemorySizeLimitInBytes,
                               boolean pDebug)
  {
    mContext = pContext;
    mMemorySizeLimitInBytes = pMemorySizeLimitInBytes;
    mDebug = pDebug;
    debug("Creating FastFusionMemoryPool with preferred size limit of %.0f MB\n",
          mMemorySizeLimitInBytes / (1024d * 1024d));
  }

  /**
   * Allocates an image of given data gtype and dimensions
   *
   * @param pDataType   data type
   * @param pDimensions dimensions
   * @return image
   */
  private ClearCLImage allocateImage(ImageChannelDataType pDataType, long... pDimensions)
  {
    ClearCLImage
        lImage =
        freeMemoryIfNecessaryAndRun(() -> mContext.createSingleChannelImage(HostAccessType.ReadWrite,
                                                                            KernelAccessType.ReadWrite,
                                                                            pDataType,
                                                                            pDimensions),
                                    String.format(
                                        "Couldn't allocate image of type '%s' with dimensions %s",
                                        pDataType.toString(),
                                        Arrays.toString(pDimensions)));
    mCurrentUsedMemorySizeInBytes += lImage.getSizeInBytes();
    return lImage;
  }

  /**
   * Closes an image, i.e. releases all its resources...
   *
   * @param pImage image to close
   */
  private void closeImage(ClearCLImage pImage)
  {
    mCurrentUsedMemorySizeInBytes -= pImage.getSizeInBytes();
    pImage.close();
    debug("                  free:      %32s = %3.0f MB  ->  %s\n",
          getKey(pImage).toString(),
          pImage.getSizeInBytes() / (1024d * 1024d),
          toString());
  }

  /**
   * Returns the size in bytes of a given image specification (type, dimensions)
   *
   * @param pDataType   data type
   * @param pDimensions dimensions
   * @return size in bytes
   */
  private long getSizeInBytes(ImageChannelDataType pDataType, long... pDimensions)
  {
    long lVolume = LongStream.of(pDimensions).reduce(1, (a, b) -> a * b);
    return lVolume * pDataType.getNativeType().getSizeInBytes();
  }

  /**
   * Return true if there is not enough memory to allocate a given number of bytes.
   * addition aml
   *
   * @param pAdditionalMemoryInBytes amount of memory in bytes needed
   * @return true if there is not enough available memory
   */
  private boolean notEnoughMemoryAvailable(long pAdditionalMemoryInBytes)
  {
    return (mCurrentUsedMemorySizeInBytes + pAdditionalMemoryInBytes) > mMemorySizeLimitInBytes;
  }

  /**
   * Rerturns true if it is possible to free additional memory.
   *
   * @return true if possible to free memory
   */
  private boolean freeMemIsPossible()
  {
    return getNumberOfAvailableImages() > 0;
  }

  /**
   * Returns true if freeing more memory is nescessary and possible.
   * <p>
   * Note: this method uses a temporal record of the last accessed images and thus starts
   * clearing the images that are the oldest in terms of usage.
   *
   * @param pAdditionalMemoryInBytes amount of memory in bytes needed
   */
  private void freeMemIfNecessaryAndPossible(long pAdditionalMemoryInBytes)
  {
    if (notEnoughMemoryAvailable(pAdditionalMemoryInBytes) && freeMemIsPossible())
    {
      assert !mImageAccess.isEmpty();
      for (Pair<ImageChannelDataType, List<Long>> lKeyAccess : mImageAccess)
      {
        Stack<ClearCLImage> lImageStack = mImagesAvailable.get(lKeyAccess);
        assert lImageStack != null;
        while (!lImageStack.isEmpty()
               && notEnoughMemoryAvailable(pAdditionalMemoryInBytes))
        {
          closeImage(lImageStack.pop());
        }
      }
    }
  }

  /**
   * Requests an image of a given data type and dimensions
   *
   * @param pDataType   data type
   * @param pDimensions dimensions
   * @return image
   */
  public ClearCLImage requestImage(ImageChannelDataType pDataType, long... pDimensions)
  {
    return requestImage(null, pDataType, pDimensions);
  }

  /**
   * Requests an image of a given data type and dimensions. Assigns it a name for logging
   * purposes.
   *
   * @param pName       debug name
   * @param pDataType   data type
   * @param pDimensions dimensions
   * @return image
   */
  public ClearCLImage requestImage(final String pName,
                                   final ImageChannelDataType pDataType,
                                   final long... pDimensions)
  {
    Pair<ImageChannelDataType, List<Long>> lKey = getKey(pDataType, pDimensions);
    Stack<ClearCLImage> lSpecificImagesAvailable = mImagesAvailable.get(lKey);
    boolean allocated;
    ClearCLImage lImage;
    if (lSpecificImagesAvailable == null || lSpecificImagesAvailable.isEmpty())
    {
      // try to free memory if new allocation will go beyond preferred size
      freeMemIfNecessaryAndPossible(getSizeInBytes(pDataType, pDimensions));
      allocated = true;
      lImage = allocateImage(pDataType, pDimensions);
    }
    else
    {
      allocated = false;
      lImage = lSpecificImagesAvailable.pop();
    }
    assert !mImagesInUseSet.contains(lImage);
    mImagesInUseSet.add(lImage);
    debug("%15s - %s  %32s = %3.0f MB  ->  %s\n",
          prettyName(pName, 15),
          allocated ? "allocate:" : "reuse:   ",
          lKey.toString(),
          getSizeInBytes(pDataType, pDimensions) / (1024d * 1024d),
          toString());
    return lImage;
  }

  /**
   * Releases the given image.
   *
   * @param pImage image
   */
  public void releaseImage(ClearCLImage pImage)
  {
    releaseImage(null, pImage);
  }

  /**
   * Releases the given image. Assigns it a name for logging purposes.
   *
   * @param pName  name
   * @param pImage image
   */
  public void releaseImage(String pName, ClearCLImage pImage)
  {
    if (pImage == null)
      return;

    // Make sure that image is really in use:
    assert mImagesInUseSet.contains(pImage);

    // Remove image from the in-use set:
    mImagesInUseSet.remove(pImage);

    // Get key for image:
    Pair<ImageChannelDataType, List<Long>> lKey = getKey(pImage);

    // Record access:
    recordAccess(lKey);

    // Returns theb stack of available images of a given type:
    Stack<ClearCLImage> lSpecificImagesAvailable = mImagesAvailable.get(lKey);

    if (lSpecificImagesAvailable == null)
    {
      // If there is no imagew of that type and dimensions, we initialise:
      lSpecificImagesAvailable = new Stack<>();
      mImagesAvailable.put(lKey, lSpecificImagesAvailable);
    }

    // We add the newly available image to the stack:
    lSpecificImagesAvailable.push(pImage);

    debug("%15s - release:   %32s = %3.0f MB  ->  %s\n",
          prettyName(pName, 15),
          lKey.toString(),
          pImage.getSizeInBytes() / (1024d * 1024d),
          toString());

    freeMemIfNecessaryAndPossible(0);
  }

  /**
   * Free memory if necessary and run some code.
   *
   * @param pRunnable runnable
   */
  public void freeMemoryIfNecessaryAndRun(Runnable pRunnable)
  {
    freeMemoryIfNecessaryAndRun(pRunnable, null);
  }

  /**
   * Free memory if necessary and run some code that returns a result.
   *
   * @param pSupplier supplier
   * @param <T>       result type
   * @return result
   */
  public <T> T freeMemoryIfNecessaryAndRun(Supplier<T> pSupplier)
  {
    return freeMemoryIfNecessaryAndRun(pSupplier, null);
  }

  /**
   * Free memory if necessary and run some code. An error message is provided in case of
   * an exception.
   *
   * @param pRunnable runnable
   * @param pErrorMsg error message
   */
  public void freeMemoryIfNecessaryAndRun(Runnable pRunnable, String pErrorMsg)
  {
    freeMemoryIfNecessaryAndRun(() -> {
      pRunnable.run();
      return true;
    }, pErrorMsg);
  }

  /**
   * Free memory if necessary and run some code that returns a result. An error message is
   * provided in case of an exception.
   *
   * @param pSupplier supplier
   * @param pErrorMsg error message
   * @param <T>       result type
   * @return result
   */
  public <T> T freeMemoryIfNecessaryAndRun(Supplier<T> pSupplier, String pErrorMsg)
  {
    try
    {
      return pSupplier.get();
    }
    catch (Throwable e)
    {
      debug("Problem occurred during freeMemoryIfNecessaryAndRun(): %s\n",
            e.getMessage());
      if (isMemoryAllocationFailure(e))
      {
        if (freeMemIsPossible())
        {
          free();
          return pSupplier.get();
        }
        else
        {
          String lErrorMsg = pErrorMsg != null ? pErrorMsg : "";
          throw new FastFusionException(e, lErrorMsg);
        }
      }
      else
        throw e;
    }
  }

  /**
   * Analyses a provided exception and returns true if memory allocation failed.
   *
   * @param pThrowable exception to analyse
   * @return true if memory allocation failed
   */
  private boolean isMemoryAllocationFailure(Throwable pThrowable)
  {
    boolean
        foundIt =
        pThrowable instanceof OpenCLException
        && ((OpenCLException) pThrowable).getErrorCode() == -4;
    if (foundIt)
      return true;
    else
      return (pThrowable.getCause() == null) ?
             false :
             isMemoryAllocationFailure(pThrowable.getCause());
  }

  /**
   * Closes
   *
   * @throws Exception
   */
  @Override public void close() throws Exception
  {
    free(true);
  }

  /**
   *
   */
  public void free()
  {
    free(false);
  }

  /**
   * Frees all available images with the option ot also free used images (dangerous).
   *
   * @param pFreeImagesInUse if true free also images in-use.
   */
  public void free(boolean pFreeImagesInUse)
  {
    debug("Freeing all available images\n");
    for (Stack<ClearCLImage> lStack : mImagesAvailable.values())
      while (!lStack.isEmpty())
        closeImage(lStack.pop());
    mImagesAvailable.clear();
    mImageAccess.clear();
    if (pFreeImagesInUse)
    {
      debug("Freeing all images that are still in use\n");
      Iterator<ClearCLImage> it = mImagesInUseSet.iterator();
      while (it.hasNext())
      {
        ClearCLImage lImage = it.next();
        it.remove();
        closeImage(lImage);
      }
      assert mCurrentUsedMemorySizeInBytes == 0;
    }
  }

  /**
   * Returns the memory size limit in bytes.
   * @return memory size limit in bytes
   */
  public long getMemorySizeLimitInBytes()
  {
    return mMemorySizeLimitInBytes;
  }

  /**
   * Sets the memory size limit in bytes that this pool will try to enforce.
   * @param pMemorySizeLimitInBytes
   */
  public void setMemorySizeLimitInBytes(long pMemorySizeLimitInBytes)
  {
    mMemorySizeLimitInBytes = pMemorySizeLimitInBytes;
  }


  /**
   * Returns memory size in bytes for used images.
   * @return current used memory
   */
  public long getCurrentUsedMemorySizeInBytes()
  {
    return mCurrentUsedMemorySizeInBytes;
  }

  /**
   * Returns total memory size in bytes of available images.
   * @return current used memory
   */
  public long getCurrentAvailableMemorySizeInBytes()
  {
    long lCurrentAvailableMemorySizeInBytes=0;

    for (Map.Entry<Pair<ImageChannelDataType, List<Long>>, Stack<ClearCLImage>> lEntry : mImagesAvailable
        .entrySet())
    {
      final Stack<ClearCLImage> lStack = lEntry.getValue();

      for (ClearCLImage lClearCLImage : lStack)
      {
        lCurrentAvailableMemorySizeInBytes +=lClearCLImage.getSizeInBytes();
      }
    }
    return lCurrentAvailableMemorySizeInBytes;
  }


  /**
   * Returns the _total_ amount of memory -- in-use and available -- in bytes.
   * @return current used memory size in bytes
   */
  @Override
  public long getSizeInBytes()
  {
    return getCurrentUsedMemorySizeInBytes()+getCurrentAvailableMemorySizeInBytes();
  }

  /**
   * Returns the number of available images.
   * @return number of available images
   */
  public int getNumberOfAvailableImages()
  {
    int lNumAvailable = 0;
    for (Stack<ClearCLImage> lStack : mImagesAvailable.values())
      lNumAvailable += lStack.size();
    return lNumAvailable;
  }

  /**
   * Returns the number of available images.
   * @return number of in-use images
   */
  public int getNumberOfInUseImages()
  {
    return mImagesInUseSet.size();
  }

  /**
   * Returns true if a given imnage is in use.
   * @param pImage image to test
   * @return true if in use
   */
  public boolean isImageInUse(ClearCLImage pImage)
  {
    return pImage != null && mImagesInUseSet.contains(pImage);
  }


  /**
   * @param pKey
   */
  private void recordAccess(Pair<ImageChannelDataType, List<Long>> pKey)
  {
    mImageAccess.remove(pKey);
    mImageAccess.add(pKey);
  }

  /**
   * @param pImage
   * @return
   */
  private Pair<ImageChannelDataType, List<Long>> getKey(final ClearCLImage pImage)
  {
    return getKey(pImage.getChannelDataType(), pImage.getDimensions());
  }

  /**
   * @param pDataType
   * @param pDimensions
   * @return
   */
  private Pair<ImageChannelDataType, List<Long>> getKey(final ImageChannelDataType pDataType,
                                                        final long... pDimensions)
  {
    return Pair.create(pDataType, Arrays.asList(ArrayUtils.toObject(pDimensions)));
  }

  /**
   * @param pName
   * @param pLength
   * @return
   */
  private String prettyName(String pName, int pLength)
  {
    if (pName == null)
      return "<unnamed>";
    if (pName.length() <= pLength)
      return pName;
    return pName.substring(0, pLength - 3) + "...";
  }



  /**
   * @param format
   * @param args
   */
  private void debug(String format, Object... args)
  {
    if (mDebug)
      cDebugOut.printf(format, args);
  }



  @Override public String toString()
  {
    return String.format("MemoryPool(used = %2d, avail = %2d, memory = %4.0f | %.0f MB)",
                         getNumberOfInUseImages(),
                         getNumberOfAvailableImages(),
                         getCurrentUsedMemorySizeInBytes() / (1024d * 1024d),
                         getMemorySizeLimitInBytes() / (1024d * 1024d));
  }
}
