package clearcontrol.microscope.lightsheet.adaptive.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.adaptive.instructions.FocusFinderZInstruction;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class FocusFinderZInstructionPanel extends CustomGridPane
{
  public FocusFinderZInstructionPanel(FocusFinderZInstruction pFocusFinderZScheduler)
  {
    addDoubleField(pFocusFinderZScheduler.getDeltaZVariable(), 0);
    addIntegerField(pFocusFinderZScheduler.getNumberOfImagesToTakeVariable(), 1);
    addDoubleField(pFocusFinderZScheduler.getExposureTimeInSecondsVariable(), 2);
    addIntegerField(pFocusFinderZScheduler.getImageWidthVariable(), 3);
    addIntegerField(pFocusFinderZScheduler.getImageHeightVariable(), 4);
    addCheckbox(pFocusFinderZScheduler.getResetAllTheTime(), 5);
  }

}
