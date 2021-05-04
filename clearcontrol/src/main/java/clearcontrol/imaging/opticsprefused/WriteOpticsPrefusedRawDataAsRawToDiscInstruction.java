package clearcontrol.imaging.opticsprefused;

import clearcontrol.LightSheetMicroscope;
import clearcontrol.warehouse.containers.io.WriteStackInterfaceContainerAsRawToDiscInstructionBase;

/**
 * This instructions writes the raw data from the oldest optics prefused acquisition
 * stored in the DataWarehouse to disc.
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class WriteOpticsPrefusedRawDataAsRawToDiscInstruction extends WriteStackInterfaceContainerAsRawToDiscInstructionBase
{
  public WriteOpticsPrefusedRawDataAsRawToDiscInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("IO: Write optics prefused raw data to disc", OpticsPrefusedImageDataContainer.class, listKeys(pLightSheetMicroscope.getNumberOfDetectionArms()), null, pLightSheetMicroscope);
  }

  private static String[] listKeys(int pNumberOfDetectionArms)
  {
    String[] result = new String[pNumberOfDetectionArms];
    for (int d = 0; d < pNumberOfDetectionArms; d++)
    {
      result[d] = "C" + d + "opticsprefused";
    }
    return result;
  }

  @Override
  public WriteOpticsPrefusedRawDataAsRawToDiscInstruction copy()
  {
    return new WriteOpticsPrefusedRawDataAsRawToDiscInstruction(getLightSheetMicroscope());
  }
}
