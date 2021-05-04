package clearcontrol.calibrator.modules.impl.gui;

import clearcontrol.calibrator.modules.impl.CalibrationW;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public class CalibrationWPanel extends StandardCalibrationModulePanel
{
  public CalibrationWPanel(CalibrationW pCalibrationW)
  {
    super(pCalibrationW);

    addNumberTextFieldForVariable(pCalibrationW.getNumberOfSamplesVariable().getName(), pCalibrationW.getNumberOfSamplesVariable());
    addNumberTextFieldForVariable(pCalibrationW.getDetectionArmVariable().getName(), pCalibrationW.getDetectionArmVariable());
  }
}
