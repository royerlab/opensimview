package clearcontrol.microscope.lightsheet.component.lightsheet.si;

/**
 * Base class for all implementations of the structured illumination pattern interface
 *
 * @author royer
 */
public abstract class StructuredIlluminationPatternBase
{

  protected static final double clamp01(final double x)
  {
    return Math.max(0, Math.min(1, x));
  }
}
