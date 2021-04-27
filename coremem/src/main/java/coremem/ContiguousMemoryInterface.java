package coremem;

import coremem.interfaces.*;
import coremem.rgc.Freeable;

/**
 * ContiguousMemoryInterface represents contiguous chunks of memory that can be
 * accessed, copied, written and read from disk, and can be exchanged with NIO,
 * BridJ.
 * 
 * @author royer
 */
public interface ContiguousMemoryInterface extends
                                           PointerAccessible,
                                           JNAPointerWrappable,
                                           BridJPointerWrappable,
                                           ByteBufferWrappable,
                                           ReadAtAligned,
                                           WriteAtAligned,
                                           ReadAt,
                                           WriteAt,
                                           Copyable<ContiguousMemoryInterface>,
                                           CopyFromToNIOBuffers,
                                           CopyFromToJavaArray,
                                           CopyRangeFromToJavaArray,
                                           ReadWriteBytesFileChannel,
                                           SizedInBytes,
                                           Freeable
{

  /**
   * Returns a contiguous memory object representing for a memory sub region.
   * 
   * @param pOffsetInBytes
   *          offset in bytes
   * @param pLenghInBytes
   *          length in bytes
   * @return contiguous memory for sub region
   */
  ContiguousMemoryInterface subRegion(long pOffsetInBytes,
                                      long pLenghInBytes);

}
