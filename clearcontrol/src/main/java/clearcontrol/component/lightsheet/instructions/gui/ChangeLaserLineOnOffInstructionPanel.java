package clearcontrol.component.lightsheet.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.component.lightsheet.instructions.ChangeLaserLineOnOffInstruction;

public class ChangeLaserLineOnOffInstructionPanel extends CustomGridPane
{
  public ChangeLaserLineOnOffInstructionPanel(ChangeLaserLineOnOffInstruction pInstruction)
  {
    addIntegerField(pInstruction.getLaserLineIndexVariable(), 0);
  }
}
