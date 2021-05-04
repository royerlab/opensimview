package clearcontrol.microscope.lightsheet.adaptive.controlplanestate;

import clearcontrol.microscope.lightsheet.configurationstate.ConfigurationState;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public interface HasControlPlaneState
{

  ConfigurationState getControlPlaneState(int pLightSheetIndex, int pControlPlaneIndex);

  String getControlPlaneStateDescription(int pLightSheetIndex, int pControlPlaneIndex);

  void addControlPlaneStateChangeListener(ControlPlaneStateListener pControlPlaneStateChangeListener);
}
