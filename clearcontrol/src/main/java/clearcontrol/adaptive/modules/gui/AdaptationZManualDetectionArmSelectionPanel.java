package clearcontrol.adaptive.modules.gui;

import clearcontrol.adaptive.modules.AdaptationZManualDetectionArmSelection;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public class AdaptationZManualDetectionArmSelectionPanel extends AdaptationZPanel
{
  public AdaptationZManualDetectionArmSelectionPanel(AdaptationZManualDetectionArmSelection pAdaptationZManualDetectionArmSelection)
  {
    super(pAdaptationZManualDetectionArmSelection);

    addCheckBoxForVariable("First and last control plane zero", pAdaptationZManualDetectionArmSelection.getFirstAndLastControlPlaneZero());

    for (int i = 0; i < pAdaptationZManualDetectionArmSelection.getNumberOfControlPlanes(); i++)
    {
      addNumberTextFieldForVariable(pAdaptationZManualDetectionArmSelection.getDetectionArmChoiceVariable(i).getName(), pAdaptationZManualDetectionArmSelection.getDetectionArmChoiceVariable(i), pAdaptationZManualDetectionArmSelection.getDetectionArmChoiceVariable(i).getMin(), pAdaptationZManualDetectionArmSelection.getDetectionArmChoiceVariable(i).getMax(), pAdaptationZManualDetectionArmSelection.getDetectionArmChoiceVariable(i).getGranularity());
    }
  }

}
