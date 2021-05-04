package clearcontrol.timelapse.instructions;

import clearcontrol.instructions.InstructionBase;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.timelapse.LightSheetTimelapse;
import clearcontrol.timelapse.io.ProgramWriter;

import java.io.File;

/**
 * The TimelapseLogInstruction writes the current time lapse instruction list to disc.
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class TimelapseLogInstruction extends InstructionBase
{
  private final LightSheetTimelapse mTimelapse;

  public TimelapseLogInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("IO: Log content of the timelapse schedule to disc");

    mTimelapse = pLightSheetMicroscope.getTimelapse();
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    new ProgramWriter(mTimelapse.getCurrentProgram(), new File(mTimelapse.getWorkingDirectory(), "program" + mTimelapse.getTimePointCounterVariable().get() + ".txt")).write();
    return true;
  }

  @Override
  public TimelapseLogInstruction copy()
  {
    return new TimelapseLogInstruction((LightSheetMicroscope) mTimelapse.getMicroscope());
  }
}
