package coremem.test;

import coremem.ContiguousMemoryInterface;
import coremem.enums.MemoryType;
import coremem.interfaces.Copyable;
import coremem.interfaces.MappableMemory;
import coremem.interfaces.RangeCopyable;
import coremem.interfaces.Resizable;
import coremem.offheap.OffHeapMemoryAccess;

import static org.junit.Assert.*;

/**
 * Helper class for contiguous memory tests
 *
 * @author royer
 */
public class ContiguousMemoryTestsHelper
{

  /**
   * Tests basic functionality of a contiguous memory interface
   *
   * @param pContiguousMemoryInterface contiguous memory
   * @param pMemoryType                memory type
   * @param pResize                    true -> attempt resize
   */
  public static void testBasics(ContiguousMemoryInterface pContiguousMemoryInterface, MemoryType pMemoryType, final boolean pResize)
  {
    final long lLength = pContiguousMemoryInterface.getSizeInBytes();

    if (pContiguousMemoryInterface instanceof MappableMemory) ((MappableMemory) pContiguousMemoryInterface).map();

    assertTrue(pContiguousMemoryInterface.getAddress() != 0L);

    assertTrue(pContiguousMemoryInterface.getMemoryType() == pMemoryType);

    assertFalse(pContiguousMemoryInterface.isFree());

    if (pResize)
    {
      if (pContiguousMemoryInterface instanceof Resizable) ((Resizable) pContiguousMemoryInterface).resize(lLength / 2);

      assertTrue(pContiguousMemoryInterface.getSizeInBytes() == lLength / 2);

      if (pContiguousMemoryInterface instanceof Resizable) ((Resizable) pContiguousMemoryInterface).resize(lLength);

      assertTrue(pContiguousMemoryInterface.getSizeInBytes() == lLength);
    }

    if (pContiguousMemoryInterface instanceof MappableMemory) ((MappableMemory) pContiguousMemoryInterface).unmap();

    pContiguousMemoryInterface.free();

  }

  /**
   * Tests the copy from one memory to another
   *
   * @param pContiguousMemoryInterface1 memory 1
   * @param pContiguousMemoryInterface2 memory 2
   */
  public static void testCopySameSize(ContiguousMemoryInterface pContiguousMemoryInterface1, ContiguousMemoryInterface pContiguousMemoryInterface2)
  {
    if (pContiguousMemoryInterface1 instanceof Copyable<?>)
    {
      if (pContiguousMemoryInterface1 instanceof MappableMemory) ((MappableMemory) pContiguousMemoryInterface1).map();
      if (pContiguousMemoryInterface2 instanceof MappableMemory) ((MappableMemory) pContiguousMemoryInterface2).map();

      // OffHeapMemory lRAMDirect1 = new OffHeapMemory(1L *
      // Integer.MAX_VALUE);
      // OffHeapMemory lRAMDirect2 = new OffHeapMemory(1L *
      // Integer.MAX_VALUE);
      // System.out.println(lRAMDirect1.getLength() / (1024 * 1024));

      OffHeapMemoryAccess.setByte(pContiguousMemoryInterface1.getAddress(), (byte) 123);

      OffHeapMemoryAccess.setByte(pContiguousMemoryInterface1.getAddress() + pContiguousMemoryInterface1.getSizeInBytes() / 2, (byte) 456);

      pContiguousMemoryInterface1.copyTo(pContiguousMemoryInterface2);

      assertEquals((byte) 123, OffHeapMemoryAccess.getByte(pContiguousMemoryInterface2.getAddress()));
      assertEquals((byte) 456, OffHeapMemoryAccess.getByte(pContiguousMemoryInterface2.getAddress() + pContiguousMemoryInterface1.getSizeInBytes() / 2));

      pContiguousMemoryInterface1.free();
      pContiguousMemoryInterface2.free();

      try
      {
        pContiguousMemoryInterface1.copyTo(pContiguousMemoryInterface2);
        fail();
      } catch (final Throwable e)
      {
      }

      if (pContiguousMemoryInterface1 instanceof MappableMemory) ((MappableMemory) pContiguousMemoryInterface1).unmap();
      if (pContiguousMemoryInterface2 instanceof MappableMemory) ((MappableMemory) pContiguousMemoryInterface2).unmap();
    }
  }

  /**
   * Tests the copy of a range between two different memory objects
   *
   * @param pContiguousMemoryInterface1 memory 1
   * @param pContiguousMemoryInterface2 memory 2
   */
  @SuppressWarnings("unchecked")
  public static void testCopyRange(ContiguousMemoryInterface pContiguousMemoryInterface1, ContiguousMemoryInterface pContiguousMemoryInterface2)
  {
    // OffHeapMemory lRAMDirect1 = new OffHeapMemory(4);
    // OffHeapMemory lRAMDirect2 = new OffHeapMemory(8);
    // System.out.println(lRAMDirect1.getLength() / (1024 * 1024));

    if (pContiguousMemoryInterface1 instanceof MappableMemory) ((MappableMemory) pContiguousMemoryInterface1).map();
    if (pContiguousMemoryInterface2 instanceof MappableMemory) ((MappableMemory) pContiguousMemoryInterface2).map();

    OffHeapMemoryAccess.setByte(pContiguousMemoryInterface1.getAddress() + 2, (byte) 123);

    OffHeapMemoryAccess.setByte(pContiguousMemoryInterface1.getAddress() + 3, (byte) 111);

    if (pContiguousMemoryInterface1 instanceof Copyable<?>)
    {
      ((RangeCopyable<ContiguousMemoryInterface>) pContiguousMemoryInterface1).copyRangeTo(2, pContiguousMemoryInterface2, 4, 2);

      assertEquals((byte) 123, OffHeapMemoryAccess.getByte(pContiguousMemoryInterface2.getAddress() + 4));
      assertEquals((byte) 111, OffHeapMemoryAccess.getByte(pContiguousMemoryInterface2.getAddress() + 5));

      pContiguousMemoryInterface1.free();
      pContiguousMemoryInterface2.free();

      try
      {
        ((RangeCopyable<ContiguousMemoryInterface>) pContiguousMemoryInterface1).copyRangeTo(2, pContiguousMemoryInterface2, 4, 2);
        fail();
      } catch (final Throwable e)
      {
      }
    }

    if (pContiguousMemoryInterface1 instanceof MappableMemory) ((MappableMemory) pContiguousMemoryInterface1).unmap();
    if (pContiguousMemoryInterface2 instanceof MappableMemory) ((MappableMemory) pContiguousMemoryInterface2).unmap();
  }

  /**
   * tests that copy range checks work
   *
   * @param pContiguousMemoryInterface1 memory 1
   * @param pContiguousMemoryInterface2 memory 2
   */
  @SuppressWarnings("unchecked")
  public static void testCopyChecks(ContiguousMemoryInterface pContiguousMemoryInterface1, ContiguousMemoryInterface pContiguousMemoryInterface2)
  {
    // OffHeapMemory lRAMDirect1 = new OffHeapMemory(4);
    // OffHeapMemory lRAMDirect2 = new OffHeapMemory(8);
    if (pContiguousMemoryInterface1 instanceof Copyable<?>)
    {
      try
      {
        ((RangeCopyable<ContiguousMemoryInterface>) pContiguousMemoryInterface1).copyRangeTo(2, pContiguousMemoryInterface2, 4, 3);
        fail();
      } catch (final Throwable e1)
      {
      }

      try
      {
        ((RangeCopyable<ContiguousMemoryInterface>) pContiguousMemoryInterface1).copyRangeTo(2, pContiguousMemoryInterface2, 4, -2);
        fail();
      } catch (final Throwable e1)
      {
      }

      try
      {
        ((RangeCopyable<ContiguousMemoryInterface>) pContiguousMemoryInterface1).copyRangeTo(2, pContiguousMemoryInterface1, 4, 9);
        fail();
      } catch (final Throwable e1)
      {
      }

      pContiguousMemoryInterface1.free();
      pContiguousMemoryInterface2.free();

      try
      {
        ((RangeCopyable<ContiguousMemoryInterface>) pContiguousMemoryInterface1).copyRangeTo(2, pContiguousMemoryInterface2, 4, 2);
        fail();
      } catch (final Throwable e)
      {
      }
    }
  }

  /**
   * Checks read write single native types
   *
   * @param pContiguousMemoryInterface memory
   */
  public static void testWriteRead(ContiguousMemoryInterface pContiguousMemoryInterface)
  {
    // OffHeapMemory lRAMDirect = new OffHeapMemory(4);

    if (pContiguousMemoryInterface instanceof MappableMemory) ((MappableMemory) pContiguousMemoryInterface).map();

    pContiguousMemoryInterface.setByteAligned(0, (byte) 255);
    pContiguousMemoryInterface.setByteAligned(1, (byte) 255);
    pContiguousMemoryInterface.setByteAligned(2, (byte) 255);
    pContiguousMemoryInterface.setByteAligned(3, (byte) 255);

    assertEquals(Character.MAX_VALUE, pContiguousMemoryInterface.getCharAligned(0));
    assertEquals(-1, pContiguousMemoryInterface.getShortAligned(0));
    assertEquals(-1, pContiguousMemoryInterface.getIntAligned(0));

    assertEquals(Double.NaN, pContiguousMemoryInterface.getFloatAligned(0), 0);

    if (pContiguousMemoryInterface instanceof MappableMemory) ((MappableMemory) pContiguousMemoryInterface).unmap();
  }

}
