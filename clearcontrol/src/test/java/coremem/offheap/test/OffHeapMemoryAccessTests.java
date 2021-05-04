package coremem.offheap.test;

import coremem.exceptions.InvalidNativeMemoryAccessException;
import coremem.offheap.OffHeapMemoryAccess;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Off heap memory access tests
 *
 * @author royer
 */
public class OffHeapMemoryAccessTests
{
  final private static long cBufferSize = 2 * (long) Integer.MAX_VALUE;

  /**
   * tests max allocation
   */
  @Test
  public void testMaxAllocation()
  {
    try
    {
      int i = 0;
      try
      {
        OffHeapMemoryAccess.freeAll();
        OffHeapMemoryAccess.setMaximumAllocatableMemory(1000L * 1000L);
        for (; i < 2000; i++)
        {
          OffHeapMemoryAccess.allocateMemory(1000);
        }
        fail();
      } catch (final OutOfMemoryError e)
      {
        // System.out.println("i=" + i);
        assertTrue(i >= 1000);
      } catch (final Throwable lE)
      {
        fail();
      }

      OffHeapMemoryAccess.freeAll();
      assertEquals(0, OffHeapMemoryAccess.getTotalAllocatedMemory());
    } catch (final Throwable e)
    {
      e.printStackTrace();
      fail();
    } finally
    {
      OffHeapMemoryAccess.setMaximumAllocatableMemory(Long.MAX_VALUE);
    }
  }

  /**
   * Tests reallocate and free
   */
  @Test
  public void testAllocateReallocateFree()
  {
    try
    {
      // System.out.println(cBufferSize);
      final long lAddress = OffHeapMemoryAccess.allocateMemory(cBufferSize);

      OffHeapMemoryAccess.setByte(lAddress, (byte) 123);
      assertEquals(OffHeapMemoryAccess.getByte(lAddress), (byte) 123);

      final long lAddressReallocated = OffHeapMemoryAccess.reallocateMemory(lAddress, 10);

      OffHeapMemoryAccess.setByte(lAddressReallocated + 9, (byte) 123);
      assertEquals(OffHeapMemoryAccess.getByte(lAddressReallocated + 9), (byte) 123);

      OffHeapMemoryAccess.freeMemory(lAddressReallocated);
    } catch (final InvalidNativeMemoryAccessException e)
    {
      e.printStackTrace();
      fail();
    }

  }

  /**
   * Tests super big allocation
   */
  @Test
  public void testSuperBig()
  {
    // System.out.println("begin");

    try
    {
      final long lLength = 1L * 1000L * 1000L * 1000L;

      // System.out.println("allocateMemory");
      final long lAddress = OffHeapMemoryAccess.allocateMemory(lLength);
      // System.out.println("lAddress=" + lAddress);
      assertFalse(lAddress == 0);

      // System.out.println("setMemory");
      OffHeapMemoryAccess.fillMemory(lAddress, lLength, (byte) 0);

      // System.out.println("setByte(s)");
      for (long i = 0; i < lLength; i += 1000L * 1000L)
      {
        OffHeapMemoryAccess.setByte(lAddress + i, (byte) i);
      }

      // System.out.println("getByte(s)");
      for (long i = 0; i < lLength; i += 1000L * 1000L)
      {
        final byte lValue = OffHeapMemoryAccess.getByte(lAddress + i);
        assertEquals((byte) i, lValue);
      }

      // Thread.sleep(10000);

      // System.out.println("freeMemory");
      OffHeapMemoryAccess.freeMemory(lAddress);

      // System.out.println("end");
    } catch (final InvalidNativeMemoryAccessException e)
    {
      e.printStackTrace();
      fail();
    }
  }

}
