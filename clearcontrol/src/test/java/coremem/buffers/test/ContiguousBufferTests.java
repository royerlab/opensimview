package coremem.buffers.test;

import coremem.buffers.ContiguousBuffer;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * Contiguous buffer tests
 *
 * @author royer
 */
public class ContiguousBufferTests
{

  /**
   * Tests
   */
  @Test
  public void test()
  {
    ContiguousBuffer lContiguousBuffer = ContiguousBuffer.allocate(124);

    assertEquals(124, lContiguousBuffer.remainingBytes());

    assertEquals(124, lContiguousBuffer.getSizeInBytes());

    assertEquals(124, lContiguousBuffer.getContiguousMemory().getSizeInBytes());

    assertTrue(lContiguousBuffer.hasRemaining(124));
    assertTrue(lContiguousBuffer.hasRemainingChar());
    assertTrue(lContiguousBuffer.hasRemainingShort());
    assertTrue(lContiguousBuffer.hasRemainingInt());
    assertTrue(lContiguousBuffer.hasRemainingLong());
    assertTrue(lContiguousBuffer.hasRemainingFloat());
    assertTrue(lContiguousBuffer.hasRemainingDouble());

    assertTrue(lContiguousBuffer.isPositionValid());

    int i = 0;
    while (lContiguousBuffer.hasRemainingByte())
    {
      lContiguousBuffer.writeByte((byte) (i++));
      assertEquals(124 - i, lContiguousBuffer.remainingBytes());
    }

    lContiguousBuffer.rewind();

    i = 0;
    while (lContiguousBuffer.hasRemainingByte()) assertEquals(i++, lContiguousBuffer.readByte());

    lContiguousBuffer.setPosition(100);
    lContiguousBuffer.pushPosition();
    lContiguousBuffer.writeFloat(1.0f);
    lContiguousBuffer.writeFloat(1.0f);
    lContiguousBuffer.writeFloat(1.0f);
    lContiguousBuffer.writeFloat(1.0f);
    lContiguousBuffer.popPosition();
    assertEquals(1.0f, lContiguousBuffer.readFloat(), 0.01);
    assertEquals(1.0f, lContiguousBuffer.readFloat(), 0.01);
    assertEquals(1.0f, lContiguousBuffer.readFloat(), 0.01);
    assertEquals(1.0f, lContiguousBuffer.readFloat(), 0.01);
    assertNotEquals(1.0f, lContiguousBuffer.readFloat(), 0.01);

    try
    {
      lContiguousBuffer.popPosition();
      fail();
    } catch (NoSuchElementException e)
    {
    }

    ContiguousBuffer lAnotherContiguousBuffer = ContiguousBuffer.allocate(124 / 2);
    lAnotherContiguousBuffer.fillBytes((byte) 0);
    lAnotherContiguousBuffer.rewind();

    lContiguousBuffer.rewind();
    lContiguousBuffer.writeContiguousMemory(lAnotherContiguousBuffer.getContiguousMemory());
    assertEquals(124 / 2, lAnotherContiguousBuffer.remainingBytes());
    lContiguousBuffer.writeContiguousBuffer(lAnotherContiguousBuffer);

    assertEquals(0, lContiguousBuffer.remainingBytes());
    assertEquals(0, lAnotherContiguousBuffer.remainingBytes());

  }

}
