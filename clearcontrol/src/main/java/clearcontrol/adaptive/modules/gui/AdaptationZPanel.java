package clearcontrol.adaptive.modules.gui;

import clearcontrol.adaptive.modules.AdaptationZ;

/**
 * @author royer
 */
public class AdaptationZPanel extends StandardAdaptationModulePanel
{

  /**
   * Instantiates an adaptation Z panel
   *
   * @param pAdaptationZ adaptation Z module
   */
  public AdaptationZPanel(AdaptationZ pAdaptationZ)
  {
    super(pAdaptationZ);

    addNumberTextFieldForVariable(pAdaptationZ.getDeltaZVariable().getName(), pAdaptationZ.getDeltaZVariable(), pAdaptationZ.getDeltaZVariable().getMin(), pAdaptationZ.getDeltaZVariable().getMax(), pAdaptationZ.getDeltaZVariable().getGranularity());
  }

}
