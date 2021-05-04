package coremem.memmap;

import coremem.offheap.OffHeapMemoryAccess;
import coremem.rgc.Cleaner;

import java.nio.channels.FileChannel;

/**
 * Memory mapped file cleaner
 *
 * @author royer
 */
public class MemoryMappedFileCleaner implements Cleaner
{
  private final long mAddressToClean;
  private final FileChannel mFileChannelToClean;
  private final long mMappedRegionLength;
  private final Long mCleanerSignature;

  /**
   * Instanciates a cleaner that can free the ressources associated to a memroy
   * mapped file.
   *
   * @param pFileChannel        file channel
   * @param pMemoryMapAddress   memory mapped address
   * @param pMappedRegionLength mapped region length
   * @param pSignature          signature
   */
  public MemoryMappedFileCleaner(FileChannel pFileChannel, final long pMemoryMapAddress, final long pMappedRegionLength, final Long pSignature)
  {
    mFileChannelToClean = pFileChannel;
    mAddressToClean = pMemoryMapAddress;
    mMappedRegionLength = pMappedRegionLength;
    mCleanerSignature = pSignature;
  }

  @Override
  public void run()
  {
    if (OffHeapMemoryAccess.isAllocatedMemory(mAddressToClean, mCleanerSignature))
    {
      MemoryMappedFileUtils.unmap(mFileChannelToClean, mAddressToClean, mMappedRegionLength);
      format("Successfully unmaped memory! channel=%s, address=%s, signature=%d \n", mFileChannelToClean, mAddressToClean, mCleanerSignature);/**/
    } else
    {
      format("Attempted to unmap already unmapped memory, or memorywith wrong signature! channel=%s, address=%s, signature=%d \n", mFileChannelToClean, mAddressToClean, mCleanerSignature);/**/
    }
  }

  private void format(String format, Object... args)
  {
    System.out.format(format, args);
  }

}