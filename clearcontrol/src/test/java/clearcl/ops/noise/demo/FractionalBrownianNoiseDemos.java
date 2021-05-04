package clearcl.ops.noise.demo;

import clearcl.ClearCL;
import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.enums.HostAccessType;
import clearcl.enums.KernelAccessType;
import clearcl.enums.MemAllocMode;
import clearcl.ops.noise.FractionalBrownianNoise;
import clearcl.util.ElapsedTime;
import clearcl.viewer.ClearCLImageViewer;
import coremem.enums.NativeTypeEnum;
import org.junit.Test;

import java.io.IOException;

/**
 * Fractional Brownian Noise Demos
 *
 * @author royer
 */
public class FractionalBrownianNoiseDemos
{

  /**
   * Tests 2D FBM
   *
   * @throws InterruptedException NA
   * @throws IOException          NA
   */
  @Test
  public void demo2DFBM() throws InterruptedException, IOException
  {
    ElapsedTime.sStandardOutput = true;

    ClearCLBackendInterface lClearCLBackendInterface = ClearCLBackends.getBestBackend();

    try (ClearCL lClearCL = new ClearCL(lClearCLBackendInterface))
    {
      ClearCLDevice lFastestGPUDevice = lClearCL.getFastestGPUDeviceForImages();

      System.out.println(lFastestGPUDevice);

      ClearCLContext lContext = lFastestGPUDevice.createContext();

      FractionalBrownianNoise lNoise = new FractionalBrownianNoise(lContext.getDefaultQueue());

      ClearCLBuffer lBuffer = lContext.createBuffer(MemAllocMode.Best, HostAccessType.ReadOnly, KernelAccessType.WriteOnly, 1, NativeTypeEnum.Float, 512, 512);

      ClearCLImageViewer lViewImage = ClearCLImageViewer.view(lBuffer);

      for (int i = 0; i < 10 && lViewImage.isShowing(); i++)
      {
        lNoise.setSeed(i);
        lNoise.fbm2D(lBuffer, true);
        // Thread.sleep(5000);
      }

    }
  }

  /**
   * Tests 3D FBM
   *
   * @throws InterruptedException NA
   * @throws IOException          NA
   */
  @Test
  public void demoFBM3D() throws InterruptedException, IOException
  {
    ElapsedTime.sStandardOutput = true;

    ClearCLBackendInterface lClearCLBackendInterface = ClearCLBackends.getBestBackend();

    try (ClearCL lClearCL = new ClearCL(lClearCLBackendInterface))
    {
      ClearCLDevice lFastestGPUDevice = lClearCL.getFastestGPUDeviceForImages();

      System.out.println(lFastestGPUDevice);

      ClearCLContext lContext = lFastestGPUDevice.createContext();

      FractionalBrownianNoise lNoise = new FractionalBrownianNoise(lContext.getDefaultQueue());

      ClearCLBuffer lBuffer = lContext.createBuffer(MemAllocMode.Best, HostAccessType.ReadOnly, KernelAccessType.WriteOnly, 1, NativeTypeEnum.Float, 128, 128, 128);

      ClearCLImageViewer lViewImage = ClearCLImageViewer.view(lBuffer);

      for (int i = 0; i < 10 && lViewImage.isShowing(); i++)
      {
        lNoise.setSeed(i);
        lNoise.fbm3D(lBuffer, true);
        // Thread.sleep(1000);
      }

    }

  }

}
