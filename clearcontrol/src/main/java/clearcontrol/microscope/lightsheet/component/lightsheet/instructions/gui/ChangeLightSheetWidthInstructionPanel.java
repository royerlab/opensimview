package clearcontrol.microscope.lightsheet.component.lightsheet.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.component.lightsheet.instructions.ChangeLightSheetWidthInstruction;

public class ChangeLightSheetWidthInstructionPanel extends CustomGridPane
{
  public ChangeLightSheetWidthInstructionPanel(ChangeLightSheetWidthInstruction pInstruction)
  {
    addDoubleField(pInstruction.getLightSheetWidth(), 0);
  }
}
