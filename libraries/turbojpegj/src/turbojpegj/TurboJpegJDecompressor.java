package turbojpegj;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

import org.bridj.Pointer;

import turbojpeg.TurbojpegLibrary;
import turbojpeg.TurbojpegLibrary.TJPF;
import turbojpeg.utils.StopWatch;

public class TurboJpegJDecompressor implements AutoCloseable
{

  private Pointer<?> mPointerToDecompressor;

  private Pointer<Integer> mPointerToWidth;
  private Pointer<Integer> mPointerToHeight;
  private ByteBuffer mDecompressedImageByteBuffer;
  private Pointer<Byte> mPointerToDecompressedImageByteBuffer;

  private long mLastDecompressionElapsedTimeInMs;

  private int mQuality = 100;


  public TurboJpegJDecompressor()
  {
    super();
    mPointerToDecompressor = TurbojpegLibrary.tjInitDecompress();
    mPointerToWidth = Pointer.allocateInt();
    mPointerToHeight = Pointer.allocateInt();
  }

  @Override
  public void close() throws IOException
  {
    if (mPointerToDecompressor == null) return;

    TurbojpegLibrary.tjDestroy(mPointerToDecompressor);
    mPointerToDecompressor = null;
    mDecompressedImageByteBuffer = null;

  }

  public boolean decompressMonochrome(final ByteBuffer p8BitImageCompressedByteBuffer)
  {
    if (mPointerToDecompressor == null) return false;

    // allocateCompressedBuffer(p8BitImageByteBuffer.limit());
    final StopWatch lCompressionTime = StopWatch.start();

    final long lCompressedImageBufferSize = p8BitImageCompressedByteBuffer.limit();

    p8BitImageCompressedByteBuffer.position(0);


    final Pointer lPointerTo8BitImageCompressedByteBuffer = Pointer.pointerToBytes(p8BitImageCompressedByteBuffer);
    TurbojpegLibrary.tjDecompressHeader(mPointerToDecompressor, lPointerTo8BitImageCompressedByteBuffer, lCompressedImageBufferSize, mPointerToWidth, mPointerToHeight);
    lPointerTo8BitImageCompressedByteBuffer.release();

    final int lWidth = mPointerToWidth.getInt();
    final int lHeight = mPointerToHeight.getInt();

    allocateDecompressedBuffer(lWidth * lHeight);
    mDecompressedImageByteBuffer.rewind();

    final int lErrorCode = TurbojpegLibrary.tjDecompress2(mPointerToDecompressor, lPointerTo8BitImageCompressedByteBuffer, lCompressedImageBufferSize, mPointerToDecompressedImageByteBuffer, lWidth, 0, lHeight, (int) TJPF.TJPF_GRAY.value, TurbojpegLibrary.TJFLAG_NOREALLOC | TurbojpegLibrary.TJFLAG_FORCESSE3 | TurbojpegLibrary.TJFLAG_FASTDCT);

    mLastDecompressionElapsedTimeInMs = lCompressionTime.time(TimeUnit.MILLISECONDS);

    lPointerTo8BitImageCompressedByteBuffer.release();


    return lErrorCode == 0;
  }

  private void allocateDecompressedBuffer(final int pLength)
  {
    if (mDecompressedImageByteBuffer != null && mDecompressedImageByteBuffer.capacity() >= pLength) return;

    System.out.println("TurboJpegJCompressor: Allocating new buffer for decompressed image!");

    mDecompressedImageByteBuffer = ByteBuffer.allocateDirect(pLength).order(ByteOrder.nativeOrder());
    mPointerToDecompressedImageByteBuffer = Pointer.pointerToBytes(mDecompressedImageByteBuffer);

  }

  public ByteBuffer getDecompressedBuffer()
  {
    return mDecompressedImageByteBuffer;
  }

  public int getLastImageCompressionElapsedTimeInMs()
  {
    return (int) mLastDecompressionElapsedTimeInMs;
  }

  public int getQuality()
  {
    return mQuality;
  }

  public void setQuality(int quality)
  {
    mQuality = quality;
  }

}
