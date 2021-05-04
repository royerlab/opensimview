package clearcontrol.microscope.lightsheet.timelapse.instructions;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.timelapse.TimelapseInterface;

/**
 * The TimelapseStopInstruction stops the running time lapse
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class TimelapseStopInstruction extends LightSheetMicroscopeInstructionBase
{
  public TimelapseStopInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Smart: Stop timelapse", pLightSheetMicroscope);
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    TimelapseInterface lTimelapse = (TimelapseInterface) getLightSheetMicroscope().getDevice(TimelapseInterface.class, 0);
    if (lTimelapse != null)
    {
      lTimelapse.stopTimelapse();
    }
    return true;
  }

  @Override
  public TimelapseStopInstruction copy()
  {
    return new TimelapseStopInstruction(getLightSheetMicroscope());
  }
}
