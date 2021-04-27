package clearcontrol.microscope.lightsheet.component.lightsheet.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;

import java.util.ArrayList;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class MultiChannelInstruction extends LightSheetMicroscopeInstructionBase implements
                                                         LoggingFeature
{

  boolean mInitialized = false;
  ArrayList<Double> mLaserPower = new ArrayList<Double>();

  /**
   * INstanciates a virtual device with a given name
   *
   */
  public MultiChannelInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Multichannel scheduler", pLightSheetMicroscope);
  }


  @Override public boolean initialize()
  {
    mTimePointCount = -1;
    return true;
  }

  int mTimePointCount = -1;
  @Override public boolean enqueue(long pTimePoint)
  {

    ArrayList<LaserDeviceInterface> lLaserList =
        getLightSheetMicroscope().getDevices(LaserDeviceInterface.class);

    if (!mInitialized) {
      for (LaserDeviceInterface lLaserDeviceInterface : lLaserList) {
        mLaserPower.add(lLaserDeviceInterface.getTargetPowerInPercent());
      }
      mInitialized = true;
    }

    mTimePointCount++;

    if (lLaserList.size() == 2) {
      LaserDeviceInterface lLaser1 = lLaserList.get(0);
      LaserDeviceInterface lLaser2 = lLaserList.get(1);

      if (mTimePointCount % 2 == 0) {
        lLaser1.setLaserPowerOn(true);
        lLaser1.setLaserOn(true);
        lLaser1.setTargetPowerInPercent(mLaserPower.get(0));
        lLaser2.setLaserPowerOn(false);
        lLaser2.setLaserOn(false);
      } else {
        lLaser1.setLaserPowerOn(false);
        lLaser1.setLaserOn(false);
        lLaser2.setLaserPowerOn(true);
        lLaser2.setLaserOn(true);
        lLaser1.setTargetPowerInPercent(mLaserPower.get(1));
      }
    } else {
      warning("Error: Wrong number of lasers!!");
      return false;
    }
    return false;
  }

  @Override
  public MultiChannelInstruction copy() {
    return new MultiChannelInstruction(getLightSheetMicroscope());
  }
}
