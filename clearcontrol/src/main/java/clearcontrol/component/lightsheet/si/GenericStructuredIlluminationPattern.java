package clearcontrol.component.lightsheet.si;

import clearcontrol.devices.signalgen.staves.StaveInterface;

/**
 * Generic illumination pattern
 *
 * @param <S> stave type
 * @author royer
 */
public class GenericStructuredIlluminationPattern<S extends StaveInterface> extends StructuredIlluminationPatternBase implements StructuredIlluminationPatternInterface
{

  private volatile S mStave;
  private volatile int mNumberOfPhases = 1;

  /**
   * Instantiates a generic illumination pattern
   *
   * @param pStave          stave
   * @param pNumberOfPhases number of phases
   */
  public GenericStructuredIlluminationPattern(S pStave, int pNumberOfPhases)
  {
    super();
    setStave(pStave);
    mNumberOfPhases = pNumberOfPhases;
  }

  /**
   * Sets the stave to use for the illumination pattern
   *
   * @param pStave stave
   */
  public void setStave(S pStave)
  {
    mStave = pStave;
  }

  @Override
  public StaveInterface getStave(double pMarginTimeRelativeUnits)
  {
    return mStave;
  }

  @Override
  public int getNumberOfPhases()
  {
    return mNumberOfPhases;
  }

  /**
   * Sets the number of phases
   *
   * @param pNumberOfPhases number of phases
   */
  public void setNumberOfPhases(int pNumberOfPhases)
  {
    mNumberOfPhases = pNumberOfPhases;
  }

}
