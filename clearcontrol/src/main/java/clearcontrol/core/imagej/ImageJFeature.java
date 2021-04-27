package clearcontrol.core.imagej;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) February
 * 2018
 */
public interface ImageJFeature
{
  default void showImageJ()
  {
    ImageJSingleton.getInstance().showImageJ();
  }
}
