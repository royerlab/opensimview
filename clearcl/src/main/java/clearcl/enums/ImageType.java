package clearcl.enums;

/**
 * OpenCL image type
 *
 * @author royer
 */
public enum ImageType
{
 /**
  * 1D image type
  */
 IMAGE1D,

 /**
  * 2D image type
  */
 IMAGE2D,

 /**
  * 3D image type
  */
 IMAGE3D;

  /**
   * Returns the image type from dimensions
   * 
   * @param pDimensions
   *          vararg dimensions
   * @return image type
   */
  public static ImageType fromDimensions(long... pDimensions)
  {
    int lDimension = pDimensions.length;
    if (lDimension == 1)
      return IMAGE1D;
    else if (lDimension == 2)
      return IMAGE2D;
    else if (lDimension == 3)
      return IMAGE3D;
    return null;
  }
}
