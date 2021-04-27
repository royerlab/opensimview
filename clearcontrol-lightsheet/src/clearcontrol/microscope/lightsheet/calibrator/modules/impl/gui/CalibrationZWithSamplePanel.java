package clearcontrol.microscope.lightsheet.calibrator.modules.impl.gui;

import clearcontrol.microscope.lightsheet.calibrator.modules.impl.CalibrationZWithSample;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) December
 * 2017
 */
public class CalibrationZWithSamplePanel extends StandardCalibrationModulePanel
{
  public CalibrationZWithSamplePanel(CalibrationZWithSample pCalibrationZWithSample)
  {
    super(pCalibrationZWithSample);

    addNumberTextFieldForVariable(pCalibrationZWithSample.getNumberOfISamples().getName(),
                                  pCalibrationZWithSample.getNumberOfISamples());
    addNumberTextFieldForVariable(pCalibrationZWithSample.getNumberOfDSamples().getName(),
                                  pCalibrationZWithSample.getNumberOfDSamples());
    addNumberTextFieldForVariable(pCalibrationZWithSample.getExposureTimeInSecondsVariable()
                                                         .getName(),
                                  pCalibrationZWithSample.getExposureTimeInSecondsVariable());
    addNumberTextFieldForVariable(pCalibrationZWithSample.getMaxDeltaZ().getName(),
                                  pCalibrationZWithSample.getMaxDeltaZ());
    addNumberTextFieldForVariable(pCalibrationZWithSample.getMaxIterationsVariable()
                                                         .getName(),
                                  pCalibrationZWithSample.getMaxIterationsVariable());
    addNumberTextFieldForVariable(pCalibrationZWithSample.getStoppingConditionErrorThreshold()
                                                         .getName(),
                                  pCalibrationZWithSample.getStoppingConditionErrorThreshold());
    addStringField(pCalibrationZWithSample.getDebugPath().getName(),
                   pCalibrationZWithSample.getDebugPath());

  }
}
