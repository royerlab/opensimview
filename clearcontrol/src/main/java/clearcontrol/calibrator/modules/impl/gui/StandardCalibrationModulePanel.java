package clearcontrol.calibrator.modules.impl.gui;

import clearcontrol.gui.jfx.var.customvarpanel.CustomVariablePane;
import clearcontrol.calibrator.modules.CalibrationModuleInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public class StandardCalibrationModulePanel extends CustomVariablePane
{
  public StandardCalibrationModulePanel(CalibrationModuleInterface pCalibrationModuleInterface)
  {
    super();
    addTab(""); // "X: " + pCalibrationModuleInterface.getName());
  }
}
