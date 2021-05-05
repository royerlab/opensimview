package clearcontrol.component.lightsheet.instructions.gui;

import clearcontrol.component.lightsheet.instructions.ChangeLightSheetXInstruction;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;

public class ChangeLightSheetXInstructionPanel extends CustomGridPane
{
  public ChangeLightSheetXInstructionPanel(ChangeLightSheetXInstruction pInstruction)
  {
    addDoubleField(pInstruction.getLightSheetX(), 0);
    addIntegerField(pInstruction.getLightSheetIndex(), 1);
  }
}
