package clearcl.util;

import java.io.File;

/**
 * This utility class gives access to the ClearCL folder used for caching and
 * configuration (~/.clearcl)
 *
 * @author royer
 */
public class ClearCLFolder
{

  /**
   * Return teh ClearCL folder.
   *
   * @return ClearCL folder
   */
  static public File get()
  {
    File lFolder = new File(System.getProperty("user.home"), ".clearcl");
    lFolder.mkdirs();
    return lFolder;
  }
}
