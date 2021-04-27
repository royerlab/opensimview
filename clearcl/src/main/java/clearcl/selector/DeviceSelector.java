package clearcl.selector;

import java.util.ArrayList;

import clearcl.ClearCLDevice;

/**
 * A device selector is used to pick
 *
 * @author royer
 */
public interface DeviceSelector
{
  /**
   * In some case the whole list of devices is needed before being able to
   * decide on whether a particular device is selected, this method provided
   * this possibility.
   * 
   * @param pDevices
   *          list of devices to consider
   */
  void init(ArrayList<ClearCLDevice> pDevices);

  /**
   * Returns true if the device should be selected, multiple devices can be
   * selected.
   * 
   * @param pClearCLDevice
   *          device
   * @return true if selected, false otherwise.
   */
  boolean selected(ClearCLDevice pClearCLDevice);
}
