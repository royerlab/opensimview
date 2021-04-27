package clearcl.backend.javacl.test;

import static org.junit.Assert.assertTrue;

import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.jocl.ClearCLBackendJOCL;

import org.junit.Test;

/**
 * 
 *
 * @author royer
 */
public class ClearCLBackendJavaCLTests
{

  /**
   * 
   */
  @Test
  public void test()
  {
    ClearCLBackendInterface lClearCLBackend =
                                            new ClearCLBackendJOCL();

    // System.out.println(lClearCLBackendJavaCL.getNumberOfPlatforms());
    assertTrue(lClearCLBackend.getNumberOfPlatforms() > 0);

  }

}
