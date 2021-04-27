package clearcontrol.microscope.lightsheet.calibrator.modules.impl.gui;

import clearcontrol.microscope.lightsheet.calibrator.modules.impl.CalibrationP;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public class CalibrationPPanel extends StandardCalibrationModulePanel
{
  public CalibrationPPanel(CalibrationP pCalibrationP)
  {
    super(pCalibrationP);

    addNumberTextFieldForVariable(pCalibrationP.getNumberOfSamplesVariable().getName(),
                                  pCalibrationP.getNumberOfSamplesVariable());
    addNumberTextFieldForVariable(pCalibrationP.getDetectionArmVariable().getName(),
                                  pCalibrationP.getDetectionArmVariable());
    addNumberTextFieldForVariable(pCalibrationP.getExposureTimeInSecondsVariable()
                                               .getName(),
                                  pCalibrationP.getExposureTimeInSecondsVariable());
    addNumberTextFieldForVariable(pCalibrationP.getMaxIterationsVariable().getName(),
                                  pCalibrationP.getMaxIterationsVariable());
    addNumberTextFieldForVariable(pCalibrationP.getStoppingConditionErrorThreshold()
                                               .getName(),
                                  pCalibrationP.getStoppingConditionErrorThreshold());

  }
}
