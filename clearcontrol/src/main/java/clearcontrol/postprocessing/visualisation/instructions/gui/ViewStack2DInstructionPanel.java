package clearcontrol.postprocessing.visualisation.instructions.gui;

import clearcontrol.postprocessing.visualisation.instructions.ViewStack2DInstruction;

/**
 * ViewStack2DInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public class ViewStack2DInstructionPanel extends ViewStackInstructionBasePanel
{
  public ViewStack2DInstructionPanel(ViewStack2DInstruction pInstruction)
  {
    super(pInstruction);
    addIntegerField(pInstruction.getViewerIndexVariable(), 1);
  }
}
