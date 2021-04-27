package clearcl.io;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import clearcl.interfaces.ClearCLImageInterface;
import coremem.buffers.ContiguousBuffer;
import coremem.enums.NativeTypeEnum;
import coremem.offheap.OffHeapMemory;
import coremem.util.Size;

/**
 * Image raw writer
 *
 * @author royer
 */
public class RawWriter extends WriterBase
                       implements WriterInterface, AutoCloseable
{

  private OffHeapMemory mTransferMemory, mTempMemory;

  /**
   * Instanciates a image raw writer.
   * 
   */
  public RawWriter()
  {
    super();
  }

  /**
   * Instanciates an image raw writer. The voxel values produced by the phantom
   * are scaled accoding to y = a*x+b, and the data is saved using the provided
   * data type.
   * 
   * @param pNativeTypeEnum
   *          native type to save to.
   * @param pScaling
   *          value scaling a
   * @param pOffset
   *          value offset b
   */
  public RawWriter(NativeTypeEnum pNativeTypeEnum,
                   float pScaling,
                   float pOffset)
  {
    super(pNativeTypeEnum, pScaling, pOffset);
  }

  @Override
  public boolean write(ClearCLImageInterface pImage,
                       File pFile) throws IOException
  {
    pFile.getParentFile().mkdirs();

    if (pFile.exists())
      return false;

    int lWidth = (int) pImage.getWidth();
    int lHeight = (int) pImage.getHeight();
    int lDepth = (int) pImage.getDepth();

    if (getDataType() == NativeTypeEnum.Float)
      writeFloats(pImage, pFile, lWidth, lHeight, lDepth);
    else if (getDataType() == NativeTypeEnum.Byte)
      writeBytes(pImage, pFile, lWidth, lHeight, lDepth);

    return true;
  }

  private void writeFloats(ClearCLImageInterface pImage,
                           File pFile,
                           int lWidth,
                           int lHeight,
                           int lDepth) throws IOException
  {
    copyToTransferMemory(pImage, lWidth, lHeight, lDepth);

    float[] lMinMax = computeMinMax(mTransferMemory);

    System.out.println("Min=" + lMinMax[0]);
    System.out.println("Max=" + lMinMax[1]);

    Path lFilePath = pFile.toPath();
    FileChannel lFileChannel =
                             FileChannel.open(lFilePath,
                                              StandardOpenOption.CREATE,
                                              StandardOpenOption.WRITE);
    mTransferMemory.writeBytesToFileChannel(lFileChannel, 0);
    lFileChannel.close();
  }

  private void copyToTransferMemory(ClearCLImageInterface pImage,
                                    int lWidth,
                                    int lHeight,
                                    int lDepth)
  {
    long lSizeInBytes = lWidth * lHeight * lDepth * Size.FLOAT;

    if (mTransferMemory == null
        || lSizeInBytes != mTransferMemory.getSizeInBytes())
    {
      mTransferMemory = OffHeapMemory.allocateBytes("mTransferMemory",
                                                    lSizeInBytes);
    }

    pImage.writeTo(mTransferMemory, true);
  }

  private void writeBytes(ClearCLImageInterface pImage,
                          File pFile,
                          int lWidth,
                          int lHeight,
                          int lDepth) throws IOException
  {
    copyToTransferMemory(pImage, lWidth, lHeight, lDepth);

    int lVolume = lWidth * lHeight * lDepth;
    long lSizeInBytes = lVolume * Size.BYTE;

    if (mTempMemory == null
        || mTempMemory.getSizeInBytes() != lSizeInBytes)
    {
      mTempMemory = OffHeapMemory.allocateBytes("mTempMemory",
                                                lSizeInBytes);
    }

    ContiguousBuffer lContiguousBufferFloats =
                                             new ContiguousBuffer(mTransferMemory);
    ContiguousBuffer lContiguousBufferBytes =
                                            new ContiguousBuffer(mTempMemory);

    while (lContiguousBufferFloats.hasRemainingFloat())
    {
      float lFloatValue = lContiguousBufferFloats.readFloat();
      lFloatValue = getScaling() * lFloatValue + getOffset();
      int lIntValue = round(lFloatValue);
      byte lByteValue = (byte) (lIntValue & 0xFF);
      lContiguousBufferBytes.writeByte(lByteValue);
    }

    Path lFilePath = pFile.toPath();
    FileChannel lFileChannel =
                             FileChannel.open(lFilePath,
                                              StandardOpenOption.CREATE,
                                              StandardOpenOption.WRITE);
    mTempMemory.writeBytesToFileChannel(lFileChannel, 0);
    lFileChannel.close();
  }

  private float[] computeMinMax(OffHeapMemory pTransferMemory)
  {
    ContiguousBuffer lContiguousBuffer =
                                       new ContiguousBuffer(pTransferMemory);

    float lMin = Float.POSITIVE_INFINITY,
        lMax = Float.NEGATIVE_INFINITY;

    while (lContiguousBuffer.hasRemainingFloat())
    {
      float lValue = lContiguousBuffer.readFloat();
      lMin = min(lMin, lValue);
      lMax = max(lMax, lValue);
    }

    return new float[]
    { lMin, lMax };
  }

  @Override
  public void close() throws Exception
  {
    if (mTempMemory != null)
      mTempMemory.free();
    if (mTransferMemory != null)
      mTransferMemory.free();
  }

}
