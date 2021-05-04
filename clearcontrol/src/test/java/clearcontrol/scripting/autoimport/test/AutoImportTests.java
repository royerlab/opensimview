package clearcontrol.scripting.autoimport.test;

import clearcontrol.scripting.autoimport.AutoImport;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

public class AutoImportTests
{

  @Test
  public void test() throws IOException
  {
    final String lScriptText = IOUtils.toString(this.getClass().getResourceAsStream("script.txt"), "UTF-8");
    final String lGeneratedImportStatements = AutoImport.generateImportStatements(new String[]{"clearcontrol"}, lScriptText);

    System.out.println(lGeneratedImportStatements);
  }
}
