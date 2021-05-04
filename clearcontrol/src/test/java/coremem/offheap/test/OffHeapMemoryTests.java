package coremem.offheap.test;

import coremem.enums.MemoryType;
import coremem.offheap.OffHeapMemory;
import coremem.offheap.OffHeapMemoryAccess;
import coremem.rgc.RessourceCleaner;
import coremem.test.ContiguousMemoryTestsHelper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * OffHeap memory tests
 *
 * @author royer
 */
public class OffHeapMemoryTests
{

  /**
   * Tests basics
   */
  @Test
  public void testBasics()
  {
    final OffHeapMemory lOffHeapMemory = new OffHeapMemory(2L * Integer.MAX_VALUE);

    ContiguousMemoryTestsHelper.testBasics(lOffHeapMemory, MemoryType.CPURAMDIRECT, true);

  }

  /**
   * Tests resso
   *
   * @throws InterruptedException NA
   */
  @Test
  public void testRessourceCleaning() throws InterruptedException
  {
    // Forces the loading of the Ressource Cleaner...
    RessourceCleaner.cleanNow();

    final long lInitialNumberOfRegisteredObjects = RessourceCleaner.getNumberOfRegisteredObjects();

    final long lInitialAllocatedMemory = OffHeapMemoryAccess.getTotalAllocatedMemory();
    // System.out.println(lInitialAllocatedMemory);

    for (int i = 0; i < 100; i++)
    {
      final OffHeapMemory lOffHeapMemory = new OffHeapMemory(1024);
      // System.out.println("Allocated: " + lOffHeapMemory.getAddress());
      lOffHeapMemory.setByte(i % lOffHeapMemory.getSizeInBytes(), (byte) i);
      System.gc();
      // System.out.println("A * OffHeapMemoryAccess.getTotalAllocatedMemory()="
      // + OffHeapMemoryAccess.getTotalAllocatedMemory());
      Thread.sleep(1);
    }

    for (int i = 0; i < 1000; i++)
    {
      System.gc();
      // System.out.println("B * OffHeapMemoryAccess.getTotalAllocatedMemory()="
      // + OffHeapMemoryAccess.getTotalAllocatedMemory());
      Thread.sleep(1);
      // System.out.println(OffHeapMemoryAccess.getTotalAllocatedMemory());

      if (OffHeapMemoryAccess.getTotalAllocatedMemory() == 0 && OffHeapMemoryAccess.getTotalAllocatedMemory() == 0)
        break;
    }

    assertTrue(RessourceCleaner.getNumberOfRegisteredObjects() <= lInitialNumberOfRegisteredObjects);
    assertTrue(OffHeapMemoryAccess.getTotalAllocatedMemory() <= lInitialAllocatedMemory);

  }

  /**
   * Tests copy same size.
   */
  @Test
  public void testCopySameSize()
  {
    final OffHeapMemory lOffHeapMemory1 = new OffHeapMemory(1L * Integer.MAX_VALUE);
    final OffHeapMemory lOffHeapMemory2 = new OffHeapMemory(1L * Integer.MAX_VALUE);

    ContiguousMemoryTestsHelper.testCopySameSize(lOffHeapMemory1, lOffHeapMemory2);

  }

  /**
   * Tests copy different size
   */
  @Test
  public void testCopyDifferentSize()
  {
    final OffHeapMemory lOffHeapMemory1 = new OffHeapMemory(4);
    final OffHeapMemory lOffHeapMemory2 = new OffHeapMemory(8);

    ContiguousMemoryTestsHelper.testCopyRange(lOffHeapMemory1, lOffHeapMemory2);
  }

  /**
   * TEsts copy checks
   */
  @Test
  public void testCopyChecks()
  {
    final OffHeapMemory lOffHeapMemory1 = new OffHeapMemory(4);
    final OffHeapMemory lOffHeapMemory2 = new OffHeapMemory(8);

    ContiguousMemoryTestsHelper.testCopyChecks(lOffHeapMemory1, lOffHeapMemory2);
  }

  /**
   * Tests write read.
   */
  @Test
  public void testWriteRead()
  {
    final OffHeapMemory lOffHeapMemory = new OffHeapMemory(4);

    ContiguousMemoryTestsHelper.testWriteRead(lOffHeapMemory);
  }

  /**
   * tests write and read to from file channel
   *
   * @throws IOException NA
   */
  @Test
  public void testWriteToReadFromFileChannel() throws IOException
  {
    final File lTempFile = File.createTempFile(this.getClass().getSimpleName(), "testWriteToReadFromFileChannel");
    // System.out.println(lTempFile);
    lTempFile.delete();
    // lTempFile.deleteOnExit();

    final FileChannel lFileChannel1 = FileChannel.open(lTempFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);

    final OffHeapMemory lOffHeapMemory1 = new OffHeapMemory(1023);

    for (int i = 0; i < lOffHeapMemory1.getSizeInBytes(); i++)
      lOffHeapMemory1.setByte(i, (byte) i);

    lOffHeapMemory1.writeBytesToFileChannel(lFileChannel1, 512);
    lOffHeapMemory1.free();
    lFileChannel1.close();

    assertTrue(lTempFile.exists());
    assertEquals(512 + 1023, lTempFile.length());

    final FileChannel lFileChannel2 = FileChannel.open(lTempFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);

    assertEquals(512 + 1023, lFileChannel2.size());

    final OffHeapMemory lOffHeapMemory2 = new OffHeapMemory(1023);
    lOffHeapMemory2.readBytesFromFileChannel(lFileChannel2, 512, lOffHeapMemory2.getSizeInBytes());

    for (int i = 0; i < lOffHeapMemory2.getSizeInBytes(); i++)
      assertEquals((byte) i, lOffHeapMemory2.getByte(i));

    lOffHeapMemory2.free();
    lFileChannel2.close();
  }
}
