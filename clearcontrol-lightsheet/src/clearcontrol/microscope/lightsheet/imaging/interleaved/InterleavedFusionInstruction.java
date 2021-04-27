package clearcontrol.microscope.lightsheet.imaging.interleaved;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.processor.fusion.FusionInstruction;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.stack.StackInterface;

/**
 * This FusionInstruction takes the oldest InterleavedImageDataContainer from the
 * DataWarehouse and fuses the images. Results are saved as FusedImageContainer back to
 * the DataWarehouse.
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class InterleavedFusionInstruction extends FusionInstruction implements
                                                                    InstructionInterface,
                                                                    LoggingFeature
{
  /**
   * INstanciates a virtual device with a given name
   */
  public InterleavedFusionInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Post-processing: Interleaved fusion", pLightSheetMicroscope);
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    DataWarehouse lDataWarehouse = getLightSheetMicroscope().getDataWarehouse();
    final InterleavedImageDataContainer
        lContainer =
        lDataWarehouse.getOldestContainer(InterleavedImageDataContainer.class);
    String[]
        lInputImageKeys =
        new String[getLightSheetMicroscope().getNumberOfDetectionArms()];

    int count = 0;
    for (int d = 0; d < getLightSheetMicroscope().getNumberOfDetectionArms(); d++)
    {
      lInputImageKeys[count] = "C" + d + "interleaved";
      count++;
    }

    StackInterface lFusedStack = fuseStacks(lContainer, lInputImageKeys);
    if (lFusedStack == null)
    {
      return false;
    }

    storeFusedContainer(lFusedStack);
    return true;
  }

  @Override public InterleavedFusionInstruction copy()
  {
    return new InterleavedFusionInstruction(getLightSheetMicroscope());
  }
}
