package dcamj2;

import dcamapi.DCAMAPI_INIT;
import dcamapi.DcamapiLibrary;
import dcamapi.DcamapiLibrary.DCAMERR;
import org.bridj.BridJ;
import org.bridj.IntValuedEnum;

import static org.bridj.Pointer.pointerTo;

/**
 * Dcam Library
 *
 * @author royer
 */
public class DcamLibrary
{

  // Prevents instantiation
  private DcamLibrary()
  {
    super();
  }

  private static boolean sInitialized = false;
  private static long sNumberOfDevices = -1;

  /**
   * Initializes the library
   *
   * @return true: success
   */
  public static final boolean initialize()
  {
    if (isInitialized()) return true;

    final DCAMAPI_INIT lDCAMAPI_INIT = new DCAMAPI_INIT();
    lDCAMAPI_INIT.size(BridJ.sizeOf(DCAMAPI_INIT.class));
    @SuppressWarnings("deprecation") final IntValuedEnum<DCAMERR> dcamapiInit = DcamapiLibrary.dcamapiInit(pointerTo(lDCAMAPI_INIT));

    final boolean lSuccess = hasSucceeded(dcamapiInit);

    if (lSuccess)
    {
      sInitialized = true;

      sNumberOfDevices = lDCAMAPI_INIT.iDeviceCount();

      Runtime.getRuntime().addShutdownHook(new Thread()
      {
        @Override
        public void run()
        {
          try
          {
            System.out.println("DcamJ: shutdown thread uninitializing Dcam driver");
            uninitialize();
          } catch (Throwable e)
          {
            e.printStackTrace();
          }
        }
      });

      return true;
    } else
    {
      return false;
    }
  }

  /**
   * Returns true if library successfully initialized
   *
   * @return true: success, false otherwise
   */
  public static final boolean isInitialized()
  {
    return sInitialized;
  }

  /**
   * Returns the number of connected devices
   *
   * @return number of connected devices
   */
  public static final int getNumberOfDevices()
  {
    return (int) sNumberOfDevices;
  }

  /**
   * Returns a camera device for a given device index
   *
   * @param pDeviceId device index
   * @return camera device
   */
  public static final DcamDevice getDeviceForId(final long pDeviceId, boolean pOpen, boolean pExternalTrigger)
  {
    if (!isInitialized()) throw new DcamException("Library must be first initialized");

    final DcamDevice lDcamDevice = new DcamDevice(pDeviceId, pOpen, pExternalTrigger);
    return lDcamDevice;
  }

  /**
   * Uninitialized library
   *
   * @return true: success, false otherwise
   */
  public static final boolean uninitialize()
  {
    if (!isInitialized()) return false;

    final IntValuedEnum<DCAMERR> lDcamapiUninit = DcamapiLibrary.dcamapiUninit();
    final boolean lSuccess = hasSucceeded(lDcamapiUninit);
    return lSuccess;
  }

  /**
   * Returns true if the corresponding Dcam API return code means success.
   *
   * @param pDcamReturnCode Dcam return code
   * @return true: success, false otherwise
   */
  public static boolean hasSucceeded(final IntValuedEnum<DCAMERR> pDcamReturnCode)
  {
    return pDcamReturnCode == DCAMERR.DCAMERR_SUCCESS;
  }
}
