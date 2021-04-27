package clearcontrol.microscope.lightsheet.component.lightsheet.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.component.lightsheet.instructions.ChangeLightSheetBrightnessInstruction;

public class ChangeLightSheetBrightnessInstructionPanel extends CustomGridPane
{
  public ChangeLightSheetBrightnessInstructionPanel(ChangeLightSheetBrightnessInstruction pInstruction)
  {
    addIntegerField(pInstruction.getLightSheetIndex(), 0);
    addDoubleField(pInstruction.getLightSheetBrightness(), 1);
  }
}
