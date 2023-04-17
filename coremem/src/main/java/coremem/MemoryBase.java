package coremem;

import coremem.enums.MemoryType;
import coremem.exceptions.InvalidNativeMemoryAccessException;
import coremem.exceptions.InvalidWriteAtReadOnly;
import coremem.exceptions.MemoryMapException;
import coremem.interfaces.MappableMemory;
import coremem.interfaces.PointerAccessible;
import coremem.interfaces.RangeCopyable;
import coremem.interfaces.SizedInBytes;
import coremem.interop.BridJInterop;
import coremem.interop.JNAInterop;
import coremem.interop.NIOBuffersInterop;
import coremem.offheap.OffHeapMemory;
import coremem.offheap.OffHeapMemoryAccess;
import coremem.rgc.Cleanable;
import coremem.rgc.Freeable;
import coremem.rgc.FreeableBase;
import coremem.util.Size;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * This abstract base class offers basic functionality for off-heap memory
 * access, copying, sizing, and memory life-cycle management and garbage
 * collection.
 *
 * @author royer
 */
public abstract class MemoryBase extends FreeableBase implements PointerAccessible, SizedInBytes, ContiguousMemoryInterface, RangeCopyable<MemoryBase>, Freeable, Cleanable

{

  protected long mAddressInBytes = 0;
  protected long mLengthInBytes = 0;
  protected boolean mIsFree = false;

  /**
   * Protected parameterless constructor
   */
  protected MemoryBase()
  {
  }

  /**
   * Constructs a MemoryBase given an address and length (absolute and all in
   * bytes).
   *
   * @param pAddressInBytes absolute address in bytes
   * @param pLengtInBytes   length in bytes.
   */
  public MemoryBase(long pAddressInBytes, long pLengtInBytes)
  {
    mAddressInBytes = pAddressInBytes;
    mLengthInBytes = pLengtInBytes;
  }

  @Override
  public abstract MemoryType getMemoryType();

  @Override
  public long getSizeInBytes()
  {
    complainIfFreed();
    return mLengthInBytes;
  }

  @Override
  public void copyFrom(ContiguousMemoryInterface pFrom)
  {
    pFrom.copyTo(this);
  }

  @Override
  public void copyTo(ContiguousMemoryInterface pTo)
  {
    complainIfFreed();
    checkMappableMemory(pTo);
    checkMappableMemory(this);

    if (this.getSizeInBytes() != pTo.getSizeInBytes())
    {
      final String lErrorString = String.format("Attempted to copy memory regions of different sizes: src.len=%d, dst.len=%d", this.getSizeInBytes(), pTo.getSizeInBytes());
      // error("KAM", lErrorString);
      throw new InvalidNativeMemoryAccessException(lErrorString);
    }
    OffHeapMemoryAccess.copyMemory(this.getAddress(), pTo.getAddress(), pTo.getSizeInBytes());
  }

  /* (non-Javadoc)
   * @see coremem.interfaces.RangeCopyable#copyRangeTo(long, java.lang.Object, long, long)
   */
  @Override
  public void copyRangeTo(long pSourceOffset, MemoryBase pTo, long pDestinationOffset, long pLengthToCopy)
  {
    complainIfFreed();
    checkMappableMemory(pTo);
    checkMappableMemory(this);

    if (pLengthToCopy == 0) return;

    final long lSrcAddress = this.getAddress();
    final long lDstAddress = pTo.getAddress();

    final long lSrcLength = this.getSizeInBytes();
    final long lDstLength = pTo.getSizeInBytes();

    if ((pLengthToCopy < 0))
    {
      final String lErrorString = "Attempted to copy a region of negative length!";
      // error("KAM", lErrorString);
      throw new InvalidNativeMemoryAccessException(lErrorString);
    }

    if ((pLengthToCopy + pSourceOffset > lSrcLength))
    {
      final String lErrorString = "Attempted to read past the end of the source memory region";
      // error("KAM", lErrorString);
      throw new InvalidNativeMemoryAccessException(lErrorString);
    }

    if ((pLengthToCopy + pDestinationOffset > lDstLength))
    {
      final String lErrorString = "Attempted to writing past the end of the destination memory region";
      // error("KAM", lErrorString);
      throw new InvalidNativeMemoryAccessException(lErrorString);
    }

    final long lSrcCopyAddress = lSrcAddress + pSourceOffset;
    final long lDstCopyAddress = lDstAddress + pDestinationOffset;

    OffHeapMemoryAccess.copyMemory(lSrcCopyAddress, lDstCopyAddress, pLengthToCopy);
  }

  /**
   * Checks whether the provided ContiguousMemoryInterface is mappable and
   * currently mapped.
   *
   * @param pContiguousMemoryInterface
   */
  void checkMappableMemory(ContiguousMemoryInterface pContiguousMemoryInterface)
  {
    if (pContiguousMemoryInterface instanceof MappableMemory)
    {
      final MappableMemory lMappableMemory = (MappableMemory) pContiguousMemoryInterface;
      if (!lMappableMemory.isCurrentlyMapped()) throw new MemoryMapException("Memory is not mapped!");
    }
  }

  @Override
  public long getAddress()
  {
    complainIfFreed();
    checkMappableMemory(this);
    return mAddressInBytes;
  }

  @Override
  public void free()
  {
    mIsFree = true;
  }

  @Override
  public boolean isFree()
  {
    return mIsFree;
  }

  @Override
  public void setByteAligned(long pOffset, byte pValue)
  {
    OffHeapMemoryAccess.setByte(mAddressInBytes + (pOffset << Size.BYTESHIFT), pValue);
  }

  @Override
  public void setCharAligned(long pOffset, char pValue)
  {
    OffHeapMemoryAccess.setChar(mAddressInBytes + (pOffset << Size.CHARSHIFT), pValue);
  }

  @Override
  public void setShortAligned(long pOffset, short pValue)
  {
    OffHeapMemoryAccess.setShort(mAddressInBytes + (pOffset << Size.SHORTSHIFT), pValue);
  }

  @Override
  public void setIntAligned(long pOffset, int pValue)
  {
    OffHeapMemoryAccess.setInt(mAddressInBytes + (pOffset << Size.INTSHIFT), pValue);
  }

  @Override
  public void setLongAligned(long pOffset, long pValue)
  {
    OffHeapMemoryAccess.setLong(mAddressInBytes + (pOffset << Size.LONGSHIFT), pValue);
  }

  @Override
  public void setFloatAligned(long pOffset, float pValue)
  {
    OffHeapMemoryAccess.setFloat(mAddressInBytes + (pOffset << Size.FLOATSHIFT), pValue);
  }

  @Override
  public void setDoubleAligned(long pOffset, double pValue)
  {
    OffHeapMemoryAccess.setDouble(mAddressInBytes + (pOffset << Size.DOUBLESHIFT), pValue);
  }

  @Override
  public byte getByteAligned(long pOffset)
  {
    return OffHeapMemoryAccess.getByte(mAddressInBytes + (pOffset << Size.BYTESHIFT));
  }

  @Override
  public char getCharAligned(long pOffset)
  {
    return OffHeapMemoryAccess.getChar(mAddressInBytes + (pOffset << Size.CHARSHIFT));
  }

  @Override
  public short getShortAligned(long pOffset)
  {
    return OffHeapMemoryAccess.getShort(mAddressInBytes + (pOffset << Size.SHORTSHIFT));
  }

  @Override
  public int getIntAligned(long pOffset)
  {
    return OffHeapMemoryAccess.getInt(mAddressInBytes + (pOffset << Size.INTSHIFT));
  }

  @Override
  public long getLongAligned(long pOffset)
  {
    return OffHeapMemoryAccess.getLong(mAddressInBytes + (pOffset << Size.LONGSHIFT));
  }

  @Override
  public float getFloatAligned(long pOffset)
  {
    return OffHeapMemoryAccess.getFloat(mAddressInBytes + (pOffset << Size.FLOATSHIFT));
  }

  @Override
  public double getDoubleAligned(long pOffset)
  {
    return OffHeapMemoryAccess.getDouble(mAddressInBytes + (pOffset << Size.DOUBLESHIFT));
  }

  @Override
  public void setByte(long pOffset, byte pValue)
  {
    OffHeapMemoryAccess.setByte(mAddressInBytes + pOffset, pValue);
  }

  @Override
  public void setChar(long pOffset, char pValue)
  {
    OffHeapMemoryAccess.setChar(mAddressInBytes + Size.CHARSHIFT, pValue);
  }

  @Override
  public void setShort(long pOffset, short pValue)
  {
    OffHeapMemoryAccess.setShort(mAddressInBytes + pOffset, pValue);
  }

  @Override
  public void setInt(long pOffset, int pValue)
  {
    OffHeapMemoryAccess.setInt(mAddressInBytes + pOffset, pValue);
  }

  @Override
  public void setLong(long pOffset, long pValue)
  {
    OffHeapMemoryAccess.setLong(mAddressInBytes + pOffset, pValue);
  }

  @Override
  public void setFloat(long pOffset, float pValue)
  {
    OffHeapMemoryAccess.setFloat(mAddressInBytes + pOffset, pValue);
  }

  @Override
  public void setDouble(long pOffset, double pValue)
  {
    OffHeapMemoryAccess.setDouble(mAddressInBytes + pOffset, pValue);
  }

  @Override
  public byte getByte(long pOffset)
  {
    return OffHeapMemoryAccess.getByte(mAddressInBytes + pOffset);
  }

  @Override
  public char getChar(long pOffset)
  {
    return OffHeapMemoryAccess.getChar(mAddressInBytes + pOffset);
  }

  @Override
  public short getShort(long pOffset)
  {
    return OffHeapMemoryAccess.getShort(mAddressInBytes + pOffset);
  }

  @Override
  public int getInt(long pOffset)
  {
    return OffHeapMemoryAccess.getInt(mAddressInBytes + pOffset);
  }

  @Override
  public long getLong(long pOffset)
  {
    return OffHeapMemoryAccess.getLong(mAddressInBytes + pOffset);
  }

  @Override
  public float getFloat(long pOffset)
  {
    return OffHeapMemoryAccess.getFloat(mAddressInBytes + pOffset);
  }

  @Override
  public double getDouble(long pOffset)
  {
    return OffHeapMemoryAccess.getDouble(mAddressInBytes + pOffset);
  }

  @Override
  public void copyFrom(Buffer pBuffer)
  {
    complainIfFreed();
    if (!pBuffer.isDirect())
    {
      if (pBuffer instanceof ByteBuffer)
      {
        final byte[] lByteArray = (byte[]) pBuffer.array();
        this.copyFrom(lByteArray);
      }
    } else
    {
      final OffHeapMemory lContiguousMemoryFrom = NIOBuffersInterop.getContiguousMemoryFrom(pBuffer);
      this.copyFrom(lContiguousMemoryFrom);
    }
  }

  @Override
  public void copyTo(Buffer pBuffer)
  {
    complainIfFreed();

    if (pBuffer.isReadOnly()) throw new InvalidWriteAtReadOnly("Cannot write to read-only buffer!");
    if (!pBuffer.isDirect())
    {
      if (pBuffer instanceof ByteBuffer)
      {
        final byte[] lByteArray = (byte[]) pBuffer.array();
        this.copyFrom(lByteArray);
      }
    } else
    {
      final OffHeapMemory lContiguousMemoryFrom = NIOBuffersInterop.getContiguousMemoryFrom(pBuffer);
      this.copyTo(lContiguousMemoryFrom);
    }

  }

  @Override
  public long writeBytesToFileChannel(FileChannel pFileChannel, long pFilePositionInBytes) throws IOException
  {
    complainIfFreed();
    return writeBytesToFileChannel(0, pFileChannel, pFilePositionInBytes, getSizeInBytes());
  }

  @Override
  public void copyTo(byte[] pTo)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyToArray(getAddress(), pTo, 0, getSizeInBytes());
  }

  @Override
  public void copyTo(short[] pTo)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyToArray(getAddress(), pTo, 0, getSizeInBytes());
  }

  @Override
  public void copyTo(char[] pTo)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyToArray(getAddress(), pTo, 0, getSizeInBytes());
  }

  @Override
  public void copyTo(int[] pTo)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyToArray(getAddress(), pTo, 0, getSizeInBytes());
  }

  @Override
  public void copyTo(long[] pTo)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyToArray(getAddress(), pTo, 0, getSizeInBytes());
  }

  @Override
  public void copyTo(float[] pTo)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyToArray(getAddress(), pTo, 0, getSizeInBytes());
  }

  @Override
  public void copyTo(double[] pTo)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyToArray(getAddress(), pTo, 0, getSizeInBytes());
  }

  @Override
  public void copyFrom(byte[] pFrom)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyFromArray(pFrom, 0, getAddress(), getSizeInBytes());
  }

  @Override
  public void copyFrom(short[] pFrom)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyFromArray(pFrom, 0, getAddress(), getSizeInBytes());
  }

  @Override
  public void copyFrom(char[] pFrom)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyFromArray(pFrom, 0, getAddress(), getSizeInBytes());
  }

  @Override
  public void copyFrom(int[] pFrom)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyFromArray(pFrom, 0, getAddress(), getSizeInBytes());
  }

  @Override
  public void copyFrom(long[] pFrom)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyFromArray(pFrom, 0, getAddress(), getSizeInBytes());
  }

  @Override
  public void copyFrom(float[] pFrom)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyFromArray(pFrom, 0, getAddress(), getSizeInBytes());
  }

  @Override
  public void copyFrom(double[] pFrom)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyFromArray(pFrom, 0, getAddress(), getSizeInBytes());
  }

  @Override
  public void copyTo(byte[] pTo, long pSrcOffset, int pDstOffset, int pLength)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyToArray(getAddress() + pSrcOffset * (long) Size.BYTE, pTo, pDstOffset * (long) Size.BYTE, pLength * (long) Size.BYTE);
  }

  @Override
  public void copyTo(short[] pTo, long pSrcOffset, int pDstOffset, int pLength)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyToArray(getAddress() + pSrcOffset * (long) Size.SHORT, pTo, pDstOffset * (long) Size.SHORT, pLength * (long) Size.SHORT);
  }

  @Override
  public void copyTo(char[] pTo, long pSrcOffset, int pDstOffset, int pLength)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyToArray(getAddress() + pSrcOffset * (long) Size.CHAR, pTo, pDstOffset * (long) Size.CHAR, pLength * (long) Size.CHAR);
  }

  @Override
  public void copyTo(int[] pTo, long pSrcOffset, int pDstOffset, int pLength)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyToArray(getAddress() + pSrcOffset * (long) Size.INT, pTo, pDstOffset * (long) Size.INT, pLength * (long) Size.INT);
  }

  @Override
  public void copyTo(long[] pTo, long pSrcOffset, int pDstOffset, int pLength)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyToArray(getAddress() + pSrcOffset * (long) Size.LONG, pTo, pDstOffset * (long) Size.LONG, pLength * (long) Size.LONG);
  }

  @Override
  public void copyTo(float[] pTo, long pSrcOffset, int pDstOffset, int pLength)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyToArray(getAddress() + pSrcOffset * (long) Size.FLOAT, pTo, pDstOffset * (long) Size.FLOAT, pLength * (long) Size.FLOAT);
  }

  @Override
  public void copyTo(double[] pTo, long pSrcOffset, int pDstOffset, int pLength)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyToArray(getAddress() + pSrcOffset * (long) Size.DOUBLE, pTo, pDstOffset * (long) Size.DOUBLE, pLength * (long) Size.DOUBLE);
  }

  @Override
  public void copyFrom(byte[] pFrom, int pSrcOffset, long pDstOffset, int pLength)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyFromArray(pFrom, pSrcOffset, getAddress() + pDstOffset * (long) Size.BYTE, pLength * (long) Size.BYTE);
  }

  @Override
  public void copyFrom(short[] pFrom, int pSrcOffset, long pDstOffset, int pLength)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyFromArray(pFrom, pSrcOffset, getAddress() + pDstOffset * (long) Size.SHORT, pLength * (long) Size.SHORT);
  }

  @Override
  public void copyFrom(char[] pFrom, int pSrcOffset, long pDstOffset, int pLength)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyFromArray(pFrom, pSrcOffset, getAddress() + pDstOffset * (long) Size.CHAR, pLength * (long) Size.CHAR);
  }

  @Override
  public void copyFrom(int[] pFrom, int pSrcOffset, long pDstOffset, int pLength)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyFromArray(pFrom, pSrcOffset, getAddress() + pDstOffset * (long) Size.INT, pLength * (long) Size.INT);
  }

  @Override
  public void copyFrom(long[] pFrom, int pSrcOffset, long pDstOffset, int pLength)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyFromArray(pFrom, pSrcOffset, getAddress() + pDstOffset * (long) Size.LONG, pLength * (long) Size.LONG);
  }

  @Override
  public void copyFrom(float[] pFrom, int pSrcOffset, long pDstOffset, int pLength)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyFromArray(pFrom, pSrcOffset, getAddress() + pDstOffset * (long) Size.FLOAT, pLength * (long) Size.FLOAT);
  }

  @Override
  public void copyFrom(double[] pFrom, int pSrcOffset, long pDstOffset, int pLength)
  {
    complainIfFreed();
    OffHeapMemoryAccess.copyFromArray(pFrom, pSrcOffset, getAddress() + pDstOffset * (long) Size.DOUBLE, pLength * (long) Size.DOUBLE);
  }

  @Override
  public long writeBytesToFileChannel(long pPositionInBufferInBytes, FileChannel pFileChannel, long pFilePositionInBytes, long pLengthInBytes) throws IOException
  {
    complainIfFreed();
    return writeBytesToFileChannel(0, pFileChannel, pFilePositionInBytes, getSizeInBytes(), Integer.MAX_VALUE - 1024);
  }

  @Override
  public long writeBytesToFileChannel(
    long pPositionInBufferInBytes,
    FileChannel pFileChannel,
    long pFilePositionInBytes,
    long pLengthInBytes,
    long pBufferSizeInBytes
  ) throws IOException
  {
    complainIfFreed();

    ArrayList<ByteBuffer> lByteBuffersForContiguousMemory = NIOBuffersInterop.getByteBuffersForContiguousMemory(this, pPositionInBufferInBytes, pLengthInBytes, pBufferSizeInBytes);

    pFileChannel.position(pFilePositionInBytes);
    for (ByteBuffer lByteBuffer : lByteBuffersForContiguousMemory)
    {
      pFileChannel.write(lByteBuffer);
    }
    return pFilePositionInBytes + pLengthInBytes;
  }

  @Override
  public long readBytesFromFileChannel(FileChannel pFileChannel, long pFilePositionInBytes, long pLengthInBytes) throws IOException
  {
    complainIfFreed();
    return readBytesFromFileChannel(0, pFileChannel, pFilePositionInBytes, pLengthInBytes);
  }

  @Override
  public long readBytesFromFileChannel(long pPositionInBufferInBytes, FileChannel pFileChannel, long pFilePositionInBytes, long pLengthInBytes) throws IOException
  {
    complainIfFreed();

    ArrayList<ByteBuffer> lByteBuffersForContiguousMemory = NIOBuffersInterop.getByteBuffersForContiguousMemory(this, pPositionInBufferInBytes, pLengthInBytes);

    pFileChannel.position(pFilePositionInBytes);
    for (ByteBuffer lByteBuffer : lByteBuffersForContiguousMemory)
    {
      pFileChannel.read(lByteBuffer);
    }

    return pFilePositionInBytes + pLengthInBytes;

  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public org.bridj.Pointer getBridJPointer(Class pTargetClass)
  {
    complainIfFreed();
    return BridJInterop.getBridJPointer(this, pTargetClass);
  }

  /**
   * Rerturn a JNA pointer. Usefull when interacting with JNA based bindings.
   *
   * @return off-heap memory object
   */
  @Override
  public com.sun.jna.Pointer getJNAPointer()
  {
    complainIfFreed();
    return JNAInterop.getJNAPointer(this);
  }

  @Override
  public ByteBuffer getByteBuffer()
  {
    complainIfFreed();
    final ByteBuffer lByteBuffer = getBridJPointer(Byte.class).getByteBuffer();
    return lByteBuffer;
  }

}
