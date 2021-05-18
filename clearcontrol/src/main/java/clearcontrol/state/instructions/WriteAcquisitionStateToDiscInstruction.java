package clearcontrol.state.instructions;

import clearcontrol.LightSheetMicroscope;
import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.state.InterpolatedAcquisitionState;
import clearcontrol.state.io.InterpolatedAcquisitionStateWriter;
import clearcontrol.timelapse.Timelapse;

import java.io.File;

/**
 * WriteAcquisitionStateToDiscInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public class WriteAcquisitionStateToDiscInstruction extends LightSheetMicroscopeInstructionBase implements PropertyIOableInstructionInterface
{

  public Variable<String> mFilename = new Variable<String>("Filename", "state.acqstate");

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pLightSheetMicroscope
   */
  public WriteAcquisitionStateToDiscInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("IO: Write acquisition state to disc", pLightSheetMicroscope);
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

    File acqStateFile = new File(lTimelapse.getWorkingDirectory(), mFilename.get());
    System.out.println(acqStateFile);

    return new InterpolatedAcquisitionStateWriter(acqStateFile, lState).write();
  }

  @Override
  public WriteAcquisitionStateToDiscInstruction copy()
  {
    WriteAcquisitionStateToDiscInstruction copied = new WriteAcquisitionStateToDiscInstruction(getLightSheetMicroscope());
    copied.mFilename.set(mFilename.get());
    return copied;
  }

  public Variable<String> getFilename()
  {
    return mFilename;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]{getFilename()};
  }
}
