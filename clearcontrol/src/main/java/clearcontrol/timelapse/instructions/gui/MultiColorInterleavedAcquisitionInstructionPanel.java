package clearcontrol.timelapse.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.timelapse.instructions.MultiColorInterleavedAcquisitionInstruction;
import clearcontrol.timelapse.instructions.SequentialAcquisitionInstruction;

public class MultiColorInterleavedAcquisitionInstructionPanel extends CustomGridPane
{
  public MultiColorInterleavedAcquisitionInstructionPanel(MultiColorInterleavedAcquisitionInstruction pInstruction)
  {
    int row = 0;

    addStringField(pInstruction.getChannelNameVariable(), row++);

    int lNumberOfLaserLines = pInstruction.getLightSheetMicroscope().getNumberOfLaserLines();

    for(int l=0; l<lNumberOfLaserLines; l++)
      addIntegerField(pInstruction.getPeriodVariable(l), row++);

    for(int l=0; l<lNumberOfLaserLines; l++)
      addIntegerField(pInstruction.getOffsetVariable(l), row++);

    for(int l=0; l<lNumberOfLaserLines; l++)
      addDoubleField(pInstruction.getLaserPowerAdjustmentVariable(l), row++);
  }
}
