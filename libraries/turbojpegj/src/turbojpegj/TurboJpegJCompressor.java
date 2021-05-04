package turbojpegj;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

import org.bridj.CLong;
import org.bridj.Pointer;

import turbojpeg.TurbojpegLibrary;
import turbojpeg.TurbojpegLibrary.TJPF;
import turbojpeg.TurbojpegLibrary.TJSAMP;
import turbojpeg.utils.StopWatch;

public class TurboJpegJCompressor implements AutoCloseable
{

  private Pointer<?> mPointerToCompressor;

  private ByteBuffer mCompressedImageByteBuffer;
  private Pointer<Byte> mPointerTo8BitImageByteBuffer;
  private Pointer<Pointer<Byte>> mPointerToCompressedImageByteBufferPointer;
  private Pointer<CLong> mPointerToCompressedBufferEffectiveSize;
  private long mLastCompressionElapsedTimeInMs;
  private double mLastCompressionRatio;

  private int mQuality = 100;

  public TurboJpegJCompressor()
  {
    super();
    mPointerToCompressor = TurbojpegLibrary.tjInitCompress();
    mPointerToCompressedBufferEffectiveSize = Pointer.allocateCLong();
    mPointerToCompressedImageByteBufferPointer = Pointer.allocatePointer(Byte.class);
  }

  @Override
  public void close() throws IOException
  {
    if (mPointerToCompressor == null) return;

    TurbojpegLibrary.tjDestroy(mPointerToCompressor);
    mPointerToCompressor = null;
    mPointerToCompressedImageByteBufferPointer = null;
    mPointerToCompressedBufferEffectiveSize.release();

  }

  public boolean compressMonochrome(final int pWidth, final int pHeight, final ByteBuffer p8BitImageByteBuffer)
  {
    if (mPointerToCompressor == null) return false;
    allocateCompressedBuffer((int) (1.5 * p8BitImageByteBuffer.limit()));
    final StopWatch lCompressionTime = StopWatch.start();
    p8BitImageByteBuffer.position(0);

    final Pointer lPointerTo8BitImageByteBuffer = Pointer.pointerToBytes(p8BitImageByteBuffer);

    final int lErrorCode = TurbojpegLibrary.tjCompress2(mPointerToCompressor, lPointerTo8BitImageByteBuffer, pWidth, 0, pHeight, (int) TJPF.TJPF_GRAY.value, mPointerToCompressedImageByteBufferPointer, mPointerToCompressedBufferEffectiveSize, (int) TJSAMP.TJSAMP_GRAY.value, mQuality, TurbojpegLibrary.TJFLAG_NOREALLOC | TurbojpegLibrary.TJFLAG_FORCESSE3 | TurbojpegLibrary.TJFLAG_FASTDCT);
    mLastCompressionElapsedTimeInMs = lCompressionTime.time(TimeUnit.MILLISECONDS);
    mCompressedImageByteBuffer.limit((int) mPointerToCompressedBufferEffectiveSize.getCLong());
    mLastCompressionRatio = ((double) mCompressedImageByteBuffer.limit()) / p8BitImageByteBuffer.limit();

    lPointerTo8BitImageByteBuffer.release();
    return lErrorCode == 0;

  }

  private void allocateCompressedBuffer(final int pLength)
  {
    if (mCompressedImageByteBuffer != null && mCompressedImageByteBuffer.capacity() >= pLength) return;

    System.out.println("TurboJpegJCompressor: Allocating new buffer for compressed image!");

    mCompressedImageByteBuffer = ByteBuffer.allocateDirect(pLength).order(ByteOrder.nativeOrder());

    // if (mPointerToCompressedImageByteBufferPointer != null)
    // mPointerToCompressedImageByteBufferPointer.release();
    mPointerToCompressedImageByteBufferPointer.setPointer(Pointer.pointerToBytes(mCompressedImageByteBuffer));

    // if (mPointerToCompressedBufferEffectiveSize != null)
    // mPointerToCompressedBufferEffectiveSize.release();
    mPointerToCompressedBufferEffectiveSize.setCLong(mCompressedImageByteBuffer.capacity());
  }

  public ByteBuffer getCompressedBuffer()
  {
    return mCompressedImageByteBuffer;
  }

  public int getLastImageCompressionElapsedTimeInMs()
  {
    return (int) mLastCompressionElapsedTimeInMs;
  }

  public double getLastCompressionRatio()
  {
    return mLastCompressionRatio;
  }

  public int getQuality()
  {
    return mQuality;
  }

  public void setQuality(int pQuality)
  {
    pQuality = Double.isNaN(pQuality) ? 50 : pQuality;
    pQuality = Double.isInfinite(pQuality) ? 50 : pQuality;
    pQuality = pQuality < 0 ? 0 : pQuality;
    pQuality = pQuality > 100 ? 100 : pQuality;

    mQuality = pQuality;
  }

}
