package clearcl.test;

import static org.junit.Assert.assertEquals;

import clearcl.ClearCL;
import clearcl.ClearCLDevice;
import clearcl.ClearCLHostImageBuffer;
import clearcl.backend.jocl.ClearCLBackendJOCL;
import coremem.enums.NativeTypeEnum;

import org.junit.Test;

/**
 * Host image tests
 *
 * @author royer
 */
public class ClearCLHostImageTests
{

  /**
   * Basic tests
   */
  @Test
  public void testBasics()
  {
    ClearCLBackendJOCL lClearCLJOCLBackend = new ClearCLBackendJOCL();

    try (ClearCL lClearCL = new ClearCL(lClearCLJOCLBackend))
    {

      ClearCLDevice lBestGPUDevice = lClearCL.getBestGPUDevice();

      ClearCLHostImageBuffer lHostImage =
                                        new ClearCLHostImageBuffer(lBestGPUDevice.createContext(),
                                                                   NativeTypeEnum.UnsignedShort,
                                                                   10,
                                                                   10,
                                                                   10);

      assertEquals(10 * 10 * 10 * 2, lHostImage.getSizeInBytes());

      lHostImage.close();
    }
  }

}
