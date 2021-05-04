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
 * ChangeLightSheetYInstruction allows controlling the scan head to change the Y position
 * of light sheets
 * <p>
 * Author: @haesleinhuepf July 2018
 */
public class ChangeLightSheetYInstruction extends LightSheetMicroscopeInstructionBase implements PropertyIOableInstructionInterface
{

  private final BoundedVariable<Integer> mLightSheetIndex;
  private final BoundedVariable<Double> mLightSheetY = new BoundedVariable<Double>("Light sheet Y", 0.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.01);

  public ChangeLightSheetYInstruction(LightSheetMicroscope pLightSheetMicroscope, int pLightSheetIndex, double pLightSheetY)
  {
    super("Adaptation: Change light sheet Y", pLightSheetMicroscope);
    mLightSheetY.set(pLightSheetY);
    mLightSheetIndex = new BoundedVariable<Integer>("Light sheet index", pLightSheetIndex, 0, pLightSheetMicroscope.getNumberOfLightSheets());
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
        lState.getInterpolationTables().set(LightSheetDOF.IX, cpi, l, mLightSheetY.get());
      }
    }
    return true;
  }

  @Override
  public ChangeLightSheetYInstruction copy()
  {
    return new ChangeLightSheetYInstruction(getLightSheetMicroscope(), mLightSheetIndex.get(), mLightSheetY.get());
  }

  public BoundedVariable<Double> getLightSheetY()
  {
    return mLightSheetY;
  }

  public BoundedVariable<Integer> getLightSheetIndex()
  {
    return mLightSheetIndex;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]{getLightSheetIndex(), getLightSheetY()};
  }
}
