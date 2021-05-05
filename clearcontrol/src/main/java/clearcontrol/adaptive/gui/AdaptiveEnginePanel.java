package clearcontrol.adaptive.gui;

import clearcontrol.adaptive.AdaptiveEngine;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsolePanel;

/**
 * Adaptor Panel
 *
 * @author royer
 */
public class AdaptiveEnginePanel extends VisualConsolePanel
{

  /**
   * Instantiates a panel for displaying information about the adaptive engine.
   *
   * @param pAdaptiveEngine adaptive engine
   */
  public AdaptiveEnginePanel(AdaptiveEngine<?> pAdaptiveEngine)
  {
    super(pAdaptiveEngine);
  }

}
