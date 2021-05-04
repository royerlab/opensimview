package clearcontrol.imaging.sequential;

import clearcontrol.LightSheetMicroscope;
import clearcontrol.warehouse.containers.io.WriteStackInterfaceContainerAsRawToDiscInstructionBase;

/**
 * This instructions writes the raw data from the sequential acquisition stored in the
 * DataWarehouse to disc.
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class WriteSequentialRawDataToDiscInstruction extends WriteStackInterfaceContainerAsRawToDiscInstructionBase
{
  /**
   * INstanciates a virtual device with a given name
   */
  public WriteSequentialRawDataToDiscInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("IO: Write sequential raw data to disc", SequentialImageDataContainer.class, listKeys(pLightSheetMicroscope.getNumberOfDetectionArms(), pLightSheetMicroscope.getNumberOfLightSheets()), null, pLightSheetMicroscope);
  }

  private static String[] listKeys(int pNumberOfDetectionArms, int pNumberOfLightSheets)
  {
    String[] result = new String[pNumberOfDetectionArms * pNumberOfLightSheets];
    int count = 0;
    for (int l = 0; l < pNumberOfLightSheets; l++)
    {
      for (int d = 0; d < pNumberOfDetectionArms; d++)
      {
        result[count] = "C" + d + "L" + l;
        count++;
      }
    }
    return result;
  }

  @Override
  public WriteSequentialRawDataToDiscInstruction copy()
  {
    return new WriteSequentialRawDataToDiscInstruction(getLightSheetMicroscope());
  }
}
