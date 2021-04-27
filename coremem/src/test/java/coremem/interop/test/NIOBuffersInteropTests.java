package coremem.interop.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;

import coremem.ContiguousMemoryInterface;
import coremem.interop.NIOBuffersInterop;

import org.junit.Test;

/**
 * NIO buffers interop tests
 *
 * @author royer
 */
public class NIOBuffersInteropTests
{

  private static final int cBufferLength = 32;

  /**
   * Tests byte buffers
   */
  @Test
  public void testByteBuffer()
  {
    final ByteBuffer lBuffer =
                             ByteBuffer.allocateDirect(cBufferLength)
                                       .order(ByteOrder.nativeOrder());
    final ContiguousMemoryInterface lContiguousMemory =
                                                      NIOBuffersInterop.getContiguousMemoryFrom(lBuffer);

    assertTrue(lContiguousMemory.getAddress() > 0);
    assertEquals(cBufferLength, lContiguousMemory.getSizeInBytes());

    lBuffer.putInt(15, 12345);

    // for (int i = 0; i < cBufferLength - 4; i++)
    // System.out.println(i + " -> \t" + lBuffer.getInt(i));
    // System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
    // for (int i = 0; i < cBufferLength - 4; i++)
    // System.out.println(i + " -> \t" + lContiguousMemory.getInt(i));

    assertEquals(12345, lContiguousMemory.getInt(15));

  }

  /**
   * Tests double buffer
   */
  @Test
  public void testDoubleBuffer()
  {
    final DoubleBuffer lBuffer =
                               ByteBuffer.allocateDirect(cBufferLength)
                                         .order(ByteOrder.nativeOrder())
                                         .asDoubleBuffer();
    final ContiguousMemoryInterface lContiguousMemory =
                                                      NIOBuffersInterop.getContiguousMemoryFrom(lBuffer);

    assertTrue(lContiguousMemory.getAddress() > 0);
    assertEquals(cBufferLength, lContiguousMemory.getSizeInBytes());

    lBuffer.put(3, 12345.54321);
    assertEquals(12345.54321,
                 lContiguousMemory.getDoubleAligned(3),
                 0.001);
  }

}
