package clearcl.test;

import clearcl.*;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.ClearCLBackends;
import clearcl.enums.*;
import clearcl.exceptions.OpenCLException;
import coremem.enums.NativeTypeEnum;
import coremem.offheap.OffHeapMemory;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static org.junit.Assert.*;

/**
 * Basic tests.
 *
 * @author royer
 */
public class ClearCLBasicTests
{

  private static final int cFloatArrayLength = 1024 * 1024;

  /**
   * test with best backend
   *
   * @throws Exception NA
   */
  @Test
  public void testBasics() throws Exception
  {
    final ClearCLBackendInterface lClearCLBackendInterface = ClearCLBackends.getBestBackend();

    testWithBackend(lClearCLBackendInterface);

  }

  private void testWithBackend(final ClearCLBackendInterface pClearCLBackendInterface) throws Exception
  {
    try (ClearCL lClearCL = new ClearCL(pClearCLBackendInterface))
    {

      final int lNumberOfPlatforms = lClearCL.getNumberOfPlatforms();

      // System.out.println("lNumberOfPlatforms=" + lNumberOfPlatforms);

      for (int p = 0; p < lNumberOfPlatforms; p++)
      {
        final ClearCLPlatform lPlatform = lClearCL.getPlatform(p);

        // System.out.println(lPlatform.getInfoString());

        for (int d = 0; d < lPlatform.getNumberOfDevices(); d++)
        {
          final ClearCLDevice lClearClDevice = lPlatform.getDevice(d);

          /*System.out.println("\t" + d
                             + " -> \n"
                             + lClearClDevice.getInfoString());/**/

          final ClearCLContext lContext = lClearClDevice.createContext();

          final ClearCLProgram lProgram = lContext.createProgram(this.getClass(), "test.cl");
          lProgram.addDefine("CONSTANT", "1");

          // System.out.println(lProgram.getSourceCode());

          final BuildStatus lBuildStatus = lProgram.buildAndLog();

          // System.out.println(lProgram.getBuildLog());

          System.out.println(lBuildStatus);

          assertEquals(lBuildStatus, BuildStatus.Success);
          // assertTrue(lProgram.getBuildLog().isEmpty());

          testBuffers(lContext, lProgram);

          testImages(lContext, lProgram);

        }

      }
    }
  }

  private void testImages(final ClearCLContext lContext, final ClearCLProgram pProgram)
  {

    final ClearCLImage lImageSrc = lContext.createImage(HostAccessType.WriteOnly, KernelAccessType.ReadWrite, ImageChannelOrder.Intensity, ImageChannelDataType.Float, 100, 100, 100);

    final ClearCLKernel lKernel = pProgram.createKernel("fillimagexor");

    lKernel.setArgument("image", lImageSrc);
    lKernel.setArgument("u", 1f);
    lKernel.setGlobalSizes(100, 100, 100);
    lKernel.run();

    final ClearCLImage lImageDst = lContext.createImage(HostAccessType.ReadOnly, KernelAccessType.WriteOnly, ImageChannelOrder.Intensity, ImageChannelDataType.Float, 10, 10, 10);

    lImageSrc.copyTo(lImageDst, new long[]{10, 20, 30}, new long[]{0, 0, 0}, new long[]{10, 10, 10}, true);

    final OffHeapMemory lBuffer = OffHeapMemory.allocateBytes(lImageDst.getSizeInBytes());
    lImageDst.writeTo(lBuffer, new long[]{0, 0, 0}, new long[]{10, 10, 10}, true);

    // for(int i=0; i<lBuffer.getSizeInBytes()/4; i++)
    // System.out.println(lBuffer.getFloatAligned(i));

    assertEquals((10 + 1) ^ (20 + 2 + 1) ^ (30 + 3 + 2), lBuffer.getFloatAligned(1 + 2 * 10 + 3 * 10 * 10), 0.1);

  }

  private void testBuffers(final ClearCLContext lCreateContext, final ClearCLProgram pProgram) throws IOException
  {

    try
    {
      final ClearCLBuffer lBufferTooBig = lCreateContext.createBuffer(HostAccessType.WriteOnly, KernelAccessType.ReadOnly, NativeTypeEnum.Float, Long.MAX_VALUE);
      System.out.println("size in bytes:" + lBufferTooBig.getSizeInBytes());
      fail();
    } catch (final OpenCLException e)
    {
      // System.out.println("ERROR:" + e.getMessage());
      assertTrue(e.getErrorCode() == -61 || e.getErrorCode() == -6);
    }

    final float[] lArrayA = new float[cFloatArrayLength];
    final float[] lArrayB = new float[cFloatArrayLength];

    for (int j = 0; j < cFloatArrayLength; j++)
    {
      lArrayA[j] = j;
      lArrayB[j] = 1.5f * j;
    }

    final ClearCLBuffer lBufferA = lCreateContext.createBuffer(HostAccessType.WriteOnly, KernelAccessType.ReadOnly, NativeTypeEnum.Float, cFloatArrayLength);

    final ClearCLBuffer lBufferB = lCreateContext.createBuffer(HostAccessType.WriteOnly, KernelAccessType.ReadOnly, NativeTypeEnum.Float, cFloatArrayLength);

    final ClearCLBuffer lBufferC = lCreateContext.createBuffer(HostAccessType.ReadOnly, KernelAccessType.WriteOnly, NativeTypeEnum.Float, cFloatArrayLength);

    lBufferA.readFrom(FloatBuffer.wrap(lArrayA), 0L, cFloatArrayLength, true);
    lBufferB.readFrom(FloatBuffer.wrap(lArrayB), 0L, cFloatArrayLength, true);

    final ClearCLKernel lKernel = pProgram.createKernel("buffersum");

    lKernel.setArguments(11f, lBufferA, lBufferB, lBufferC);

    lKernel.setGlobalSizes(cFloatArrayLength);
    lKernel.run();

    final FloatBuffer lArrayC = ByteBuffer.allocateDirect(4 * cFloatArrayLength).order(ByteOrder.nativeOrder()).asFloatBuffer();

    lBufferC.writeTo(lArrayC, 0, cFloatArrayLength, true);

    for (int j = 0; j < cFloatArrayLength; j++)
    {
      final float lObservedValue = lArrayC.get(j);
      final float lTrueValue = j + (1.5f * j) + 11;

      if (lObservedValue != lTrueValue)
      {
        /*System.out.format("NOT EQUAL: (c[%d] = %g) != %g \n",
                          j,
                          lObservedValue,
                          lTrueValue);/**/
        assertTrue(false);
        break;
      }
      // if (j % 100000 == 0)
      // System.out.println(lObservedValue + " == " + lTrueValue);
    }
  }

}
