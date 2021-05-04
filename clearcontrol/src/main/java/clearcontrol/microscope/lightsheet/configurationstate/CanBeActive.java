package clearcontrol.microscope.lightsheet.configurationstate;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public interface CanBeActive
{

  /**
   * Convenience method for obtaining the is-active flag.
   *
   * @return true -> module active, false otherwise
   */
  boolean isActive();
}
