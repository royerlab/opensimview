package coremem.interfaces;

import java.nio.ByteBuffer;

/**
 * Memory objects implementing this interface can be wrapped into a NIO
 * ByteBuffer.
 *
 * @author royer
 */
public interface ByteBufferWrappable
{
  /**
   * Returns NIO ByteBuffer
   * 
   * @return NIO ByteBuffer
   */
  public ByteBuffer getByteBuffer();
}
