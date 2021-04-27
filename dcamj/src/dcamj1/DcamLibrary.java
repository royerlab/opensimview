package dcamj1;

import static org.bridj.Pointer.pointerTo;

import dcamapi.DCAMAPI_INIT;
import dcamapi.DcamapiLibrary;
import dcamapi.DcamapiLibrary.DCAMERR;

import org.bridj.BridJ;
import org.bridj.IntValuedEnum;

public class DcamLibrary
{

  // Prevents instantiation
  private DcamLibrary()
  {
    super();
  }

  private static boolean sInitialized = false;
  private static long sNumberOfDevices = -1;

  public static final boolean initialize()
  {
    final DCAMAPI_INIT lDCAMAPI_INIT = new DCAMAPI_INIT();
    lDCAMAPI_INIT.size(BridJ.sizeOf(DCAMAPI_INIT.class));
    final IntValuedEnum<DCAMERR> dcamapiInit =
                                             DcamapiLibrary.dcamapiInit(pointerTo(lDCAMAPI_INIT));

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
            uninitialize();
          }
          catch (Throwable e)
          {
            e.printStackTrace();
          }
        }
      });

      return true;
    }
    else
    {
      return false;
    }
  }

  public static final boolean isInitialized()
  {
    return sInitialized;
  }

  public static final int getNumberOfDevices()
  {
    return (int) sNumberOfDevices;
  }

  public static final DcamDevice getDeviceForId(final int pDeviceId)
  {
    if (!isInitialized())
    {
      return null;
    }
    final DcamDevice lDcamDevice = new DcamDevice(pDeviceId);
    return lDcamDevice;
  }

  public static final boolean uninitialize()
  {
    if (!isInitialized())
    {
      return false;
    }
    final IntValuedEnum<DCAMERR> lDcamapiUninit =
                                                DcamapiLibrary.dcamapiUninit();
    final boolean lSuccess = hasSucceeded(lDcamapiUninit);
    return lSuccess;
  }

  public static boolean hasSucceeded(final IntValuedEnum<DCAMERR> dcamapiInit)
  {
    return dcamapiInit == DCAMERR.DCAMERR_SUCCESS;
  }
}
