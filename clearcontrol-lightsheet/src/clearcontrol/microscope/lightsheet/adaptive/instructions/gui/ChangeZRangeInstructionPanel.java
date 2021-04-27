package clearcontrol.microscope.lightsheet.adaptive.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.adaptive.instructions.ChangeZRangeInstruction;

/**
 * ChangeZRangeInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 09 2018
 */
public class ChangeZRangeInstructionPanel extends CustomGridPane
{
  public ChangeZRangeInstructionPanel(ChangeZRangeInstruction instruction)
  {
    addDoubleField(instruction.getMinZ(), 0);
    addDoubleField(instruction.getMaxZ(), 1);
    addDoubleField(instruction.getStepZ(), 2);
  }
}
