package clearcontrol.instructions;

import clearcontrol.core.device.VirtualDevice;

/**
 * The Instruction base is used to have a common class for all instructions.
 * <p>
 * Todo: Determine if deriving from VirtualDevice makes sense or not. Maybe, this link
 * csn/should be removed after Instructions are no longer devices of the microscope
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) January
 * 2018
 */
public abstract class InstructionBase extends VirtualDevice implements
                                                            InstructionInterface
{
  /**
   * @param pInstructionName instruction name
   */
  public InstructionBase(String pInstructionName)
  {
    super(pInstructionName);
  }
}
