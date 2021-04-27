package clearcontrol.microscope.lightsheet.state.instructions;

import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.state.io.InterpolatedAcquisitionStateWriter;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;

import java.io.File;

/**
 * WriteAcquisitionStateToDiscInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public class WriteAcquisitionStateToDiscInstruction extends
                                                    LightSheetMicroscopeInstructionBase implements
                                                                                        PropertyIOableInstructionInterface
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

  @Override public boolean initialize()
  {
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {

    LightSheetTimelapse
        lTimelapse =
        (LightSheetTimelapse) getLightSheetMicroscope().getDevice(LightSheetTimelapse.class,
                                                                  0);

    InterpolatedAcquisitionState
        lState =
        (InterpolatedAcquisitionState) (getLightSheetMicroscope().getAcquisitionStateManager()
                                                                 .getCurrentState());

    File acqStateFile = new File(lTimelapse.getWorkingDirectory(), mFilename.get());
    System.out.println(acqStateFile);

    return new InterpolatedAcquisitionStateWriter(acqStateFile, lState).write();
  }

  @Override public WriteAcquisitionStateToDiscInstruction copy()
  {
    WriteAcquisitionStateToDiscInstruction
        copied =
        new WriteAcquisitionStateToDiscInstruction(getLightSheetMicroscope());
    copied.mFilename.set(mFilename.get());
    return copied;
  }

  public Variable<String> getFilename()
  {
    return mFilename;
  }

  @Override public Variable[] getProperties()
  {
    return new Variable[] { getFilename() };
  }
}
