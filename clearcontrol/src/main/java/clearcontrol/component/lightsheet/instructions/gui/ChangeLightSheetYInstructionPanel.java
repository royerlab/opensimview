package clearcontrol.component.lightsheet.instructions.gui;

import clearcontrol.component.lightsheet.instructions.ChangeLightSheetYInstruction;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;

public class ChangeLightSheetYInstructionPanel extends CustomGridPane
{
  public ChangeLightSheetYInstructionPanel(ChangeLightSheetYInstruction pInstruction)
  {
    addDoubleField(pInstruction.getLightSheetY(), 0);
    addIntegerField(pInstruction.getLightSheetIndex(), 1);
  }
}
