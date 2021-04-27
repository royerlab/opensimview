package clearcl.util;

/**
 * Represents a 2D origin or 2D region.
 *
 * @author royer
 */
public class Region2
{

  /**
   * Returns a zero-origin: (0,0)
   * 
   * @return zero origin
   */
  public static long[] originZero()
  {
    return new long[2];
  }

  /**
   * Given a vararg of longs it produces a correct 2D origin array for OpenCL -
   * non specified entries are filled with zeros.
   * 
   * @param pArray
   *          varrag of longs
   * @return array
   */
  public static long[] origin(long... pArray)
  {
    if (pArray.length == 2)
      return pArray;
    long[] lArray = new long[2];
    for (int i = 0; i < lArray.length; i++)
      lArray[i] = 0;
    for (int i = 0; i < Math.min(pArray.length, lArray.length); i++)
      lArray[i] = pArray[i];
    return lArray;
  }

  /**
   * Given a vararg of longs it produces a correct 3D region array for OpenCL -
   * non specified entries are filled with ones.
   * 
   * @param pArray
   *          varrag of longs
   * @return array
   */
  public static long[] region(long... pArray)
  {
    if (pArray.length == 2)
      return pArray;
    long[] lArray = new long[2];
    for (int i = 0; i < lArray.length; i++)
      lArray[i] = 1;
    for (int i = 0; i < Math.min(pArray.length, lArray.length); i++)
      lArray[i] = pArray[i];
    return lArray;
  }

}
