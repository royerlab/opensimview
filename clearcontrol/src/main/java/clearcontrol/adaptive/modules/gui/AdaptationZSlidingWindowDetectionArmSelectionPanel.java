package clearcontrol.adaptive.modules.gui;

import clearcontrol.adaptive.modules.AdaptationZSlidingWindowDetectionArmSelection;

/**
 * @author royer
 * @author Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public class AdaptationZSlidingWindowDetectionArmSelectionPanel extends StandardAdaptationModulePanel
{

  /**
   * Instantiates an adaptation Z panel
   *
   * @param pAdaptationZSlidingWindowDetectionArmSelection adaptation module for Z
   */
  public AdaptationZSlidingWindowDetectionArmSelectionPanel(AdaptationZSlidingWindowDetectionArmSelection pAdaptationZSlidingWindowDetectionArmSelection)
  {
    super(pAdaptationZSlidingWindowDetectionArmSelection);

    addNumberTextFieldForVariable("Delta Z: ", pAdaptationZSlidingWindowDetectionArmSelection.getDeltaZVariable(), 0.0, Double.POSITIVE_INFINITY, 0.001);

    addNumberTextFieldForVariable("Sliding window width for detection arm runEpoch (no of control planes)): ", pAdaptationZSlidingWindowDetectionArmSelection.getSlidingWindowWidthVariable(), 0, Integer.MAX_VALUE, 1);

    addCheckBoxForVariable("First and last control plane zero", pAdaptationZSlidingWindowDetectionArmSelection.getFirstAndLastControlPlaneZero());
  }

}
