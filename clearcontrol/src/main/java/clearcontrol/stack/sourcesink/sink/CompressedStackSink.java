package clearcontrol.stack.sourcesink.sink;

import clearcontrol.stack.StackInterface;
import clearcontrol.stack.sourcesink.StackSinkSourceInterface;
import com.sun.jna.NativeLong;
import coremem.fragmented.FragmentedMemoryInterface;
import coremem.interop.JNAInterop;
import coremem.offheap.OffHeapMemory;
import org.blosc.IBloscDll;
import org.blosc.JBlosc;
import org.blosc.PrimitiveSizes;
import org.blosc.Shuffle;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

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

    OffHeapMemory lRawBuffer = lFragmentedMemory.makeConsolidatedCopy();
    OffHeapMemory lCompressedBuffer = OffHeapMemory.allocateBytes(lRawBuffer.getSizeInBytes() + JBlosc.OVERHEAD);



    //		int w = IBloscDll.blosc_compress_ctx(
    //		compressionLevel,
    //		shuffleType,
    //		new NativeLong(typeSize)
    //	    new NativeLong(srcLength),
    //	    src,
    //	    dest,
    //	    new NativeLong(destLength),
    //	    compressorName,
    //		new NativeLong(blockSize),
    //		numThreads);

    long lCompressedBufferLength =IBloscDll.blosc_compress_ctx(
            3,
            Shuffle.BYTE_SHUFFLE,
            new NativeLong(PrimitiveSizes.SHORT_FIELD_SIZE),
            new NativeLong(lRawBuffer.getSizeInBytes()),
            JNAInterop.getJNAPointer(lRawBuffer),
            JNAInterop.getJNAPointer(lCompressedBuffer),
            new NativeLong(lCompressedBuffer.getSizeInBytes()),
            "zstd",
            new NativeLong(0),
            Runtime.getRuntime().availableProcessors()/2);

    System.out.println("Raw        size: "+lRawBuffer.getSizeInBytes());
    System.out.println("Compressed size: "+lCompressedBufferLength);

    lCompressedBuffer.writeBytesToFileChannel(lBinaryFileChannel, 0);

    lBinaryFileChannel.force(false);
    lBinaryFileChannel.close();
  }


}
