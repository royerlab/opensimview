package clearcontrol.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.instructions.SkipInstruction;

public class SkipInstructionPanel extends CustomGridPane
{
  public SkipInstructionPanel(SkipInstruction pInstruction)
  {
    addCheckbox(pInstruction.getInvertedVariable(), 0);
    addIntegerField(pInstruction.getSkipPeriodVariable(), 1);
    addIntegerField(pInstruction.getNumberOfInstructionsToSkipVariable(), 2);
  }
}
