package clearcontrol.configurationstate;

import java.util.EventListener;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public interface ConfigurationStateChangeListener extends EventListener
{
  void configurationStateChanged(HasConfigurationState pHasConfigurationState);
}
