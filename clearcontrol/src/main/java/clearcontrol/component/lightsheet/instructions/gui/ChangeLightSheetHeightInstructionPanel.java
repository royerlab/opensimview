package clearcontrol.component.lightsheet.instructions.gui;

import clearcontrol.component.lightsheet.instructions.ChangeLightSheetHeightInstruction;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;

public class ChangeLightSheetHeightInstructionPanel extends CustomGridPane
{
  public ChangeLightSheetHeightInstructionPanel(ChangeLightSheetHeightInstruction pInstruction)
  {
    addDoubleField(pInstruction.getLightSheetHeight(), 0);
    addIntegerField(pInstruction.getLightSheetIndex(), 1);
  }
}
