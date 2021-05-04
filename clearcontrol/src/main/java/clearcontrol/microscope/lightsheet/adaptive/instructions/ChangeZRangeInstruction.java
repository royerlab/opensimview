package clearcontrol.microscope.lightsheet.adaptive.instructions;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;

/**
 * ChangeZRangeInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 09 2018
 */
public class ChangeZRangeInstruction extends LightSheetMicroscopeInstructionBase implements PropertyIOableInstructionInterface
{

  private BoundedVariable<Double> minZ = new BoundedVariable<Double>("Start Z in microns", 0.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.001);
  private BoundedVariable<Double> maxZ = new BoundedVariable<Double>("End Z in microns", 320.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.001);
  private BoundedVariable<Double> stepZ = new BoundedVariable<Double>("Step Z in microns", 2.5, -Double.MAX_VALUE, Double.MAX_VALUE, 0.001);

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pLightSheetMicroscope
   */
  public ChangeZRangeInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Adaptation: Change range in Z", pLightSheetMicroscope);
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    InterpolatedAcquisitionState state = (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState();
    state.getStackZStepVariable().set(1.0); // this is neccessary to prevent an
    // endless loop while updating
    state.getStackZLowVariable().set(minZ.get());
    state.getStackZHighVariable().set(maxZ.get());
    state.getStackZStepVariable().set(stepZ.get());
    return true;
  }

  @Override
  public ChangeZRangeInstruction copy()
  {
    ChangeZRangeInstruction copied = new ChangeZRangeInstruction(getLightSheetMicroscope());
    copied.minZ.set(minZ.get());
    copied.maxZ.set(maxZ.get());
    copied.stepZ.set(stepZ.get());
    return copied;
  }

  public BoundedVariable<Double> getMinZ()
  {
    return minZ;
  }

  public BoundedVariable<Double> getMaxZ()
  {
    return maxZ;
  }

  public BoundedVariable<Double> getStepZ()
  {
    return stepZ;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]{getMinZ(), getMaxZ(), getStepZ()};
  }
}
