package coremem.interfaces;

/**
 * Memory objects implementing this interface can copy their contents to and
 * from Java primitive arrays.
 *
 * @author royer
 */
public interface CopyRangeFromToJavaArray
{
  /**
   * Copies the content of that memory object to a byte array.
   * 
   * @param pTo
   *          preallocated byte array.
   * @param pSrcOffset
   *          source offset (array elements)
   * @param pDstOffset
   *          destination offset (array elements)
   * @param pLength
   *          length in array elements
   */
  public void copyTo(byte[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength);

  /**
   * Copies the content of that memory object to a short array.
   * 
   * @param pTo
   *          preallocated short array.
   * @param pSrcOffset
   *          source offset (array elements)
   * @param pDstOffset
   *          destination offset (array elements)
   * @param pLength
   *          length in array elements
   */
  public void copyTo(short[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength);

  /**
   * Copies the content of that memory object to a char array.
   * 
   * @param pTo
   *          preallocated char array.
   * @param pSrcOffset
   *          source offset (array elements)
   * @param pDstOffset
   *          destination offset (array elements)
   * @param pLength
   *          length in array elements
   */
  public void copyTo(char[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength);

  /**
   * Copies the content of that memory object to a int array.
   * 
   * @param pTo
   *          preallocated int array.
   * @param pSrcOffset
   *          source offset (array elements)
   * @param pDstOffset
   *          destination offset (array elements)
   * @param pLength
   *          length in array elements
   */
  public void copyTo(int[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength);

  /**
   * Copies the content of that memory object to a long array.
   * 
   * @param pTo
   *          preallocated long array.
   * @param pSrcOffset
   *          source offset (array elements)
   * @param pDstOffset
   *          destination offset (array elements)
   * @param pLength
   *          length in array elements
   */
  public void copyTo(long[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength);

  /**
   * Copies the content of that memory object to a float array.
   * 
   * @param pTo
   *          preallocated float array.
   * @param pSrcOffset
   *          source offset (array elements)
   * @param pDstOffset
   *          destination offset (array elements)
   * @param pLength
   *          length in array elements
   */
  public void copyTo(float[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength);

  /**
   * Copies the content of that memory object to a double array.
   * 
   * @param pTo
   *          preallocated double array.
   * @param pSrcOffset
   *          source offset (array elements)
   * @param pDstOffset
   *          destination offset (array elements)
   * @param pLength
   *          length in array elements
   */
  public void copyTo(double[] pTo,
                     long pSrcOffset,
                     int pDstOffset,
                     int pLength);

  /**
   * Copies the content of a byte array to this memory object.
   * 
   * @param pFrom
   *          preallocated byte array.
   * @param pSrcOffset
   *          source offset (array elements)
   * @param pDstOffset
   *          destination offset (array elements)
   * @param pLength
   *          length in array elements
   */
  public void copyFrom(byte[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength);

  /**
   * Copies the content of a short array to this memory object.
   * 
   * @param pFrom
   *          preallocated short array.
   * @param pSrcOffset
   *          source offset (array elements)
   * @param pDstOffset
   *          destination offset (array elements)
   * @param pLength
   *          length in array elements
   */
  public void copyFrom(short[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength);

  /**
   * Copies the content of a char array to this memory object.
   * 
   * @param pFrom
   *          preallocated char array.
   * @param pSrcOffset
   *          source offset (array elements)
   * @param pDstOffset
   *          destination offset (array elements)
   * @param pLength
   *          length in array elements
   */
  public void copyFrom(char[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength);

  /**
   * Copies the content of a int array to this memory object.
   * 
   * @param pFrom
   *          preallocated int array.
   * @param pSrcOffset
   *          source offset (array elements)
   * @param pDstOffset
   *          destination offset (array elements)
   * @param pLength
   *          length in array elements
   */
  public void copyFrom(int[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength);

  /**
   * Copies the content of a long array to this memory object.
   * 
   * @param pFrom
   *          preallocated long array.
   * @param pSrcOffset
   *          source offset (array elements)
   * @param pDstOffset
   *          destination offset (array elements)
   * @param pLength
   *          length in array elements
   */
  public void copyFrom(long[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength);

  /**
   * Copies the content of a float array to this memory object.
   * 
   * @param pFrom
   *          preallocated float array.
   * @param pSrcOffset
   *          source offset (array elements)
   * @param pDstOffset
   *          destination offset (array elements)
   * @param pLength
   *          length in array elements
   */
  public void copyFrom(float[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength);

  /**
   * Copies the content of a double array to this memory object.
   * 
   * @param pFrom
   *          preallocated double array.
   * @param pSrcOffset
   *          source offset (array elements)
   * @param pDstOffset
   *          destination offset (array elements)
   * @param pLength
   *          length in array elements
   */
  public void copyFrom(double[] pFrom,
                       int pSrcOffset,
                       long pDstOffset,
                       int pLength);

}
