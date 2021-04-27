package clearcontrol.microscope.lightsheet.imaging.interleaved;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * This container contains the raw images resulting from interleaved acquisition. For
 * example for a microscope with two cameras, the stack have these keys:
 * <p>
 * C0interleaved C1interleaved
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class InterleavedImageDataContainer extends StackInterfaceContainer
{
  private final LightSheetMicroscope mLightSheetMicroscope;

  public InterleavedImageDataContainer(LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pLightSheetMicroscope.getDevice(LightSheetTimelapse.class, 0)
                               .getTimePointCounterVariable()
                               .get());
    mLightSheetMicroscope = pLightSheetMicroscope;
  }

  @Override public boolean isDataComplete()
  {
    for (int d = 0; d < mLightSheetMicroscope.getNumberOfDetectionArms(); d++)
    {
      if (!super.containsKey("C" + d + "interleaved"))
      {
        return false;
      }
    }
    return true;
  }
}
