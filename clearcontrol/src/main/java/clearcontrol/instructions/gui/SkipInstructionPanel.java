package clearcontrol.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.instructions.SkipInstruction;

public class SkipInstructionPanel extends CustomGridPane
{
  public SkipInstructionPanel(SkipInstruction pInstruction)
  {
    addIntegerField(pInstruction.getSkipPeriod(), 0);
    addIntegerField(pInstruction.getNumberOfInstructionsToSkip(), 1);
  }
}
