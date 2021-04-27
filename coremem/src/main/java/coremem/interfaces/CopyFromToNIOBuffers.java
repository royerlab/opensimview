package coremem.interfaces;

import java.nio.Buffer;

/**
 * Memory objects implementing this interface can copy their contents to and
 * from Java NIO buffers.
 *
 * @author royer
 */
public interface CopyFromToNIOBuffers
{
  /**
   * Copy to NIO buffer.
   * 
   * @param pBuffer
   *          NIO buffer
   */
  public void copyTo(Buffer pBuffer);

  /**
   * Copy from NIO buffer.
   * 
   * @param pBuffer
   *          NIO buffer
   */
  public void copyFrom(Buffer pBuffer);

}
