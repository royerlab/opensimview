package dcamj2;

import dcamapi.*;
import dcamapi.DcamapiLibrary.*;
import org.bridj.BridJ;
import org.bridj.IntValuedEnum;
import org.bridj.Pointer;

import static org.bridj.Pointer.allocateBytes;
import static org.bridj.Pointer.pointerTo;
import static org.junit.Assert.assertTrue;

/**
 * Dcam device
 *
 * @author royer
 */
public class DcamDevice extends DcamBase implements AutoCloseable
{
  private long mDeviceID;
  private long mDevicePeer;
  private boolean mExternalTriggering;

  DcamProperties mDcamProperties;
  DcamWait mDcamWait;

  DcamBufferControl mBufferControl;


  /**
   * Instantiates a camera device given a device id (index)
   *
   * @param pDeviceID        device id (index)
   * @param pOpen            true: opens device at construction time
   * @param pExternalTrigger true: external triggering active
   */
  public DcamDevice(final long pDeviceID, boolean pOpen, boolean pExternalTrigger)
  {
    super();
    mDeviceID = pDeviceID;
    mExternalTriggering = pExternalTrigger;
    if (pOpen) open();

  }


  /**
   * Returns the device id (index)
   *
   * @return device id
   */
  public long getDeviceID()
  {
    return mDeviceID;
  }

  public boolean isExternalTriggering()
  {
    return mExternalTriggering;
  }

  /**
   * Opens camera device
   *
   * @return true: success
   */
  public final boolean open()
  {
    final DCAMDEV_OPEN lDCAMDEV_OPEN = new DCAMDEV_OPEN();
    final long size = BridJ.sizeOf(DCAMDEV_OPEN.class);
    assertTrue(size == 16);
    lDCAMDEV_OPEN.size(size);

    lDCAMDEV_OPEN.index(getDeviceID());
    @SuppressWarnings("deprecation") final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamdevOpen(pointerTo(lDCAMDEV_OPEN));
    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);

    if (lSuccess)
    {
      mDevicePeer = lDCAMDEV_OPEN.hdcam().getPeer();
    }

    for (int i = 0; i < 100; i++)
    {

      if (isReady() || isUnstable()) break;
      try
      {
        System.out.println("Waiting for camera to be ready!");
        System.out.println("Status:" + getStatus());
        Thread.sleep(100);
      } catch (InterruptedException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    if (mExternalTriggering) setInputTriggerToExternalFastEdge();
    else setInputTriggerToInternal();
    setOutputTriggerToExposure();
    setDefectCorectionMode(true);

    return lSuccess;
  }

  /**
   * Returns whether this device is busy.
   *
   * @return true if busy
   */
  public boolean isBusy()
  {
    return (DCAMCAP_STATUS.DCAMCAP_STATUS_BUSY.value & getStatus().value()) == DCAMCAP_STATUS.DCAMCAP_STATUS_BUSY.value;
  }

  /**
   * Returns whether this device is ready.
   *
   * @return true if ready
   */
  public boolean isReady()
  {
    return (DCAMCAP_STATUS.DCAMCAP_STATUS_READY.value & getStatus().value()) == DCAMCAP_STATUS.DCAMCAP_STATUS_READY.value;
  }

  /**
   * Returns whether this device has an error.
   *
   * @return true if error
   */
  public boolean isError()
  {
    return (DCAMCAP_STATUS.DCAMCAP_STATUS_ERROR.value & getStatus().value()) == DCAMCAP_STATUS.DCAMCAP_STATUS_ERROR.value;
  }

  /**
   * Returns whether this device is stable
   *
   * @return true if stable
   */
  public boolean isStable()
  {
    return (DCAMCAP_STATUS.DCAMCAP_STATUS_STABLE.value & getStatus().value()) == DCAMCAP_STATUS.DCAMCAP_STATUS_STABLE.value;
  }

  /**
   * Returns whether this device is unstable
   *
   * @return true if unstable
   */
  public boolean isUnstable()
  {
    return (DCAMCAP_STATUS.DCAMCAP_STATUS_UNSTABLE.value & getStatus().value()) == DCAMCAP_STATUS.DCAMCAP_STATUS_UNSTABLE.value;
  }

  /**
   * Returns the camera device current exposure
   *
   * @return current exposure in seconds
   */
  public double getExposure()
  {
    return getProperties().getDoublePropertyValue(DCAMIDPROP.DCAM_IDPROP_EXPOSURETIME);
  }

  /**
   * Sets a new exposure
   *
   * @param pExposure exposure in seconds
   */
  public void setExposure(final double pExposure)
  {
    getProperties().setDoublePropertyValue(DCAMIDPROP.DCAM_IDPROP_EXPOSURETIME, pExposure);
  }

  /**
   * Sets and gets the actual exposure
   *
   * @param pExposure exposure in seconds
   * @return actual exposure (camera snaps exposure to specific values that are
   * different from what is asked for)
   */
  public double setAndGetExposure(final double pExposure)
  {
    final double lEffectiveExposure = getProperties().setAndGetDoublePropertyValue(DCAMIDPROP.DCAM_IDPROP_EXPOSURETIME, pExposure);
    /*System.out.format("DcamJ: exposure requested: %g effective exposure: %g  \n",
                      pExposure,
                      lEffectiveExposure);/**/

    return lEffectiveExposure;
  }

  /**
   * Sets centered ROI
   *
   * @param pCenteredWidth  centered width
   * @param pCenteredHeight centered height
   * @return true
   */
  public boolean setCenteredROI(final long pCenteredWidth, final long pCenteredHeight)
  {
    format(getDeviceID() + "Status before sleep in setCenteredROI " + getStatus());

    final long lWidth = adjustWidthHeight(pCenteredWidth, 4);
    final long lHeight = adjustWidthHeight(pCenteredHeight, 4);

    if (getWidth() != lWidth || getHeight() != lHeight)
    {
      close();
      sleep(100);
      open();
      sleep(100);
    }

    final long hpos = adjustWidthHeight(1024 - lWidth / 2, 4);
    final long vpos = adjustWidthHeight(1024 - lHeight / 2, 4);

    boolean lSuccessHsize = getProperties().setDoublePropertyValue(DCAMIDPROP.DCAM_IDPROP_SUBARRAYHSIZE, lWidth);

    if (!lSuccessHsize) format(getDeviceID() + "could not set hsize\n");

    boolean lSuccessVsize = getProperties().setDoublePropertyValue(DCAMIDPROP.DCAM_IDPROP_SUBARRAYVSIZE, lHeight);

    if (!lSuccessVsize) format(getDeviceID() + "could not set vsize\n");

    boolean lSuccessHpos = getProperties().setDoublePropertyValue(DCAMIDPROP.DCAM_IDPROP_SUBARRAYHPOS, hpos);
    if (!lSuccessHpos) format(getDeviceID() + "could not set hpos\n");

    boolean lSuccessVpos = getProperties().setDoublePropertyValue(DCAMIDPROP.DCAM_IDPROP_SUBARRAYVPOS, vpos);

    if (!lSuccessVpos) format(getDeviceID() + "could not set vpos\n");

    boolean lSuccessSubArray = getProperties().setDoublePropertyValue(DCAMIDPROP.DCAM_IDPROP_SUBARRAYMODE, 2);

    if (!lSuccessSubArray) format(getDeviceID() + "could not set subarray mode");

    boolean lSuccess = lSuccessHsize && lSuccessVsize && lSuccessHpos && lSuccessVpos && lSuccessSubArray;

    format(getDeviceID() + "DcamJ: ROI: parameters: cwidth=%d, cheight=%d, hpos=%d, vpos=%d, width=%d, height=%d --> success=%s  \n", pCenteredWidth, pCenteredHeight, hpos, vpos, lWidth, lHeight, lSuccess ? "true" : "false");/**/

    return lSuccess;
  }

  private void sleep(int lSteepTime)
  {
    try
    {
      Thread.sleep(lSteepTime);
    } catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Returns current image width (centered ROI)
   *
   * @return image width
   */
  public long getWidth()
  {
    long hsize = (long) getProperties().getDoublePropertyValue(DCAMIDPROP.DCAM_IDPROP_SUBARRAYHSIZE);
    return hsize;
  }

  /**
   * Returns current image height (centered ROI)
   *
   * @return image height
   */
  public long getHeight()
  {
    long vsize = (long) getProperties().getDoublePropertyValue(DCAMIDPROP.DCAM_IDPROP_SUBARRAYVSIZE);
    return vsize;
  }

  /**
   * Returns current image X position (centered ROI)
   *
   * @return image X position
   */
  public long getX()
  {
    long hpos = (long) getProperties().getDoublePropertyValue(DCAMIDPROP.DCAM_IDPROP_SUBARRAYHPOS);
    return hpos;
  }

  /**
   * Returns current image position (centered ROI)
   *
   * @return image Y position
   */
  public long getY()
  {
    long vpos = (long) getProperties().getDoublePropertyValue(DCAMIDPROP.DCAM_IDPROP_SUBARRAYVPOS);
    return vpos;
  }

  /**
   * Sets pixel binning
   *
   * @param pBinSize bin size
   * @return true: success
   */
  public boolean setBinning(final int pBinSize)
  {
    boolean lSuccess = true;
    lSuccess &= getProperties().setDoublePropertyValue(DCAMIDPROP.DCAM_IDPROP_BINNING, pBinSize);

    return lSuccess;
  }

  /**
   * Returns current pixel binning (1, 2 or 4)
   *
   * @return binning (1, 2 or 4)
   */
  public long getBinning()
  {
    return (long) getProperties().getDoublePropertyValue(DCAMIDPROP.DCAM_IDPROP_BINNING);
  }

  /**
   * Sets input trigger defaults
   */
  public void setInputTriggerDefaults()
  {
    getProperties().setModePropertyValue(DCAMIDPROP.DCAM_IDPROP_TRIGGER_MODE, DCAMPROPMODEVALUE.DCAMPROP_TRIGGER_MODE__NORMAL);
    getProperties().setModePropertyValue(DCAMIDPROP.DCAM_IDPROP_TRIGGERPOLARITY, DCAMPROPMODEVALUE.DCAMPROP_TRIGGERPOLARITY__POSITIVE);

    getProperties().setModePropertyValue(DCAMIDPROP.DCAM_IDPROP_TRIGGER_CONNECTOR, DCAMPROPMODEVALUE.DCAMPROP_TRIGGER_CONNECTOR__BNC);

    getProperties().setDoublePropertyValue(DCAMIDPROP.DCAM_IDPROP_TRIGGERTIMES, 1);

    getProperties().setDoublePropertyValue(DCAMIDPROP.DCAM_IDPROP_TRIGGERDELAY, 0);
  }

  /**
   * Sets output trigger defaults
   */
  public void setOutputTriggerDefaults()
  {
    getProperties().setModePropertyValue(DCAMIDPROP.DCAM_IDPROP_TRIGGER_CONNECTOR, DCAMPROPMODEVALUE.DCAMPROP_TRIGGER_CONNECTOR__BNC);
  }

  /**
   * Sets input trigger to internal triggering
   */
  public void setInputTriggerToInternal()
  {
    getProperties().setModePropertyValue(DCAMIDPROP.DCAM_IDPROP_TRIGGERSOURCE, DCAMPROPMODEVALUE.DCAMPROP_TRIGGERSOURCE__INTERNAL);
  }

  /**
   * Sets input trigger to external edge triggering
   */
  public void setInputTriggerToExternalEdge()
  {
    setInputTriggerDefaults();
    getProperties().setModePropertyValue(DCAMIDPROP.DCAM_IDPROP_TRIGGERSOURCE, DCAMPROPMODEVALUE.DCAMPROP_TRIGGERSOURCE__EXTERNAL);
    getProperties().setModePropertyValue(DCAMIDPROP.DCAM_IDPROP_TRIGGERACTIVE, DCAMPROPMODEVALUE.DCAMPROP_TRIGGERACTIVE__EDGE);
    setOutputTriggerToExposure();
  }

  /**
   * Sets input trigger to external level
   */
  public void setInputTriggerToExternalLevel()
  {
    setInputTriggerDefaults();
    getProperties().setModePropertyValue(DCAMIDPROP.DCAM_IDPROP_TRIGGERSOURCE, DCAMPROPMODEVALUE.DCAMPROP_TRIGGERSOURCE__EXTERNAL);
    getProperties().setModePropertyValue(DCAMIDPROP.DCAM_IDPROP_TRIGGERACTIVE, DCAMPROPMODEVALUE.DCAMPROP_TRIGGERACTIVE__LEVEL);
    setOutputTriggerToExposure();
  }

  /**
   * Sets input trigger to external fast edge.
   * (This is the default mode used by CC)
   */
  public void setInputTriggerToExternalFastEdge()
  {
    setInputTriggerDefaults();
    getProperties().setModePropertyValue(DCAMIDPROP.DCAM_IDPROP_TRIGGERSOURCE, DCAMPROPMODEVALUE.DCAMPROP_TRIGGERSOURCE__EXTERNAL);
    getProperties().setModePropertyValue(DCAMIDPROP.DCAM_IDPROP_TRIGGERACTIVE, DCAMPROPMODEVALUE.DCAMPROP_TRIGGERACTIVE__SYNCREADOUT);
    setOutputTriggerToExposure();
  }

  /**
   * Sets input triggering to software
   */
  public void setInputTriggerToSoftware()
  {
    getProperties().setModePropertyValue(DCAMIDPROP.DCAM_IDPROP_TRIGGERSOURCE, DCAMPROPMODEVALUE.DCAMPROP_TRIGGERSOURCE__SOFTWARE);
  }

  /**
   * Sets output trigger to exposure
   */
  public void setOutputTriggerToExposure()
  {
    setOutputTriggerDefaults();
    getProperties().setModePropertyValue(DCAMIDPROP.DCAM_IDPROP_OUTPUTTRIGGER_POLARITY, DCAMPROPMODEVALUE.DCAMPROP_OUTPUTTRIGGER_POLARITY__POSITIVE);

    getProperties().setModePropertyValue(DCAMIDPROP.DCAM_IDPROP_OUTPUTTRIGGER_KIND, DCAMPROPMODEVALUE.DCAMPROP_OUTPUTTRIGGER_KIND__EXPOSURE);

  }

  /**
   * Sets output trigger to programmable
   */
  public void setOutputTriggerToProgrammable()
  {
    setOutputTriggerDefaults();
    getProperties().setModePropertyValue(DCAMIDPROP.DCAM_IDPROP_OUTPUTTRIGGER_POLARITY, DCAMPROPMODEVALUE.DCAMPROP_OUTPUTTRIGGER_POLARITY__POSITIVE);

    getProperties().setModePropertyValue(DCAMIDPROP.DCAM_IDPROP_OUTPUTTRIGGER_KIND, DCAMPROPMODEVALUE.DCAMPROP_OUTPUTTRIGGER_KIND__PROGRAMABLE);

  }

  /**
   * Sets defect correction mode
   *
   * @param pDefectCorrections defect correction mode on (true) or off (false)
   */
  public void setDefectCorectionMode(final boolean pDefectCorrections)
  {
    getProperties().setModePropertyValue(DCAMIDPROP.DCAM_IDPROP_DEFECTCORRECT_MODE, pDefectCorrections ? DCAMPROPMODEVALUE.DCAMPROP_MODE__ON : DCAMPROPMODEVALUE.DCAMPROP_MODE__OFF);
  }

  /**
   * Utility function to adjust the width and height of camera images to the for
   * a given multiple.
   *
   * @param pWidthOrHeight camera image width or height
   * @param pMultiple      Multiple to adjust camera width or height
   * @return closest multiple of 4
   */
  public long adjustWidthHeight(long pWidthOrHeight, long pMultiple)
  {
    return (pMultiple * Math.round(pWidthOrHeight / pMultiple));
  }

  @SuppressWarnings("deprecation")
  final Pointer<HDCAM_struct> getHDCAMPointer()
  {
    return Pointer.pointerToAddress(mDevicePeer, HDCAM_struct.class);
  }

  final String getDeviceString(final DCAM_IDSTR pDCAM_IDSTR)
  {
    final Pointer<Byte> lPointerToString = allocateBytes(256);

    final DCAMDEV_STRING lDCAMDEV_STRING = new DCAMDEV_STRING();
    lDCAMDEV_STRING.size(BridJ.sizeOf(DCAMDEV_STRING.class));
    lDCAMDEV_STRING.iString(pDCAM_IDSTR.value());
    lDCAMDEV_STRING.text(lPointerToString);
    lDCAMDEV_STRING.textbytes(lPointerToString.getValidBytes());

    @SuppressWarnings("deprecation") final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamdevGetstring(getHDCAMPointer(), pointerTo(lDCAMDEV_STRING));

    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);
    if (lSuccess)
    {
      final String lString = new String(lPointerToString.getBytes());
      return lString.trim();
    } else
    {
      return "DcamJ: Could not retreive String for: " + pDCAM_IDSTR.toString();
    }
  }

  /**
   * Returns the name of this device - the concatenation of the vendor, model
   * and id.
   *
   * @return device name
   */
  public String getCameraName()
  {
    final String lVendor = getDeviceString(DCAM_IDSTR.DCAM_IDSTR_VENDOR);

    final String lModel = getDeviceString(DCAM_IDSTR.DCAM_IDSTR_MODEL);

    final String lCameraId = getDeviceString(DCAM_IDSTR.DCAM_IDSTR_CAMERAID);

    String lName = lVendor + " " + lModel + " " + lCameraId;

    return lName;
  }

  /**
   * Returns information about this device on the standard output
   */
  public final void printDeviceInfo()
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

  /**
   * Starts continuous acquisition
   *
   * @return true: success, false otherwise
   */
  public final boolean startContinuous()
  {
    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamcapStart(getHDCAMPointer(), DCAMCAP_START.DCAMCAP_START_SEQUENCE.value);
    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);
    return lSuccess;
  }

  /**
   * Starts sequence acquisition
   *
   * @return true: success, false otherwise
   */
  public final boolean startSequence()
  {
    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamcapStart(getHDCAMPointer(), DCAMCAP_START.DCAMCAP_START_SNAP.value);
    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);
    return lSuccess;
  }

  /**
   * triggers in software one acquisition
   *
   * @return true: success, false otherwise
   */
  public final boolean triggerOneAcquisition()
  {
    // DCAMERR DCAMAPI dcamcap_firetrigger ( HDCAM h, long iKind
    // DCAM_DEFAULT_ARG );
    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamcapFiretrigger(getHDCAMPointer(), 0);
    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);
    return lSuccess;
  }

  /**
   * Stops acquisition
   *
   * @return true: success, false otherwise
   */
  public final boolean stop()
  {
    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamcapStop(getHDCAMPointer());
    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);
    return lSuccess;
  }

  @Override
  public final void close()
  {
    if (mBufferControl != null)
    {
      mBufferControl.releaseBuffers();
      mBufferControl = null;
    }

    if (mDcamWait != null)
    {
      mDcamWait.close();
      mDcamWait = null;
    }

    if (mDcamProperties != null)
    {
      mDcamProperties = null;
    }

    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamdevClose(getHDCAMPointer());
    addErrorToListAndCheckHasSucceeded(lError);

    mDevicePeer = -1;

  }

  /**
   * Returns a buffer control object for this device
   *
   * @return buffer control object for this device
   */
  public final DcamBufferControl getBufferControl()
  {
    if (mBufferControl == null)
    {
      mBufferControl = new DcamBufferControl(this);
    }
    return mBufferControl;
  }

  /**
   * Return a properties object for this device
   *
   * @return properties object for this device
   */
  public DcamProperties getProperties()
  {
    if (mDcamProperties == null)
    {
      mDcamProperties = new DcamProperties(this);
    }
    return mDcamProperties;
  }

  /**
   * Return a wait object for this device
   *
   * @return wait object for this device
   */
  public final DcamWait getDcamWait()
  {
    if (mDcamWait == null) mDcamWait = new DcamWait(this);
    return mDcamWait;
  }

  /**
   * Shows a panel for this device
   *
   * @return true: success, false otherwise
   */
  public final boolean showPanel()
  {
    final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamdevShowpanel(getHDCAMPointer(), 1);
    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);
    return lSuccess;
  }

  /**
   * Returns this device status
   *
   * @return camera device status
   */
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

  final DCAMCAP_TRANSFERINFO getTransferInfo()
  {
    // DCAMERR DCAMAPI dcamcap_transferinfo ( HDCAM h, DCAMCAP_TRANSFERINFO*
    // param );

    final DCAMCAP_TRANSFERINFO lDCAMCAP_TRANSFERINFO = new DCAMCAP_TRANSFERINFO();
    lDCAMCAP_TRANSFERINFO.size(BridJ.sizeOf(DCAMCAP_TRANSFERINFO.class));

    @SuppressWarnings("deprecation") final IntValuedEnum<DCAMERR> lError = DcamapiLibrary.dcamcapTransferinfo(getHDCAMPointer(), pointerTo(lDCAMCAP_TRANSFERINFO));

    final boolean lSuccess = addErrorToListAndCheckHasSucceeded(lError);
    if (lSuccess)
    {
      return lDCAMCAP_TRANSFERINFO;
    }
    return null;
  }


  @Override
  public String toString()
  {
    return getCameraName();
  }


}
