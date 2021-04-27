package coremem.memmap;

import java.io.IOException;
import java.nio.channels.FileChannel;

import coremem.offheap.OffHeapMemoryAccess;
import coremem.rgc.Cleanable;
import coremem.rgc.Cleaner;

/**
 *
 *
 * @author royer
 */
public class MemoryMappedFile implements AutoCloseable, Cleanable
{

  private final FileChannel mFileChannel;
  private final MemoryMappedFileAccessMode mAccessMode;
  private final boolean mExtendIfNeeded;

  private final long mRequestedFilePosition;
  private final long mRequestedMappedRegionLength;

  private final long mActualMappingFilePosition;
  private final long mActualMappingRegionLength;

  private final long mMappingPointerAddress;

  private final Long mSignature;

  /**
   * Instanciates a memory mapped file for a given file channel, access mode,
   * file position, mapped region length, and 'extend-if-needed' flag.
   * 
   * @param pFileChannel
   *          file channel
   * @param pAccessMode
   *          access mode
   * @param pFilePosition
   *          file position
   * @param pMappedRegionLength
   *          region length
   * @param pExtendIfNeeded
   *          true -> extend if needed
   */
  public MemoryMappedFile(FileChannel pFileChannel,
                          MemoryMappedFileAccessMode pAccessMode,
                          final long pFilePosition,
                          final long pMappedRegionLength,
                          final boolean pExtendIfNeeded)
  {
    super();
    mFileChannel = pFileChannel;
    mAccessMode = pAccessMode;
    mRequestedFilePosition = pFilePosition;
    mRequestedMappedRegionLength = pMappedRegionLength;
    mExtendIfNeeded = pExtendIfNeeded;

    mActualMappingFilePosition = mRequestedFilePosition
                                 - (mRequestedFilePosition
                                    % MemoryMappedFileUtils.cAllocationGranularity);
    mActualMappingRegionLength = (mRequestedFilePosition
                                  % MemoryMappedFileUtils.cAllocationGranularity)
                                 + mRequestedMappedRegionLength;

    mMappingPointerAddress =
                           MemoryMappedFileUtils.map(mFileChannel,
                                                     mAccessMode,
                                                     mActualMappingFilePosition,
                                                     mActualMappingRegionLength,
                                                     mExtendIfNeeded);

    mSignature =
               OffHeapMemoryAccess.getSignature(mMappingPointerAddress);

  }

  /**
   * @param pFilePosition
   *          file position
   * @return address at file position
   */
  public long getAddressAtFilePosition(long pFilePosition)
  {
    if (pFilePosition < mRequestedFilePosition)
      throw new IndexOutOfBoundsException("File position index invalid: accessing before the mapped file region");

    if (mRequestedFilePosition
        + mRequestedMappedRegionLength <= pFilePosition)
      throw new IndexOutOfBoundsException("File position index invalid: accessing after the mapped file region");

    return mMappingPointerAddress
           + (pFilePosition - mActualMappingFilePosition);
  }

  /* (non-Javadoc)
   * @see java.lang.AutoCloseable#close()
   */
  @Override
  public void close() throws IOException
  {
    MemoryMappedFileUtils.unmap(mFileChannel,
                                mMappingPointerAddress,
                                mActualMappingRegionLength);

  }

  /* (non-Javadoc)
   * @see coremem.rgc.Cleanable#getCleaner()
   */
  @Override
  public Cleaner getCleaner()
  {
    return new MemoryMappedFileCleaner(mFileChannel,
                                       mMappingPointerAddress,
                                       mActualMappingRegionLength,
                                       mSignature);
  }

}
