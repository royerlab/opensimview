package clearcl.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import clearcl.ClearCL;
import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.enums.BuildStatus;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.KernelAccessType;
import clearcl.test.ClearCLBasicTests;
import coremem.enums.NativeTypeEnum;
import coremem.offheap.OffHeapMemory;

import org.junit.Test;

/**
 * ClearCL basic demos
 *
 * @author royer
 */
public class ClearCLBasicDemo
{

  private static final int cFloatArrayLength = 1024 * 1024;

  /**
   * Basic demo
   * 
   * @throws Exception
   *           NA
   */
  @Test
  public void demoClearCL() throws Exception
  {

    ClearCLBackendInterface lClearCLBackend =
                                            ClearCLBackends.getBestBackend();

    try (ClearCL lClearCL = new ClearCL(lClearCLBackend))
    {

      ClearCLDevice lBestGPUDevice = lClearCL.getBestGPUDevice();

      System.out.println(lBestGPUDevice.getInfoString());

      ClearCLContext lContext = lBestGPUDevice.createContext();

      ClearCLProgram lProgram =
                              lContext.createProgram(ClearCLBasicTests.class,
                                                     "test.cl");
      lProgram.addDefine("CONSTANT", "1");

      // System.out.println(lProgram.getSourceCode());

      BuildStatus lBuildStatus = lProgram.buildAndLog();

      assertEquals(lBuildStatus, BuildStatus.Success);

      demoBuffers(lContext, lProgram);

      demoImages(lContext, lProgram);

    }
  }

  private void demoImages(ClearCLContext lContext,
                          ClearCLProgram pProgram)
  {

    ClearCLImage lImageSrc =
                           lContext.createSingleChannelImage(HostAccessType.WriteOnly,
                                                             KernelAccessType.ReadWrite,
                                                             ImageChannelDataType.Float,
                                                             100,
                                                             100,
                                                             100);

    ClearCLKernel lKernel = pProgram.createKernel("fillimagexor");

    lKernel.setArgument("image", lImageSrc);
    lKernel.setArgument("u", 1f);
    lKernel.setGlobalSizes(lImageSrc);
    lKernel.run();

    ClearCLImage lImageDst =
                           lContext.createSingleChannelImage(HostAccessType.ReadOnly,
                                                             KernelAccessType.WriteOnly,
                                                             ImageChannelDataType.Float,
                                                             10,
                                                             10,
                                                             10);

    lImageSrc.copyTo(lImageDst, new long[]
    { 10, 20, 30 }, new long[]
    { 0, 0, 0 }, new long[]
    { 10, 10, 10 }, true);

    OffHeapMemory lBuffer =
                          OffHeapMemory.allocateBytes(lImageDst.getSizeInBytes());
    lImageDst.writeTo(lBuffer, new long[]
    { 0, 0, 0 }, new long[]
    { 10, 10, 10 }, true);

    assertEquals((10 + 1) ^ (20 + 2 + 1)
                 ^ (30 + 3 + 2),
                 lBuffer.getFloatAligned(1 + 2 * 10 + 3 * 10 * 10),
                 0.1);

  }

  private void demoBuffers(ClearCLContext lCreateContext,
                           ClearCLProgram pProgram) throws IOException
  {

    float[] lArrayA = new float[cFloatArrayLength];
    float[] lArrayB = new float[cFloatArrayLength];

    for (int j = 0; j < cFloatArrayLength; j++)
    {
      lArrayA[j] = j;
      lArrayB[j] = 1.5f * j;
    }

    ClearCLBuffer lBufferA =
                           lCreateContext.createBuffer(HostAccessType.WriteOnly,
                                                       KernelAccessType.ReadOnly,
                                                       NativeTypeEnum.Float,
                                                       cFloatArrayLength);

    ClearCLBuffer lBufferB =
                           lCreateContext.createBuffer(HostAccessType.WriteOnly,
                                                       KernelAccessType.ReadOnly,
                                                       NativeTypeEnum.Float,
                                                       cFloatArrayLength);

    ClearCLBuffer lBufferC =
                           lCreateContext.createBuffer(HostAccessType.ReadOnly,
                                                       KernelAccessType.WriteOnly,
                                                       NativeTypeEnum.Float,
                                                       cFloatArrayLength);

    lBufferA.readFrom(FloatBuffer.wrap(lArrayA),
                      0L,
                      cFloatArrayLength,
                      true);
    lBufferB.readFrom(FloatBuffer.wrap(lArrayB),
                      0L,
                      cFloatArrayLength,
                      true);

    ClearCLKernel lKernel = pProgram.createKernel("buffersum");

    lKernel.setArguments(11f, lBufferA, lBufferB, lBufferC);

    lKernel.setGlobalSizes(cFloatArrayLength);
    lKernel.run();

    FloatBuffer lArrayC = ByteBuffer
                                    .allocateDirect(4
                                                    * cFloatArrayLength)
                                    .order(ByteOrder.nativeOrder())
                                    .asFloatBuffer();

    lBufferC.writeTo(lArrayC, 0, cFloatArrayLength, true);

    for (int j = 0; j < cFloatArrayLength; j++)
    {
      float lObservedValue = lArrayC.get(j);
      float lTrueValue = j + (1.5f * j) + 11;

      if (lObservedValue != lTrueValue)
      {
        System.out.format("NOT EQUAL: (c[%d] = %g) != %g \n",
                          j,
                          lObservedValue,
                          lTrueValue);
        assertTrue(false);
        break;
      }
      if (j % 100000 == 0)
        System.out.println(lObservedValue + " == " + lTrueValue);
    }
  }

}
