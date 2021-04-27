package clearcontrol.microscope.lightsheet.warehouse.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.warehouse.instructions.FilterStacksInStackInterfaceContainerInstruction;

/**
 * FilterStacksInStackInterfaceContainerInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 09 2018
 */
public class FilterStacksInStackInterfaceContainerInstructionPanel extends CustomGridPane
{
  public FilterStacksInStackInterfaceContainerInstructionPanel(
      FilterStacksInStackInterfaceContainerInstruction instruction)
  {
    addStringField(instruction.getFilter(), 0);
    addCheckbox(instruction.getMatchExactly(), 1);
  }
}
