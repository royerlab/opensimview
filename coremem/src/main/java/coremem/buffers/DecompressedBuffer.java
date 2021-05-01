package coremem.buffers;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;
import coremem.ContiguousMemoryInterface;
import coremem.exceptions.CoreMemException;
import coremem.fragmented.FragmentedMemoryInterface;
import coremem.interop.JNAInterop;
import coremem.interop.NIOBuffersInterop;
import coremem.offheap.OffHeapMemory;
import org.blosc.IBloscDll;

import java.nio.ByteBuffer;

/**
 * DecompressedBuffer.
 * Buffer that can write decompressed data.
 */
public class DecompressedBuffer extends ContiguousBuffer
{
  private int mNumThreads;

  /**
   * Constructs a DecompressedBuffer that decompresses data as it is written to it.
   *
   */
  public DecompressedBuffer(ContiguousMemoryInterface pDecompressedMemory,int pNumThreads)
  {
    super(pDecompressedMemory);
    mNumThreads = pNumThreads == -1 ? Runtime.getRuntime().availableProcessors()/2 : pNumThreads;
  }

  /**
   * Constructs a DecompressedBuffer that decompresses data as it is written to it.
   *
   */
  public DecompressedBuffer(ContiguousMemoryInterface pDecompressedMemory)
  {
    this(pDecompressedMemory, -1);
  }

  public ContiguousMemoryInterface getDecompressedContiguousMemory()
  {
    return this.getContiguousMemory().subRegion(0, getCurrentRelativePosition());
  }

  public void writeDecompressedMemory(FragmentedMemoryInterface pFragmentedMemory)
  {
    for (final ContiguousMemoryInterface lContiguousMemory : pFragmentedMemory)
    {
      writeDecompressedMemory(lContiguousMemory);
    }
  }

  public void writeDecompressedMemory(ContiguousMemoryInterface pContiguousMemory)
  {
    ContiguousBuffer lCompressedDataBuffer = new ContiguousBuffer(pContiguousMemory);

    while(lCompressedDataBuffer.hasRemaining(32))
    {
      long lNumberOfCompressedBytesRead = writeDecompressedMemorySingleChunk(lCompressedDataBuffer.getRemainingContiguousMemory());
      lCompressedDataBuffer.skipBytes(lNumberOfCompressedBytesRead);
    }
  }

  private long writeDecompressedMemorySingleChunk(ContiguousMemoryInterface pContiguousMemory)
  {
    ContiguousMemoryInterface lHeader = pContiguousMemory.subRegion(0, 32);
    ByteBuffer lHeaderNIOBuffer = NIOBuffersInterop.getByteBuffersForContiguousMemory(lHeader).get(0);

    NativeLongByReference lNumberOfDecompressedBytes = new NativeLongByReference();
    NativeLongByReference lNumberOfCompressedBytes = new NativeLongByReference();
    NativeLongByReference lBlockSize = new NativeLongByReference();

    IBloscDll.blosc_cbuffer_sizes(lHeaderNIOBuffer, lNumberOfDecompressedBytes, lNumberOfCompressedBytes, lBlockSize);

    if (this.remainingBytes() < lNumberOfDecompressedBytes.getValue().longValue())
      throw new CoreMemException("Too little space remaining on decompression buffer!");

    IBloscDll.blosc_decompress_ctx(
              JNAInterop.getJNAPointer(pContiguousMemory),
              JNAInterop.getJNAPointer(getRemainingContiguousMemory()),
              new NativeLong(remainingBytes()),
              mNumThreads
              );

    skipBytes(lNumberOfDecompressedBytes.getValue().longValue());

    return lNumberOfCompressedBytes.getValue().longValue();
  }
}
