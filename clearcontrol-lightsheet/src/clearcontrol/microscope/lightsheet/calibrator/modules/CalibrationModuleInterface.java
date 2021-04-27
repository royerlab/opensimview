package clearcontrol.microscope.lightsheet.calibrator.modules;

import clearcontrol.core.device.name.ReadOnlyNameableInterface;
import clearcontrol.microscope.lightsheet.configurationstate.HasConfigurationState;

/**
 * Calibration module interface
 *
 * @author royer
 */
public interface CalibrationModuleInterface extends HasConfigurationState,
                                                    ReadOnlyNameableInterface
{

  /**
   * Resets this calbration module
   */
  void reset();

  String getName();

}
