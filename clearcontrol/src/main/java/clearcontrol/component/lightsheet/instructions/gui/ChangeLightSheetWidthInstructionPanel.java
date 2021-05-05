package clearcontrol.component.lightsheet.instructions.gui;

import clearcontrol.component.lightsheet.instructions.ChangeLightSheetWidthInstruction;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;

public class ChangeLightSheetWidthInstructionPanel extends CustomGridPane
{
  public ChangeLightSheetWidthInstructionPanel(ChangeLightSheetWidthInstruction pInstruction)
  {
    addDoubleField(pInstruction.getLightSheetWidth(), 0);
  }
}
