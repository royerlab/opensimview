package clearcontrol.imaging;

import clearcontrol.LightSheetMicroscope;
import clearcontrol.stack.StackInterface;

/**
 * This is the interface for all Imagers. Imager is a convenience layer for sychronous
 * image acquisition, e.g. from within scripts.
 * <p>
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) March
 * 2018
 */
public interface ImagerInterface
{
  void setExposureTimeInSeconds(double pExposureTimeInSeconds);

  LightSheetMicroscope getLightSheetMicroscope();

  void setImageWidth(int pImageWidth);

  void setImageHeight(int pImageHeight);

  StackInterface acquire();
}
