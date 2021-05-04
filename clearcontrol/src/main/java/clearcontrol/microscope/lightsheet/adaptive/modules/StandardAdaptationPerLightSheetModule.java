package clearcontrol.microscope.lightsheet.adaptive.modules;

import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.configurationstate.ConfigurationState;
import clearcontrol.microscope.lightsheet.configurationstate.ConfigurationStateChangeListener;
import clearcontrol.microscope.lightsheet.configurationstate.ConfigurationStatePerLightSheetChangeListener;
import clearcontrol.microscope.lightsheet.configurationstate.HasConfigurationStatePerLightSheet;

import java.util.HashMap;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public abstract class StandardAdaptationPerLightSheetModule extends StandardAdaptationModule implements HasConfigurationStatePerLightSheet
{

  private HashMap<Integer, ConfigurationState> mConfigurationStates = new HashMap<>();

  public StandardAdaptationPerLightSheetModule(String pModuleName, LightSheetDOF pLightSheetDOF, int pNumberOfSamples, double pProbabilityThreshold, double pImageMetricThreshold, double pExposureInSeconds, double pLaserPower)
  {
    super(pModuleName, pLightSheetDOF, pNumberOfSamples, pProbabilityThreshold, pImageMetricThreshold, pExposureInSeconds, pLaserPower);
  }

  protected void resetState()
  {
    super.resetState();
    mConfigurationStates.clear();
  }

  protected void setConfigurationState(int pLightSheetIndex, ConfigurationState pState)
  {
    if (mConfigurationStates.containsKey(pLightSheetIndex))
    {
      mConfigurationStates.remove(pLightSheetIndex);
    }
    mConfigurationStates.put(pLightSheetIndex, pState);
    invokeConfigurationStateChangeListeners(pLightSheetIndex);
  }

  private void invokeConfigurationStateChangeListeners(int pLightSheetIndex)
  {
    // call listeners
    for (ConfigurationStateChangeListener lConfigurationStateChangeListener : mConfigurationStateChangeListeners)
    {
      if (lConfigurationStateChangeListener instanceof ConfigurationStatePerLightSheetChangeListener)
      {
        ((ConfigurationStatePerLightSheetChangeListener) lConfigurationStateChangeListener).configurationStateOfLightSheetChanged(this, pLightSheetIndex);
      } else
      {
        lConfigurationStateChangeListener.configurationStateChanged(this);
      }
    }
  }

  @Override
  public ConfigurationState getConfigurationState(int pIntLightSheetIndex)
  {
    if (mConfigurationStates.containsKey(pIntLightSheetIndex))
    {
      return mConfigurationStates.get(pIntLightSheetIndex);
    }
    return ConfigurationState.UNINITIALIZED;
  }

  @Override
  public void addConfigurationStateChangeListener(ConfigurationStateChangeListener pConfigurationStateChangeListener)
  {
    super.addConfigurationStateChangeListener(pConfigurationStateChangeListener);

    for (int lLightSheetIndex : mConfigurationStates.keySet())
    {

      invokeConfigurationStateChangeListeners(lLightSheetIndex);
    }

  }
}
