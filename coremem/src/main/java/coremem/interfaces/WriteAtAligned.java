package coremem.interfaces;

/**
 * Memory objects implementing this interface provide methods to write single
 * primitive types. Offsets are aligned to the written type.
 * 
 * @author royer
 */
public interface WriteAtAligned extends MemoryTyped
{
  /**
   * Writes a value at a given offset. The offset unit is 1 byte.
   * 
   * @param pOffset
   *          offset
   * @param pValue
   *          value to set
   */
  public void setByteAligned(final long pOffset, final byte pValue);

  /**
   * Writes a value at a given offset. The offset unit is 2 bytes (1 char).
   * 
   * @param pOffset
   *          offset
   * @param pValue
   *          value to set
   */
  public void setCharAligned(final long pOffset, final char pValue);

  /**
   * Writes a value at a given offset. The offset unit is 2 bytes (1 short).
   * 
   * @param pOffset
   *          offset
   * @param pValue
   *          value to set
   */
  public void setShortAligned(final long pOffset, final short pValue);

  /**
   * Writes a value at a given offset. The offset unit is 4 bytes (1 int).
   * 
   * @param pOffset
   *          offset
   * @param pValue
   *          value to set
   */
  public void setIntAligned(final long pOffset, final int pValue);

  /**
   * Writes a value at a given offset. The offset unit is 8 bytes (1 long).
   * 
   * @param pOffset
   *          offset
   * @param pValue
   *          value to set
   */
  public void setLongAligned(final long pOffset, final long pValue);

  /**
   * Writes a value at a given offset. The offset unit is 4 bytes (1 float).
   * 
   * @param pOffset
   *          offset
   * @param pValue
   *          value to set
   */
  public void setFloatAligned(final long pOffset, final float pValue);

  /**
   * Writes a value at a given offset. The offset unit is 8 bytes (1 double).
   * 
   * @param pOffset
   *          offset
   * @param pValue
   *          value to set
   */
  public void setDoubleAligned(final long pOffset,
                               final double pValue);

}
