package clearcontrol.component.lightsheet.si;

import clearcontrol.devices.signalgen.staves.IntervalStave;
import clearcontrol.devices.signalgen.staves.StaveInterface;

/**
 * Constant illumination pattern
 *
 * @author royer
 */
public class ConstantIlluminationPattern extends StructuredIlluminationPatternBase implements StructuredIlluminationPatternInterface
{

  private final IntervalStave mStave;

  /**
   * Instantiates a constant illumination pattern
   */
  public ConstantIlluminationPattern()
  {
    super();
    mStave = new IntervalStave("trigger.out.e", 0, 1, 1, 0);
  }

  @Override
  public StaveInterface getStave(double pMarginTimeRelativeUnits)
  {
    mStave.setStart((float) clamp01(pMarginTimeRelativeUnits));
    mStave.setStop((float) clamp01(1 - pMarginTimeRelativeUnits));
    return mStave;
  }

  @Override
  public int getNumberOfPhases()
  {
    return 1;
  }

}
