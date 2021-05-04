package clearcontrol.microscope.lightsheet.configurationstate;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public abstract class ConfigurationStatePerLightSheetChangeListener implements ConfigurationStateChangeListener
{
  public abstract void configurationStateOfLightSheetChanged(HasConfigurationStatePerLightSheet pHasConfigurationStatePerLightSheet, int pLightSheetIndex);
}
