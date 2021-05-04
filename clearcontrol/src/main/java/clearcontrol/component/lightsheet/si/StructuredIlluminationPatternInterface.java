package clearcontrol.component.lightsheet.si;

import clearcontrol.devices.signalgen.staves.StaveInterface;

/**
 * Structured illumination pattern interface
 *
 * @author royer
 */
public interface StructuredIlluminationPatternInterface
{

  /**
   * Returns the stave used for the structured illumination pattern
   *
   * @param pMarginTimeRelativeUnits margin time in relative units
   * @return stave interface
   */
  public StaveInterface getStave(double pMarginTimeRelativeUnits);

  /**
   * Returns the number of phases in the pattern
   *
   * @return number of phase
   */
  public int getNumberOfPhases();

}
