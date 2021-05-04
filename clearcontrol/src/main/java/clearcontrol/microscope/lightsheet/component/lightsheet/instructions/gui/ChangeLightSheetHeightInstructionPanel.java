package clearcontrol.microscope.lightsheet.component.lightsheet.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.component.lightsheet.instructions.ChangeLightSheetHeightInstruction;

public class ChangeLightSheetHeightInstructionPanel extends CustomGridPane
{
  public ChangeLightSheetHeightInstructionPanel(ChangeLightSheetHeightInstruction pInstruction)
  {
    addDoubleField(pInstruction.getLightSheetHeight(), 0);
    addIntegerField(pInstruction.getLightSheetIndex(), 1);
  }
}
