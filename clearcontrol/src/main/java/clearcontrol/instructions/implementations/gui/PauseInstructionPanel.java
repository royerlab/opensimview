package clearcontrol.instructions.implementations.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.instructions.implementations.PauseInstruction;

/**
 * PauseInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class PauseInstructionPanel extends CustomGridPane
{
  public PauseInstructionPanel(PauseInstruction pPauseInstruction)
  {
    addIntegerField(pPauseInstruction.getPauseTimeInMilliseconds(), 0);
  }
}
