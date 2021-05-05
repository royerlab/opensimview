package clearcontrol.adaptive.modules.gui;

import clearcontrol.adaptive.modules.AdaptationP;
import clearcontrol.gui.jfx.var.customvarpanel.CustomVariablePane;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public class AdaptationPPanel extends CustomVariablePane
{

  /**
   * Instantiates an adaptation power panel
   *
   * @param pAdaptationP adaptation power module
   */
  public AdaptationPPanel(AdaptationP pAdaptationP)
  {
    super();
    addTab("");

    addNumberTextFieldForVariable(pAdaptationP.getTargetLaserPowerVariable().getName(), pAdaptationP.getTargetLaserPowerVariable(), pAdaptationP.getTargetLaserPowerVariable().getMin(), pAdaptationP.getTargetLaserPowerVariable().getMax(), pAdaptationP.getTargetLaserPowerVariable().getGranularity());

  }

}