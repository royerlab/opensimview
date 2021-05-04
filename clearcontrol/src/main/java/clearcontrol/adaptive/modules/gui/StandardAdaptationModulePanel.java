package clearcontrol.adaptive.modules.gui;

import clearcontrol.gui.jfx.var.customvarpanel.CustomVariablePane;
import clearcontrol.adaptive.modules.StandardAdaptationModule;

/**
 * Standard adaptation module panel
 *
 * @author royer
 */
public class StandardAdaptationModulePanel extends CustomVariablePane
{

  /**
   * Instantiates a standard adaptation module panel
   *
   * @param pAdaptationModule standard adaptation module
   */
  public StandardAdaptationModulePanel(StandardAdaptationModule pAdaptationModule)
  {
    super();

    addTab("");

    addNumberTextFieldForVariable("Laser line to use: ", pAdaptationModule.getLaserLineVariable(), 0, 8, 1);

    addNumberTextFieldForVariable("Number of samples: ", pAdaptationModule.getNumberOfSamplesVariable(), 0, 257, 1);

    addNumberTextFieldForVariable("image metric threshold: ", pAdaptationModule.getImageMetricThresholdVariable(), 0.0, 1.0, 0.00001);

    addNumberTextFieldForVariable("probability threshold [0,1]: ", pAdaptationModule.getProbabilityThresholdVariable(), 0.0, 1.0, 0.001);

    addNumberTextFieldForVariable("max (absolute) correction: ", pAdaptationModule.getMaxCorrectionVariable(), 0.0, 1000.0, 0.001);

    addNumberTextFieldForVariable("exposure (sec): ", pAdaptationModule.getExposureInSecondsVariable(), 0.0, 10.0, 0.001);

    addNumberTextFieldForVariable("laser power (%): ", pAdaptationModule.getLaserPowerVariable(), 0.0, 1.0, 0.001);
  }

}
