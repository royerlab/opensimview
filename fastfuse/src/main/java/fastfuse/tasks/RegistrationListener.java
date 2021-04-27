package fastfuse.tasks;

/**
 * Registration listener interface. Listeners are notified of transformation
 * computed and used
 *
 * @author royer
 */
public interface RegistrationListener
{

  /**
   * Called to notify of a new computed transformation. This transformation is
   * not necessarily used for actual registration as it might undergo temporal
   * filtering and limiting.
   * 
   * @param pTheta
   *          transformation
   */
  void newComputedTheta(double[] pTheta);

  /**
   * Called to notify a of the actual transformation last used to register the
   * two stacks.
   * 
   * @param pTheta
   *          transformation
   */
  void newUsedTheta(double[] pTheta);

  /**
   * Called to notify of a new registration score (typically normalized cross
   * correlation) obtained for the original images
   * 
   * @param pScore
   *          new registration score
   */
  void notifyListenersOfNewScoreForComputedTheta(double pScore);

  /**
   * Called to notify of a new registration score (typically normalized cross
   * correlation) obtained for the original images _after_ temporal filtering of
   * the transform
   * 
   * @param pScore
   *          new registration score
   */
  void notifyListenersOfNewScoreForUsedTheta(double pScore);

}
