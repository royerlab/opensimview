package clearcontrol.adaptive.modules.gui;

import clearcontrol.adaptive.modules.AdaptationA;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public class AdaptationAPanel extends StandardAdaptationModulePanel
{

  /**
   * Instantiates an adaptation angle panel
   *
   * @param pAdaptationA adaptation angle module
   */
  public AdaptationAPanel(AdaptationA pAdaptationA)
  {
    super(pAdaptationA);

    addNumberTextFieldForVariable(pAdaptationA.getMaxDefocusVariable().getName(), pAdaptationA.getMaxDefocusVariable(), pAdaptationA.getMaxDefocusVariable().getMin(), pAdaptationA.getMaxDefocusVariable().getMax(), pAdaptationA.getMaxDefocusVariable().getGranularity());
  }

}