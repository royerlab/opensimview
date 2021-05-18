package clearcontrol.timelapse.instructions.gui;

import clearcontrol.component.lightsheet.instructions.ChangeLightSheetBrightnessInstruction;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;

public class SequentialAcquistionInstructionPanel extends CustomGridPane
{
  public SequentialAcquistionInstructionPanel(ChangeLightSheetBrightnessInstruction pInstruction)
  {
    addIntegerField(pInstruction.getLightSheetIndex(), 0);
    addDoubleField(pInstruction.getLightSheetBrightness(), 1);
  }
}
