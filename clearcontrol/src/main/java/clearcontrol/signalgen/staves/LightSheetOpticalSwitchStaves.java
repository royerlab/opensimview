package clearcontrol.signalgen.staves;

import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.devices.signalgen.measure.Measure;
import clearcontrol.devices.signalgen.staves.ConstantStave;
import clearcontrol.component.opticalswitch.LightSheetOpticalSwitch;
import clearcontrol.component.opticalswitch.LightSheetOpticalSwitchQueue;

/**
 * Light sheet microscope optical switch staves. These staves are used when controlling a
 * lightsheeet microscope using digital signals to switch lightsheets on and off.
 *
 * @author royer
 */
public class LightSheetOpticalSwitchStaves
{
  private LightSheetOpticalSwitchQueue mLightSheetOpticalSwitchQueue;

  private final ConstantStave[] mBitStave;

  private int[] mStaveIndex;

  /**
   * Instanciates given a lightsheet optical switch device and default stave index.
   *
   * @param pLightSheetOpticalSwitchQueue lightsheet optical switch device queue
   * @param pDefaultStaveIndex            default stave index
   */
  public LightSheetOpticalSwitchStaves(LightSheetOpticalSwitchQueue pLightSheetOpticalSwitchQueue, int pDefaultStaveIndex)
  {
    super();
    mLightSheetOpticalSwitchQueue = pLightSheetOpticalSwitchQueue;
    int lNumberOfSwitches = mLightSheetOpticalSwitchQueue.getNumberOfSwitches();
    mBitStave = new ConstantStave[lNumberOfSwitches];
    mStaveIndex = new int[lNumberOfSwitches];

    for (int i = 0; i < mBitStave.length; i++)
    {
      mStaveIndex[i] = MachineConfiguration.get().getIntegerProperty("device.lsm.switch." + getLightSheetOpticalSwitch().getName() + i + ".index", pDefaultStaveIndex);
      mBitStave[i] = new ConstantStave("lightsheet.s." + i, 0);
    }
  }

  private LightSheetOpticalSwitch getLightSheetOpticalSwitch()
  {
    return mLightSheetOpticalSwitchQueue.getLightSheetOpticalSwitch();
  }

  /**
   * Adds staves to staging measures.
   *
   * @param pBeforeExposureMeasure before exposure measure
   * @param pExposureMeasure       exposure measure
   * @param pFinalMeasure          final measure
   */
  public void addStavesToMeasures(Measure pBeforeExposureMeasure, Measure pExposureMeasure, Measure pFinalMeasure)
  {
    for (int i = 0; i < mBitStave.length; i++)
    {
      pBeforeExposureMeasure.setStave(mStaveIndex[i], mBitStave[i]);
      pExposureMeasure.setStave(mStaveIndex[i], mBitStave[i]);
      pFinalMeasure.setStave(mStaveIndex[i], mBitStave[i]);
    }
  }

  /**
   * Updates staves
   *
   * @param pExposureMeasure       exposure measure
   * @param pBeforeExposureMeasure before exposure measure
   */
  public void update(Measure pBeforeExposureMeasure, Measure pExposureMeasure)
  {
    synchronized (this)
    {
      for (int i = 0; i < mBitStave.length; i++)
      {
        mBitStave[i].setValue(mLightSheetOpticalSwitchQueue.getSwitchVariable(i).get() ? 1 : 0);
      }
    }
  }
}
