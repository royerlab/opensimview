package aptj;

/**
 * APTJ device type
 *
 * @author royer
 */
public enum APTJDeviceType
{
 /**
  * 1 Ch benchtop stepper driver
  */
 BSC001(11),

 /**
  * 1 Ch benchtop stepper driver
  */
 BSC101(12),

 /**
  * 2 Ch benchtop stepper driver
  */
 BSC002(13),

 /**
  * 1 Ch benchtop DC servo driver
  */
 BDC101(14),

 /**
  * 1 Ch stepper driver card (used within BSC102,103 units)
  */
 SCC001(21),

 /**
  * 1 Ch DC servo driver card (used within BDC102,103 units)
  */
 DCC001(22),

 /**
  * 1 Ch DC servo driver cube
  */
 ODC001(24),

 /**
  * 1 Ch stepper driver cube
  */
 OST001(25),

 /**
  * 2 Ch modular stepper driver module
  */
 MST601(26),

 /**
  * 1 Ch Stepper driver T-Cube
  */
 TST001(29),

 /**
  * 1 Ch DC servo driver T-Cube
  */
 TDC001(31),

 /**
  * LTS300/LTS150 Long Travel Integrated Driver/Stages
  */
 LTSXXX(42),

 /**
  * L490MZ Integrated Driver/Labjack
  */
 L490MZ(43),

 /**
  * 1/2/3 Ch benchtop brushless DC servo driver
  */
 BBD10X(44);

  private int mTypeId;

  APTJDeviceType(int pTypeId)
  {
    this.mTypeId = pTypeId;
  }

  /**
   * Returns the device type id
   * 
   * @return device type id
   */
  public int getTypeId()
  {
    return mTypeId;
  }

}
