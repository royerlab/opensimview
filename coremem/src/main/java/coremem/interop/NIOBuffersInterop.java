package coremem.interop;

import static java.lang.Math.min;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import coremem.ContiguousMemoryInterface;
import coremem.exceptions.UnsupportedWrappingException;
import coremem.offheap.OffHeapMemory;
import coremem.util.Size;

import org.bridj.JNI;

/**
 * NIO buffers interoperability
 *
 * @author royer
 */
@SuppressWarnings("deprecation")
public class NIOBuffersInterop
{
  /**
   * Returns a contiguous buffer from a NIO buffer
   * 
   * @param pBuffer
   *          NIO buffer
   * @return contiguous memory
   */
  public static OffHeapMemory getContiguousMemoryFrom(Buffer pBuffer)
  {

    final long lSizeInBytes = Size.of(pBuffer);

    final OffHeapMemory lOffHeapMemory = getContiguousMemoryFrom(pBuffer, lSizeInBytes);

    return lOffHeapMemory;
  }

  /**
   * Returns a contiguous buffer from a NIO buffer
   *
   * @param pBuffer
   *          NIO buffer
   *
   * @param pSizeInBytes
   *
   * @return contiguous memory
   */
  public static OffHeapMemory getContiguousMemoryFrom(Buffer pBuffer, long pSizeInBytes)
  {
    if (!pBuffer.isDirect())
      throw new UnsupportedWrappingException("Cannot wrap a non-native NIO Buffer");

    final long lBufferAddress = getAddress(pBuffer);

    final OffHeapMemory lOffHeapMemory =
            new OffHeapMemory(pBuffer,
                    lBufferAddress,
                    pSizeInBytes);
    return lOffHeapMemory;
  }

  private static long getAddress(Buffer buffer)
  {
    Field lField = null;
    try
    {
      lField = Buffer.class.getDeclaredField("address");
      lField.setAccessible(true);
      return lField.getLong(buffer);
    }
    catch (final Throwable e)
    {
      e.printStackTrace();
    }
    finally
    {
      if (lField != null)
      {
        lField.setAccessible(false);
      }
    }
    return 0;
  }

  /**
   * This method creates a list of ByteBuffers that cover sequentially a given
   * ContiguousMemory region.
   * @param pContiguousMemory
   *          contiguous memory
   * @return array of NIO byte buffers corresponding to given contiguous memory
   *         region
   */
  public static ArrayList<ByteBuffer> getByteBuffersForContiguousMemory(ContiguousMemoryInterface pContiguousMemory)
  {
    return getByteBuffersForContiguousMemory(pContiguousMemory,
            0,
            pContiguousMemory.getSizeInBytes()
            );
  }

  /**
   * This method creates a list of ByteBuffers that cover sequentially a given
   * ContiguousMemory region. This should only be used within the CoreMem
   * classes. It's use is tricky... IMPORTANT: the bytebuffers returned do not
   * hold references to the parent responsible for the memory lifecycle. This
   * means that the references of these bytebuffers cannot escape the scope
   * within which the ContiguousMemory reference is held. If the GC cleans up
   * the ContiguousMemory and there is still a returned ByteBuffer 'alive', this
   * will necessarily lead to a segmentation fault.
   * 
   * @param pContiguousMemory
   *          contiguous memory
   * @param pPositionInBytes
   *          position in bytes
   * @param pLengthInBytes
   *          length in bytes
   * @return array of NIO byte buffers corresponding to given contiguous memory
   *         region
   */
  public static ArrayList<ByteBuffer> getByteBuffersForContiguousMemory(ContiguousMemoryInterface pContiguousMemory,
                                                                        long pPositionInBytes,
                                                                        long pLengthInBytes)
  {
    long lBufferSizeInBytes = pContiguousMemory.getSizeInBytes();

    long lLargestByteBufferSizeInBytes = Integer.MAX_VALUE - 1024; // a bit less
                                                                   // to be
                                                                   // safe...

    long lAddress = pContiguousMemory.getAddress() + pPositionInBytes;
    long lLeftToAssign = min(pLengthInBytes, lBufferSizeInBytes);

    int lEstimatedNumberOfChuncks =
                                  (int) (1
                                         + (lLeftToAssign
                                            / lLargestByteBufferSizeInBytes));
    ArrayList<ByteBuffer> lListOfBuffers =
                                         new ArrayList<ByteBuffer>(lEstimatedNumberOfChuncks);

    while (lLeftToAssign > 0)
    {
      long lChunkSize = min(lLeftToAssign,
                            lLargestByteBufferSizeInBytes);
      ByteBuffer lDirectByteBuffer =
                                   JNI.newDirectByteBuffer(lAddress,
                                                           lChunkSize);
      lListOfBuffers.add(lDirectByteBuffer);
      lAddress += lChunkSize;
      lLeftToAssign -= lChunkSize;
    }

    return lListOfBuffers;
  }
}
