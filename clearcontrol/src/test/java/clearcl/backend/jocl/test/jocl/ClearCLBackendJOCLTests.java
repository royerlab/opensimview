package clearcl.backend.jocl.test.jocl;

import clearcl.backend.jocl.ClearCLBackendJOCL;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author royer
 */
public class ClearCLBackendJOCLTests
{

  /**
   * Basic test
   */
  @Test
  public void test()
  {
    ClearCLBackendJOCL lClearCLBackendJOCL = new ClearCLBackendJOCL();

    assertTrue(lClearCLBackendJOCL.getNumberOfPlatforms() > 0);
  }

}
