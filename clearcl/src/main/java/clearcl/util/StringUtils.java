package clearcl.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * String Utils for ClearCL
 *
 * @author royer
 */
public class StringUtils
{
  /**
   * Reads the content of a file into a String.
   * 
   * From:
   * https://stackoverflow.com/questions/326390/how-do-i-create-a-java-string-from-the-contents-of-a-file
   * 
   * @param pFile
   *          file
   * @return String
   * @throws IOException
   *           if file cannot be read.
   */
  public static final String readFileToString(File pFile) throws IOException
  {
    BufferedReader reader = new BufferedReader(new FileReader(pFile));
    String line = null;
    StringBuilder stringBuilder = new StringBuilder();
    String ls = System.getProperty("line.separator");

    try
    {
      while ((line = reader.readLine()) != null)
      {
        stringBuilder.append(line);
        stringBuilder.append(ls);
      }

      return stringBuilder.toString();
    }
    finally
    {
      reader.close();
    }
  }

  /**
   * Writes the content of a string to a file.
   * 
   * From:
   * https://stackoverflow.com/questions/1053467/how-do-i-save-a-string-to-a-text-file-using-java
   * 
   * @param pFile
   *          file
   * @param pString
   *          string
   * @throws IOException
   *           if file cannot be writen
   */
  public static final void writeStringToFile(File pFile,
                                             String pString) throws IOException
  {

    try (PrintWriter out = new PrintWriter(pFile))
    {
      out.print(pString);
    }
  }

  /**
   * Writes astream to a string.
   * 
   * @param pResourceAsStream
   *          stream
   * @param pCharSetName
   *          charset
   * @return string
   */
  public static String streamToString(InputStream pResourceAsStream,
                                      String pCharSetName)
  {
    Scanner lScanner = new Scanner(pResourceAsStream, pCharSetName);
    String lString = lScanner.useDelimiter("\\A").next();
    lScanner.close();
    return lString;
  }

}
