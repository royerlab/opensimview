package clearcontrol.state.instructions;

import clearcontrol.LightSheetMicroscope;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.instructions.PropertyIOableInstructionInterface;

/**
 * ChangeExposureTimeInstruction
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class ChangeExposureTimeInstruction extends LightSheetMicroscopeInstructionBase implements PropertyIOableInstructionInterface
{
  BoundedVariable<Double> mExposureTimeInSecondsVariable = new BoundedVariable<Double>("Exposure time in seconds", 0.01, 0.0, Double.MAX_VALUE, 0.00001);

  public ChangeExposureTimeInstruction(double pExposureTimeInSeconds, LightSheetMicroscope pLightSheetMicroscope)
  {

    super("Adaptation: Change exposure time to " + pExposureTimeInSeconds + " s", pLightSheetMicroscope);
    mExposureTimeInSecondsVariable.set(pExposureTimeInSeconds);
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState().getExposureInSecondsVariable().set(mExposureTimeInSecondsVariable.get());
    return true;
  }

  @Override
  public ChangeExposureTimeInstruction copy()
  {
    return new ChangeExposureTimeInstruction(mExposureTimeInSecondsVariable.get(), getLightSheetMicroscope());
  }

  public BoundedVariable<Double> getExposureTimeInSecondsVariable()
  {
    return mExposureTimeInSecondsVariable;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]{getExposureTimeInSecondsVariable()};
  }
}
