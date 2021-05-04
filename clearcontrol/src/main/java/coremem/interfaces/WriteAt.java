package coremem.interfaces;

/**
 * Memory objects implementing this interface provide methods to write single
 * primitive types. Offsets are byte-based.
 *
 * @author royer
 */
public interface WriteAt extends MemoryTyped
{
  /**
   * Writes a value at a given offset.
   *
   * @param pOffset offset
   * @param pValue  value to set
   */
  public void setByte(final long pOffset, final byte pValue);

  /**
   * Writes a value at a given offset.
   *
   * @param pOffset offset
   * @param pValue  value to set
   */
  public void setChar(final long pOffset, final char pValue);

  /**
   * Writes a value at a given offset.
   *
   * @param pOffset offset
   * @param pValue  value to set
   */
  public void setShort(final long pOffset, final short pValue);

  /**
   * Writes a value at a given offset.
   *
   * @param pOffset offset
   * @param pValue  value to set
   */
  public void setInt(final long pOffset, final int pValue);

  /**
   * Writes a value at a given offset.
   *
   * @param pOffset offset
   * @param pValue  value to set
   */
  public void setLong(final long pOffset, final long pValue);

  /**
   * Writes a value at a given offset.
   *
   * @param pOffset offset
   * @param pValue  value to set
   */
  public void setFloat(final long pOffset, final float pValue);

  /**
   * Writes a value at a given offset.
   *
   * @param pOffset offset
   * @param pValue  value to set
   */
  public void setDouble(final long pOffset, final double pValue);

}
