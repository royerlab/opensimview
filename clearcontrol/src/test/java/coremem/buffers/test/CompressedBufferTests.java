package coremem.buffers.test;

import coremem.ContiguousMemoryInterface;
import coremem.buffers.CompressedBuffer;
import coremem.buffers.DecompressedBuffer;
import coremem.offheap.OffHeapMemory;
import org.junit.Test;

/**
 * Contiguous buffer tests
 *
 * @author royer
 */
public class CompressedBufferTests
{

  /**
   * Tests
   */
  @Test
  public void roundTripTest()
  {
    int cNumberOfChunks = 3;
    long cRawDataLength = 102400L;
    long cMaxCompressedData = cNumberOfChunks * (cRawDataLength + 32);
    long cMaxDecompressedData = cNumberOfChunks * cRawDataLength;

    CompressedBuffer lCompressedBuffer = new CompressedBuffer(OffHeapMemory.allocateBytes(cMaxCompressedData));


    final OffHeapMemory lRawData = OffHeapMemory.allocateBytes(cRawDataLength);

    for (int c = 0; c < cNumberOfChunks; c++)
    {
      for (int i = 0; i < cRawDataLength; i++)
        lRawData.setByte(i, (byte) (i ^ (i * (1 + c))));

      lCompressedBuffer.writeCompressedMemory(lRawData);
    }

    ContiguousMemoryInterface lCompressedMemory = lCompressedBuffer.getCompressedContiguousMemory();


    DecompressedBuffer lDecompressedBuffer = new DecompressedBuffer(OffHeapMemory.allocateBytes(cMaxDecompressedData));
    lDecompressedBuffer.writeDecompressedMemory(lCompressedMemory);

    ContiguousMemoryInterface lDecompressedRawData = lDecompressedBuffer.getDecompressedContiguousMemory();

    for (int c = 0; c < cNumberOfChunks; c++)
      for (int i = 0; i < cRawDataLength; i++)
      {
        //System.out.println("value: "+lDecompressedRawData.getByte(cRawDataLength*c+i));
        assert (((byte) (i ^ (i * (1 + c)))) == lDecompressedRawData.getByte(cRawDataLength * c + i));
      }

    assert lCompressedMemory.getSizeInBytes() < cNumberOfChunks * lRawData.getSizeInBytes();

    System.out.println("Raw        size: " + cNumberOfChunks * lRawData.getSizeInBytes());
    System.out.println("Compressed size: " + lCompressedMemory.getSizeInBytes());

  }

  /**
   * Tests
   */
  @Test
  public void roundTripTestVeryLargeData()
  {
    long cRawDataLength = 2147483647L + 4491L; // a big number, bigger than 'bigint'
    long cMaxCompressedData = (cRawDataLength + 32L);
    long cMaxDecompressedData = cRawDataLength;

    System.out.println("            OffHeapMemory.allocateBytes(cRawDataLength);");
    final OffHeapMemory lRawData = OffHeapMemory.allocateBytes(cRawDataLength);

    System.out.println("    for (int i = 0; i < cRawDataLength; i++)  setByte");
    for (long i = 0; i < cRawDataLength; i++)
    {
      byte lValue = (byte) ((int) (i + 1L));
      lRawData.setByte(i, lValue);
//      if (i%(1<<21)==0)
//        System.out.println("index: "+i+" value: "+lValue);
    }

    System.out.println("    CompressedBuffer lCompressedBuffer = new CompressedBuffer(OffHeapMemory.allocateBytes(cMaxCompressedData));");
    CompressedBuffer lCompressedBuffer = new CompressedBuffer(OffHeapMemory.allocateBytes(cMaxCompressedData));

    System.out.println("    lCompressedBuffer.writeCompressedMemory(lRawData);");
    lCompressedBuffer.writeCompressedMemory(lRawData);

    ContiguousMemoryInterface lCompressedMemory = lCompressedBuffer.getCompressedContiguousMemory();


    DecompressedBuffer lDecompressedBuffer = new DecompressedBuffer(OffHeapMemory.allocateBytes(cMaxDecompressedData));
    System.out.println("    lDecompressedBuffer.writeDecompressedMemory(lCompressedMemory);");
    lDecompressedBuffer.writeDecompressedMemory(lCompressedMemory);

    ContiguousMemoryInterface lDecompressedRawData = lDecompressedBuffer.getDecompressedContiguousMemory();

    assert lDecompressedRawData.getSizeInBytes() == lRawData.getSizeInBytes();

    for (long i = 0; i < 256; i++)
      System.out.println("index: " + i + " raw value: " + lRawData.getByte(i) + " decompressed value: " + lDecompressedRawData.getByte(i));

    System.out.println("    for (int i=0; i<cRawDataLength; i++)  getByte ");
    for (long i = 0; i < cRawDataLength; i++)
    {
      if (lRawData.getByte(i) != lDecompressedRawData.getByte(i))
        System.out.println("index: " + i + " raw value: " + lRawData.getByte(i) + " decompressed value: " + lDecompressedRawData.getByte(i));
      assert (lRawData.getByte(i) == lDecompressedRawData.getByte(i));
    }

    System.out.println("assert lCompressedMemory.getSizeInBytes() < lRawData.getSizeInBytes();");
    assert lCompressedMemory.getSizeInBytes() < lRawData.getSizeInBytes();

    System.out.println("Raw        size: " + lRawData.getSizeInBytes());
    System.out.println("Compressed size: " + lCompressedMemory.getSizeInBytes());

  }

}
