package coremem.interfaces;

import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Memory objects implementing this interface can read and write bytes to file
 * channels.
 *
 * @author royer
 */
public interface ReadWriteBytesFileChannel
{

  /**
   * Writes the whole contents of this memory object at a given offset of a file
   * channel.
   * 
   * @param pFileChannel
   *          file channel
   * @param pFilePositionInBytes
   *          file position
   * @return file position past the last written byte.
   * @throws IOException
   *           Thrown in case of IO problem.
   */
  public long writeBytesToFileChannel(final FileChannel pFileChannel,
                                      final long pFilePositionInBytes) throws IOException;

  /**
   * Writes data at a given range from this memory object at a given offset of a
   * file channel.
   * 
   * @param pBufferPositionInBytes
   *          offset in memory object
   * @param pFileChannel
   *          file channel
   * @param pFilePositionInBytes
   *          file channel offset
   * @param pLengthInBytes
   *          length in bytes
   * @return file position past the last written byte.
   * @throws IOException
   *           Thrown in case of IO problem.
   */
  public long writeBytesToFileChannel(final long pBufferPositionInBytes,
                                      final FileChannel pFileChannel,
                                      final long pFilePositionInBytes,
                                      final long pLengthInBytes) throws IOException;

  /**
   * Reads data into this memory object for a certain position of a file
   * channel.
   * 
   * @param pFileChannel
   *          file channel
   * @param pFilePositionInBytes
   *          offset in file channel
   * @param pLengthInBytes
   *          length in bytes
   * @return File position past the last byte read.
   * @throws IOException
   *           Thrown in case of IO problem.
   */
  public long readBytesFromFileChannel(final FileChannel pFileChannel,
                                       final long pFilePositionInBytes,
                                       final long pLengthInBytes) throws IOException;

  /**
   * @param pBufferPositionInBytes
   *          offset in memory object
   * @param pFileChannel
   *          file channel
   * @param pFilePositionInBytes
   *          offset in file channel
   * @param pLengthInBytes
   *          length in bytes
   * @return File position past the last byte read.
   * @throws IOException
   *           Thrown in case of IO problem.
   */
  public long readBytesFromFileChannel(final long pBufferPositionInBytes,
                                       final FileChannel pFileChannel,
                                       final long pFilePositionInBytes,
                                       final long pLengthInBytes) throws IOException;

}
