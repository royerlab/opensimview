package coremem.memmap.test;

import coremem.ContiguousMemoryInterface;
import coremem.enums.MemoryType;
import coremem.memmap.FileMappedMemoryRegion;
import coremem.test.ContiguousMemoryTestsHelper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * File mapped memory region tests
 *
 * @author royer
 */
public class FileMappedMemoryRegionTests
{

  private static final long cMemoryRegionSize = 1024;

  /**
   * Tests large memory maps
   *
   * @throws IOException NA
   */
  @Test
  public void testLargeMemory() throws IOException
  {
    ContiguousMemoryInterface lContiguousMemoryInterface = new FileMappedMemoryRegion(createTempFile(), 1000, (long) (1.1 * Integer.MAX_VALUE));
    ContiguousMemoryTestsHelper.testBasics(lContiguousMemoryInterface, MemoryType.FILERAM, false);

  }

  /**
   * Tests basics
   *
   * @throws IOException NA
   */
  @Test
  public void testBasics() throws IOException
  {
    ContiguousMemoryInterface lContiguousMemoryInterface = new FileMappedMemoryRegion(createTempFile(), 1000, cMemoryRegionSize);

    ContiguousMemoryTestsHelper.testBasics(lContiguousMemoryInterface, MemoryType.FILERAM, false);
  }

  private File createTempFile() throws IOException
  {
    File lCreateTempFile = File.createTempFile(FileMappedMemoryRegionTests.class.toString(), "" + Math.random());
    lCreateTempFile.deleteOnExit();
    return lCreateTempFile;
  }

  /**
   * tests same size copy
   *
   * @throws IOException NA
   */
  @Test
  public void testCopySameSize() throws IOException
  {
    ContiguousMemoryInterface lContiguousMemoryInterface1 = new FileMappedMemoryRegion(createTempFile(), 1000, cMemoryRegionSize);
    ContiguousMemoryInterface lContiguousMemoryInterface2 = new FileMappedMemoryRegion(createTempFile(), 1000, cMemoryRegionSize);

    ContiguousMemoryTestsHelper.testCopySameSize(lContiguousMemoryInterface1, lContiguousMemoryInterface2);

  }

  /**
   * @throws IOException NA
   */
  @Test
  public void testCopyDifferentSize() throws IOException
  {
    ContiguousMemoryInterface lContiguousMemoryInterface1 = new FileMappedMemoryRegion(createTempFile(), 1, 4);
    ContiguousMemoryInterface lContiguousMemoryInterface2 = new FileMappedMemoryRegion(createTempFile(), 1, 8);

    ContiguousMemoryTestsHelper.testCopyRange(lContiguousMemoryInterface1, lContiguousMemoryInterface2);
  }

  /**
   * @throws IOException NA
   */
  @Test
  public void testCopyChecks() throws IOException
  {
    ContiguousMemoryInterface lContiguousMemoryInterface1 = new FileMappedMemoryRegion(createTempFile(), 1, 4);
    ContiguousMemoryInterface lContiguousMemoryInterface2 = new FileMappedMemoryRegion(createTempFile(), 1, 8);

    ContiguousMemoryTestsHelper.testCopyChecks(lContiguousMemoryInterface1, lContiguousMemoryInterface2);
  }

  /**
   * @throws IOException NA
   */
  @Test
  public void testWriteRead() throws IOException
  {
    ContiguousMemoryInterface lContiguousMemoryInterface = new FileMappedMemoryRegion(createTempFile(), 1, 4);

    ContiguousMemoryTestsHelper.testWriteRead(lContiguousMemoryInterface);
  }
}
