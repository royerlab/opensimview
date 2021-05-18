package clearcontrol.instructions;

import clearcontrol.LightSheetMicroscope;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.timelapse.TimelapseInterface;
import clearcontrol.timelapse.instructions.AbstractAcquistionInstruction;

/**
 * This instructions skips n instructions in the program every k time points
 */
public class SkipInstruction extends AbstractAcquistionInstruction implements InstructionInterface, PropertyIOableInstructionInterface, LoggingFeature
{
  private final BoundedVariable<Integer> mSkipPeriod;
  private final BoundedVariable<Integer> mNumberOfInstructionsToSkipVariable;
  private TimelapseInterface mTimeLapse;

  public SkipInstruction(LightSheetMicroscope pLightSheetMicroscope, TimelapseInterface pTimeLapse)
  {
    super("Smart: Skip instruction", pLightSheetMicroscope);
    mTimeLapse = pTimeLapse;
    mSkipPeriod = new BoundedVariable<>("Skipping period", -1, -1, 100000);
    mNumberOfInstructionsToSkipVariable = new BoundedVariable<>("Number of instructions to skip", 0, 0, 1000);
  }

  @Override
  public boolean execute(long pTimePoint)
  {
    int lSkipPeriod = mSkipPeriod.get();
    if (lSkipPeriod>0 && pTimePoint % lSkipPeriod == 0)
    {

      int lCurrentProgramIndex = mTimeLapse.getInstructionIndexVariable().get();
      int lNumberOfInstructionsToSkip = mNumberOfInstructionsToSkipVariable.get();
      lCurrentProgramIndex += lNumberOfInstructionsToSkip;
      info("SkipInstruction: skipping "+lNumberOfInstructionsToSkip+" instructions at time point "+pTimePoint);
      mTimeLapse.getInstructionIndexVariable().set(lCurrentProgramIndex);
    }
    return true;
  }


  @Override
  public SkipInstruction copy()
  {
    return new SkipInstruction(getLightSheetMicroscope(), mTimeLapse);
  }

  public BoundedVariable<Integer> getSkipPeriod()
  {
    return mSkipPeriod;
  }

  public BoundedVariable<Integer> getNumberOfInstructionsToSkip()
  {
    return mNumberOfInstructionsToSkipVariable;
  }


  @Override
  public Variable[] getProperties()
  {
    Variable[] lVariables = new Variable[]{mSkipPeriod, mNumberOfInstructionsToSkipVariable};
    return lVariables;
  }
}
