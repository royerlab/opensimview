package clearcontrol.microscope.lightsheet.adaptive.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class ControlPlaneFocusFinderZInstruction extends
                                                 LightSheetMicroscopeInstructionBase implements
                                                                                     InstructionInterface,
                                                                                     LoggingFeature
{
  private final int mControlPlaneIndex;
  private final int mDetectionArmIndex;

  private Variable<Boolean>
      mResetAllTheTime =
      new Variable<Boolean>("resetAllTheTime", false);

  boolean mNeedsReset = true;

  public ControlPlaneFocusFinderZInstruction(int pDetectionArmIndex,
                                             int pControlPlaneIndex,
                                             LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Adaptation: Focus finder Z for C"
          + pDetectionArmIndex
          + "LxCPI"
          + pControlPlaneIndex, pLightSheetMicroscope);
    mDetectionArmIndex = pDetectionArmIndex;
    mControlPlaneIndex = pControlPlaneIndex;
  }

  @Override public boolean initialize()
  {
    mNeedsReset = true;
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    int
        lNumberOfControlPlanes =
        ((InterpolatedAcquisitionState) (getLightSheetMicroscope().getAcquisitionStateManager()
                                                                  .getCurrentState())).getNumberOfControlPlanes();

    for (int lLightSheetIndex = 0; lLightSheetIndex
                                   < getLightSheetMicroscope().getNumberOfLightSheets(); lLightSheetIndex++)
    {
      FocusFinderZInstruction
          lFocusFinder =
          new FocusFinderZInstruction(lLightSheetIndex,
                                      mDetectionArmIndex,
                                      mControlPlaneIndex,
                                      getLightSheetMicroscope());
      lFocusFinder.initialize();
      lFocusFinder.mNeedsReset = mNeedsReset;
      lFocusFinder.enqueue(pTimePoint); // this method returns success; we
      // ignore it and continue focussing
    }

    mNeedsReset = false;
    return true;
  }

  @Override public ControlPlaneFocusFinderZInstruction copy()
  {
    return new ControlPlaneFocusFinderZInstruction(mDetectionArmIndex,
                                                   mControlPlaneIndex,
                                                   getLightSheetMicroscope());
  }
}
