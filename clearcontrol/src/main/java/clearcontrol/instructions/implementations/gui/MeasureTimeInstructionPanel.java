package clearcontrol.instructions.implementations.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.textfield.StringVariableTextField;
import clearcontrol.instructions.implementations.MeasureTimeInstruction;

/**
 * MeasureTimeInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class MeasureTimeInstructionPanel extends CustomGridPane
{
  public MeasureTimeInstructionPanel(MeasureTimeInstruction pInstruction)
  {
    StringVariableTextField lTextField = new StringVariableTextField(pInstruction.getMeasuredTimeKeyVariable().getName(), pInstruction.getMeasuredTimeKeyVariable());
    add(lTextField.getLabel(), 0, 0);
    add(lTextField.getTextField(), 1, 0);
  }
}
