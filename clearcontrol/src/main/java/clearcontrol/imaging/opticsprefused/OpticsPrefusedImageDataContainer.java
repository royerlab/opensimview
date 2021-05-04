package clearcontrol.imaging.opticsprefused;

import clearcontrol.LightSheetMicroscope;
import clearcontrol.timelapse.LightSheetTimelapse;
import clearcontrol.warehouse.containers.StackInterfaceContainer;

/**
 * This container contains the raw images resulting from optics prefused acquisition. For
 * example for a microscope with two cameras, the stack have these keys:
 * <p>
 * C0opticsprefused C1opticsprefused
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class OpticsPrefusedImageDataContainer extends StackInterfaceContainer
{
  LightSheetMicroscope mLightSheetMicroscope;

  public OpticsPrefusedImageDataContainer(LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pLightSheetMicroscope.getDevice(LightSheetTimelapse.class, 0).getTimePointCounterVariable().get());
    mLightSheetMicroscope = pLightSheetMicroscope;
  }

  @Override
  public boolean isDataComplete()
  {
    for (int d = 0; d < mLightSheetMicroscope.getNumberOfDetectionArms(); d++)
    {
      if (!super.containsKey("C" + d + "opticsprefused"))
      {
        return false;
      }
    }
    return true;
  }
}
