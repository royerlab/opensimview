package coremem;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import coremem.enums.MemoryType;
import coremem.exceptions.FreedException;
import coremem.exceptions.InvalidNativeMemoryAccessException;

import org.bridj.Pointer;

/**
 * SafeContiguousMemory instances wrap instances of ContiguousMemoryInterface
 * and provide range checking for most operations. This is usefull when
 * debugging.
 * 
 * @author royer
 */
public class SafeContiguousMemory implements ContiguousMemoryInterface
{

  /**
   * Wrapped ContiguousMemoryInterface instance.
   */
  private final ContiguousMemoryInterface mDelegatedContiguousMemoryInterface;

  /**
   * Wraps a contiguous memory with a safe facade that does additional access
   * checks.
   * 
   * @param pContiguousMemory
   *          contiguous memory to wrap.
   * @return wrapped contiguous memory
   */
  public static final ContiguousMemoryInterface wrap(ContiguousMemoryInterface pContiguousMemory)
  {
    return wrap(pContiguousMemory, true);
  }

  /**
   * Wraps a contiguous memory with a safe facade that does additional access
   * checks - but only if the given flag is set to true.
   * 
   * @param pContiguousMemory
   *          contiguous memory to wrap.
   * @param pDoWrap
   *          flag that determines if the wrapping occurs or not
   * @return wrapped (or not wrapped) contiguous memory
   */
  public static final ContiguousMemoryInterface wrap(ContiguousMemoryInterface pContiguousMemory,
                                                     boolean pDoWrap)
  {
    if (pDoWrap)
    {
      return new SafeContiguousMemory(pContiguousMemory);
    }
    else
    {
      return pContiguousMemory;
    }
  }

  /**
   * Constructs a SafeContiguousMemory by wrapping a ContiguousMemoryInterface.
   * 
   * @param pContiguousMemoryInterface
   *          contiguous memory to wrap
   * 
   */
  public SafeContiguousMemory(ContiguousMemoryInterface pContiguousMemoryInterface)
  {
    super();
    mDelegatedContiguousMemoryInterface = pContiguousMemoryInterface;
  }

  /**
   * Checks whether this offset is valid.
   * 
   * @param pOffset
   */
  private void checkOffset(long pOffset)
  {
    if (pOffset < 0
        || pOffset >= mDelegatedContiguousMemoryInterface.getSizeInBytes())
      throw new InvalidNativeMemoryAccessException("Offset is out of bounds for this contiguous memory block.");
  }

  @Override
  public long getAddress()
  {
    return mDelegatedContiguousMemoryInterface.getAddress();
  }

  @Override
  public long getSizeInBytes()
  {
    return mDelegatedContiguousMemoryInterface.getSizeInBytes();
  }

  @Override
  public <T> Pointer<T> getBridJPointer(Class<T> pTargetClass)
  {
    return mDelegatedContiguousMemoryInterface.getBridJPointer(pTargetClass);
  }

  @Override
  public com.sun.jna.Pointer getJNAPointer()
  {
    return mDelegatedContiguousMemoryInterface.getJNAPointer();
  }

  @Override
  public ByteBuffer getByteBuffer()
  {
    return mDelegatedContiguousMemoryInterface.getByteBuffer();
  }

  @Override
  public byte getByteAligned(long pOffset)
  {
    checkOffset(pOffset);
    return mDelegatedContiguousMemoryInterface.getByteAligned(pOffset);
  }

  @Override
  public char getCharAligned(long pOffset)
  {
    checkOffset(pOffset);
    return mDelegatedContiguousMemoryInterface.getCharAligned(pOffset);
  }

  @Override
  public short getShortAligned(long pOffset)
  {
    checkOffset(pOffset);
    return mDelegatedContiguousMemoryInterface.getShortAligned(pOffset);
  }

  @Override
  public int getIntAligned(long pOffset)
  {
    checkOffset(pOffset);
    return mDelegatedContiguousMemoryInterface.getIntAligned(pOffset);
  }

  @Override
  public long getLongAligned(long pOffset)
  {
    checkOffset(pOffset);
    return mDelegatedContiguousMemoryInterface.getLongAligned(pOffset);
  }

  @Override
  public float getFloatAligned(long pOffset)
  {
    checkOffset(pOffset);
    return mDelegatedContiguousMemoryInterface.getFloatAligned(pOffset);
  }

  @Override
  public double getDoubleAligned(long pOffset)
  {
    checkOffset(pOffset);
    return mDelegatedContiguousMemoryInterface.getDoubleAligned(pOffset);
  }

  @Override
  public MemoryType getMemoryType()
  {
    return mDelegatedContiguousMemoryInterface.getMemoryType();
  }

  @Override
  public void setByteAligned(long pOffset, byte pValue)
  {
    checkOffset(pOffset);
    mDelegatedContiguousMemoryInterface.setByteAligned(pOffset,
                                                       pValue);
  }

  @Override
  public void setCharAligned(long pOffset, char pValue)
  {
    checkOffset(pOffset);
    mDelegatedContiguousMemoryInterface.setCharAligned(pOffset,
                                                       pValue);
  }

  @Override
  public void setShortAligned(long pOffset, short pValue)
  {
    checkOffset(pOffset);
    mDelegatedContiguousMemoryInterface.setShortAligned(pOffset,
                                                        pValue);
  }

  @Override
  public void setIntAligned(long pOffset, int pValue)
  {
    checkOffset(pOffset);
    mDelegatedContiguousMemoryInterface.setIntAligned(pOffset,
                                                      pValue);
  }

  @Override
  public void setLongAligned(long pOffset, long pValue)
  {
    checkOffset(pOffset);
    mDelegatedContiguousMemoryInterface.setLongAligned(pOffset,
                                                       pValue);
  }

  @Override
  public void setFloatAligned(long pOffset, float pValue)
  {
    checkOffset(pOffset);
    mDelegatedContiguousMemoryInterface.setFloatAligned(pOffset,
                                                        pValue);
  }

  @Override
  public void setDoubleAligned(long pOffset, double pValue)
  {
    checkOffset(pOffset);
    mDelegatedContiguousMemoryInterface.setDoubleAligned(pOffset,
                                                         pValue);
  }

  @Override
  public byte getByte(long pOffset)
  {
    checkOffset(pOffset);
    return mDelegatedContiguousMemoryInterface.getByte(pOffset);
  }

  @Override
  public char getChar(long pOffset)
  {
    checkOffset(pOffset);
    return mDelegatedContiguousMemoryInterface.getChar(pOffset);
  }

  @Override
  public short getShort(long pOffset)
  {
    checkOffset(pOffset);
    return mDelegatedContiguousMemoryInterface.getShort(pOffset);
  }

  @Override
  public int getInt(long pOffset)
  {
    checkOffset(pOffset);
    return mDelegatedContiguousMemoryInterface.getInt(pOffset);
  }

  @Override
  public long getLong(long pOffset)
  {
    checkOffset(pOffset);
    return mDelegatedContiguousMemoryInterface.getLong(pOffset);
  }

  @Override
  public float getFloat(long pOffset)
  {
    checkOffset(pOffset);
    return mDelegatedContiguousMemoryInterface.getFloat(pOffset);
  }

  @Override
  public double getDouble(long pOffset)
  {
    checkOffset(pOffset);
    return mDelegatedContiguousMemoryInterface.getDouble(pOffset);
  }

  @Override
  public void setByte(long pOffset, byte pValue)
  {
    checkOffset(pOffset);
    mDelegatedContiguousMemoryInterface.setByte(pOffset, pValue);
  }

  @Override
  public void setChar(long pOffset, char pValue)
  {
    checkOffset(pOffset);
    mDelegatedContiguousMemoryInterface.setChar(pOffset, pValue);
  }

  @Override
  public void setShort(long pOffset, short pValue)
  {
    checkOffset(pOffset);
    mDelegatedContiguousMemoryInterface.setShort(pOffset, pValue);
  }

  @Override
  public void setInt(long pOffset, int pValue)
  {
    checkOffset(pOffset);
    mDelegatedContiguousMemoryInterface.setInt(pOffset, pValue);
  }

  @Override
  public void setLong(long pOffset, long pValue)
  {
    checkOffset(pOffset);
    mDelegatedContiguousMemoryInterface.setLong(pOffset, pValue);
  }

  @Override
  public void setFloat(long pOffset, float pValue)
  {
    checkOffset(pOffset);
    mDelegatedContiguousMemoryInterface.setFloat(pOffset, pValue);
  }

  @Override
  public void setDouble(long pOffset, double pValue)
  {
    checkOffset(pOffset);
    mDelegatedContiguousMemoryInterface.setDouble(pOffset, pValue);
  }

  @Override
  public long writeBytesToFileChannel(FileChannel pFileChannel,
                                      long pFilePositionInBytes) throws IOException
  {
    return mDelegatedContiguousMemoryInterface.writeBytesToFileChannel(pFileChannel,
                                                                       pFilePositionInBytes);
  }

  @Override
  public long writeBytesToFileChannel(long pBufferPositionInBytes,
                                      FileChannel pFileChannel,
                                      long pFilePositionInBytes,
                                      long pLengthInBytes) throws IOException
  {
    checkOffset(pBufferPositionInBytes);
    checkOffset(pBufferPositionInBytes + pLengthInBytes - 1);
    return mDelegatedContiguousMemoryInterface.writeBytesToFileChannel(pBufferPositionInBytes,
                                                                       pFileChannel,
                                                                       pFilePositionInBytes,
                                                                       pLengthInBytes);
  }

  @Override
  public long readBytesFromFileChannel(FileChannel pFileChannel,
                                       long pFilePositionInBytes,
                                       long pLengthInBytes) throws IOException
  {
    checkOffset(0);
    checkOffset(pLengthInBytes - 1);
    return mDelegatedContiguousMemoryInterface.readBytesFromFileChannel(pFileChannel,
                                                                        pFilePositionInBytes,
                                                                        pLengthInBytes);
  }

  @Override
  public long readBytesFromFileChannel(long pBufferPositionInBytes,
                                       FileChannel pFileChannel,
                                       long pFilePositionInBytes,
                                       long pLengthInBytes) throws IOException
  {
    checkOffset(pBufferPositionInBytes);
    checkOffset(pBufferPositionInBytes + pLengthInBytes - 1);
    return mDelegatedContiguousMemoryInterface.readBytesFromFileChannel(pBufferPositionInBytes,
                                                                        pFileChannel,
                                                                        pFilePositionInBytes,
                                                                        pLengthInBytes);
  }

  @Override
  public void free()
  {
    mDelegatedContiguousMemoryInterface.free();
  }

  @Override
  public boolean isFree()
  {
    return mDelegatedContiguousMemoryInterface.isFree();
  }

  @Override
  public void complainIfFreed() throws FreedException
  {
    mDelegatedContiguousMemoryInterface.complainIfFreed();
  }

  @Override
  public ContiguousMemoryInterface subRegion(long pOffset,
                                             long pLenghInBytes)
  {
    checkOffset(pOffset);
    checkOffset(pOffset + pLenghInBytes - 1);
    return mDelegatedContiguousMemoryInterface.subRegion(pOffset,
                                                         pLenghInBytes);
  }

  @Override
  public void copyTo(Buffer pTo)
  {
    mDelegatedContiguousMemoryInterface.copyTo(pTo);
  }

  @Override
  public void copyFrom(Buffer pFrom)
  {
    mDelegatedContiguousMemoryInterface.copyFrom(pFrom);
  }

  @Override
  public void copyTo(ContiguousMemoryInterface pTo)
  {
    mDelegatedContiguousMemoryInterface.copyTo(pTo);
  }

  @Override
  public void copyFrom(ContiguousMemoryInterface pFrom)
  {
    mDelegatedContiguousMemoryInterface.copyFrom(pFrom);
  }

  @Override
  public void copyTo(byte[] pTo)
  {
    mDelegatedContiguousMemoryInterface.copyTo(pTo);
  }

  @Override
  public void copyTo(short[] pTo)
  {
    mDelegatedContiguousMemoryInterface.copyTo(pTo);
  }

  @Override
  public void copyTo(char[] pTo)
  {
    mDelegatedContiguousMemoryInterface.copyTo(pTo);
  }

  @Override
  public void copyTo(int[] pTo)
  {
    mDelegatedContiguousMemoryInterface.copyTo(pTo);
  }

  @Override
  public void copyTo(long[] pTo)
  {
    mDelegatedContiguousMemoryInterface.copyTo(pTo);
  }

  @Override
  public void copyTo(float[] pTo)
  {
    mDelegatedContiguousMemoryInterface.copyTo(pTo);
  }

  @Override
  public void copyTo(double[] pTo)
  {
    mDelegatedContiguousMemoryInterface.copyTo(pTo);
  }

  @Override
  public void copyFrom(byte[] pFrom)
  {
    mDelegatedContiguousMemoryInterface.copyFrom(pFrom);
  }

  @Override
  public void copyFrom(short[] pFrom)
  {
    mDelegatedContiguousMemoryInterface.copyFrom(pFrom);
  }

  @Override
  public void copyFrom(char[] pFrom)
  {
    mDelegatedContiguousMemoryInterface.copyFrom(pFrom);
  }

  @Override
  public void copyFrom(int[] pFrom)
  {
    mDelegatedContiguousMemoryInterface.copyFrom(pFrom);
  }

  @Override
  public void copyFrom(long[] pFrom)
  {
    mDelegatedContiguousMemoryInterface.copyFrom(pFrom);
  }

  @Override
  public void copyFrom(float[] pFrom)
  {
    mDelegatedContiguousMemoryInterface.copyFrom(pFrom);
  }

  @Override
  public void copyFrom(double[] pFrom)
  {
    mDelegatedContiguousMemoryInterface.copyFrom(pFrom);
  }

  @Override
  public void copyTo(byte[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength)
  {
    mDelegatedContiguousMemoryInterface.copyTo(pTo,
                                               pSrcOffset,
                                               pDstOffset,
                                               pLength);
  }

  @Override
  public void copyTo(short[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength)
  {
    mDelegatedContiguousMemoryInterface.copyTo(pTo,
                                               pSrcOffset,
                                               pDstOffset,
                                               pLength);
  }

  @Override
  public void copyTo(char[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength)
  {
    mDelegatedContiguousMemoryInterface.copyTo(pTo,
                                               pSrcOffset,
                                               pDstOffset,
                                               pLength);
  }

  @Override
  public void copyTo(int[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength)
  {
    mDelegatedContiguousMemoryInterface.copyTo(pTo,
                                               pSrcOffset,
                                               pDstOffset,
                                               pLength);
  }

  @Override
  public void copyTo(long[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength)
  {
    mDelegatedContiguousMemoryInterface.copyTo(pTo,
                                               pSrcOffset,
                                               pDstOffset,
                                               pLength);
  }

  @Override
  public void copyTo(float[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength)
  {
    mDelegatedContiguousMemoryInterface.copyTo(pTo,
                                               pSrcOffset,
                                               pDstOffset,
                                               pLength);
  }

  @Override
  public void copyTo(double[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength)
  {
    mDelegatedContiguousMemoryInterface.copyTo(pTo,
                                               pSrcOffset,
                                               pDstOffset,
                                               pLength);
  }

  @Override
  public void copyFrom(byte[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength)
  {
    mDelegatedContiguousMemoryInterface.copyFrom(pFrom,
                                                 pSrcOffset,
                                                 pDstOffset,
                                                 pLength);
  }

  @Override
  public void copyFrom(short[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength)
  {
    mDelegatedContiguousMemoryInterface.copyFrom(pFrom,
                                                 pSrcOffset,
                                                 pDstOffset,
                                                 pLength);
  }

  @Override
  public void copyFrom(char[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength)
  {
    mDelegatedContiguousMemoryInterface.copyFrom(pFrom,
                                                 pSrcOffset,
                                                 pDstOffset,
                                                 pLength);
  }

  @Override
  public void copyFrom(int[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength)
  {
    mDelegatedContiguousMemoryInterface.copyFrom(pFrom,
                                                 pSrcOffset,
                                                 pDstOffset,
                                                 pLength);
  }

  @Override
  public void copyFrom(long[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength)
  {
    mDelegatedContiguousMemoryInterface.copyFrom(pFrom,
                                                 pSrcOffset,
                                                 pDstOffset,
                                                 pLength);
  }

  @Override
  public void copyFrom(float[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength)
  {
    mDelegatedContiguousMemoryInterface.copyFrom(pFrom,
                                                 pSrcOffset,
                                                 pDstOffset,
                                                 pLength);
  }

  @Override
  public void copyFrom(double[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength)
  {
    mDelegatedContiguousMemoryInterface.copyFrom(pFrom,
                                                 pSrcOffset,
                                                 pDstOffset,
                                                 pLength);
  }

}
