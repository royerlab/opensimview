package clearcontrol.microscope.lightsheet.state.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.textfield.StringVariableTextField;
import clearcontrol.microscope.lightsheet.state.instructions.ReadAcquisitionStateFromDiscInstruction;

/**
 * ReadAcquisitionStateFromDiscInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public class ReadAcquisitionStateFromDiscInstructionPanel extends CustomGridPane
{
  public ReadAcquisitionStateFromDiscInstructionPanel(
      ReadAcquisitionStateFromDiscInstruction pInstruction)
  {
    StringVariableTextField
        lTextField =
        new StringVariableTextField(pInstruction.getFilename().getName(),
                                    pInstruction.getFilename());
    add(lTextField.getLabel(), 0, 0);
    add(lTextField.getTextField(), 1, 0);

  }
}
