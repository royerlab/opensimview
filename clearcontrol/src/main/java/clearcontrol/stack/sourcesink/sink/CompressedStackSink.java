package clearcontrol.stack.sourcesink.sink;

import clearcontrol.stack.StackInterface;
import clearcontrol.stack.sourcesink.StackSinkSourceInterface;
import coremem.ContiguousMemoryInterface;
import coremem.buffers.CompressedBuffer;
import coremem.offheap.OffHeapMemory;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Raw file stack sink
 *
 * @author royer
 */
public class CompressedStackSink extends RawFileStackSink
{

  private CompressedBuffer mCompressedBuffer;

  /**
   * Instantiates a compressed file stack sink.
   */
  public CompressedStackSink()
  {
    super();
  }


  protected void writeStackData(long pIndex, String pChannel, final StackInterface pStack) throws IOException
  {
    String lFileName = String.format(StackSinkSourceInterface.cCompressedFormat, pIndex);
    File lFile = new File(getChannelFolder(pChannel), lFileName);
    FileChannel lBinaryFileChannel = getFileChannel(lFile, false);
    ContiguousMemoryInterface lContiguousMemory = pStack.getContiguousMemory();

    long lDataLength = lContiguousMemory.getSizeInBytes();

    if (mCompressedBuffer == null || mCompressedBuffer.getContiguousMemory().getSizeInBytes() < lDataLength + CompressedBuffer.Overhead)
      mCompressedBuffer = new CompressedBuffer(OffHeapMemory.allocateBytes(lDataLength + CompressedBuffer.Overhead));

    mCompressedBuffer.rewind();
    mCompressedBuffer.writeCompressedMemory(lContiguousMemory);
    mCompressedBuffer.getCompressedContiguousMemory().writeBytesToFileChannel(lBinaryFileChannel, 0);
    double lCompressionRatio = mCompressedBuffer.getCompressionRatio();

    lBinaryFileChannel.force(false);
    lBinaryFileChannel.close();

    info("Finished writing stack: " + pStack + " to compressed file stack sink, compression ratio: " + lCompressionRatio);
  }


}
