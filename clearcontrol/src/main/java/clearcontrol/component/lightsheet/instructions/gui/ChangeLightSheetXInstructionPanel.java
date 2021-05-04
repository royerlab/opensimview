package clearcontrol.component.lightsheet.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.component.lightsheet.instructions.ChangeLightSheetXInstruction;

public class ChangeLightSheetXInstructionPanel extends CustomGridPane
{
  public ChangeLightSheetXInstructionPanel(ChangeLightSheetXInstruction pInstruction)
  {
    addDoubleField(pInstruction.getLightSheetX(), 0);
    addIntegerField(pInstruction.getLightSheetIndex(), 1);
  }
}
