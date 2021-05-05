package clearcontrol.component.lightsheet.instructions;

import clearcontrol.LightSheetDOF;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.state.AcquisitionStateManager;
import clearcontrol.state.InterpolatedAcquisitionState;

/**
 * ChangeLightSheetWidthInstruction allows controlling the irises in the illumination
 * arms
 * <p>
 * Todo: allow to control independent light sheets individually
 * <p>
 * XWing specific: * All irises are controlled together * Value 0 corresponds to an open
 * iris * Value 0.45 corresponds to an almost closed iris
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class ChangeLightSheetWidthInstruction extends LightSheetMicroscopeInstructionBase implements PropertyIOableInstructionInterface
{

  private final BoundedVariable<Double> mLightSheetWidth = new BoundedVariable<Double>("Light sheet width", 0.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.01);

  public ChangeLightSheetWidthInstruction(LightSheetMicroscope pLightSheetMicroscope, double pLightSheetWidth)
  {
    super("Adaptation: Change light sheet width", pLightSheetMicroscope);
    mLightSheetWidth.set(pLightSheetWidth);
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
      for (int l = 0; l < lState.getNumberOfLightSheets(); l++)
      {
        lState.getInterpolationTables().set(LightSheetDOF.IW, cpi, l, mLightSheetWidth.get());
      }
    }
    return true;
  }

  @Override
  public ChangeLightSheetWidthInstruction copy()
  {
    return new ChangeLightSheetWidthInstruction(getLightSheetMicroscope(), mLightSheetWidth.get());
  }

  public BoundedVariable<Double> getLightSheetWidth()
  {
    return mLightSheetWidth;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]{getLightSheetWidth()};
  }
}
