package clearcontrol.microscope.lightsheet.component.lightsheet.instructions;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.state.AcquisitionStateManager;

/**
 * ChangeLightSheetHeightInstruction allows controlling the scan head to change the height
 * of light sheets
 * <p>
 * Author: @haesleinhuepf July 2018
 */
public class ChangeLightSheetHeightInstruction extends LightSheetMicroscopeInstructionBase implements PropertyIOableInstructionInterface
{

  private final BoundedVariable<Integer> mLightSheetIndex;
  private final BoundedVariable<Double> mLightSheetHeight = new BoundedVariable<Double>("Light sheet height", 0.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.01);

  public ChangeLightSheetHeightInstruction(LightSheetMicroscope pLightSheetMicroscope, int pLightSheetIndex, double pLightSheetHeight)
  {
    super("Adaptation: Change light sheet height", pLightSheetMicroscope);
    mLightSheetHeight.set(pLightSheetHeight);
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
        lState.getInterpolationTables().set(LightSheetDOF.IH, cpi, l, mLightSheetHeight.get());
      }
    }
    return true;
  }

  @Override
  public ChangeLightSheetHeightInstruction copy()
  {
    return new ChangeLightSheetHeightInstruction(getLightSheetMicroscope(), mLightSheetIndex.get(), mLightSheetHeight.get());
  }

  public BoundedVariable<Double> getLightSheetHeight()
  {
    return mLightSheetHeight;
  }

  public BoundedVariable<Integer> getLightSheetIndex()
  {
    return mLightSheetIndex;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]{getLightSheetHeight(), getLightSheetIndex()};
  }
}
