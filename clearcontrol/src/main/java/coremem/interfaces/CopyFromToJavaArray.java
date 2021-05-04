package coremem.interfaces;

/**
 * Memory objects implementing this interface can copy their contents to and
 * from Java primitive arrays.
 *
 * @author royer
 */
public interface CopyFromToJavaArray
{
  /**
   * Copies the content of that memory object to a byte array.
   *
   * @param pTo preallocated byte array.
   */
  public void copyTo(byte[] pTo);

  /**
   * Copies the content of that memory object to a short array.
   *
   * @param pTo preallocated short array.
   */
  public void copyTo(short[] pTo);

  /**
   * Copies the content of that memory object to a char array.
   *
   * @param pTo preallocated char array.
   */
  public void copyTo(char[] pTo);

  /**
   * Copies the content of that memory object to a int array.
   *
   * @param pTo preallocated int array.
   */
  public void copyTo(int[] pTo);

  /**
   * Copies the content of that memory object to a long array.
   *
   * @param pTo preallocated long array.
   */
  public void copyTo(long[] pTo);

  /**
   * Copies the content of that memory object to a float array.
   *
   * @param pTo preallocated float array.
   */
  public void copyTo(float[] pTo);

  /**
   * Copies the content of that memory object to a double array.
   *
   * @param pTo preallocated double array.
   */
  public void copyTo(double[] pTo);

  /**
   * Copies the content of a byte array to this memory object.
   *
   * @param pFrom preallocated byte array.
   */
  public void copyFrom(byte[] pFrom);

  /**
   * Copies the content of a short array to this memory object.
   *
   * @param pFrom preallocated short array.
   */
  public void copyFrom(short[] pFrom);

  /**
   * Copies the content of a char array to this memory object.
   *
   * @param pFrom preallocated char array.
   */
  public void copyFrom(char[] pFrom);

  /**
   * Copies the content of a int array to this memory object.
   *
   * @param pFrom preallocated int array.
   */
  public void copyFrom(int[] pFrom);

  /**
   * Copies the content of a long array to this memory object.
   *
   * @param pFrom preallocated long array.
   */
  public void copyFrom(long[] pFrom);

  /**
   * Copies the content of a float array to this memory object.
   *
   * @param pFrom preallocated float array.
   */
  public void copyFrom(float[] pFrom);

  /**
   * Copies the content of a double array to this memory object.
   *
   * @param pFrom preallocated double array.
   */
  public void copyFrom(double[] pFrom);

}
