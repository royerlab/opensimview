package coremem.offheap;

import coremem.exceptions.InvalidAllocationParameterException;
import coremem.exceptions.InvalidNativeMemoryAccessException;
import coremem.exceptions.OutOfMemoryException;
import coremem.util.Size;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * OfHeam memory access. This class offers static methods for memory allocation,
 * dealocation and read/write access.
 *
 * @author royer
 */
public final class OffHeapMemoryAccess
{

  static private Unsafe cUnsafe;

  static
  {
    Field lTheUnsafeField;
    try
    {
      lTheUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
      lTheUnsafeField.setAccessible(true);
      cUnsafe = (Unsafe) lTheUnsafeField.get(null);
    } catch (final Throwable e)
    {
      e.printStackTrace();
    }
  }

  static private final AtomicLong cMaximumAllocatableMemory = new AtomicLong(Long.MAX_VALUE);

  static private final ConcurrentHashMap<Long, Long> cAllocatedMemoryPointers = new ConcurrentHashMap<Long, Long>();
  static private final ConcurrentHashMap<Long, Long> cAllocatedMemoryPointersSignatures = new ConcurrentHashMap<Long, Long>();
  static private final AtomicLong cTotalAllocatedMemory = new AtomicLong(0);

  static private final Object mLock = new OffHeapMemoryAccess();

  /**
   * Registers a memory region at a given address of given length.
   *
   * @param pAddress address
   * @param pLength  length in bytes
   */
  public static final void registerMemoryRegion(long pAddress, long pLength)
  {
    synchronized (mLock)
    {
      cTotalAllocatedMemory.addAndGet(pLength);
      cAllocatedMemoryPointers.put(pAddress, pLength);
      cAllocatedMemoryPointersSignatures.put(pAddress, System.nanoTime());
    }
  }

  /**
   * Deregisters a memory region at a given address.
   *
   * @param pAddress address to deregister
   */
  public static final void deregisterMemoryRegion(long pAddress)
  {
    synchronized (mLock)
    {
      cTotalAllocatedMemory.addAndGet(-cAllocatedMemoryPointers.get(pAddress));
      cAllocatedMemoryPointers.remove(pAddress);
      cAllocatedMemoryPointersSignatures.remove(pAddress);
    }
  }

  /**
   * Returns the maximum amount of memory that can be allocated.
   *
   * @return max allocatable memory
   */
  public static long getMaximumAllocatableMemory()
  {
    return cMaximumAllocatableMemory.get();
  }

  /**
   * Sets the maximum amount of memory that can be allocated.
   *
   * @param pMaximumAllocatableMemory new max allocatable memory
   */
  public static void setMaximumAllocatableMemory(long pMaximumAllocatableMemory)
  {
    cMaximumAllocatableMemory.set(pMaximumAllocatableMemory);
  }

  /**
   * Overrides the current total amount of memory that has been allocated.
   *
   * @param pTotalAllocatedMemory new current allocated memory
   */
  public static final void overrideTotalAllocatedMemory(long pTotalAllocatedMemory)
  {
    cTotalAllocatedMemory.set(pTotalAllocatedMemory);
  }

  /**
   * Returns the current total amount of memory that has been allocated.
   *
   * @return total amount of allocated memory
   */
  public static final long getTotalAllocatedMemory()
  {
    return cTotalAllocatedMemory.get();
  }

  /**
   * Returns the page size. https://en.wikipedia.org/wiki/Page_(computer_memory)
   *
   * @return page size in bytes
   */
  public static final int getPageSize()
  {
    return cUnsafe.pageSize();
  }

  /**
   * Allocates a memory region of given length.
   *
   * @param pLengthInBytes length in bytes
   * @return address
   */
  public static final long allocateMemory(final long pLengthInBytes)
  {
    synchronized (mLock)
    {
      checkMaxAllocatableMemory(pLengthInBytes);

      if (pLengthInBytes <= 0)
        throw new InvalidAllocationParameterException("cUnsafe.allocateMemory requires a strictly positive allocation length: " + pLengthInBytes);
      final long lAddress = cUnsafe.allocateMemory(pLengthInBytes);
      if (lAddress <= 0)
        throw new OutOfMemoryException("cUnsafe.allocateMemory returned null or negative pointer: " + lAddress);
      registerMemoryRegion(lAddress, pLengthInBytes);
      return lAddress;
    }
  }

  /**
   * Checks whether memory of a given size can be allocated given the
   * restriction on max memory allocation.
   *
   * @param pLengthInBytes length in bytes
   * @throws OutOfMemoryError thrown if constraints violated.
   */
  static void checkMaxAllocatableMemory(final long pLengthInBytes) throws OutOfMemoryError
  {
    if (cTotalAllocatedMemory.get() + pLengthInBytes > cMaximumAllocatableMemory.get())
      throw new OutOfMemoryError(String.format("Cannot allocate memory region of length: %d without reaching maximum allocatable memory %d (currently %d bytes are allocated )\n", pLengthInBytes, cMaximumAllocatableMemory.get(), cTotalAllocatedMemory.get()));
  }

  /**
   * Reallocates memory.
   *
   * @param pAddress          memory address
   * @param pNewLengthInBytes new length in bytes
   * @return new pointer
   * @throws InvalidNativeMemoryAccessException thrown if pointer is invalid.
   */
  public static final long reallocateMemory(final long pAddress, final long pNewLengthInBytes) throws InvalidNativeMemoryAccessException
  {
    synchronized (mLock)
    {
      if (cAllocatedMemoryPointers.get(pAddress) == null)
        throw new InvalidNativeMemoryAccessException("Cannot free unallocated memory!");

      final Long lCurrentlyAllocatedLength = cAllocatedMemoryPointers.get(pAddress);
      checkMaxAllocatableMemory(pNewLengthInBytes - lCurrentlyAllocatedLength);

      final long lReallocatedMemoryAddress = cUnsafe.reallocateMemory(pAddress, pNewLengthInBytes);
      if (lReallocatedMemoryAddress != pAddress)
      {
        deregisterMemoryRegion(pAddress);
        registerMemoryRegion(lReallocatedMemoryAddress, pNewLengthInBytes);
      }
      return lReallocatedMemoryAddress;
    }
  }

  /**
   * Returns true if this address and signature corresponds to an allocated
   * pointer. Signatures provide away to distinguish different allocated memory
   * regions and prevent that the garbage collector try to release already
   * released memory.
   *
   * @param pAddress   address
   * @param pSignature signature for given address
   * @return true if memory allocated at given address with given signature.
   */
  public static final boolean isAllocatedMemory(final long pAddress, long pSignature)
  {
    synchronized (mLock)
    {
      final Long lLength = cAllocatedMemoryPointers.get(pAddress);
      final Long lKnownSignature = cAllocatedMemoryPointersSignatures.get(pAddress);
      return (lLength != null) && (lKnownSignature == pSignature);
    }
  }

  /**
   * Returns the signature for a memory region allocated at a given address.
   *
   * @param pAddress address
   * @return signature.
   */
  public static Long getSignature(long pAddress)
  {
    synchronized (mLock)
    {
      final Long lKnownSignature = cAllocatedMemoryPointersSignatures.get(pAddress);
      return lKnownSignature;
    }
  }

  /**
   * Frees memory at a given address.
   *
   * @param pAddress address
   * @throws InvalidNativeMemoryAccessException thrown if no memory is allocated at that address.
   */
  public static final void freeMemory(final long pAddress) throws InvalidNativeMemoryAccessException
  {
    synchronized (mLock)
    {
      if (cAllocatedMemoryPointers.get(pAddress) == null)
        throw new InvalidNativeMemoryAccessException("Cannot free unallocated memory!");
      cUnsafe.freeMemory(pAddress);
      deregisterMemoryRegion(pAddress);
    }
  }

  /**
   * Fast copying of data from a Java array into an off-heap memory region.
   *
   * @param pSrcArray      source array
   * @param pSrcOffset     source offset in elements
   * @param pAddressDest   destination address
   * @param pLengthInBytes length in bytes
   * @throws InvalidNativeMemoryAccessException thrown if the length in bytes is not the same as the size in
   *                                            bytes of the Java array.
   */
  public static final void copyFromArray(final Object pSrcArray, final long pSrcOffset, final long pAddressDest, final long pLengthInBytes) throws InvalidNativeMemoryAccessException
  {
    //synchronized (mLock)
    {
      int lArrayBaseOffset = cUnsafe.arrayBaseOffset(pSrcArray.getClass());

      int lArrayLengthInBytes = Size.ofPrimitive1DArray(pSrcArray);

      if (lArrayLengthInBytes - pSrcOffset < pLengthInBytes)
        throw new InvalidNativeMemoryAccessException(String.format("Incompatible lengths: Array has length %d bytes, given length is %d bytes", lArrayLengthInBytes, pLengthInBytes));

      cUnsafe.copyMemory(pSrcArray, lArrayBaseOffset, null, pAddressDest, pLengthInBytes);
    }
  }

  /**
   * Fast copying of data from an off-heap memory region to a Java array.
   *
   * @param pAddressSrc    source address
   * @param pDstArray      destination array.
   * @param pDstOffset     destination array offset in bytes.
   * @param pLengthInBytes length in bytes
   * @throws InvalidNativeMemoryAccessException thrown if the length in bytes is not the same as the size in
   *                                            bytes of the Java array.
   */
  public static final void copyToArray(final long pAddressSrc, final Object pDstArray, final long pDstOffset, final long pLengthInBytes) throws InvalidNativeMemoryAccessException
  {
    //synchronized (mLock)
    {
      int lArrayBaseOffset = cUnsafe.arrayBaseOffset(pDstArray.getClass());

      int lArrayLengthInBytes = Size.ofPrimitive1DArray(pDstArray);

      if (lArrayLengthInBytes - pDstOffset < pLengthInBytes)
        throw new InvalidNativeMemoryAccessException(String.format("Incompatible lengths: Array has length %d bytes, given length is %d bytes", lArrayLengthInBytes, pLengthInBytes));

      cUnsafe.copyMemory(null, pAddressSrc, pDstArray, lArrayBaseOffset + pDstOffset, pLengthInBytes);
    }
  }

  /**
   * Memory copy between two addresses.
   *
   * @param pAddressOrg    source address
   * @param pAddressDest   destination address
   * @param pLengthInBytes length in bytes
   */
  public static final void copyMemory(final long pAddressOrg, final long pAddressDest, final long pLengthInBytes)
  {
    //synchronized (mLock)
    {
      cUnsafe.copyMemory(pAddressOrg, pAddressDest, pLengthInBytes);
    }
  }

  /**
   * Memory copy between two addresses.
   *
   * @param pAddressOrg    source address
   * @param pAddressDest   destination address
   * @param pLengthInBytes length in bytes
   * @throws InvalidNativeMemoryAccessException thrown if the pointers are invalid or the lengths incompatible.
   */
  public static final void copyMemorySafely(final long pAddressOrg, final long pAddressDest, final long pLengthInBytes) throws InvalidNativeMemoryAccessException
  {
    //synchronized (mLock)
    {
      final Long lLengthOrg = cAllocatedMemoryPointers.get(pAddressOrg);
      if (lLengthOrg == null)
        throw new InvalidNativeMemoryAccessException("Cannot copy from an unallocated memory region!");

      final Long lLengthDest = cAllocatedMemoryPointers.get(pAddressDest);
      if (lLengthDest == null)
        throw new InvalidNativeMemoryAccessException("Cannot copy to an unallocated memory region!");

      if (pLengthInBytes > lLengthOrg)
        throw new InvalidNativeMemoryAccessException(String.format("Cannot copy - source too small! %d < %d)", lLengthOrg, pLengthInBytes));

      if (pLengthInBytes > lLengthDest)
        throw new InvalidNativeMemoryAccessException(String.format("Cannot copy - destination too small! %d < %d)", lLengthDest, pLengthInBytes));/**/

      cUnsafe.copyMemory(pAddressOrg, pAddressDest, pLengthInBytes);
    }
  }

  /**
   * Fills a given memory region with a given byte value (repeated).
   *
   * @param pAddress       memory region address
   * @param pLengthInBytes length in bytes
   * @param pValue         byte value
   */
  public static final void fillMemory(final long pAddress, final long pLengthInBytes, final byte pValue)
  {
    //synchronized (mLock)
    {
      cUnsafe.setMemory(pAddress, pLengthInBytes, pValue);
    }
  }

  /**
   * Fills a given memory region with a given byte value (repeated).
   *
   * @param pAddress       memory region address
   * @param pLengthInBytes length in bytes
   * @param pValue         byte value
   * @throws InvalidNativeMemoryAccessException thrown if the pointer is invalid or the lengths incompatible.
   */
  public static final void setMemorySafely(final long pAddress, final long pLengthInBytes, final byte pValue) throws InvalidNativeMemoryAccessException
  {
    //synchronized (mLock)
    {
      final Long lLength = cAllocatedMemoryPointers.get(pAddress);
      if (lLength == null) throw new InvalidNativeMemoryAccessException("Cannot set unallocated memory region!");

      if (pLengthInBytes > lLength)
        throw new InvalidNativeMemoryAccessException(String.format("Cannot set - memory region too small! %d < %d)", lLength, pLengthInBytes));/**/

      cUnsafe.setMemory(pAddress, pLengthInBytes, pValue);
    }
  }

  /**
   * Store reordering fence
   */
  public static final void storeReorderingFence()
  {
    cUnsafe.storeFence();
  }

  /**
   * Load reordering fence
   */
  public static final void loadReorderingFence()
  {
    cUnsafe.loadFence();
  }

  /**
   * Full reordering fence
   */
  public static final void fullReorderingFence()
  {
    cUnsafe.fullFence();
  }/**/

  /**
   * Returns value at given address.
   *
   * @param pAddress address
   * @return value
   */
  public static final byte getByte(final long pAddress)
  {
    return cUnsafe.getByte(pAddress);
  }

  /**
   * Returns value at given address.
   *
   * @param pAddress address
   * @return value
   */
  public static final char getChar(final long pAddress)
  {
    return cUnsafe.getChar(pAddress);
  }

  /**
   * Returns value at given address.
   *
   * @param pAddress address
   * @return value
   */
  public static final short getShort(final long pAddress)
  {
    return cUnsafe.getShort(pAddress);
  }

  /**
   * Returns value at given address.
   *
   * @param pAddress address
   * @return value
   */
  public static final int getInt(final long pAddress)
  {
    return cUnsafe.getInt(pAddress);
  }

  /**
   * Returns value at given address.
   *
   * @param pAddress address
   * @return value
   */
  public static final long getLong(final long pAddress)
  {
    return cUnsafe.getLong(pAddress);
  }

  /**
   * Returns value at given address.
   *
   * @param pAddress address
   * @return value
   */
  public static final float getFloat(final long pAddress)
  {
    return cUnsafe.getFloat(pAddress);
  }

  /**
   * Returns value at given address.
   *
   * @param pAddress address
   * @return value
   */
  public static final double getDouble(final long pAddress)
  {
    return cUnsafe.getDouble(pAddress);
  }

  /**
   * Sets value at given address.
   *
   * @param pAddress address
   * @param pValue   value
   */
  public static final void setByte(final long pAddress, final byte pValue)
  {
    cUnsafe.putByte(pAddress, pValue);
  }

  /**
   * Sets value at given address.
   *
   * @param pAddress address
   * @param pValue   value
   */
  public static final void setChar(final long pAddress, final char pValue)
  {
    cUnsafe.putChar(pAddress, pValue);
  }

  /**
   * Sets value at given address.
   *
   * @param pAddress address
   * @param pValue   value
   */
  public static final void setShort(final long pAddress, final short pValue)
  {
    cUnsafe.putShort(pAddress, pValue);
  }

  /**
   * Sets value at given address.
   *
   * @param pAddress address
   * @param pValue   value
   */
  public static final void setInt(final long pAddress, final int pValue)
  {
    cUnsafe.putInt(pAddress, pValue);
  }

  /**
   * Sets value at given address.
   *
   * @param pAddress address
   * @param pValue   value
   */
  public static final void setLong(final long pAddress, final long pValue)
  {
    cUnsafe.putLong(pAddress, pValue);
  }

  /**
   * Sets value at given address.
   *
   * @param pAddress address
   * @param pValue   value
   */
  public static final void setFloat(final long pAddress, final float pValue)
  {
    cUnsafe.putFloat(pAddress, pValue);
  }

  /**
   * Sets value at given address.
   *
   * @param pAddress address
   * @param pValue   value
   */
  public static final void setDouble(final long pAddress, final double pValue)
  {
    cUnsafe.putDouble(pAddress, pValue);
  }

  /**
   * Frees all allocated off-heap memory. Highly non-recommended as this will
   * invalidate memory that other objects might rely upon.
   */
  public static void freeAll()
  {
    for (final Map.Entry<Long, Long> lEntry : cAllocatedMemoryPointers.entrySet())
    {
      final long lAddress = lEntry.getKey();
      freeMemory(lAddress);
    }
  }

}
