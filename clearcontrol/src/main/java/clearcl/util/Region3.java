package clearcl.util;

/**
 * Represents a 3D origin or 3D region.
 *
 * @author royer
 */
public class Region3
{

  /**
   * Returns a zero-origin: (0,0,0)
   *
   * @return zero origin
   */
  public static long[] originZero()
  {
    return new long[3];
  }

  /**
   * Given a vararg of longs it produces a correct 3D origin array for OpenCL -
   * non specified entries are filled with zeros.
   *
   * @param pArray varrag of longs
   * @return array
   */
  public static long[] origin(long... pArray)
  {
    if (pArray.length == 3) return pArray;
    long[] lArray = new long[3];
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
   * @param pArray varrag of longs
   * @return array
   */
  public static long[] region(long... pArray)
  {
    if (pArray.length == 3) return pArray;
    long[] lArray = new long[3];
    for (int i = 0; i < lArray.length; i++)
      lArray[i] = 1;
    for (int i = 0; i < Math.min(pArray.length, lArray.length); i++)
      lArray[i] = pArray[i];
    return lArray;
  }

  /**
   * Returns the volume of a given region (total number of voxels)
   *
   * @param pRegion 3D region
   * @return volume
   */
  public static long volume(long[] pRegion)
  {
    long lVolume = 1;
    for (int i = 0; i < pRegion.length; i++)
      lVolume *= pRegion[i] == 0 ? 1 : pRegion[i];
    return lVolume;
  }

}
