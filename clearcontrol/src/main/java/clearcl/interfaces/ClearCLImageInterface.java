package clearcl.interfaces;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLImage;
import clearcl.backend.ClearCLBackendInterface;
import coremem.ContiguousMemoryInterface;
import coremem.enums.NativeTypeEnum;

import java.nio.Buffer;

/**
 * @author royer
 */
public interface ClearCLImageInterface extends ClearCLMemInterface
{

  /**
   * Copies the contents of this image to a given image.
   *
   * @param pImage         destination image
   * @param pBlockingWrite true -> blocking call, false -> asynchronous call
   */
  void copyTo(ClearCLImage pImage, boolean pBlockingWrite);

  /**
   * Copies the contents of this image to a given buffer.
   *
   * @param pBuffer        destination buffer
   * @param pBlockingWrite true -> blocking call, false -> asynchronous call
   */
  void copyTo(ClearCLBuffer pBuffer, boolean pBlockingWrite);

  /**
   * Copies image into CoreMem contiguous memory
   *
   * @param pContiguousMemory contiguous memory
   * @param pBlockingWrite    true -> blocking call, false -> asynchronous call
   */
  void writeTo(ContiguousMemoryInterface pContiguousMemory, boolean pBlockingWrite);

  /**
   * Copies image into NIO buffer
   *
   * @param pBuffer        NIO buffer
   * @param pBlockingWrite true -> blocking call, false -> asynchronous call
   */
  void writeTo(Buffer pBuffer, boolean pBlockingWrite);

  /**
   * Copies image from CoreMem contiguous memory
   *
   * @param pContiguousMemory contiguous memory
   * @param pBlockingRead     true -> blocking call, false -> asynchronous call
   */
  void readFrom(ContiguousMemoryInterface pContiguousMemory, boolean pBlockingRead);

  /**
   * Copies image into NIO buffer
   *
   * @param pBuffer       NIO buffer
   * @param pBlockingRead true -> blocking call, false -> asynchronous call
   */
  void readFrom(Buffer pBuffer, boolean pBlockingRead);

  /**
   * Returns backend
   *
   * @return backend
   */
  public ClearCLBackendInterface getBackend();

  /**
   * Returns native type
   *
   * @return native type
   */
  public NativeTypeEnum getNativeType();

  /**
   * Returns pixel/voxel size in bytes
   *
   * @return pixel/voxel size in bytes
   */
  public default long getPixelSizeInBytes()
  {
    return getNativeType().getSizeInBytes() * getNumberOfChannels();
  }

  /**
   * Returns the number of channels in this image
   *
   * @return number of channels
   */
  public long getNumberOfChannels();

  /**
   * Returns this image dimensions.
   *
   * @return dimensions
   */
  public long[] getDimensions();

  /**
   * Returns this image width
   *
   * @return width
   */
  public default long getWidth()
  {
    return getDimensions()[0];
  }

  /**
   * Returns this image height
   *
   * @return height
   */
  public default long getHeight()
  {
    if (getDimensions().length < 2) return 1;
    return getDimensions()[1];
  }

  /**
   * Returns this image depth
   *
   * @return depth
   */
  public default long getDepth()
  {
    if (getDimensions().length < 3) return 1;
    return getDimensions()[2];
  }

  /**
   * Return this image dimension (1D, 2D, or 3D).
   *
   * @return image dimension (1, 2, or 3)
   */
  public default long getDimension()
  {
    return getDimensions().length;
  }

  /**
   * Returns this image volume
   *
   * @return depth
   */
  public default long getVolume()
  {
    return getWidth() * getHeight() * getDepth();
  }

}
