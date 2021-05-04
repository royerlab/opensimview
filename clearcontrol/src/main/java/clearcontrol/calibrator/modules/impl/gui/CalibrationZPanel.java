package clearcontrol.calibrator.modules.impl.gui;

import clearcontrol.calibrator.modules.impl.CalibrationZ;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public class CalibrationZPanel extends StandardCalibrationModulePanel
{
  public CalibrationZPanel(CalibrationZ pCalibrationZ)
  {
    super(pCalibrationZ);

    addNumberTextFieldForVariable(pCalibrationZ.getNumberOfISamples().getName(), pCalibrationZ.getNumberOfISamples());
    addNumberTextFieldForVariable(pCalibrationZ.getNumberOfDSamples().getName(), pCalibrationZ.getNumberOfDSamples());
    addNumberTextFieldForVariable(pCalibrationZ.getExposureTimeInSecondsVariable().getName(), pCalibrationZ.getExposureTimeInSecondsVariable());
    addNumberTextFieldForVariable(pCalibrationZ.getLaserPowerVariable().getName(), pCalibrationZ.getLaserPowerVariable());
    addCheckBoxForVariable(pCalibrationZ.getFullRangeVariable().getName(), pCalibrationZ.getFullRangeVariable());
    addNumberTextFieldForVariable(pCalibrationZ.getMaxDeltaZ().getName(), pCalibrationZ.getMaxDeltaZ());
    addNumberTextFieldForVariable(pCalibrationZ.getMaxIterationsVariable().getName(), pCalibrationZ.getMaxIterationsVariable());
    addNumberTextFieldForVariable(pCalibrationZ.getStoppingConditionErrorThreshold().getName(), pCalibrationZ.getStoppingConditionErrorThreshold());
    addStringField(pCalibrationZ.getDebugPath().getName(), pCalibrationZ.getDebugPath());

  }
}
