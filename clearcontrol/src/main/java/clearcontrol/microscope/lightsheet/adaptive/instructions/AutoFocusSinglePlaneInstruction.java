package clearcontrol.microscope.lightsheet.adaptive.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.state.tables.InterpolationTables;

/**
 * AutoFocusSinglePlaneInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public class AutoFocusSinglePlaneInstruction extends LightSheetMicroscopeInstructionBase implements LoggingFeature, PropertyIOableInstructionInterface
{

  private BoundedVariable<Integer> mControlPlaneIndex = new BoundedVariable<Integer>("Control plane index", 5, 0, Integer.MAX_VALUE);
  private BoundedVariable<Integer> mDetectionArmIndex = new BoundedVariable<Integer>("Detection arm index", 0, 0, Integer.MAX_VALUE);

  public AutoFocusSinglePlaneInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Smart: Rapid autofocus (single plane) for Z and alpha", pLightSheetMicroscope);

    InterpolatedAcquisitionState lAcquisitionState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState();

    mControlPlaneIndex.set((int) (lAcquisitionState.getNumberOfControlPlanes() * 0.75));
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    InterpolatedAcquisitionState lAcquisitionState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState();

    if (mControlPlaneIndex.get() >= lAcquisitionState.getNumberOfControlPlanes())
    {
      warning("Error: control plane " + mControlPlaneIndex + " does not exist.");
      return false;
    }

    ControlPlaneFocusFinderZInstruction lFocusScheduler = new ControlPlaneFocusFinderZInstruction(mDetectionArmIndex.get(), mControlPlaneIndex.get(), getLightSheetMicroscope());
    ControlPlaneFocusFinderAlphaByVariationInstruction lAlphaScheduler = new ControlPlaneFocusFinderAlphaByVariationInstruction(mDetectionArmIndex.get(), mControlPlaneIndex.get(), getLightSheetMicroscope());

    InstructionInterface[] lSchedulers = new InstructionInterface[]{lAlphaScheduler, lFocusScheduler,};

    for (InstructionInterface lScheduler : lSchedulers)
    {
      lScheduler.initialize();
      lScheduler.enqueue(pTimePoint);
    }

    // Copy configuration to other control planes
    for (int lLightSheetIndex = 0; lLightSheetIndex < getLightSheetMicroscope().getNumberOfLightSheets(); lLightSheetIndex++)
    {
      for (int cpi = 0; cpi < lAcquisitionState.getNumberOfControlPlanes(); cpi++)
      {
        InterpolationTables it = lAcquisitionState.getInterpolationTables();

        for (LightSheetDOF lDOF : new LightSheetDOF[]{LightSheetDOF.IZ, LightSheetDOF.IA})
        {
          it.set(lDOF, cpi, lLightSheetIndex, it.get(lDOF, mControlPlaneIndex.get(), lLightSheetIndex));
        }
      }
    }

    return true;
  }

  @Override
  public AutoFocusSinglePlaneInstruction copy()
  {
    AutoFocusSinglePlaneInstruction copied = new AutoFocusSinglePlaneInstruction(getLightSheetMicroscope());
    copied.mControlPlaneIndex.set(mControlPlaneIndex.get());
    return copied;
  }

  public BoundedVariable<Integer> getControlPlaneIndex()
  {
    return mControlPlaneIndex;
  }

  public BoundedVariable<Integer> getDetectionArmIndex()
  {
    return mDetectionArmIndex;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]{getControlPlaneIndex(), getDetectionArmIndex()};
  }
}
