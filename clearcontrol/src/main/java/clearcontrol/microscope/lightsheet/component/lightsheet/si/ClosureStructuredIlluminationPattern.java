package clearcontrol.microscope.lightsheet.component.lightsheet.si;

import clearcontrol.devices.signalgen.staves.ClosurePatternSteppingStave;
import clearcontrol.devices.signalgen.staves.SteppingFunction;

/**
 * Closure structured illumination pattern.
 * <p>
 * Convenience structured illumination pattern that can be defined with a closure
 * (functional interface)
 *
 * @author royer
 */
public class ClosureStructuredIlluminationPattern extends GenericStructuredIlluminationPattern<ClosurePatternSteppingStave> implements StructuredIlluminationPatternInterface
{

  /**
   * Instantiates a closure-defined structure illumination pattern given a stepping
   * function and number of phases.
   *
   * @param pSteppingFunction stepping function
   * @param pNumberOfPhases   number of phases
   */
  public ClosureStructuredIlluminationPattern(SteppingFunction pSteppingFunction, int pNumberOfPhases)
  {
    super(new ClosurePatternSteppingStave("trigger.out.e", pSteppingFunction), pNumberOfPhases);
  }

  /**
   * Sets the stepping function
   *
   * @param pSteppingFunction stepping function
   */
  public void setSteppingFunction(SteppingFunction pSteppingFunction)
  {
    setStave(new ClosurePatternSteppingStave("trigger.out.e", pSteppingFunction));
  }

}
