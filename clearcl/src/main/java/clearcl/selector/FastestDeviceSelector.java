package clearcl.selector;

import java.io.File;
import java.util.ArrayList;

import clearcl.ClearCLDevice;
import clearcl.benchmark.Benchmark;
import clearcl.enums.BenchmarkTest;
import clearcl.util.ClearCLFolder;
import clearcl.util.StringUtils;

/**
 * Selects a device based on the actual computation speed based on benchmarking.
 * Benchmarking results are cached.
 *
 * @author royer
 */
public class FastestDeviceSelector implements DeviceSelector
{

  /**
   * Device selector that uses a image centric benchmark to determine the
   * fastest device
   */
  public static FastestDeviceSelector FastestForImages =
                                                       new FastestDeviceSelector(BenchmarkTest.Image);
  /**
   * Device selector that uses a buffer centric benchmark to determine the
   * fastest device
   */
  public static FastestDeviceSelector FastestForBuffers =
                                                        new FastestDeviceSelector(BenchmarkTest.Buffer);

  private ClearCLDevice mFastestDevice;

  private BenchmarkTest mBenchmarkTest;

  /**
   * prevents direct instantiation.
   */
  private FastestDeviceSelector(BenchmarkTest pBenchmarkTest)
  {
    super();
    mBenchmarkTest = pBenchmarkTest;
  }

  @Override
  public void init(ArrayList<ClearCLDevice> pDevices)
  {
    try
    {
      File lClearCLFolder = ClearCLFolder.get();

      File lFastestDeviceFile =
                              new File(lClearCLFolder,
                                       String.format("fastestdevice_%s.txt",
                                                     mBenchmarkTest));

      if (lFastestDeviceFile.exists())
      {
        String lFastestDeviceName =
                                  StringUtils.readFileToString(lFastestDeviceFile)
                                             .trim();
        mFastestDevice = getDeviceWithName(pDevices,
                                           lFastestDeviceName);
      }
      else
      {
        mFastestDevice = Benchmark.getFastestDevice(pDevices,
                                                    mBenchmarkTest);
        if (mFastestDevice != null)
          StringUtils.writeStringToFile(lFastestDeviceFile,
                                        mFastestDevice.getName());
      }
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }
  }

  private ClearCLDevice getDeviceWithName(ArrayList<ClearCLDevice> pDevices,
                                          String pDeviceName)
  {
    for (ClearCLDevice lDevice : pDevices)
    {
      if (lDevice.getName()
                 .trim()
                 .toLowerCase()
                 .equals(pDeviceName.toLowerCase()))
        return lDevice;
    }
    return null;
  }

  @Override
  public boolean selected(ClearCLDevice pClearCLDevice)
  {
    // in case there was a problem, we don't select anything...
    if (mFastestDevice == null)
      return true;

    return mFastestDevice == pClearCLDevice;
  }

}
