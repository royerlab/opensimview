package clearcontrol.state.instructions;

import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.state.InterpolatedAcquisitionState;
import clearcontrol.state.io.InterpolatedAcquisitionStateReader;
import clearcontrol.timelapse.LightSheetTimelapse;

import java.io.File;

/**
 * ReadAcquisitionStateFromDiscInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public class ReadAcquisitionStateFromDiscInstruction extends LightSheetMicroscopeInstructionBase implements PropertyIOableInstructionInterface
{

  public Variable<String> mFilename = new Variable<String>("Filename", "state.acqstate");

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pLightSheetMicroscope
   */
  public ReadAcquisitionStateFromDiscInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("IO: Read acquisition state from disc", pLightSheetMicroscope);
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {

    LightSheetTimelapse lTimelapse = (LightSheetTimelapse) getLightSheetMicroscope().getDevice(LightSheetTimelapse.class, 0);

    InterpolatedAcquisitionState lState = (InterpolatedAcquisitionState) (getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState());

    File acqStateFile = new File(lTimelapse.getWorkingDirectory(), mFilename.get());
    System.out.println(acqStateFile);

    return new InterpolatedAcquisitionStateReader(acqStateFile, lState).read();
  }

  @Override
  public ReadAcquisitionStateFromDiscInstruction copy()
  {
    ReadAcquisitionStateFromDiscInstruction copied = new ReadAcquisitionStateFromDiscInstruction(getLightSheetMicroscope());
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
