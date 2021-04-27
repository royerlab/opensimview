package clearcontrol.devices.lasers.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;

/**
 * The ChangeLaserPowerInstruction allows changing laser power of a given device during a
 * time lapse
 * <p>
 * Author: @haesleinhuepf September 2018
 */
public class ChangeLaserPowerInstruction extends InstructionBase implements
                                                                 InstructionInterface,
                                                                 LoggingFeature,
                                                                 PropertyIOableInstructionInterface
{
  private final LaserDeviceInterface laserDevice;
  private final BoundedVariable<Double>
      laserPowerInMilliwatt =
      new BoundedVariable<Double>("Laser power in milliwatt",
                                  0.0,
                                  0.0,
                                  Double.MAX_VALUE,
                                  0.1);

  /**
   * @param pLaser laser to control
   */
  public ChangeLaserPowerInstruction(LaserDeviceInterface pLaser)
  {
    super("Laser: Change laser power of " + pLaser.getName());
    laserDevice = pLaser;
    // laserPowerInMilliwatt.setMinMax(0, pLaser.getMaxPowerInMilliWatt());
  }

  @Override public boolean initialize()
  {
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    laserDevice.getTargetPowerInMilliWattVariable().set(laserPowerInMilliwatt.get());
    return true;
  }

  @Override public ChangeLaserPowerInstruction copy()
  {
    ChangeLaserPowerInstruction copied = new ChangeLaserPowerInstruction(laserDevice);
    copied.laserPowerInMilliwatt.set(laserPowerInMilliwatt.get());
    return copied;
  }

  public BoundedVariable<Double> getLaserPowerInMilliwatt()
  {
    return laserPowerInMilliwatt;
  }

  @Override public Variable[] getProperties()
  {
    return new Variable[] { getLaserPowerInMilliwatt() };
  }
}
