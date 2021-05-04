package clearcontrol.component.lightsheet.instructions;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.LightSheetDOF;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.state.InterpolatedAcquisitionState;
import clearcontrol.state.AcquisitionStateManager;

/**
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class ChangeLightSheetBrightnessInstruction extends LightSheetMicroscopeInstructionBase implements PropertyIOableInstructionInterface
{

  private final BoundedVariable<Integer> mLightSheetIndex = new BoundedVariable<Integer>("Light sheet index", 0, 0, 2, 1);

  private final BoundedVariable<Double> mLightSheetBrightness = new BoundedVariable<Double>("Light sheet brightness", 0.0, 0.0, 1.0, 0.005);

  public ChangeLightSheetBrightnessInstruction(LightSheetMicroscope pLightSheetMicroscope, int pLightSheetIndex, double pLightSheetBrightness)
  {
    super("Adaptation: Change light sheet brightness", pLightSheetMicroscope);
    mLightSheetIndex.set(pLightSheetIndex);
    mLightSheetBrightness.set(pLightSheetBrightness);
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    InterpolatedAcquisitionState lState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getDevice(AcquisitionStateManager.class, 0).getCurrentState();
    for (int cpi = 0; cpi < lState.getNumberOfControlPlanes(); cpi++)
    {
      int lLightSheetIndex = mLightSheetIndex.get();
      lState.getInterpolationTables().set(LightSheetDOF.IP, cpi, lLightSheetIndex, mLightSheetBrightness.get());
    }
    return true;
  }

  @Override
  public ChangeLightSheetBrightnessInstruction copy()
  {
    return new ChangeLightSheetBrightnessInstruction(getLightSheetMicroscope(), mLightSheetIndex.get(), mLightSheetBrightness.get());
  }

  public BoundedVariable<Double> getLightSheetBrightness()
  {
    return mLightSheetBrightness;
  }

  public BoundedVariable<Integer> getLightSheetIndex()
  {
    return mLightSheetIndex;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]{getLightSheetIndex(), getLightSheetBrightness()};
  }
}
