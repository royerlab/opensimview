package clearcontrol.microscope.lightsheet.processor.fusion;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerAsRawToDiscInstructionBase;

/**
 * This instructions writes a fused image to disc. Depending on how the images was fused,
 * it might be stored in different folders.
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class WriteFusedImageAsRawToDiscInstruction extends
                                                   WriteStackInterfaceContainerAsRawToDiscInstructionBase
{
  public WriteFusedImageAsRawToDiscInstruction(String pChannelName,
                                               LightSheetMicroscope pLightSheetMicroscope)
  {
    super("IO: Write " + pChannelName + " fused stack to disc",
          FusedImageDataContainer.class,
          new String[] { "fused" },
          pChannelName,
          pLightSheetMicroscope);
  }

  @Override public WriteFusedImageAsRawToDiscInstruction copy()
  {
    return new WriteFusedImageAsRawToDiscInstruction(mChannelName,
                                                     getLightSheetMicroscope());
  }
}
