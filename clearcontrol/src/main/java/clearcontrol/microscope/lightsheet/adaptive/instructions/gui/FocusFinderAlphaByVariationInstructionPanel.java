package clearcontrol.microscope.lightsheet.adaptive.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.adaptive.instructions.FocusFinderAlphaByVariationInstruction;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class FocusFinderAlphaByVariationInstructionPanel extends CustomGridPane
{
  public FocusFinderAlphaByVariationInstructionPanel(FocusFinderAlphaByVariationInstruction pFocusFinderAlphaByVariationScheduler)
  {
    addDoubleField(pFocusFinderAlphaByVariationScheduler.getAlphaStepVariable(), 0);
    addIntegerField(pFocusFinderAlphaByVariationScheduler.getNumberOfImagesToTakeVariable(), 1);
    addDoubleField(pFocusFinderAlphaByVariationScheduler.getExposureTimeInSecondsVariable(), 2);
    addIntegerField(pFocusFinderAlphaByVariationScheduler.getImageWidthVariable(), 3);
    addIntegerField(pFocusFinderAlphaByVariationScheduler.getImageHeightVariable(), 4);
  }

}