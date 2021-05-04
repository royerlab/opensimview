package clearcontrol.state.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.textfield.StringVariableTextField;
import clearcontrol.state.instructions.WriteAcquisitionStateToDiscInstruction;

/**
 * WriteAcquisitionStateToDiscInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public class WriteAcquisitionStateToDiscInstructionPanel extends CustomGridPane
{
  public WriteAcquisitionStateToDiscInstructionPanel(WriteAcquisitionStateToDiscInstruction pInstruction)
  {
    StringVariableTextField lTextField = new StringVariableTextField(pInstruction.getFilename().getName(), pInstruction.getFilename());
    add(lTextField.getLabel(), 0, 0);
    add(lTextField.getTextField(), 1, 0);

  }
}
