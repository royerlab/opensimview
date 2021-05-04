package clearcontrol.microscope.lightsheet.timelapse.containers;

import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.postprocessing.containers.MeasurementContainer;

/**
 * The InstructionDurationContainer is used to store a time to the warehouse. The idea is
 * doing statistics: After some instructions have been executed and their duration was
 * saved, it might be possible to predict how long instructions might take in the future.
 * <p>
 * Author: @haesleinhuepf May 2018
 */
public class InstructionDurationContainer extends MeasurementContainer
{
  private final InstructionInterface mInstructionInterface;

  public InstructionDurationContainer(long pTimePoint, InstructionInterface pInstructionInterface, double pDurationInMilliseconds)
  {
    super(pTimePoint, pDurationInMilliseconds);
    mInstructionInterface = pInstructionInterface;
  }

  public InstructionInterface getSchedulerInterface()
  {
    return mInstructionInterface;
  }

  public double getDurationInMilliSeconds()
  {
    return getMeasurement();
  }

  public String toString()
  {
    return this.getClass().getSimpleName() + "[" + mInstructionInterface + "] " + getDurationInMilliSeconds() + " ms";
  }
}
