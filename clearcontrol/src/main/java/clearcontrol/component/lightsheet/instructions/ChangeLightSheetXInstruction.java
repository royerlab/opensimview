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
 * ChangeLightSheetXInstruction allows controlling the scan head to change the X position
 * of light sheets
 * <p>
 * Author: @haesleinhuepf July 2018
 */
public class ChangeLightSheetXInstruction extends LightSheetMicroscopeInstructionBase implements PropertyIOableInstructionInterface
{

  private final BoundedVariable<Integer> mLightSheetIndex;
  private final BoundedVariable<Double> mLightSheetX = new BoundedVariable<Double>("Light sheet X", 0.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.01);

  public ChangeLightSheetXInstruction(LightSheetMicroscope pLightSheetMicroscope, int pLightSheetIndex, double pLightSheetX)
  {
    super("Adaptation: Change light sheet X", pLightSheetMicroscope);
    mLightSheetX.set(pLightSheetX);
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
        lState.getInterpolationTables().set(LightSheetDOF.IX, cpi, l, mLightSheetX.get());
      }
    }
    return true;
  }

  @Override
  public ChangeLightSheetXInstruction copy()
  {
    return new ChangeLightSheetXInstruction(getLightSheetMicroscope(), mLightSheetIndex.get(), mLightSheetX.get());
  }

  public BoundedVariable<Double> getLightSheetX()
  {
    return mLightSheetX;
  }

  public BoundedVariable<Integer> getLightSheetIndex()
  {
    return mLightSheetIndex;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]{getLightSheetIndex(), getLightSheetX()};
  }
}
