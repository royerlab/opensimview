package clearcontrol.core.imagej;

import ij.ImageJ;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) February
 * 2018
 */
class ImageJSingleton
{
  private ImageJSingleton()
  {
  }

  private static ImageJSingleton mInstance = null;

  public static ImageJSingleton getInstance()
  {
    if (mInstance == null)
    {
      mInstance = new ImageJSingleton();
    }
    return mInstance;
  }

  private static ImageJ sImageJ;

  public void showImageJ()
  {
    if (sImageJ == null)
      sImageJ = new ImageJ();
    if (!sImageJ.isVisible())
      sImageJ.setVisible(true);
  }

}
