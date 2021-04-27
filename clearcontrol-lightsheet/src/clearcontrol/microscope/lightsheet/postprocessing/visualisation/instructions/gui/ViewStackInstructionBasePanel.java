package clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions.ViewStackInstructionBase;

/**
 * ViewStackInstructionBasePanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 06 2018
 */
public class ViewStackInstructionBasePanel extends CustomGridPane
{
  public ViewStackInstructionBasePanel(ViewStackInstructionBase pInstruction)
  {
    addStringField(pInstruction.getImageKeyToShowVariable(), 0);
  }
}
