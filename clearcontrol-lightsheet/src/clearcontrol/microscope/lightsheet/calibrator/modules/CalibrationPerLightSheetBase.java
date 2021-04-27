package clearcontrol.microscope.lightsheet.calibrator.modules;

import clearcontrol.microscope.lightsheet.calibrator.CalibrationEngine;
import clearcontrol.microscope.lightsheet.configurationstate.ConfigurationState;
import clearcontrol.microscope.lightsheet.configurationstate.ConfigurationStateChangeListener;
import clearcontrol.microscope.lightsheet.configurationstate.ConfigurationStatePerLightSheetChangeListener;
import clearcontrol.microscope.lightsheet.configurationstate.HasConfigurationStatePerLightSheet;

import java.util.HashMap;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public class CalibrationPerLightSheetBase extends CalibrationBase implements
                                                                  HasConfigurationStatePerLightSheet
{

  private HashMap<Integer, ConfigurationState> mConfigurationStates = new HashMap<>();

  public CalibrationPerLightSheetBase(String pName, CalibrationEngine pCalibrationEngine)
  {
    super(pName, pCalibrationEngine);
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

    // call listeners
    for (ConfigurationStateChangeListener lConfigurationStateChangeListener : mConfigurationStateChangeListeners)
    {
      if (lConfigurationStateChangeListener instanceof ConfigurationStatePerLightSheetChangeListener)
      {
        ((ConfigurationStatePerLightSheetChangeListener) lConfigurationStateChangeListener)
            .configurationStateOfLightSheetChanged(this, pLightSheetIndex);
      }
      else
      {
        lConfigurationStateChangeListener.configurationStateChanged(this);
      }
    }
  }

  @Override public ConfigurationState getConfigurationState(int pIntLightSheetIndex)
  {
    if (mConfigurationStates.containsKey(pIntLightSheetIndex))
    {
      return mConfigurationStates.get(pIntLightSheetIndex);
    }
    return ConfigurationState.UNINITIALIZED;
  }

  @Override public void addConfigurationStateChangeListener(
      ConfigurationStateChangeListener pConfigurationStateChangeListener)
  {
    super.addConfigurationStateChangeListener(pConfigurationStateChangeListener);

    for (int lLightSheetIndex = 0; lLightSheetIndex
                                   < getLightSheetMicroscope().getNumberOfLightSheets(); lLightSheetIndex++)
    {
      setConfigurationState(lLightSheetIndex, getConfigurationState(lLightSheetIndex));
    }

  }

}
