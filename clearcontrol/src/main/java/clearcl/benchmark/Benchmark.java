package clearcl.benchmark;

import clearcl.*;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.enums.*;
import coremem.enums.NativeTypeEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * This class provides a static methods for benchmarlking devices.
 *
 * @author royer
 */
public class Benchmark
{
  /**
   * This flag controls the stdout logging during benchmarking.
   */
  public static boolean sStdOutVerbose = false;

  private static final int c2DBufferSize = 1024;
  private static final int c3DImageSize = 320;
  private static final int cRepeats = 10;

  /**
   * Returns the fastest device available for a given backend and benchmark
   * test.
   *
   * @param pClearCLBackend backend
   * @param pBenchmarkTest  benchmark test
   * @param pRepeats        nb of repeats
   * @return name of device, null if any error occured.
   */
  public static String getFastestDevice(ClearCLBackendInterface pClearCLBackend, BenchmarkTest pBenchmarkTest, int pRepeats)
  {
    try (ClearCL lClearCL = new ClearCL(pClearCLBackend))
    {

      ClearCLDevice lFastestDevice = getFastestDevice(lClearCL.getAllDevices(), pBenchmarkTest, pRepeats);

      lClearCL.close();

      return lFastestDevice == null ? null : lFastestDevice.toString();
    } catch (Throwable e)
    {
      e.printStackTrace();
      return null;
    }

  }

  /**
   * Returns the fastest device for a given benchmark test.
   *
   * @param pDevices       list of devices
   * @param pBenchmarkTest benchmark type.
   * @return device
   */
  public static ClearCLDevice getFastestDevice(ArrayList<ClearCLDevice> pDevices, BenchmarkTest pBenchmarkTest)
  {
    return getFastestDevice(pDevices, pBenchmarkTest, cRepeats);
  }

  /**
   * Returns the fastest device for a given benchmark test.
   *
   * @param pDevices       list of devices
   * @param pBenchmarkTest benchmark type.
   * @param pRepeats       number of repeats (for high accuracy > 1000)
   * @return fastest device
   */
  public static ClearCLDevice getFastestDevice(ArrayList<ClearCLDevice> pDevices, BenchmarkTest pBenchmarkTest, int pRepeats)
  {

    ClearCLDevice lFastestDevice = null;
    double lMinElapsedTime = Double.POSITIVE_INFINITY;

    format("IMPORTANT: Benchmarking available OpenCl devices, please wait \n");

    for (ClearCLDevice lDevice : pDevices)
    {

      println("_______________________________________________________________________");
      println(lDevice.getInfoString());

      double lElapsedTimeInSeconds;

      try
      {
        lElapsedTimeInSeconds = executeBenchmarkOnDevice(lDevice, pBenchmarkTest, cRepeats);

        format("---> Elapsed time: %g ms \n", lElapsedTimeInSeconds);/**/

        if (lElapsedTimeInSeconds < lMinElapsedTime)
        {
          lMinElapsedTime = lElapsedTimeInSeconds;
          lFastestDevice = lDevice;
        }
      } catch (Throwable e)
      {
        e.printStackTrace();
      }

    }

    println("_______________________________________________________________________");
    format("fastest device: %s \n", lFastestDevice);

    return lFastestDevice;

  }

  /**
   * Computes the time needed to execute a given bencmark test on a particular
   * device with a given number of repetitions.
   *
   * @param pClearClDevice device
   * @param pBenchmarkTest benchmark test
   * @param pRepeats       nb of repeats
   * @return elapsed time in ms
   * @throws IOException NA
   */
  public static double executeBenchmarkOnDevice(ClearCLDevice pClearClDevice, BenchmarkTest pBenchmarkTest, int pRepeats) throws IOException
  {
    if (pClearClDevice.getType().isCPU()) return Double.POSITIVE_INFINITY;

    ClearCLContext lContext = pClearClDevice.createContext();

    ClearCLProgram lProgram = lContext.createProgram(Benchmark.class, "kernel/benchmark.cl");
    lProgram.addBuildOptionAllMathOpt();

    long lStartCompileTimeNanos = System.nanoTime();
    BuildStatus lBuildStatus = lProgram.buildAndLog();
    long lStopCompileTimeNanos = System.nanoTime();

    double lCompileElapsedTime = TimeUnit.MICROSECONDS.convert((lStopCompileTimeNanos - lStartCompileTimeNanos), TimeUnit.NANOSECONDS);

    format("Compilation time: %g us \n", lCompileElapsedTime);/**/

    if (lBuildStatus == BuildStatus.Success)
    {

      ClearCLBuffer lBufferA = lContext.createBuffer(MemAllocMode.None, HostAccessType.Undefined, KernelAccessType.Undefined, NativeTypeEnum.Float, c2DBufferSize * c2DBufferSize);
      ClearCLBuffer lBufferB = lContext.createBuffer(MemAllocMode.None, HostAccessType.Undefined, KernelAccessType.Undefined, NativeTypeEnum.Float, c2DBufferSize * c2DBufferSize);

      ClearCLBuffer lBufferC = lContext.createBuffer(MemAllocMode.None, HostAccessType.Undefined, KernelAccessType.Undefined, NativeTypeEnum.UnsignedInt, c2DBufferSize * c2DBufferSize);

      ClearCLImage lImage = lContext.createImage(MemAllocMode.None, HostAccessType.Undefined, KernelAccessType.Undefined, ImageChannelOrder.R, ImageChannelDataType.UnsignedInt16, c3DImageSize, c3DImageSize, c3DImageSize);

      ClearCLKernel lKernelCompute;

      switch (pBenchmarkTest)
      {

        case Image:
          lKernelCompute = lProgram.createKernel("image");
          lKernelCompute.setGlobalSizes(c2DBufferSize, c2DBufferSize);
          lKernelCompute.setArguments(lImage, c3DImageSize, lBufferC);
          break;

        default:
        case Buffer:
          lKernelCompute = lProgram.createKernel("buffer");
          lKernelCompute.setGlobalSizes(c2DBufferSize, c2DBufferSize);
          lKernelCompute.setArguments(lBufferA, lBufferB);
          break;

      }

      lContext.getDefaultQueue().waitToFinish();
      long lStartTimeNanos = System.nanoTime();
      for (int r = 0; r < pRepeats; r++)
      {
        lKernelCompute.run(false);
      }
      lContext.getDefaultQueue().waitToFinish();
      long lStopTimeNanos = System.nanoTime();

      double lElapsedTimeNanos = ((double) (lStopTimeNanos - lStartTimeNanos)) / pRepeats;

      double lElapsedTimeInMs = lElapsedTimeNanos * 1e-6;

      lBufferA.close();
      lBufferB.close();
      lBufferC.close();
      lImage.close();

      return lElapsedTimeInMs;
    } else return Double.POSITIVE_INFINITY;
  }

  private static void println(String pString)
  {
    if (sStdOutVerbose) System.out.println(pString);
  }

  private static void format(String format, Object... args)
  {
    if (sStdOutVerbose) System.out.format(format, args);
  }

}
