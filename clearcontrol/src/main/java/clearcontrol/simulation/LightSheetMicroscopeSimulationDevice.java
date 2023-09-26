package clearcontrol.simulation;

import clearcontrol.LightSheetMicroscopeInterface;
import clearcontrol.LightSheetMicroscopeQueue;
import clearcontrol.MicroscopeInterface;
import clearcontrol.devices.stages.StageDeviceInterface;
import simbryo.synthoscopy.microscope.lightsheet.LightSheetMicroscopeSimulator;
import simbryo.synthoscopy.microscope.parameters.StageParameter;

import java.util.ArrayList;

/**
 * Lightsheet microscope simulator device
 *
 * @author royer
 */
public class LightSheetMicroscopeSimulationDevice extends SampleSimulationDeviceBase<LightSheetMicroscopeQueue> implements SampleSimulationDeviceInterface<LightSheetMicroscopeQueue>
{

  private LightSheetMicroscopeSimulator mSimbryoLightSheetMicroscopeSimulator;

  private ArrayList<LightSheetSimulationStackProvider> mLightSheetSimulationStackProviderList = new ArrayList<>();

  /**
   * Instantiates a light sheet microscope simulator device
   *
   * @param pSimbryoLightSheetMicroscopeSimulator light sheet microscope simulator (from Simbryo
   *                                       project)
   */
  public LightSheetMicroscopeSimulationDevice(LightSheetMicroscopeSimulator pSimbryoLightSheetMicroscopeSimulator)
  {
    super();
    mSimbryoLightSheetMicroscopeSimulator = pSimbryoLightSheetMicroscopeSimulator;
  }

  /**
   * Returns the underlying simulator.
   *
   * @return simulator
   */
  public LightSheetMicroscopeSimulator getSimulator()
  {
    return mSimbryoLightSheetMicroscopeSimulator;
  }

  @Override
  public LightSheetSimulationStackProvider getStackProvider(int pIndex)
  {
    return mLightSheetSimulationStackProviderList.get(pIndex);
  }

  @Override
  public void connectTo(MicroscopeInterface<LightSheetMicroscopeQueue> pMicroscope)
  {
    if (!(pMicroscope instanceof LightSheetMicroscopeInterface))
      throw new IllegalArgumentException("Must be a lightsheet microscope");

    LightSheetMicroscopeInterface lLightSheetMicroscope = (LightSheetMicroscopeInterface) pMicroscope;

    StageDeviceInterface lMainXYZRStage = lLightSheetMicroscope.getMainStage();

    if (lMainXYZRStage != null)
    {

      lMainXYZRStage.getCurrentPositionVariable(0).addSetListener((o, n) -> mSimbryoLightSheetMicroscopeSimulator.setNumberParameter(StageParameter.StageX, 0, n));

      lMainXYZRStage.getCurrentPositionVariable(1).addSetListener((o, n) -> mSimbryoLightSheetMicroscopeSimulator.setNumberParameter(StageParameter.StageY, 0, n));

      lMainXYZRStage.getCurrentPositionVariable(2).addSetListener((o, n) -> mSimbryoLightSheetMicroscopeSimulator.setNumberParameter(StageParameter.StageZ, 0, n));

      lMainXYZRStage.getCurrentPositionVariable(3).addSetListener((o, n) -> mSimbryoLightSheetMicroscopeSimulator.setNumberParameter(StageParameter.StageRY, 0, n));
    }

    int lNumberOfCameras = mSimbryoLightSheetMicroscopeSimulator.getNumberOfDetectionArms();
    for (int i = 0; i < lNumberOfCameras; i++)
    {
      LightSheetSimulationStackProvider lLightSheetSimulationStackProvider = new LightSheetSimulationStackProvider(lLightSheetMicroscope, mSimbryoLightSheetMicroscopeSimulator, i);

      mLightSheetSimulationStackProviderList.add(lLightSheetSimulationStackProvider);
    }

  }

}
