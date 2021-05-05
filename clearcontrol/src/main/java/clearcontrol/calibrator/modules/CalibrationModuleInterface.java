package clearcontrol.calibrator.modules;

import clearcontrol.configurationstate.HasConfigurationState;
import clearcontrol.core.device.name.ReadOnlyNameableInterface;

/**
 * Calibration module interface
 *
 * @author royer
 */
public interface CalibrationModuleInterface extends HasConfigurationState, ReadOnlyNameableInterface
{

  /**
   * Resets this calbration module
   */
  void reset();

  String getName();

}
