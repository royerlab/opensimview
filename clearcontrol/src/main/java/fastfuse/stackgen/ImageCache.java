package fastfuse.stackgen;

import clearcl.ClearCLHostImageBuffer;
import clearcl.ClearCLImage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * @author royer
 */
public class ImageCache
{
  private static final File cCacheFolder = new File(System.getProperty("user.home"), "/Downloads/fastfusecache");

  private String mName;

  /**
   * Instantiates an image cache with a given name
   *
   * @param pName image cache name
   */
  public ImageCache(String pName)
  {
    super();
    mName = pName;
  }

  /**
   * Saves a stack with a given name
   *
   * @param pImageName stack name
   * @param pImage     stack to save
   * @throws IOException thrown if problem occurs while writing file
   */
  public void saveImage(String pImageName, ClearCLImage pImage) throws IOException
  {
    ClearCLHostImageBuffer lBuffer = ClearCLHostImageBuffer.allocateSameAs(pImage);

    pImage.copyTo(lBuffer, true);

    File lFile = new File(new File(cCacheFolder, mName), pImageName + ".raw");

    lFile.getParentFile().mkdirs();

    RandomAccessFile lRandomAccessFile = new RandomAccessFile(lFile, "rw");
    FileChannel lChannel = lRandomAccessFile.getChannel();

    lBuffer.getContiguousMemory().writeBytesToFileChannel(lChannel, 0);

    lChannel.close();
    lRandomAccessFile.close();

    lBuffer.close();
  }

  /**
   * Loads a stack with a given name
   *
   * @param pImageName stack name
   * @param pImage     to write data to
   * @throws IOException thrown if problem occurs while writing file
   */
  public void loadImage(String pImageName, ClearCLImage pImage) throws IOException
  {
    ClearCLHostImageBuffer lBuffer = ClearCLHostImageBuffer.allocateSameAs(pImage);

    File lFile = new File(new File(cCacheFolder, mName), pImageName + ".raw");

    RandomAccessFile lRandomAccessFile = new RandomAccessFile(lFile, "rw");
    FileChannel lChannel = lRandomAccessFile.getChannel();

    lBuffer.getContiguousMemory().readBytesFromFileChannel(lChannel, 0, pImage.getSizeInBytes());

    lChannel.close();
    lRandomAccessFile.close();

    lBuffer.copyTo(pImage, true);

    lBuffer.close();
  }

}
