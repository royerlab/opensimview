package coremem.buffers;

import com.sun.jna.NativeLong;
import coremem.ContiguousMemoryInterface;
import coremem.exceptions.CoreMemException;
import coremem.fragmented.FragmentedMemory;
import coremem.fragmented.FragmentedMemoryInterface;
import coremem.interop.JNAInterop;
import org.blosc.IBloscDll;
import org.blosc.JBlosc;
import org.blosc.PrimitiveSizes;
import org.blosc.Shuffle;

/**
 * CompressedBuffer.
 * Buffer that can write compressed data.
 */
public class CompressedBuffer extends ContiguousBuffer
{
  public static long Overhead = JBlosc.OVERHEAD;
  public static long cMaxBufferSize = 2147483600 - Overhead;
  private String mCodecName;
  private int mCompressionLevel, mNumThreads;

  /**
   * Constructs a Compressed buffer that behaves like a ContiguousBuffer, except that data can be compressed before being written to it.
   */
  public CompressedBuffer(ContiguousMemoryInterface pCompressedMemory, String pCodecName, int pCompressionLevel, int pNumThreads)
  {
    super(pCompressedMemory);
    mCodecName = pCodecName;
    mCompressionLevel = pCompressionLevel;
    mNumThreads = pNumThreads == -1 ? Runtime.getRuntime().availableProcessors() / 2 : pNumThreads;
  }

  public CompressedBuffer(ContiguousMemoryInterface pCompressedMemory)
  {
    this(pCompressedMemory, "lz4", 3, -1);
  }

  /**
   * Wraps a ContiguousMemoryInterface with a CompressedBuffer.
   *
   * @param pContiguousMemoryInterface contiguous memory to wrap
   * @return contiguous buffer
   */
  public static CompressedBuffer wrap(ContiguousMemoryInterface pContiguousMemoryInterface, String pCodecName, int pCompressionLevel, int pNumThreads)
  {
    return new CompressedBuffer(pContiguousMemoryInterface, pCodecName, pCompressionLevel, pNumThreads);
  }

  public ContiguousMemoryInterface getCompressedContiguousMemory()
  {
    return getContiguousMemory().subRegion(0, getCurrentRelativePosition());
  }

  public void writeCompressedMemory(FragmentedMemoryInterface pFragmentedMemory)
  {
    for (final ContiguousMemoryInterface lContiguousMemory : pFragmentedMemory)
    {
      writeCompressedMemory(lContiguousMemory);
    }
  }

  public void writeCompressedMemory(ContiguousMemoryInterface pContiguousMemory)
  {
    long lBufferSize = pContiguousMemory.getSizeInBytes();
    if (lBufferSize > cMaxBufferSize)
    {
      long lNumberOfChunks = (long) Math.max(2L, Math.ceil(lBufferSize / cMaxBufferSize));
      FragmentedMemory lChunks = FragmentedMemory.split(pContiguousMemory, lNumberOfChunks);
      for (ContiguousMemoryInterface lChunk : lChunks)
      {
        writeCompressedMemorySingleChunk(lChunk);
      }
    } else writeCompressedMemorySingleChunk(pContiguousMemory);

  }

  private void writeCompressedMemorySingleChunk(ContiguousMemoryInterface pContiguousMemory)
  {
    long lMaxCompressedSize = pContiguousMemory.getSizeInBytes() + JBlosc.OVERHEAD;
    if (remainingBytes() < lMaxCompressedSize)
      throw new CoreMemException("Too little space remaining on compression buffer!");

    long lCompressedBufferLength = IBloscDll.blosc_compress_ctx(mCompressionLevel, Shuffle.BIT_SHUFFLE, new NativeLong(PrimitiveSizes.SHORT_FIELD_SIZE), new NativeLong(pContiguousMemory.getSizeInBytes()), JNAInterop.getJNAPointer(pContiguousMemory), JNAInterop.getJNAPointer(getRemainingContiguousMemory()), new NativeLong(Math.min(remainingBytes(), lMaxCompressedSize)), mCodecName, new NativeLong(0), mNumThreads);

    skipBytes(lCompressedBufferLength);
  }
}
