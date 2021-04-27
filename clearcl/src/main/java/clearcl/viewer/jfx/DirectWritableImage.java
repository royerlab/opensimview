package clearcl.viewer.jfx;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import javafx.scene.image.WritableImage;

import coremem.ContiguousMemoryInterface;

/**
 * Direct writable image
 *
 * @author royer
 */
public class DirectWritableImage extends WritableImage
{

  /**
   * Instanciates a direct writable image
   * 
   * @param pWidth
   *          width
   * @param pHeight
   *          height
   */
  public DirectWritableImage(int pWidth, int pHeight)
  {
    super(pWidth, pHeight);
  }

  /**
   * Replaces the internal image buffer with the given one.
   * 
   * @param pMemory
   *          new buffer
   */
  public void replaceBuffer(ContiguousMemoryInterface pMemory)
  {
    try
    {
      replaceImageBuffer(pMemory.getByteBuffer(), this);
    }
    catch (Throwable e)
    {
      throw new RuntimeException("Error while replacing internal buffer",
                                 e);
    }
  }

  /**
   * Writes the contents of this memory object into the image
   * 
   * @param pMemory
   *          memory
   */
  public void writePixels(ContiguousMemoryInterface pMemory)
  {
    try
    {
      writeToImageDirectly(pMemory.getByteBuffer(), this);
    }
    catch (Throwable e)
    {
      throw new RuntimeException("Error while writting pixels", e);
    }
  }

  @SuppressWarnings("restriction")
  private static void writeToImageDirectly(ByteBuffer direct,
                                           WritableImage writableImg) throws NoSuchMethodException,
                                                                      IllegalAccessException,
                                                                      InvocationTargetException,
                                                                      NoSuchFieldException
  {
    // Get the platform image
    Method getWritablePlatformImage =
                                    javafx.scene.image.Image.class.getDeclaredMethod("getWritablePlatformImage");
    getWritablePlatformImage.setAccessible(true);
    com.sun.prism.Image prismImg =
                                 (com.sun.prism.Image) getWritablePlatformImage.invoke(writableImg);

    // Replace the buffer
    Field pixelBuffer =
                      com.sun.prism.Image.class.getDeclaredField("pixelBuffer");
    pixelBuffer.setAccessible(true);
    ByteBuffer lByteBuffer = (ByteBuffer) pixelBuffer.get(prismImg);

    lByteBuffer.rewind();
    direct.rewind();
    lByteBuffer.put(direct);
    lByteBuffer.rewind();

    forceUpdate(writableImg, prismImg);
  }

  @SuppressWarnings("restriction")
  private static void replaceImageBuffer(ByteBuffer direct,
                                         WritableImage writableImg) throws NoSuchMethodException,
                                                                    IllegalAccessException,
                                                                    InvocationTargetException,
                                                                    NoSuchFieldException
  {
    // Get the platform image
    Method getWritablePlatformImage =
                                    javafx.scene.image.Image.class.getDeclaredMethod("getWritablePlatformImage");
    getWritablePlatformImage.setAccessible(true);
    com.sun.prism.Image prismImg =
                                 (com.sun.prism.Image) getWritablePlatformImage.invoke(writableImg);

    // Replace the buffer
    Field pixelBuffer =
                      com.sun.prism.Image.class.getDeclaredField("pixelBuffer");
    pixelBuffer.setAccessible(true);
    pixelBuffer.set(prismImg, direct);

    forceUpdate(writableImg, prismImg);
  }

  @SuppressWarnings("restriction")
  private static void forceUpdate(WritableImage writableImg,
                                  com.sun.prism.Image prismImg) throws NoSuchFieldException,
                                                                IllegalAccessException,
                                                                NoSuchMethodException,
                                                                InvocationTargetException
  {
    // Invalidate the platform image
    Field serial =
                 com.sun.prism.Image.class.getDeclaredField("serial");
    serial.setAccessible(true);
    Array.setInt(serial.get(prismImg),
                 0,
                 Array.getInt(serial.get(prismImg), 0) + 1);

    // Invalidate the WritableImage
    Method pixelsDirty =
                       javafx.scene.image.Image.class.getDeclaredMethod("pixelsDirty");
    pixelsDirty.setAccessible(true);
    pixelsDirty.invoke(writableImg);
  }

}
