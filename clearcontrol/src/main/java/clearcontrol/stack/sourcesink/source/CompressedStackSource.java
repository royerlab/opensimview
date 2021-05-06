package clearcontrol.stack.sourcesink.source;

import clearcontrol.core.units.OrderOfMagnitude;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import clearcontrol.stack.sourcesink.StackSinkSourceInterface;
import coremem.buffers.DecompressedBuffer;
import coremem.offheap.OffHeapMemory;
import coremem.recycling.RecyclerInterface;

import java.io.File;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

/**
 * Raw file stack source
 *
 * @author royer
 */
public class CompressedStackSource extends RawFileStackSource
{

  OffHeapMemory mCompressedData;

  /**
   * Instantiates a compressed file stack source
   *
   * @param pStackRecycler stack recycler
   */
  public CompressedStackSource(final RecyclerInterface<StackInterface, StackRequest> pStackRecycler)
  {
    super(pStackRecycler);
  }

  @Override
  public StackInterface getStack(final String pChannel, final long pStackIndex, final long pTime, final TimeUnit pTimeUnit)
  {
    if (mStackRecycler == null)
    {
      return null;
    }
    try
    {

      final StackRequest lStackRequest = getStackRequest(pChannel, pStackIndex);

      final StackInterface lStack = mStackRecycler.getOrWait(pTime, pTimeUnit, lStackRequest);

      String lFileName = String.format(StackSinkSourceInterface.cCompressedFormat, pStackIndex);
      File lFile = new File(getChannelFolder(pChannel), lFileName);

      if (!lFile.exists()) return null;

      FileChannel lBinnaryFileChannel = getFileChannel(lFile, true);

      long lNumCompressedBytes = lFile.length();

      if (mCompressedData == null || mCompressedData.getSizeInBytes() != lNumCompressedBytes)
        mCompressedData = OffHeapMemory.allocateBytes(lNumCompressedBytes);

      mCompressedData.readBytesFromFileChannel(lBinnaryFileChannel, 0, lNumCompressedBytes);

      DecompressedBuffer lDecompressedBuffer = new DecompressedBuffer(lStack.getContiguousMemory());
      lDecompressedBuffer.writeDecompressedMemory(mCompressedData);


      final double lTimeStampInSeconds = getStackTimeStampInSeconds(pChannel, pStackIndex);
      lStack.getMetaData().setTimeStampInNanoseconds((long) OrderOfMagnitude.unit2nano(lTimeStampInSeconds));
      lStack.getMetaData().setIndex(pStackIndex);

      lStack.getMetaData().addAll(getStackMetaData(pChannel, pStackIndex));

      return lStack;
    } catch (final Throwable e)
    {
      e.printStackTrace();
      return null;
    }

  }


}
