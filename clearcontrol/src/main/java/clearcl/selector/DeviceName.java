package clearcl.selector;

import clearcl.ClearCLDevice;

import java.util.ArrayList;

/**
 * Selects the first device that contains a string
 *
 * @author royer
 */
public class DeviceName implements DeviceSelector
{

  private String mNameSubString = "";

  /**
   * Instanciates a device name selector with the given substring.
   *
   * @param pNameSubString substring
   * @return selector
   */
  public static DeviceSelector subString(String pNameSubString)
  {
    return new DeviceName(pNameSubString);
  }

  /**
   * Constructs a device selector that filters out devices with a certain name
   * (bad devices).
   *
   * @param pDiscrete if true select discrete, if false select non-discrete.
   */
  private DeviceName(String pNameSubString)
  {
    super();
    setNameSubString(pNameSubString);
  }

  /**
   * Adds a name to te list of bad devices.
   *
   * @param pNameSubString substring that should be contained in device name
   */
  public void setNameSubString(String pNameSubString)
  {
    mNameSubString = pNameSubString;
  }

  @Override
  public void init(ArrayList<ClearCLDevice> pDevices)
  {

  }

  @Override
  public boolean selected(ClearCLDevice pClearCLDevice)
  {
    return pClearCLDevice.getName().trim().toLowerCase().contains(mNameSubString.trim().toLowerCase());
  }

}
