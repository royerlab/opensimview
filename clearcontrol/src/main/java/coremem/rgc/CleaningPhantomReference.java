package coremem.rgc;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/**
 * Phantom reference augmented with a cleaner runnable that knows how to 'clean'
 * the ressources of the referent.
 *
 * @author royer
 */
class CleaningPhantomReference extends PhantomReference<Cleanable>
{

  private final Cleaner mCleaner;

  /**
   * Instanciates a cleaning phantom reference given a referent, a cleaner
   * runnable that knows how to 'clean' the resources of te referent, and a
   * reference queue.
   *
   * @param pReferent        referent
   * @param pCleaner         cleaner
   * @param pReferencenQueue reference queue
   */
  public CleaningPhantomReference(Cleanable pReferent, Cleaner pCleaner, ReferenceQueue<Cleanable> pReferencenQueue)
  {
    super(pReferent, pReferencenQueue);
    mCleaner = pCleaner;
  }

  /**
   * Returns the cleaner Runnable responsible for releasing the ressources of
   * the referent.
   *
   * @return cleaner
   */
  public Cleaner getCleaner()
  {
    return mCleaner;
  }

}
