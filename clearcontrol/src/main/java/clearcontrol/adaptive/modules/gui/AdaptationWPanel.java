package clearcontrol.adaptive.modules.gui;

import clearcontrol.adaptive.modules.AdaptationW;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public class AdaptationWPanel extends StandardAdaptationModulePanel
{

  /**
   * Instantiates an adaptation light sheet width panel
   *
   * @param pAdaptationW adaptation light sheet width module
   */
  public AdaptationWPanel(AdaptationW pAdaptationW)
  {
    super(pAdaptationW);

    addNumberTextFieldForVariable(pAdaptationW.getNumberOfRepeatsVariable().getName(), pAdaptationW.getNumberOfRepeatsVariable(), pAdaptationW.getNumberOfRepeatsVariable().getMin(), pAdaptationW.getNumberOfRepeatsVariable().getMax(), pAdaptationW.getNumberOfRepeatsVariable().getGranularity());

  }

}