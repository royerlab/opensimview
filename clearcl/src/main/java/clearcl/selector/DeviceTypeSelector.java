package clearcl.selector;

import java.util.ArrayList;

import clearcl.ClearCLDevice;
import clearcl.enums.DeviceType;

/**
 * Selectes devices based on the device type.
 *
 * @author royer
 */
public class DeviceTypeSelector implements DeviceSelector
{

  /**
   * GPU only device selector
   */
  public static DeviceTypeSelector GPU =
                                       new DeviceTypeSelector(DeviceType.GPU);

  /**
   * CPU only device selector
   */
  public static DeviceTypeSelector CPU =
                                       new DeviceTypeSelector(DeviceType.CPU);

  /**
   * Non-CPU and non-GPU device selector
   */
  public static DeviceTypeSelector OTHER =
                                         new DeviceTypeSelector(DeviceType.OTHER);

  DeviceType mDeviceType;

  /**
   * Creates a device type selector given a device type.
   * 
   * @param pDeviceType
   *          device type
   */
  private DeviceTypeSelector(DeviceType pDeviceType)
  {
    super();
    mDeviceType = pDeviceType;
  }

  /* (non-Javadoc)
   * @see clearcl.selector.DeviceSelector#init(java.util.ArrayList)
   */
  @Override
  public void init(ArrayList<ClearCLDevice> pDevices)
  {

  }

  /* (non-Javadoc)
   * @see clearcl.selector.DeviceSelector#selected(clearcl.ClearCLDevice)
   */
  @Override
  public boolean selected(ClearCLDevice pClearCLDevice)
  {
    return pClearCLDevice.getType() == mDeviceType;
  }

}
