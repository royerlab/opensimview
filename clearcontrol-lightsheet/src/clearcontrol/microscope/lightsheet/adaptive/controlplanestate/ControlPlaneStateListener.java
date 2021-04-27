package clearcontrol.microscope.lightsheet.adaptive.controlplanestate;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public interface ControlPlaneStateListener
{
  public void controlPlaneStateChanged(int pLightSheetIndex, int pControlPlaneIndex);
}
