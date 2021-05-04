package clearcontrol.configurationstate;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public interface HasStateDescriptionPerLightSheet extends HasStateDescription
{
  String getStateDescription(int pLightSheetIndex);
}
