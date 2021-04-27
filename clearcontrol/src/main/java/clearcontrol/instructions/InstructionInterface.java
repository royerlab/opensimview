package clearcontrol.instructions;

import clearcontrol.core.device.name.NameableInterface;

/**
 * All instructions to run the microscope must implement this interface.
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) January
 * 2018
 */
public interface InstructionInterface extends NameableInterface
{
  /**
   * The initialize method is called within a timelapse once. Before this particular
   * instruction is called the first time.
   *
   * @return success
   */
  boolean initialize();

  /**
   * The enqueue method is called at the timepoint when the instruction is next item in
   * the list of the timelapse
   *
   * @param pTimePoint
   * @return success
   */
  boolean enqueue(long pTimePoint);

  /**
   * All instruction should be able to duplicate themselfes to allow building up a
   * schedule of independent instructions
   *
   * @return a copy of the current instruction
   */
  InstructionInterface copy();
}
