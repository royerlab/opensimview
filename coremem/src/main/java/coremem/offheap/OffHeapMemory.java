package coremem.offheap;

import coremem.ContiguousMemoryInterface;
import coremem.MemoryBase;
import coremem.enums.MemoryType;
import coremem.exceptions.InvalidNativeMemoryAccessException;
import coremem.exceptions.UnsupportedMemoryResizingException;
import coremem.interfaces.Resizable;
import coremem.interop.NIOBuffersInterop;
import coremem.rgc.Cleaner;
import coremem.rgc.RessourceCleaner;
import coremem.util.Size;

/**
 * Instances of this class represent contguous regions of off-heap memory.
 *
 * @author royer
 */
public class OffHeapMemory extends MemoryBase implements
                           Resizable,
                           ContiguousMemoryInterface

{
  protected StackTraceElement[] mAllocationStackTrace =
                                                      new StackTraceElement[]
                                                      { new StackTraceElement("NULL", "NULL", "NULL", -1) };
  protected String mName = "NOTDEFINED";
  protected Long mSignature;
  protected Object mParent = null;

  /**
   * Wraps a 'raw' pointer i.e. a long pointer value and a length. This is used
   * to wrap a non-coremem memory region. A parent can be given, to prevent its
   * garbage collection and the possible release of the underlying memory
   * resource.
   *
   * @param pParent
   *          parent reference to prevent the parent's garbage collection.
   * @param pAddress
   *          address
   * @param pLengthInBytes
   *          length in bytes
   * @return off-heap memory object
   */
  public static final OffHeapMemory wrapPointer(final Object pParent,
                                                final long pAddress,
                                                final long pLengthInBytes)
  {
    return wrapPointer("WRAPNULL", pParent, pAddress, pLengthInBytes);
  }

  ;

  /**
   * Wraps a 'raw' pointer i.e. a long pointer value and a length. This is used
   * to wrap a non-coremem memory region. A parent can be given, to prevent its
   * garbage collection and the possible release of the underlying memory
   * resource.
   *
   * @param pName
   *          memory region name
   * @param pParent
   *          parent reference to prevent the parent's garbage collection.
   * @param pAddress
   *          address
   * @param pLengthInBytes
   *          length in bytes
   * @return off-heap memory object
   */
  public static final OffHeapMemory wrapPointer(final String pName,
                                                final Object pParent,
                                                final long pAddress,
                                                final long pLengthInBytes)
  {
    return new OffHeapMemory(pName,
                             pParent,
                             pAddress,
                             pLengthInBytes);
  }

  ;

  /**
   * Wraps a JNA pointer.
   *
   * @param pJNAPointer
   *          JNA pointer
   * @return off-heap memory object
   */
  public static OffHeapMemory wrapPointer(com.sun.jna.Pointer pJNAPointer,
                                          long pTargetSizeInBytes)
  {
    long lAddress = com.sun.jna.Pointer.nativeValue(pJNAPointer);
    return wrapPointer(pJNAPointer.toString(),
                       pJNAPointer,
                       lAddress,
                       pTargetSizeInBytes);
  }

  /**
   * In an ideal world, this would wraps a JNA memory or pointer. But, because
   * of a fatal design flaw in JNA: no automatic freeing on garbage collection.
   * They do use 'finalise' horror! this is highly discouraged and a mostly
   * deprecated feature of the JVM, it kills GC performance, why are they doing
   * this?
   * <p>
   * This static method here is meant to be educative and a deterrent to this
   * pattern. It should not be used, cannot be used and is not functional
   * anyway. You should always use CoreMem to allocate memory, because CoreMem
   * does the right thing by freeing memory upon garbage collection. If you need
   * a JNA memory, allocate a OffHeapMemory and pass a JNA pointer with
   * getJNAPointer(). If you don't use this pattern, then you have to free the
   *
   * @param pJNAMemory
   *          JNA memory
   * @return off-heap memory object
   */
  @Deprecated
  public static OffHeapMemory wrapPointer(com.sun.jna.Memory pJNAMemory)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Wraps a bridj pointer.
   *
   * @param pBridJPointer
   *          BridJ pointer
   * @return off-heap memory object
   */
  public static OffHeapMemory wrapPointer(org.bridj.Pointer<Byte> pBridJPointer)
  {
    long lAddress = org.bridj.Pointer.getPeer(pBridJPointer);
    long lTargetSizeInBytes = pBridJPointer.getTargetSize();
    return wrapPointer(pBridJPointer.toString(),
                       pBridJPointer,
                       lAddress,
                       lTargetSizeInBytes);
  }

  /**
   * Wraps a NIO buffer.
   *
   * @param pBuffer
   *          NIO buffer
   * @return off-heap memory object
   */
  public static final OffHeapMemory wrapBuffer(final java.nio.Buffer pBuffer)
  {
    return NIOBuffersInterop.getContiguousMemoryFrom(pBuffer);
  }

  ;

  /**
   * Creates a off-heap memory object initialized by copying the contents of a
   * byte array.
   *
   * @param pBuffer
   *          buffer to copy contents from
   * @return off-heap memory object
   */
  public static final OffHeapMemory copyFromArray(final byte[] pBuffer)
  {
    OffHeapMemory lOffHeapMemory =
                                 OffHeapMemory.allocateBytes(pBuffer.length);
    lOffHeapMemory.copyFrom(pBuffer);
    return lOffHeapMemory;
  }

  ;

  /**
   * Allocates off-heap memory that can hold a given number of bytes.
   *
   * @param pNumberOfBytes
   *          number of bytes
   * @return off-heap memory object
   */
  public static OffHeapMemory allocateBytes(long pNumberOfBytes)
  {
    return new OffHeapMemory(pNumberOfBytes * Size.BYTE);
  }

  /**
   * Allocates off-heap memory that can hold a given number of chars.
   *
   * @param pNumberOfChars
   *          number of chars
   * @return off-heap memory object
   */
  public static OffHeapMemory allocateChars(long pNumberOfChars)
  {
    return new OffHeapMemory(pNumberOfChars * Size.CHAR);
  }

  /**
   * Allocates off-heap memory that can hold a given number of shorts.
   *
   * @param pNumberOfShorts
   *          number of shorts
   * @return off-heap memory object
   */
  public static OffHeapMemory allocateShorts(long pNumberOfShorts)
  {
    return new OffHeapMemory(pNumberOfShorts * Size.SHORT);
  }

  /**
   * Allocates off-heap memory that can hold a given number of ints.
   *
   * @param pNumberOfInts
   *          number of ints
   * @return off-heap memory object
   */
  public static OffHeapMemory allocateInts(long pNumberOfInts)
  {
    return new OffHeapMemory(pNumberOfInts * Size.INT);
  }

  /**
   * Allocates off-heap memory that can hold a given number of longs.
   *
   * @param pNumberOfLongs
   *          number of longs
   * @return off-heap memory object
   */
  public static OffHeapMemory allocateLongs(long pNumberOfLongs)
  {
    return new OffHeapMemory(pNumberOfLongs * Size.LONG);
  }

  /**
   * Allocates off-heap memory that can hold a given number of floats.
   *
   * @param pNumberOfFloats
   *          number of floats
   * @return off-heap memory object
   */
  public static OffHeapMemory allocateFloats(long pNumberOfFloats)
  {
    return new OffHeapMemory(pNumberOfFloats * Size.FLOAT);
  }

  /**
   * Allocates off-heap memory that can hold a given number of doubles.
   *
   * @param pNumberOfDoubles
   *          number of doubles
   * @return off-heap memory object
   */
  public static OffHeapMemory allocateDoubles(long pNumberOfDoubles)
  {
    return new OffHeapMemory(pNumberOfDoubles * Size.DOUBLE);
  }

  /**
   * Allocates off-heap memory that can hold a given number of bytes.
   *
   * @param pName
   *          name (can be used to track allocation origin)
   * @param pNumberOfBytes
   *          number of bytes
   * @return off-heap memory object
   */
  public static OffHeapMemory allocateBytes(String pName,
                                            long pNumberOfBytes)
  {
    return new OffHeapMemory(pName, pNumberOfBytes * Size.BYTE);
  }

  /**
   * Allocates off-heap memory that can hold a given number of chars.
   *
   * @param pName
   *          name (can be used to track allocation origin)
   * @param pNumberOfChars
   *          number of chars
   * @return off-heap memory object
   */
  public static OffHeapMemory allocateChars(String pName,
                                            long pNumberOfChars)
  {
    return new OffHeapMemory(pName, pNumberOfChars * Size.CHAR);
  }

  /**
   * Allocates off-heap memory that can hold a given number of shorts.
   *
   * @param pName
   *          name (can be used to track allocation origin)
   * @param pNumberOfShorts
   *          number of shorts
   * @return off-heap memory object
   */
  public static OffHeapMemory allocateShorts(String pName,
                                             long pNumberOfShorts)
  {
    return new OffHeapMemory(pName, pNumberOfShorts * Size.SHORT);
  }

  /**
   * Allocates off-heap memory that can hold a given number of ints.
   *
   * @param pName
   *          name (can be used to track allocation origin)
   * @param pNumberOfInts
   *          number of ints
   * @return off-heap memory object
   */
  public static OffHeapMemory allocateInts(String pName,
                                           long pNumberOfInts)
  {
    return new OffHeapMemory(pName, pNumberOfInts * Size.INT);
  }

  /**
   * Allocates off-heap memory that can hold a given number of longs.
   *
   * @param pName
   *          name (can be used to track allocation origin)
   * @param pNumberOfLongs
   *          number of longs
   * @return off-heap memory object
   */
  public static OffHeapMemory allocateLongs(String pName,
                                            long pNumberOfLongs)
  {
    return new OffHeapMemory(pName, pNumberOfLongs * Size.LONG);
  }

  /**
   * Allocates off-heap memory that can hold a given number of floats.
   *
   * @param pName
   *          name (can be used to track allocation origin)
   * @param pNumberOfFloats
   *          number of floats
   * @return off-heap memory object
   */
  public static OffHeapMemory allocateFloats(String pName,
                                             long pNumberOfFloats)
  {
    return new OffHeapMemory(pName, pNumberOfFloats * Size.FLOAT);
  }

  /**
   * Allocates off-heap memory that can hold a given number of doubles.
   *
   * @param pName
   *          name (can be used to track allocation origin)
   * @param pNumberOfDoubles
   *          number of doubles
   * @return off-heap memory object
   */
  public static OffHeapMemory allocateDoubles(String pName,
                                              long pNumberOfDoubles)
  {
    return new OffHeapMemory(pName, pNumberOfDoubles * Size.DOUBLE);
  }

  /**
   * Allocates page=aligned off-heap memory that can hold a given number of
   * bytes.
   *
   * @param pName
   *          name (can be used to track allocation origin)
   * @param pNumberOfBytes
   *          number of bytes
   * @return off-heap memory object
   */
  public static OffHeapMemory allocatePageAlignedBytes(String pName,
                                                       long pNumberOfBytes)
  {
    long lPageSize = OffHeapMemoryAccess.getPageSize();
    return allocateAlignedBytes(pName, pNumberOfBytes, lPageSize);
  }

  /**
   * Allocates off-heap memory that can hold a given number of bytes, with a
   * given alignment.
   *
   * @param pName
   *          name (can be used to track allocation origin)
   * @param pNumberOfBytes
   *          number of bytes
   * @param pAlignment
   *          byte boundary to align to.
   * @return off-heap memory object
   */
  public static OffHeapMemory allocateAlignedBytes(String pName,
                                                   long pNumberOfBytes,
                                                   long pAlignment)
  {
    if (pAlignment == 0)
      return allocateBytes(pName, pNumberOfBytes);

    long lNumberOfBytesWithPadding = pNumberOfBytes + pAlignment;

    OffHeapMemory lAllocatedBytesWithPadding =
                                             allocateBytes(pName,
                                                           lNumberOfBytesWithPadding);

    long lOffset =
                 pAlignment - (lAllocatedBytesWithPadding.getAddress()
                               % pAlignment);

    OffHeapMemory lAlignedRegion =
                                 lAllocatedBytesWithPadding.subRegion(lOffset,
                                                                      pNumberOfBytes);

    return lAlignedRegion;
  }

  /**
   * Allocates an off-heap memory region of given length in bytes.
   *
   * @param pLengthInBytes
   *          length in bytes
   */
  public OffHeapMemory(final long pLengthInBytes)
  {
    this(null, pLengthInBytes);
  }

  /**
   * Allocates an off-heap memory region of given name and length in bytes.
   *
   * @param pName
   *          name
   * @param pLengthInBytes
   *          length in bytes
   */
  public OffHeapMemory(final String pName, final long pLengthInBytes)
  {
    this(pName,
         null,
         OffHeapMemoryAccess.allocateMemory(pLengthInBytes),
         pLengthInBytes);
  }

  /**
   * Warps an off-heap memory region of given parent, address, and length in
   * bytes.
   *
   * @param pParent
   *          parent
   * @param pAddress
   *          address
   * @param pLengthInBytes
   *          length in bytes
   */
  public OffHeapMemory(final Object pParent,
                       final long pAddress,
                       final long pLengthInBytes)
  {
    this("NULL", pParent, pAddress, pLengthInBytes);
  }

  /**
   * Warps an off-heap memory region of given name, parent, address, and length
   * in bytes.
   *
   * @param pName
   *          name
   * @param pParent
   *          parent
   * @param pAddress
   *          address
   * @param pLengthInBytes
   *          length in bytes
   */
  public OffHeapMemory(final String pName,
                       final Object pParent,
                       final long pAddress,
                       final long pLengthInBytes)
  {
    super(pAddress, pLengthInBytes);
    mName = pName == null ? "NULL" : pName.intern();
    mParent = pParent;
    mAllocationStackTrace = Thread.currentThread().getStackTrace();
    mSignature = OffHeapMemoryAccess.getSignature(getAddress());

    if (mParent == null)
      RessourceCleaner.register(this);
  }

  /* (non-Javadoc)
   * @see coremem.ContiguousMemoryInterface#subRegion(long, long)
   */
  @Override
  public OffHeapMemory subRegion(final long pOffset,
                                 final long pLenghInBytes)
  {
    if (mAddressInBytes + pOffset + pLenghInBytes > mAddressInBytes
                                                    + mLengthInBytes)
      throw new InvalidNativeMemoryAccessException(String.format("Cannot instanciate OffHeapMemory on subregion staring at offset %d and length %d  ",
                                                                 pOffset,
                                                                 pLenghInBytes));
    final OffHeapMemory lOffHeapMemory =
                                       new OffHeapMemory(this,
                                                         mAddressInBytes
                                                               + pOffset,
                                                         pLenghInBytes);
    return lOffHeapMemory;
  }

  /* (non-Javadoc)
   * @see coremem.MemoryBase#getMemoryType()
   */
  @Override
  public MemoryType getMemoryType()
  {
    complainIfFreed();
    return MemoryType.CPURAMDIRECT;
  }

  /* (non-Javadoc)
   * @see coremem.interfaces.Resizable#resize(long)
   */
  @Override
  public long resize(long pNewLength)
  {
    complainIfFreed();
    if (mParent != null)
      throw new UnsupportedMemoryResizingException("Cannot resize externally allocated memory region!");
    try
    {
      mAddressInBytes =
                      OffHeapMemoryAccess.reallocateMemory(mAddressInBytes,
                                                           pNewLength);
      mLengthInBytes = pNewLength;
    }
    catch (final Throwable e)
    {
      final String lErrorMessage =
                                 String.format("Could not resize memory region from %d to %d ",
                                               mLengthInBytes,
                                               pNewLength);
      // error("KAM", lErrorMessage);
      throw new UnsupportedMemoryResizingException(lErrorMessage, e);
    }
    return mLengthInBytes;
  }

  /* (non-Javadoc)
   * @see coremem.MemoryBase#free()
   */
  @Override
  public void free()
  {
    if (mParent == null && mAddressInBytes != 0)
    {
      OffHeapMemoryAccess.freeMemory(mAddressInBytes);
    }
    mAddressInBytes = 0;
    mParent = null;
    super.free();
  }

  /* (non-Javadoc)
   * @see coremem.rgc.Cleanable#getCleaner()
   */
  @Override
  public Cleaner getCleaner()
  {
    if (mParent != null)
      return new OffHeapMemoryCleaner(null,
                                      mSignature,
                                      mName,
                                      mAllocationStackTrace);
    return new OffHeapMemoryCleaner(mAddressInBytes,
                                    mSignature,
                                    mName,
                                    mAllocationStackTrace);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return "OffHeapMemory [mParent=" + mParent
           + ", mAddressInBytes="
           + mAddressInBytes
           + ", mLengthInBytes="
           + mLengthInBytes
           + ", mIsFree="
           + mIsFree
           + ", getMemoryType()="
           + getMemoryType()
           + "]";
  }

}
