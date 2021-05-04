package clearcontrol.microscope.lightsheet.adaptive.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.adaptive.instructions.AutoFocusSinglePlaneInstruction;

/**
 * AutoFocusSinglePlaneInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public class AutoFocusSinglePlaneInstructionPanel extends CustomGridPane
{
  public AutoFocusSinglePlaneInstructionPanel(AutoFocusSinglePlaneInstruction pInstruction)
  {
    addIntegerField(pInstruction.getControlPlaneIndex(), 0);
    addIntegerField(pInstruction.getDetectionArmIndex(), 1);
  }
}
