package aptj;

import aptj.bindings.APTLibrary;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.bridj.CLong;
import org.bridj.Pointer;

/**
 * APTJ Device factory
 *
 * @author royer
 */
public class APTJDeviceFactory implements AutoCloseable
{
  private APTJDeviceType mAPTDeviceType;
  private BidiMap<Integer, Long> mIndexToSerialBidiMap = new DualHashBidiMap<>();

  /**
   * Instantiates an APTJ Library object given a device type
   *
   * @param pAPTDeviceType device type
   * @throws APTJExeption exception
   */
  public APTJDeviceFactory(APTJDeviceType pAPTDeviceType) throws APTJExeption
  {
    super();
    mAPTDeviceType = pAPTDeviceType;
    checkError(APTLibrary.APTInit());
    enumerateDevices();
  }

  @Override
  public void close() throws Exception
  {
    checkError(APTLibrary.APTCleanUp());
  }

  private final void enumerateDevices() throws APTJExeption
  {
    mIndexToSerialBidiMap.clear();

    int lNumberOfDevices = getNumberOfDevices();
    for (int i = 0; i < lNumberOfDevices; i++)
    {
      Pointer<CLong> lPointerSerialNum = Pointer.allocateCLong();
      checkError(APTLibrary.GetHWSerialNumEx(mAPTDeviceType.getTypeId(), i, lPointerSerialNum));
      long lSerialNumber = lPointerSerialNum.getCLong();
      lPointerSerialNum.release();

      mIndexToSerialBidiMap.put(i, lSerialNumber);

    }
  }

  /**
   * Creates a device of given index
   *
   * @param pSerialNumber device index
   * @return APTJ device
   * @throws APTJExeption exception
   */
  public final APTJDevice createDeviceFromSerialNumber(long pSerialNumber) throws APTJExeption
  {
    return new APTJDevice(this, mAPTDeviceType, pSerialNumber);
  }

  /**
   * Creates a device of given index
   *
   * @param pDeviceIndex device index
   * @return APTJ device
   * @throws APTJExeption exception
   */
  public final APTJDevice createDeviceFromIndex(int pDeviceIndex) throws APTJExeption
  {
    return new APTJDevice(this, mAPTDeviceType, mIndexToSerialBidiMap.get(pDeviceIndex));
  }

  static long checkError(long pReturnCode) throws APTJExeption
  {
    if (pReturnCode != 0)
    {
      System.out.println("Return code=" + pReturnCode);
      throw new APTJExeption(pReturnCode);
    }
    return pReturnCode;
  }

  /**
   * Returns the number of devices for the device type associated to this
   * library instance
   *
   * @return number of devices
   */
  public final int getNumberOfDevices()
  {
    Pointer<CLong> lPointerNumDevices = Pointer.allocateCLong();

    APTLibrary.GetNumHWUnitsEx(mAPTDeviceType.getTypeId(), lPointerNumDevices);

    long lNumberOfDevices = lPointerNumDevices.getCLong();
    lPointerNumDevices.release();

    return (int) lNumberOfDevices;
  }

}
