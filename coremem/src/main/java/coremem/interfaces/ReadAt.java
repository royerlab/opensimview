package coremem.interfaces;

/**
 * Memory objects implementing this interface provide methods to read single
 * primitive types. Offsets are byte-based.
 *
 * @author royer
 */
public interface ReadAt
{
  /**
   * Reads a value at a given offset.
   * 
   * @param pOffset
   *          offset
   * @return value
   */
  public byte getByte(final long pOffset);

  /**
   * Reads a value at a given offset.
   * 
   * @param pOffset
   *          offset
   * @return value
   */
  public char getChar(final long pOffset);

  /**
   * Reads a value at a given offset.
   * 
   * @param pOffset
   *          offset
   * @return value
   */
  public short getShort(final long pOffset);

  /**
   * Reads a value at a given offset.
   * 
   * @param pOffset
   *          offset
   * @return value
   */
  public int getInt(final long pOffset);

  /**
   * Reads a value at a given offset.
   * 
   * @param pOffset
   *          offset
   * @return value
   */
  public long getLong(final long pOffset);

  /**
   * Reads a value at a given offset.
   * 
   * @param pOffset
   *          offset
   * @return value
   */
  public float getFloat(final long pOffset);

  /**
   * Reads a value at a given offset.
   * 
   * @param pOffset
   *          offset
   * @return value
   */
  public double getDouble(final long pOffset);

}
