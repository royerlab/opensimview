package coremem.interfaces;

/**
 * Memory objects implementing this interface provide methods to read single
 * primitive types. Offsets are aligned to the written type.
 *
 * @author royer
 */
public interface ReadAtAligned
{
  /**
   * Reads a value at a given offset. The offset unit is 1 byte.
   *
   * @param pOffset offset
   * @return value
   */
  public byte getByteAligned(final long pOffset);

  /**
   * Reads a value at a given offset. The offset unit is 2 bytes (1 char).
   *
   * @param pOffset offset
   * @return value
   */
  public char getCharAligned(final long pOffset);

  /**
   * Reads a value at a given offset. The offset unit is 2 bytes (1 short).
   *
   * @param pOffset offset
   * @return value
   */
  public short getShortAligned(final long pOffset);

  /**
   * Reads a value at a given offset. The offset unit is 4 bytes (1 int).
   *
   * @param pOffset offset
   * @return value
   */
  public int getIntAligned(final long pOffset);

  /**
   * Reads a value at a given offset. The offset unit is 8 bytes (1 long).
   *
   * @param pOffset offset
   * @return value
   */
  public long getLongAligned(final long pOffset);

  /**
   * Reads a value at a given offset. The offset unit is 4 bytes (1 float).
   *
   * @param pOffset offset
   * @return value
   */
  public float getFloatAligned(final long pOffset);

  /**
   * Reads a value at a given offset. The offset unit is 8 bytes (1 double).
   *
   * @param pOffset offset
   * @return value
   */
  public double getDoubleAligned(final long pOffset);

}
