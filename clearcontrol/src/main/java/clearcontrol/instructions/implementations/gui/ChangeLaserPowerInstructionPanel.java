package clearcontrol.instructions.implementations.gui;

import clearcontrol.devices.lasers.instructions.ChangeLaserPowerInstruction;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;

/**
 * ChangeLaserPowerInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 09 2018
 */
public class ChangeLaserPowerInstructionPanel extends CustomGridPane
{
  public ChangeLaserPowerInstructionPanel(ChangeLaserPowerInstruction instruction)
  {
    addDoubleField(instruction.getLaserPowerInMilliwatt(), 0);
  }
}
