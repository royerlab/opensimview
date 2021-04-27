package clearcl.io;

import static loci.formats.FormatTools.FLOAT;
import static loci.formats.FormatTools.UINT16;
import static loci.formats.FormatTools.UINT8;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;

import loci.common.services.ServiceFactory;
import loci.formats.FormatTools;
import loci.formats.MetadataTools;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import loci.formats.tiff.TiffSaver;
import ch.qos.logback.classic.Level;
import clearcl.ClearCLImage;
import clearcl.interfaces.ClearCLImageInterface;
import coremem.buffers.ContiguousBuffer;
import coremem.enums.NativeTypeEnum;
import coremem.offheap.OffHeapMemory;
import coremem.util.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Image TIFF writer
 * 
 * TODO: Replace the line marked with "Dirty hack" below. Otherwise, it may not
 * work with all kinds of images. Maybe, the ClearCLImage.writeTo method should
 * also be listed in ClearCLImageInterface
 *
 * @author royer
 * @author haesleinhuepf
 */
public class TiffWriter extends WriterBase implements WriterInterface
{
  byte[] mTransferArray;
  OffHeapMemory mTransferMemory;
  int mBytesPerPixel = 16;
  int mPixelType = FormatTools.UINT16;

  /**
   * Instanciates a Image TIFF writer. The voxel values produced by the phantom
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
  public TiffWriter(NativeTypeEnum pNativeTypeEnum,
                    float pScaling,
                    float pOffset)
  {
    super(pNativeTypeEnum, pScaling, pOffset);
  }

  public void setBytesPerPixel(int pBytesPerPixel)
  {
    if (pBytesPerPixel == 8 || pBytesPerPixel == 16
        || pBytesPerPixel == 32)
    {
      mBytesPerPixel = pBytesPerPixel;
    }
  }

  @Override
  public boolean write(ClearCLImageInterface pImage,
                       File pFile) throws Throwable
  {
    if (!getOverwrite() && pFile.exists())
    {
      throw new FileAlreadyExistsException("File "
                                           + pFile.getAbsolutePath()
                                           + " already exists.");
    }
    pFile.getParentFile().mkdirs();
    long[] dimensions = pImage.getDimensions();

    int lWidth = (int) dimensions[0];
    int lHeight =
                dimensions.length > 1 ? (int) pImage.getDimensions()[1]
                                      : 1;
    int lDepth =
               dimensions.length > 2 ? (int) pImage.getDimensions()[2]
                                     : 1;

    mPixelType = FormatTools.UINT16;
    switch (mBytesPerPixel)
    {
    case 8:
      mPixelType = UINT8;
      break;
    case 32:
      mPixelType = FormatTools.FLOAT;
      break;
    }

    long lArraySizeInBytes = lWidth * lHeight
                             * FormatTools.getBytesPerPixel(mPixelType);
    long lFloatSizeInBytes = lWidth * lHeight * lDepth * Size.FLOAT;

    if (mTransferMemory == null
        || lFloatSizeInBytes != mTransferMemory.getSizeInBytes())
    {
      mTransferMemory =
                      OffHeapMemory.allocateBytes("PhantomTiffWriter",
                                                  lFloatSizeInBytes);
    }

    if (mTransferArray == null
        || lArraySizeInBytes != mTransferArray.length)
    {
      mTransferArray = new byte[Math.toIntExact(lArraySizeInBytes)];
    }

    // Dirty hack:
    ClearCLImage lImage = (ClearCLImage) pImage;

    lImage.writeTo(mTransferMemory, new long[]
    { 0, 0, 0 }, new long[]
    { lWidth, lHeight, lDepth }, true);

    // Workaround to turn the BioFormats logger OFF
    Logger LOGGER = LoggerFactory.getLogger(TiffSaver.class);
    if (LOGGER instanceof ch.qos.logback.classic.Logger)
    {
      ((ch.qos.logback.classic.Logger) LOGGER).setLevel(Level.OFF);
    }

    ServiceFactory factory = new ServiceFactory();
    OMEXMLService service = factory.getInstance(OMEXMLService.class);
    IMetadata meta = service.createOMEXMLMetadata();

    MetadataTools.populateMetadata(meta,
                                   0,
                                   null,
                                   false,
                                   "XYZCT",
                                   FormatTools.getPixelTypeString(mPixelType),
                                   lWidth,
                                   lHeight,
                                   lDepth,
                                   1,
                                   1,
                                   1);

    String lFileName = pFile.getAbsolutePath();

    System.out.println("Writing image to '" + lFileName + "'...");
    loci.formats.out.TiffWriter writer =
                                       new loci.formats.out.TiffWriter();
    writer.setCompression(loci.formats.out.TiffWriter.COMPRESSION_LZW);
    writer.setMetadataRetrieve(meta);
    writer.setId(lFileName);

    ContiguousBuffer lBuffer = new ContiguousBuffer(mTransferMemory);

    for (int z = 0; z < lDepth; z++)
    {
      int i = 0;
      while (lBuffer.hasRemainingFloat() && i < mTransferArray.length)
      {
        i = appendValueToArray(lBuffer.readFloat(), i);
      }

      writer.saveBytes(z, mTransferArray);
    }
    writer.close();

    System.out.println("Done.");
    return true;
  }

  private int appendValueToArray(float pFloatValue, int i)
  {
    float lFloatValue = pFloatValue * mScaling + mOffset;

    int lIntValue = Math.round(lFloatValue);

    switch (mPixelType)
    {
    case UINT8:
      lIntValue = Math.round(lFloatValue);
      byte lByte = (byte) (lIntValue & 0xFF);
      mTransferArray[i++] = lByte;
      break;
    case UINT16:
      lIntValue = Math.round(lFloatValue);

      byte lLowByte = (byte) (lIntValue & 0xFF);
      byte lHighByte = (byte) ((lIntValue >> 8) & 0xFF);

      mTransferArray[i++] = lHighByte;
      mTransferArray[i++] = lLowByte;
      break;
    case FLOAT:
      lIntValue = Float.floatToIntBits(pFloatValue);
      mTransferArray[i++] = (byte) (lIntValue >>> 24);
      mTransferArray[i++] = (byte) (lIntValue >>> 16);
      mTransferArray[i++] = (byte) (lIntValue >>> 8);
      mTransferArray[i++] = (byte) lIntValue;
      break;
    }
    return i;
  }

}
