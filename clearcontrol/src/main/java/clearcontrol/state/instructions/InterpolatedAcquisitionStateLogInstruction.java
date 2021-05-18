package clearcontrol.state.instructions;

import clearcontrol.LightSheetMicroscope;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.state.InterpolatedAcquisitionState;
import clearcontrol.state.io.InterpolatedAcquisitionStateWriter;
import clearcontrol.timelapse.Timelapse;

import java.io.File;

/**
 * This instructions writes the current acquisition state to a file with the timepoint
 * being part of the filename
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class InterpolatedAcquisitionStateLogInstruction extends LightSheetMicroscopeInstructionBase implements InstructionInterface, LoggingFeature
{

  /**
   * INstanciates a virtual device with a given name
   */
  public InterpolatedAcquisitionStateLogInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("IO: Log current acquisition state to disc", pLightSheetMicroscope);
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean execute(long pTimePoint)
  {
    Timelapse lTimelapse = (Timelapse) getLightSheetMicroscope().getDevice(Timelapse.class, 0);

    InterpolatedAcquisitionState lState = (InterpolatedAcquisitionState) (getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState());

    File tempFile = new File(lTimelapse.getWorkingDirectory(), "state_t" + pTimePoint + ".acqstate");
    System.out.println(tempFile);

    new InterpolatedAcquisitionStateWriter(tempFile, lState).write();

    return true;
  }

  @Override
  public InterpolatedAcquisitionStateLogInstruction copy()
  {
    return new InterpolatedAcquisitionStateLogInstruction(getLightSheetMicroscope());
  }
}
