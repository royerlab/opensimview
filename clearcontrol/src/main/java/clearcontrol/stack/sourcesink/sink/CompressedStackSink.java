package clearcontrol.stack.sourcesink.sink;

import clearcontrol.core.units.OrderOfMagnitude;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import clearcontrol.stack.metadata.StackMetaData;
import clearcontrol.stack.sourcesink.FileStackBase;
import clearcontrol.stack.sourcesink.FileStackInterface;
import clearcontrol.stack.sourcesink.StackSinkSourceInterface;
import com.bc.zarr.ArrayParams;
import com.bc.zarr.DataType;
import com.bc.zarr.ZarrArray;
import coremem.fragmented.FragmentedMemoryInterface;
import coremem.interop.NIOBuffersInterop;
import coremem.offheap.OffHeapMemory;
import org.blosc.JBlosc;
import org.blosc.PrimitiveSizes;
import org.blosc.Shuffle;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Raw file stack sink
 *
 * @author royer
 */
public class CompressedStackSink extends  RawFileStackSink
{

  /**
   * Instantiates a compressed file stack sink.
   *
   */
  public CompressedStackSink()
  {
    super();
  }


  protected void writeStackData(long pIndex,
                                String pChannel,
                                final StackInterface pStack) throws IOException
  {

    String lFileName = String.format(StackSinkSourceInterface.cFormat,
                                     pIndex);
    File lFile = new File(getChannelFolder(pChannel), lFileName);
    FileChannel lBinaryFileChannel = getFileChannel(lFile, false);
    FragmentedMemoryInterface lFragmentedMemory =
                                                pStack.getFragmentedMemory();

    OffHeapMemory lContiguousMemory = lFragmentedMemory.makeConsolidatedCopy();
    ArrayList<ByteBuffer> lBuffersForContiguousMemory = NIOBuffersInterop.getByteBuffersForContiguousMemory(lContiguousMemory);

    if (lBuffersForContiguousMemory.size()>1)
      throw new RuntimeException("Buffer too large to be compressed!");

    ByteBuffer lRawBuffer = lBuffersForContiguousMemory.get(0);
    ByteBuffer lCompressedBuffer = ByteBuffer.allocateDirect(lRawBuffer.limit() + JBlosc.OVERHEAD);

    JBlosc jb = new JBlosc();
    jb.compressCtx(
            3,
            Shuffle.BYTE_SHUFFLE,
            PrimitiveSizes.SHORT_FIELD_SIZE,
            lRawBuffer,
            lRawBuffer.limit(),
            lCompressedBuffer,
            lCompressedBuffer.limit(),
            "zstd",
            0,
            4);
    long lCompressedBufferLength = lCompressedBuffer.position();

    OffHeapMemory lCompressedContiguousMemory = NIOBuffersInterop.getContiguousMemoryFrom(lCompressedBuffer, lCompressedBufferLength);

    lCompressedContiguousMemory.writeBytesToFileChannel(lBinaryFileChannel, 0);

    lBinaryFileChannel.force(false);
    lBinaryFileChannel.close();
  }


}
