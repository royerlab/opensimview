package clearcl;

import clearcl.abs.ClearCLBase;
import clearcl.enums.DeviceType;
import clearcl.exceptions.OpenCLException;

/**
 * ClearCLPlatform is the ClearCL abstraction for OpenCl platforms.
 *
 * @author royer
 */
public class ClearCLPlatform extends ClearCLBase
{

  private ClearCLPeerPointer mPlatformPointer;

  /**
   * This constructor is called internally from the 'starting point' ClearCl
   * object.
   *
   * @param pClearCL
   * @param pPlatformIdPointer
   */
  ClearCLPlatform(ClearCL pClearCL, ClearCLPeerPointer pPlatformIdPointer)
  {
    super(pClearCL.getBackend(), pPlatformIdPointer);
    mPlatformPointer = pPlatformIdPointer;
  }

  /**
   * Returns the number of devices for a given device type
   *
   * @param pDeviceType device type
   * @return number of devices for given device type
   */
  public int getNumberOfDevices(DeviceType pDeviceType)
  {
    try
    {
      return getBackend().getNumberOfDevicesForPlatform(mPlatformPointer, pDeviceType);
    } catch (OpenCLException e)
    {

      return 0;
    }
  }

  /**
   * Returns the total number of devices.
   *
   * @return total number of devices.
   */
  public int getNumberOfDevices()
  {
    return getBackend().getNumberOfDevicesForPlatform(mPlatformPointer);
  }

  /**
   * Returns the CPU device for a given index.
   *
   * @param pDeviceIndex device index
   * @return device
   */
  public ClearCLDevice getCPUDevice(int pDeviceIndex)
  {
    ClearCLPeerPointer lDevicePointer = getBackend().getDevicePeerPointer(mPlatformPointer, DeviceType.CPU, pDeviceIndex);
    return new ClearCLDevice(this, lDevicePointer);
  }

  /**
   * Returns the CPU device for a given index.
   *
   * @param pDeviceIndex device index
   * @return device
   */
  public ClearCLDevice getGPUDevice(int pDeviceIndex)
  {
    ClearCLPeerPointer lDevicePointer = getBackend().getDevicePeerPointer(mPlatformPointer, DeviceType.GPU, pDeviceIndex);
    return new ClearCLDevice(this, lDevicePointer);
  }

  /**
   * Returns the device for a given index.
   *
   * @param pDeviceIndex device index
   * @return device for given index
   */
  public ClearCLDevice getDevice(int pDeviceIndex)
  {
    ClearCLPeerPointer lDevicePointer = getBackend().getDevicePeerPointer(mPlatformPointer, pDeviceIndex);
    return new ClearCLDevice(this, lDevicePointer);
  }

  /**
   * Returns platform name.
   *
   * @return platform name.
   */
  public String getName()
  {
    return getBackend().getPlatformName(mPlatformPointer);
  }

  /**
   * Returns platform info string.
   *
   * @return info string
   */
  public String getInfoString()
  {
    StringBuilder lStringBuilder = new StringBuilder();

    lStringBuilder.append(String.format("Platform name: %s \n", getName()));
    lStringBuilder.append(String.format("\tNumber of CPU devices: %d \n", getNumberOfDevices(DeviceType.CPU)));
    lStringBuilder.append(String.format("\tNumber of GPU devices: %d \n", getNumberOfDevices(DeviceType.GPU)));

    return lStringBuilder.toString();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return String.format("ClearCLPlatform [name=%s]", getName());
  }

  /* (non-Javadoc)
   * @see clearcl.ClearCLBase#close()
   */
  @Override
  public void close()
  {
    if (getPeerPointer() != null)
    {
      setPeerPointer(null);
    }
  }

}
