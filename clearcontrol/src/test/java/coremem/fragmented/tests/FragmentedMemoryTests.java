package coremem.fragmented.test;

import coremem.ContiguousMemoryInterface;
import coremem.fragmented.FragmentedMemory;
import coremem.offheap.OffHeapMemory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Fragmented memory tests
 *
 * @author royer
 */
public class FragmentedMemoryTests
{

  /**
   * Tests read and write to file channel
   *
   * @throws IOException NA
   */
  @Test
  public void testWriteToReadFromFileChannel() throws IOException
  {
    final File lTempFile = File.createTempFile(this.getClass().getSimpleName(), "testWriteToReadFromFileChannel");
    // System.out.println(lTempFile);
    lTempFile.delete();
    lTempFile.deleteOnExit();

    final FileChannel lFileChannel1 = FileChannel.open(lTempFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);

    final FragmentedMemory lFragmentedMemory1 = new FragmentedMemory();

    for (int i = 0; i < 100; i++)
    {
      // System.out.format("Writing block %d \n", i);
      final OffHeapMemory lOffHeapMemory = new OffHeapMemory(129);

      for (int j = 0; j < lOffHeapMemory.getSizeInBytes(); j++)
        lOffHeapMemory.setByte(j, (byte) (i + j));

      lFragmentedMemory1.add(lOffHeapMemory);
    }

    lFragmentedMemory1.writeBytesToFileChannel(lFileChannel1, 511);
    lFragmentedMemory1.free();
    lFileChannel1.close();

    assertTrue(lTempFile.exists());
    assertEquals(511 + 100 * 129, lTempFile.length());

    final FileChannel lFileChannel2 = FileChannel.open(lTempFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);

    assertEquals(511 + 100 * 129, lFileChannel2.size());

    final FragmentedMemory lFragmentedMemory2 = new FragmentedMemory();
    for (int i = 0; i < 100; i++)
    {
      final OffHeapMemory lOffHeapMemory = new OffHeapMemory(129);
      lFragmentedMemory2.add(lOffHeapMemory);
    }

    lFragmentedMemory2.readBytesFromFileChannel(lFileChannel2, 511, lFragmentedMemory2.getSizeInBytes());

    for (int i = 0; i < 100; i++)
    {
      // System.out.println(i);
      final ContiguousMemoryInterface lContiguousMemoryInterface = lFragmentedMemory2.get(i);
      for (int j = 0; j < lContiguousMemoryInterface.getSizeInBytes(); j++)
        assertEquals((byte) (i + j), lContiguousMemoryInterface.getByte(j));
    }

    lFragmentedMemory2.free();
    lFileChannel2.close();
  }

  /**
   * Tests even split
   *
   * @throws IOException NA
   */
  @Test
  public void testSplitEven() throws IOException
  {
    final ContiguousMemoryInterface lMemory = OffHeapMemory.allocateShorts(3 * 5);
    final FragmentedMemory lSplit = FragmentedMemory.split(lMemory, 3);

    assertEquals(3, lSplit.getNumberOfFragments());
    assertEquals(2 * 5, lSplit.get(0).getSizeInBytes());
  }

  /**
   * Tests uneven split
   *
   * @throws IOException NA
   */
  @Test
  public void testSplitUneven() throws IOException
  {
    final ContiguousMemoryInterface lMemory = OffHeapMemory.allocateShorts(3 * 5 + 1);
    final FragmentedMemory lSplit = FragmentedMemory.split(lMemory, 3);

    assertEquals(3, lSplit.getNumberOfFragments());
    assertEquals(2 * 5, lSplit.get(0).getSizeInBytes());
    assertEquals(2 * (5 + 1), lSplit.get(2).getSizeInBytes());
  }

  /**
   * tests byte buffer
   *
   * @throws IOException NA
   */
  @Test
  public void testByteBuffers() throws IOException
  {
    final FragmentedMemory lFragmentedMemory = new FragmentedMemory();

    for (int i = 0; i < 10; i++)
    {
      ByteBuffer lByteBuffer = ByteBuffer.allocateDirect(10);

      lByteBuffer.clear();
      while (lByteBuffer.hasRemaining()) lByteBuffer.put((byte) i);
      lByteBuffer.clear();

      lFragmentedMemory.add(lByteBuffer);
    }

    for (int i = 0; i < 10; i++)
    {
      ContiguousMemoryInterface lContiguousMemoryInterface = lFragmentedMemory.get(i);

      byte lByte = lContiguousMemoryInterface.getByte(0);

      assertEquals(i, lByte, 0);
    }

  }

  /**
   * tests consolidate fragmented memory to single off heap memory
   *
   * @throws IOException NA
   */
  @Test
  public void testConsolidate() throws IOException
  {
    final FragmentedMemory lFragmentedMemory = new FragmentedMemory();

    for (int i = 0; i < 10; i++)
    {
      ByteBuffer lByteBuffer = ByteBuffer.allocateDirect(10);

      lByteBuffer.clear();
      while (lByteBuffer.hasRemaining()) lByteBuffer.put((byte) i);
      lByteBuffer.clear();

      lFragmentedMemory.add(lByteBuffer);
    }

    @SuppressWarnings("unused") OffHeapMemory lMakeConsolidatedCopy = lFragmentedMemory.makeConsolidatedCopy();

  }

}
