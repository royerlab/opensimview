package clearcontrol.microscope.lightsheet.adaptive.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.adaptive.instructions.ChangeImageSizeInstruction;

/**
 * ChangeImageSizeInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 09 2018
 */
public class ChangeImageSizeInstructionPanel extends CustomGridPane
{
  public ChangeImageSizeInstructionPanel(ChangeImageSizeInstruction instruction)
  {
    addIntegerField(instruction.getImageWidth(), 0);
    addIntegerField(instruction.getImageHeight(), 1);
  }
}
