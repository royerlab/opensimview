package clearcontrol.component.lightsheet.instructions.gui;

import clearcontrol.component.lightsheet.instructions.ChangeLaserLineOnOffInstruction;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;

public class ChangeLaserLineOnOffInstructionPanel extends CustomGridPane
{
  public ChangeLaserLineOnOffInstructionPanel(ChangeLaserLineOnOffInstruction pInstruction)
  {
    addIntegerField(pInstruction.getLaserLineIndexVariable(), 0);
  }
}
