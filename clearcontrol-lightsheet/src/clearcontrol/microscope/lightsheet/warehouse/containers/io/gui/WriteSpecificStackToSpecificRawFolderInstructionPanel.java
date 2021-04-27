package clearcontrol.microscope.lightsheet.warehouse.containers.io.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteSpecificStackToSpecificRawFolderInstruction;

/**
 * WriteSpecificStackToSpecificRawFolderInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 06 2018
 */
public class WriteSpecificStackToSpecificRawFolderInstructionPanel extends CustomGridPane
{
  public WriteSpecificStackToSpecificRawFolderInstructionPanel(
      WriteSpecificStackToSpecificRawFolderInstruction pInstruction)
  {
    addStringField(pInstruction.getSourceStackKeyVariable(), 0);
    addStringField(pInstruction.getTargetRawFolderNameVariable(), 1);
  }
}
