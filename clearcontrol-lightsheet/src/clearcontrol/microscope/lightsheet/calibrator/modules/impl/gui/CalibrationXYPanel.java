package clearcontrol.microscope.lightsheet.calibrator.modules.impl.gui;

import clearcontrol.microscope.lightsheet.calibrator.modules.impl.CalibrationXY;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public class CalibrationXYPanel extends StandardCalibrationModulePanel
{
  public CalibrationXYPanel(CalibrationXY pCalibrationXY)
  {
    super(pCalibrationXY);
    addNumberTextFieldForVariable(pCalibrationXY.getLightSheetWidthWhileImaging()
                                                .getName(),
                                  pCalibrationXY.getLightSheetWidthWhileImaging());
    addNumberTextFieldForVariable(pCalibrationXY.getMaxIterationsVariable().getName(),
                                  pCalibrationXY.getMaxIterationsVariable());
    addNumberTextFieldForVariable(pCalibrationXY.getNumberOfPointsVariable().getName(),
                                  pCalibrationXY.getNumberOfPointsVariable());
    addNumberTextFieldForVariable(pCalibrationXY.getStoppingConditionErrorThreshold()
                                                .getName(),
                                  pCalibrationXY.getStoppingConditionErrorThreshold());
  }
}