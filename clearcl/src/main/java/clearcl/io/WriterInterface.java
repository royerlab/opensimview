package clearcl.io;

import java.io.File;

import clearcl.interfaces.ClearCLImageInterface;

/**
 * Image writer interface
 *
 * @author royer
 */
public interface WriterInterface
{

  /**
   * Writes an image to a file.
   * 
   * @param pImage
   *          image
   * @param pFile
   *          file to write to
   * @return true if file written, false otherwise
   * @throws Throwable
   *           if anything goes wrong...
   */
  boolean write(ClearCLImageInterface pImage,
                File pFile) throws Throwable;

}
