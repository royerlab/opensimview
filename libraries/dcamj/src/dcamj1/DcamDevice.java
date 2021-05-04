package dcamj1;

import dcamapi.*;
import dcamapi.DcamapiLibrary.DCAMCAP_START;
import dcamapi.DcamapiLibrary.DCAMCAP_STATUS;
import dcamapi.DcamapiLibrary.DCAMERR;
import dcamapi.DcamapiLibrary.DCAM_IDSTR;
import org.bridj.BridJ;
import org.bridj.IntValuedEnum;
import org.bridj.Pointer;

import static org.bridj.Pointer.allocateBytes;
import static org.bridj.Pointer.pointerTo;
import static org.junit.Assert.assertTrue;

public class DcamDevice extends DcamBase implements AutoCloseable
{
  private long mDeviceID;

  private DcamProperties mDcamProperties;
  private DcamWait mDcamWait;

  private DcamBufferControl mBufferControl;

  public DcamDevice(final int pDeviceID)
  {
    super();
    mDeviceID = pDeviceID;
    open();
  }

  @SuppressWarnings("deprecation")
  final Pointer<HDCAM_struct> getHDCAMPointer()
  {
    return Pointer.pointerToAddress(mDeviceID, HDCAM_struct.class);
  }

  public final String getDeviceString(final DCAM_IDSTR pDCAM_IDSTR)
  {
    final Pointer<Byte> lPointerToString = allocateBytes(256);

    final DCAMDEV_STRING lDCAMDEV_STRING = new DCAMDEV_STRING();
    lDCAMDEV_STRING.size(BridJ.sizeOf(DCAMDEV_STRING.class));
    lDCAMDEV_STRING.iString(pDCAM_IDSTR.value());
    lDCAMDEV_STRING.text(lPointerToString);
    lDCAMDEV_STRING.textbytes(lPointerToString.getValidBytes());

    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamdevGetstring(getHDCAMPointer(), pointerTo(lDCAMDEV_STRING));

    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);
    if (lSuccess)
    {
      final String lString = new String(lPointerToString.getBytes());
      return lString;
    } else
    {
      return "DcamJ: Could not retreive String for: " + pDCAM_IDSTR.toString();
    }
  }

  public final void displayDeviceInfo()
  {
    final String lVendor = getDeviceString(DCAM_IDSTR.DCAM_IDSTR_VENDOR);
    System.out.format("DCAM_IDSTR_VENDOR         = %s\n", lVendor);

    final String lModel = getDeviceString(DCAM_IDSTR.DCAM_IDSTR_MODEL);
    System.out.format("DCAM_IDSTR_MODEL          = %s\n", lModel);

    final String lCameraId = getDeviceString(DCAM_IDSTR.DCAM_IDSTR_CAMERAID);
    System.out.format("DCAM_IDSTR_CAMERAID       = %s\n", lCameraId);

    final String lBus = getDeviceString(DCAM_IDSTR.DCAM_IDSTR_BUS);
    System.out.format("DCAM_IDSTR_BUS            = %s\n", lBus);

    final String lCameraVersion = getDeviceString(DCAM_IDSTR.DCAM_IDSTR_CAMERAVERSION);
    System.out.format("DCAM_IDSTR_CAMERAVERSION  = %s\n", lCameraVersion);

    final String lDriverVersion = getDeviceString(DCAM_IDSTR.DCAM_IDSTR_DRIVERVERSION);
    System.out.format("DCAM_IDSTR_DRIVERVERSION  = %s\n", lDriverVersion);

    final String lModuleVersion = getDeviceString(DCAM_IDSTR.DCAM_IDSTR_MODULEVERSION);
    System.out.format("DCAM_IDSTR_MODULEVERSION  = %s\n", lModuleVersion);

    final String lDcamApiVersion = getDeviceString(DCAM_IDSTR.DCAM_IDSTR_DCAMAPIVERSION);
    System.out.format("DCAM_IDSTR_DCAMAPIVERSION = %s\n", lDcamApiVersion);
  }

  private final boolean open()
  {
    final DCAMDEV_OPEN lDCAMDEV_OPEN = new DCAMDEV_OPEN();
    final long size = BridJ.sizeOf(DCAMDEV_OPEN.class);
    assertTrue(size == 16);
    lDCAMDEV_OPEN.size(size);

    lDCAMDEV_OPEN.index(mDeviceID);
    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamdevOpen(pointerTo(lDCAMDEV_OPEN));
    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);

    if (lSuccess)
    {
      mDeviceID = lDCAMDEV_OPEN.hdcam().getPeer();
    }

    return lSuccess;
  }

  public final boolean startContinuous()
  {
    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamcapStart(getHDCAMPointer(), DCAMCAP_START.DCAMCAP_START_SEQUENCE.value);
    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);
    return lSuccess;
  }

  public final boolean startSequence()
  {
    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamcapStart(getHDCAMPointer(), DCAMCAP_START.DCAMCAP_START_SNAP.value);
    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);
    return lSuccess;
  }

  public final boolean stop()
  {
    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamcapStop(getHDCAMPointer());
    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);
    return lSuccess;
  }

  public final IntValuedEnum<DCAMCAP_STATUS> getStatus()
  {
    @SuppressWarnings({"unchecked", "rawtypes"}) final Pointer<IntValuedEnum<DCAMCAP_STATUS>> lPointerToStatus = (Pointer) Pointer.allocateCLong();

    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamcapStatus(getHDCAMPointer(), lPointerToStatus);
    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);
    if (lSuccess)
    {
      final IntValuedEnum<DCAMCAP_STATUS> lStatus = DCAMCAP_STATUS.fromValue((int) lPointerToStatus.getCLong());
      lPointerToStatus.release();
      return lStatus;
    }
    return null;
  }

  public final DCAMCAP_TRANSFERINFO getTransfertInfo()
  {
    // DCAMERR DCAMAPI dcamcap_transferinfo ( HDCAM h, DCAMCAP_TRANSFERINFO*
    // param );

    final DCAMCAP_TRANSFERINFO lDCAMCAP_TRANSFERINFO = new DCAMCAP_TRANSFERINFO();
    lDCAMCAP_TRANSFERINFO.size(BridJ.sizeOf(DCAMCAP_TRANSFERINFO.class));

    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamcapTransferinfo(getHDCAMPointer(), pointerTo(lDCAMCAP_TRANSFERINFO));

    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);
    if (lSuccess)
    {
      return lDCAMCAP_TRANSFERINFO;
    }
    return null;
  }

  public final boolean triggerOneAcquisition()
  {
    // DCAMERR DCAMAPI dcamcap_firetrigger ( HDCAM h, long iKind
    // DCAM_DEFAULT_ARG );
    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamcapFiretrigger(getHDCAMPointer(), 0);
    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);
    return lSuccess;
  }

  public DcamProperties getProperties()
  {
    if (mDcamProperties == null)
    {
      mDcamProperties = new DcamProperties(this);
    }
    return mDcamProperties;
  }

  public final DcamWait getDcamWait()
  {
    if (mDcamWait == null)
    {
      mDcamWait = new DcamWait(this);
    }
    return mDcamWait;
  }

  public final DcamBufferControl getBufferControl()
  {
    if (mBufferControl == null)
    {
      mBufferControl = new DcamBufferControl(this, null);
    }
    return mBufferControl;
  }

  @Override
  public final void close()
  {
    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamdevClose(getHDCAMPointer());
    addErrorToListAndCheckHasSucceeded(lError);
  }

  public final boolean showPanel()
  {
    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamdevShowpanel(getHDCAMPointer(), 1);
    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);
    return lSuccess;
  }

}
