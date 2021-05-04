package clearcontrol.adaptive.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.state.InterpolatedAcquisitionState;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class ControlPlaneFocusFinderAlphaByVariationInstruction extends LightSheetMicroscopeInstructionBase implements InstructionInterface, LoggingFeature
{
  private final int mControlPlaneIndex;
  private final int mDetectionArmIndex;

  public ControlPlaneFocusFinderAlphaByVariationInstruction(int pDetectionArmIndex, int pControlPlaneIndex, LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Adaptation: Focus finder alpha for C" + pDetectionArmIndex + "LxCPI" + pControlPlaneIndex, pLightSheetMicroscope);
    mDetectionArmIndex = pDetectionArmIndex;
    mControlPlaneIndex = pControlPlaneIndex;
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {

    int lNumberOfControlPlanes = ((InterpolatedAcquisitionState) (getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState())).getNumberOfControlPlanes();

    for (int lLightSheetIndex = 0; lLightSheetIndex < getLightSheetMicroscope().getNumberOfLightSheets(); lLightSheetIndex++)
    {
      FocusFinderAlphaByVariationInstruction lFocusFinder = new FocusFinderAlphaByVariationInstruction(lLightSheetIndex, mDetectionArmIndex, mControlPlaneIndex, getLightSheetMicroscope());
      lFocusFinder.initialize();
      lFocusFinder.enqueue(pTimePoint); // this method returns success; we
      // ignore it and continue focussing
    }

    return true;
  }

  @Override
  public ControlPlaneFocusFinderAlphaByVariationInstruction copy()
  {
    return new ControlPlaneFocusFinderAlphaByVariationInstruction(mDetectionArmIndex, mControlPlaneIndex, getLightSheetMicroscope());
  }
}
