package clearcontrol.timelapse.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.timelapse.instructions.SequentialAcquisitionInstruction;

public class SequentialAcquisitionInstructionPanel extends CustomGridPane
{
  public SequentialAcquisitionInstructionPanel(SequentialAcquisitionInstruction pInstruction)
  {
    addStringField(pInstruction.getChannelNameVariable(), 0);
  }
}
