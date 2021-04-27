package coremem.util.test;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import coremem.ContiguousMemoryInterface;
import coremem.enums.NativeTypeEnum;
import coremem.offheap.OffHeapMemory;
import coremem.util.Size;

import org.bridj.Pointer;
import org.junit.Test;

/**
 * Size tests
 *
 * @author royer
 */
public class SizeTests
{

  /**
   * Tests primitive types
   */
  @Test
  public void testPrimitives()
  {

    assertEquals(1, Size.of(Byte.class));
    assertEquals(2, Size.of(Short.class));
    assertEquals(2, Size.of(Character.class));
    assertEquals(4, Size.of(Integer.class));
    assertEquals(8, Size.of(Long.class));
    assertEquals(4, Size.of(Float.class));
    assertEquals(8, Size.of(Double.class));

    assertEquals(1, Size.of(byte.class));
    assertEquals(2, Size.of(short.class));
    assertEquals(2, Size.of(char.class));
    assertEquals(4, Size.of(int.class));
    assertEquals(8, Size.of(long.class));
    assertEquals(4, Size.of(float.class));
    assertEquals(8, Size.of(double.class));

    {
      byte b = 0;
      short s = 0;
      char c = 0;
      int i = 0;
      long l = 0;
      float f = 0;
      double d = 0;

      assertEquals(1, Size.of(b));
      assertEquals(2, Size.of(s));
      assertEquals(2, Size.of(c));
      assertEquals(4, Size.of(i));
      assertEquals(8, Size.of(l));
      assertEquals(4, Size.of(f));
      assertEquals(8, Size.of(d));
    }

    {
      Byte b = new Byte((byte) 0);
      Short s = new Short((short) 0);
      Character c = new Character((char) 0);
      Integer i = new Integer((int) 0);
      Long l = new Long((long) 0);
      Float f = new Float((float) 0);
      Double d = new Double((double) 0);

      assertEquals(1, Size.of(b));
      assertEquals(2, Size.of(s));
      assertEquals(2, Size.of(c));
      assertEquals(4, Size.of(i));
      assertEquals(8, Size.of(l));
      assertEquals(4, Size.of(f));
      assertEquals(8, Size.of(d));

    }
  }

  /**
   * Tests native type enum
   */
  @Test
  public void testNativeTypeEnum()
  {
    assertEquals(Size.BYTE, Size.of(NativeTypeEnum.UnsignedByte));
    assertEquals(Size.BYTE, Size.of(NativeTypeEnum.Byte));
    assertEquals(Size.SHORT, Size.of(NativeTypeEnum.UnsignedShort));
    assertEquals(Size.SHORT, Size.of(NativeTypeEnum.Short));
    assertEquals(Size.INT, Size.of(NativeTypeEnum.UnsignedInt));
    assertEquals(Size.INT, Size.of(NativeTypeEnum.Int));
    assertEquals(Size.LONG, Size.of(NativeTypeEnum.Long));
    assertEquals(Size.HALFFLOAT, Size.of(NativeTypeEnum.HalfFloat));
    assertEquals(Size.FLOAT, Size.of(NativeTypeEnum.Float));
    assertEquals(Size.DOUBLE, Size.of(NativeTypeEnum.Double));
  }

  /**
   * Tests strings
   */
  @Test
  public void testStrings()
  {
    assertEquals(0, Size.of(""));
    assertEquals(4 * Size.CHAR, Size.of("1234"));
  }

  /**
   * Tests NIO Buffers
   */
  @Test
  public void testNIOBuffers()
  {

    assertEquals(11, Size.of(ByteBuffer.allocateDirect(11)));

    assertEquals(2 * 11,
                 Size.of(ByteBuffer.allocateDirect(2 * 11)
                                   .asCharBuffer()));
    assertEquals(2 * 11,
                 Size.of(ByteBuffer.allocateDirect(2 * 11)
                                   .asShortBuffer()));
    assertEquals(4 * 11,
                 Size.of(ByteBuffer.allocateDirect(4 * 11)
                                   .asIntBuffer()));
    assertEquals(8 * 11,
                 Size.of(ByteBuffer.allocateDirect(8 * 11)
                                   .asLongBuffer()));
    assertEquals(4 * 11,
                 Size.of(ByteBuffer.allocateDirect(4 * 11)
                                   .asFloatBuffer()));
    assertEquals(8 * 11,
                 Size.of(ByteBuffer.allocateDirect(8 * 11)
                                   .asDoubleBuffer()));

  }

  /**
   * Tests off heap memory
   */
  @Test
  public void testOffHeapMemory()
  {
    OffHeapMemory lOffHeapMemory = OffHeapMemory.allocateBytes(11);
    ContiguousMemoryInterface lContiguousMemoryInterface =
                                                         OffHeapMemory.allocateBytes(11);
    assertEquals(11, Size.of(lOffHeapMemory));
    assertEquals(11, Size.of(lContiguousMemoryInterface));
  }

  /**
   * Tests BridJ pointer
   */
  @Test
  public void testBridJPointer()
  {
    Pointer<Byte> lPointer = Pointer.allocateBytes(11);
    assertEquals(11, Size.of(lPointer));
  }

}
