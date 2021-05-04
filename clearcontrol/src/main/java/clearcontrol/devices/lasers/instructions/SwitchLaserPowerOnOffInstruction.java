package clearcontrol.devices.lasers.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;

/**
 * The SwitchLaserOnOffInstruction can switch on of the power of a given laser device
 * <p>
 * Author: @haesleinhuepf September 2018
 */
public class SwitchLaserPowerOnOffInstruction extends InstructionBase implements InstructionInterface, LoggingFeature
{
  LaserDeviceInterface mLaserDevice;
  boolean mTurnOn;

  public SwitchLaserPowerOnOffInstruction(LaserDeviceInterface pLaserDevice, boolean pTurnOn)
  {
    super("Laser: Switch " + pLaserDevice.getName() + " power " + (pTurnOn ? "ON" : "OFF"));
    mLaserDevice = pLaserDevice;
    mTurnOn = pTurnOn;
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    mLaserDevice.setLaserPowerOn(mTurnOn);
    mLaserDevice.getPowerOnVariable().set(mTurnOn);
    return true;
  }

  @Override
  public SwitchLaserPowerOnOffInstruction copy()
  {
    return new SwitchLaserPowerOnOffInstruction(mLaserDevice, mTurnOn);
  }
}
