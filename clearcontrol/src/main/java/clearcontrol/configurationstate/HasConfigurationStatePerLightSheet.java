package clearcontrol.configurationstate;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public interface HasConfigurationStatePerLightSheet extends HasConfigurationState
{
  default public void execute(HasConfigurationState pHasConfigurationState)
  {
  }

  ConfigurationState getConfigurationState(int pIntLightSheetIndex);
}
