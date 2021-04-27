package clearcontrol.microscope.lightsheet.component.lightsheet.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.component.lightsheet.instructions.ChangeLightSheetYInstruction;

public class ChangeLightSheetYInstructionPanel extends CustomGridPane
{
  public ChangeLightSheetYInstructionPanel(ChangeLightSheetYInstruction pInstruction)
  {
    addDoubleField(pInstruction.getLightSheetY(), 0);
    addIntegerField(pInstruction.getLightSheetIndex(), 1);
  }
}
