package clearcontrol.calibrator.gui;

import clearcontrol.calibrator.CalibrationEngine;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsolePanel;

/**
 * Calibration Engine Panel
 *
 * @author royer
 */
public class CalibrationEnginePanel extends VisualConsolePanel
{

  /**
   * Instantiates a panel for displaying information about the calibration engine.
   *
   * @param pCalibrationEngine calibration engine
   */
  public CalibrationEnginePanel(CalibrationEngine pCalibrationEngine)
  {
    super(pCalibrationEngine);
  }

}
