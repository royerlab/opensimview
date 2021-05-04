package clearcontrol.devices.lasers.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;

/**
 * The SwitchLaserOnOffInstruction can switch a laser on/off during timelapse
 * <p>
 * Author: @haesleinhuepf September 2018
 */
public class SwitchLaserOnOffInstruction extends InstructionBase implements InstructionInterface, LoggingFeature
{
  LaserDeviceInterface mLaserDevice;
  boolean mTurnOn;

  public SwitchLaserOnOffInstruction(LaserDeviceInterface pLaserDevice, boolean pTurnOn)
  {
    super("Laser: Switch " + pLaserDevice.getName() + " " + (pTurnOn ? "ON" : "OFF"));
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
    mLaserDevice.setLaserOn(mTurnOn);
    mLaserDevice.getLaserOnVariable().set(mTurnOn);

    return true;
  }

  @Override
  public SwitchLaserOnOffInstruction copy()
  {
    return new SwitchLaserOnOffInstruction(mLaserDevice, mTurnOn);
  }
}
