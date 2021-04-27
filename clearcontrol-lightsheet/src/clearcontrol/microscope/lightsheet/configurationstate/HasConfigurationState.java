package clearcontrol.microscope.lightsheet.configurationstate;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public interface HasConfigurationState
{

  ConfigurationState getConfigurationState();

  void addConfigurationStateChangeListener(ConfigurationStateChangeListener pConfigurationStateChangeListener);
}
