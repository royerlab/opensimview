package clearcontrol.devices.signalgen.staves;

public class ClosurePatternSteppingStave extends PatternSteppingStave
                                         implements StaveInterface
{

  private final SteppingFunction mSteppingFunction;

  public ClosurePatternSteppingStave(final String pName,
                                     SteppingFunction pSteppingFunction)
  {
    super(pName);
    mSteppingFunction = pSteppingFunction;
  }

  @Override
  public StaveInterface duplicate()
  {
    StaveInterface lStave = new ClosurePatternSteppingStave(getName(),
                                           getSteppingFunction());
    lStave.setEnabled(this.isEnabled());
    return lStave;
  }

  public SteppingFunction getSteppingFunction()
  {
    return mSteppingFunction;
  }

  @Override
  public float function(int pIndex)
  {
    return getSteppingFunction().function(pIndex);
  }

}
