package aptj;

/**
 * APTJ return codes
 *
 * @author royer
 */
public enum APTJReturnCode
{

  ///// General

  /**
   * An unknown Server error has occurred.
   */
  MG17_UNKNOWN_ERR(10000),

  /**
   * A Server internal error has occurred.
   */
  MG17_INTERNAL_ERR(10001),

  /**
   * A Server call has failed.
   */
  MG17_FAILED(10002),

  /**
   * An attempt has been made to pass a parameter that is invalid or out of
   * range. In the case of motor commands, this error may occur when a move is
   * requested that exceeds the stage travel or exceeds the calibration data
   */
  MG17_INVALIDPARAM_ERR(10003),

  /**
   * An attempt has been made to save or load control parameters to the registry
   * (using the SaveParamSet or LoadParamSet methods) when the unit serial number
   * has not been specified.
   */
  MG17_SETTINGSNOTFOUND(10004),

  /**
   * APT DLL not initialized.
   */
  MG17_DLLNOTINITIALISED(10005),

  ///// PC System

  /**
   * An error has occurred whilst accessing the disk. Check that the drive is not
   * full, missing or corrupted.
   */
  MG17_DISKACCESS_ERR(10050),

  /**
   * An error has occurred with the ethernet connections or the windows sockets.
   */
  MG17_ETHERNET_ERR(10051),

  /**
   * An error has occurred whilst accessing the registry.
   */
  MG17_REGISTRY_ERR(10052),

  /**
   * An internal memory allocation error or de- allocation error has occurred.
   */
  MG17_MEMORY_ERR(10053),

  /**
   * An error has occurred with the COM system. Restart the program.
   */
  MG17_COM_ERR(10054),

  /**
   * An error has occurred with the USB communications.
   */
  MG17_USB_ERR(10055),

  /**
   * Not Thorlabs USB device error.
   * <p>
   * Rack and USB Units
   */
  MG17_NOTTHORLABSDEVICE_ERR(10056),

  /**
   * A serial number has been specified that is unknown to the server.
   */
  MG17_SERIALNUMUNKNOWN_ERR(10100),

  /**
   * A duplicate serial number has been detected.Serial numbers are required to
   * be unique.
   */
  MG17_DUPLICATESERIALNUM_ERR(10101),

  /**
   * A duplicate device identifier has been detected.
   */
  MG17_DUPLICATEDEVICEIDENT_ERR(10102),

  /**
   * An invalid message source has been detected.
   */
  MG17_INVALIDMSGSRC_ERR(10103),

  /**
   * A message has been received with an unknown identifier.
   */
  MG17_UNKNOWNMSGIDENT_ERR(10104),

  /**
   * An unknown hardware identifier has been encountered.
   */
  MG17_UNKNOWNHWTYPE_ERR(10105),

  /**
   * An invalid serial number has been detected.
   */
  MG17_INVALIDSERIALNUM_ERR(10106),

  /**
   * An invalid message destination ident has been detected.
   */
  MG17_INVALIDMSGDEST_ERR(10107),

  /**
   * An invalid index parameter has been passed.
   */
  MG17_INVALIDINDEX_ERR(10108),

  /**
   * A software call has been made to a control which is not currently
   * communicating with any hardware.This may be because the control has not been
   * started or may be due to an incorrect serial number or missing hardware.
   */
  MG17_CTRLCOMMSDISABLED_ERR(10109),

  /**
   * A notification or response message has been received from a hardware unit.
   * This may be indicate a hardware fault or that an illegal command/ parameter
   * has been sent to the hardware.
   */
  MG17_HWRESPONSE_ERR(10110),

  /**
   * A time out has occurred while waiting for a hardware unit to respond.This
   * may be due to communications problems or a hardware fault.
   */
  MG17_HWTIMEOUT_ERR(10111),

  /**
   * Some functions are applicable only to later versions of embedded code.This
   * error is returned when a software call is made to a unit with an
   * incompatible version of embedded code installed.
   */
  MG17_INCORRECTVERSION_ERR(10112),

  /**
   * Some functions are applicable only to later versions of hardware. This error
   * is returned when a software call is made to an incompatible version of
   * hardware.
   */
  MG17_INCOMPATIBLEHARDWARE_ERR(10115),

  /**
   * Older version of embedded code that can still be used
   * <p>
   * Motors
   */
  MG17_OLDVERSION_ERR(10116),

  /**
   * The GetStageAxisInfo method has been called when no stage has been assigned.
   */
  MG17_NOSTAGEAXISINFO(10150),

  /**
   * An internal error has occurred when using an encoded stage.
   */
  MG17_CALIBTABLE_ERR(10151),

  /**
   * An internal error has occurred when using an encoded stage.
   */
  MG17_ENCCALIB_ERR(10152),

  /**
   * A software call applicable only to encoded stages has been made to a
   * non-encoded stage.
   */
  MG17_ENCNOTPRESENT_ERR(10153),

  /**
   * motor not homed error
   */
  MG17_MOTORNOTHOMED_ERR(10154),

  /**
   * motor disabled error
   */
  MG17_MOTORDISABLED_ERR(10155),

  /**
   * PMD processor message error
   */
  MG17_PMDMSG_ERR(10156),

  /**
   * PMD based controller stage parameter 'read only' error
   */
  MG17_PMDREADONLY_ERR(10157),

  ///// Piezos
  /**
   * Encoder not present error
   */
  MG17_PIEZOLED_ERR(10200),

  ///// NanoTraks
  /**
   * Encoder not present error
   */
  MG17_NANOTRAKLED_ERR(10250),

  /**
   * Closed loop error - closed loop selected with no feedback signal
   */
  MG17_NANOTRAKCLOSEDLOOP_ERR(10251),

  /**
   * Power supply error - voltage rails out of limits
   */
  MG17_NANOTRAKPOWER_ERR(10252);

  private final long mReturnCode;

  private APTJReturnCode(long pReturnCode)
  {
    mReturnCode = pReturnCode;
  }

  /**
   * Returns the return code
   *
   * @return return code
   */
  public long getReturnCode()
  {
    return mReturnCode;
  }

  /**
   * Return the return code enum for a given return code int.
   *
   * @param pReturnCode return code int
   * @return return code enum
   */
  public static APTJReturnCode getByReturCodeInt(long pReturnCode)
  {
    for (APTJReturnCode lAPTJReturnCode : values())
    {
      if (lAPTJReturnCode.getReturnCode() == pReturnCode) return lAPTJReturnCode;
    }
    return null;
  }

}
