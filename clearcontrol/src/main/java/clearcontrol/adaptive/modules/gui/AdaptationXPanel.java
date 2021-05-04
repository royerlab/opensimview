package clearcontrol.adaptive.modules.gui;

import clearcontrol.adaptive.modules.AdaptationX;

/**
 * @author royer
 */
public class AdaptationXPanel extends StandardAdaptationModulePanel
{

  /**
   * Instantiates an adaptation X panel
   *
   * @param pAdaptationX adaptation X module
   */
  public AdaptationXPanel(AdaptationX pAdaptationX)
  {
    super(pAdaptationX);

    addNumberTextFieldForVariable("Min X: ", pAdaptationX.getMinXVariable(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1d);

    addNumberTextFieldForVariable("Max X: ", pAdaptationX.getMaxXVariable(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1d);

  }

}
