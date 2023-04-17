package coremem.fragmented;

import coremem.ContiguousMemoryInterface;
import coremem.buffers.ContiguousBuffer;
import coremem.exceptions.InvalidFragmentedMemoryStateException;
import coremem.interop.NIOBuffersInterop;
import coremem.offheap.OffHeapMemory;
import coremem.rgc.FreeableBase;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;

import static java.lang.Math.min;

/**
 * Fragmented memory objects are lists of contiguous memory regions. Overall,
 * the referenced memory is not necessarily (but can be) contiguous.
 *
 * @author royer
 */
public class FragmentedMemory extends FreeableBase implements FragmentedMemoryInterface

{

  private final ArrayList<ContiguousMemoryInterface> mMemoryRegionList = new ArrayList<ContiguousMemoryInterface>();
  private long mTotalSizeInBytes;

  /**
   * Splits a contiguous memory regions into n pieces of same size (if possible,
   * otherwise the last one is smaller)
   *
   * @param pContiguousMemoryInterface contiguous memory region
   * @param pNumberOfFragments         number of fragments
   * @return fragmented memory
   */
  public static FragmentedMemory split(ContiguousMemoryInterface pContiguousMemoryInterface, long pNumberOfFragments)
  {
    long lAddress = pContiguousMemoryInterface.getAddress();
    final long lSizeInBytes = pContiguousMemoryInterface.getSizeInBytes();
    final long lFragmentSizeInBytes = (lSizeInBytes / pNumberOfFragments);

    final FragmentedMemory lFragmentedMemory = new FragmentedMemory();
    long lLeftToBeAssignedSizeInBytes = lSizeInBytes;
    OffHeapMemory lOffHeapMemory;
    for (int i = 0; i < pNumberOfFragments; i++)
    {
      long lEffectiveFragmentSizeInBytes;
      if (i == pNumberOfFragments - 1) lEffectiveFragmentSizeInBytes = lLeftToBeAssignedSizeInBytes;
      else lEffectiveFragmentSizeInBytes = lFragmentSizeInBytes;

      lOffHeapMemory = OffHeapMemory.wrapPointer("FragmentOf" + pContiguousMemoryInterface, pContiguousMemoryInterface, lAddress, lEffectiveFragmentSizeInBytes);
      lAddress += lFragmentSizeInBytes;
      lLeftToBeAssignedSizeInBytes -= lFragmentSizeInBytes;
      lFragmentedMemory.add(lOffHeapMemory);
    }

    return lFragmentedMemory;
  }

  /**
   * Wrap a list of contiguous memory regions into a single fragmented memory.
   *
   * @param pContiguousMemoryInterfaces array of contiguous memory regions.
   * @return fragmented memory
   */
  public static FragmentedMemoryInterface wrap(ContiguousMemoryInterface... pContiguousMemoryInterfaces)
  {
    final FragmentedMemory lFragmentedMemory = new FragmentedMemory();
    for (final ContiguousMemoryInterface lContiguousMemoryInterface : pContiguousMemoryInterfaces)
      lFragmentedMemory.add(lContiguousMemoryInterface);
    return lFragmentedMemory;
  }

  /**
   * Default constructor, fragmented memory initially empty.
   */
  public FragmentedMemory()
  {
    super();
  }

  @Override
  public ContiguousMemoryInterface get(int pIndex)
  {
    return mMemoryRegionList.get(pIndex);
  }

  @Override
  public int getNumberOfFragments()
  {
    return mMemoryRegionList.size();
  }

  @Override
  public void add(ContiguousMemoryInterface pContiguousMemoryInterface)
  {
    mMemoryRegionList.add(pContiguousMemoryInterface);
    mTotalSizeInBytes += pContiguousMemoryInterface.getSizeInBytes();
  }

  @Override
  public void remove(ContiguousMemoryInterface pContiguousMemoryInterface)
  {
    mMemoryRegionList.remove(pContiguousMemoryInterface);
    mTotalSizeInBytes -= pContiguousMemoryInterface.getSizeInBytes();
  }

  @Override
  public OffHeapMemory add(Buffer pBuffer)
  {
    OffHeapMemory lContiguousMemoryFromByteBuffer = NIOBuffersInterop.getContiguousMemoryFrom(pBuffer);
    add(lContiguousMemoryFromByteBuffer);
    return lContiguousMemoryFromByteBuffer;
  }

  @Override
  public OffHeapMemory makeConsolidatedCopy()
  {
    OffHeapMemory lOffHeapMemory = OffHeapMemory.allocateBytes(getSizeInBytes());
    makeConsolidatedCopy(lOffHeapMemory);
    return lOffHeapMemory;
  }

  @Override
  public void makeConsolidatedCopy(ContiguousMemoryInterface pDestinationMemory)
  {
    final ContiguousBuffer lContiguousBuffer = ContiguousBuffer.wrap(pDestinationMemory);

    int lNumberOfFragments = getNumberOfFragments();

    for (int i = 0; i < lNumberOfFragments; i++)
    {
      ContiguousMemoryInterface lContiguousMemoryInterface = get(i);
      lContiguousBuffer.writeContiguousMemory(lContiguousMemoryInterface);
    }

  }

  @Override
  public long writeBytesToFileChannel(FileChannel pFileChannel, long pFilePositionInBytes) throws IOException
  {
    return writeBytesToFileChannel(0, pFileChannel, pFilePositionInBytes, getSizeInBytes());
  }

  @Override
  public long writeBytesToFileChannel(long pBufferPositionInBytes, FileChannel pFileChannel, long pFilePositionInBytes, long pLengthInBytes) throws IOException
  {
    return writeBytesToFileChannel(pBufferPositionInBytes, pFileChannel, pFilePositionInBytes, pLengthInBytes, Integer.MAX_VALUE - 1024);
  }

  @Override
  public long writeBytesToFileChannel(long pBufferPositionInBytes, FileChannel pFileChannel, long pFilePositionInBytes, long pLengthInBytes, long pBufferSizeInBytes) throws IOException
  {
    complainIfFreed();
    long lBytesWritten = 0;
    long lCurrentFilePosition = pFilePositionInBytes;
    long lBufferSizeInBytes = min(Integer.MAX_VALUE - 1024, pBufferSizeInBytes);
    for (final ContiguousMemoryInterface lContiguousMemoryInterface : mMemoryRegionList)
    {
      final long lBytesLeftToBeWritten = pLengthInBytes - lBytesWritten;
      if (lBytesLeftToBeWritten <= 0) break;
      final long lBytesToBeWritten = min(lContiguousMemoryInterface.getSizeInBytes(), lBytesLeftToBeWritten);
      lCurrentFilePosition = lContiguousMemoryInterface.writeBytesToFileChannel(0, pFileChannel, lCurrentFilePosition, lBytesToBeWritten, lBufferSizeInBytes);
      lBytesWritten += lBytesToBeWritten;
    }
    return lCurrentFilePosition;
  }

  @Override
  public long readBytesFromFileChannel(FileChannel pFileChannel, long pFilePositionInBytes, long pLengthInBytes) throws IOException
  {
    return readBytesFromFileChannel(0, pFileChannel, pFilePositionInBytes, getSizeInBytes());
  }

  @Override
  public long readBytesFromFileChannel(long pBufferPositionInBytes, FileChannel pFileChannel, long pFilePositionInBytes, long pLengthInBytes) throws IOException
  {
    complainIfFreed();
    long lBytesRead = 0;
    long lCurrentFilePosition = pFilePositionInBytes;
    for (final ContiguousMemoryInterface lContiguousMemoryInterface : mMemoryRegionList)
    {
      final long lBytesLeftToBeRead = pLengthInBytes - lBytesRead;
      if (lBytesLeftToBeRead <= 0) break;
      final long lBytesToReadNow = min(lContiguousMemoryInterface.getSizeInBytes(), lBytesLeftToBeRead);
      lCurrentFilePosition = lContiguousMemoryInterface.readBytesFromFileChannel(0, pFileChannel, lCurrentFilePosition, lBytesToReadNow);
      lBytesRead += lBytesToReadNow;
    }
    return lCurrentFilePosition;

  }

  @Override
  public long getSizeInBytes()
  {
    complainIfFreed();
    return mTotalSizeInBytes;
  }

  @Override
  public void free()
  {
    for (final ContiguousMemoryInterface lContiguousMemoryInterface : mMemoryRegionList)
      lContiguousMemoryInterface.free();
  }

  @Override
  public boolean isFree()
  {
    if (mMemoryRegionList.isEmpty()) return false;

    Boolean lIsFree = null;
    for (final ContiguousMemoryInterface lContiguousMemoryInterface : mMemoryRegionList)
    {
      final boolean lLocalIsFree = lContiguousMemoryInterface.isFree();
      if (lIsFree == null) lIsFree = lLocalIsFree;
      else if (lIsFree != lLocalIsFree)
        throw new InvalidFragmentedMemoryStateException("Some contiguous memory blocks are freed and others not!");
    }

    return lIsFree;
  }

  @Override
  public Iterator<ContiguousMemoryInterface> iterator()
  {
    return mMemoryRegionList.iterator();
  }

}
