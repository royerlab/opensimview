package coremem.recycling;

import java.util.concurrent.TimeUnit;

import coremem.rgc.Freeable;

/**
 * Interface implemented by all recyclers. A recycler manages a pool of live
 * (used) recyclable objects and a pool of available (ready to be recycled)
 * recyclable objects.
 *
 * @param <R>
 *          Recyclable type
 * @param <P>
 *          Request type
 * @author royer
 */
public interface RecyclerInterface<R extends RecyclableInterface<R, P>, P extends RecyclerRequestInterface>
                                  extends Freeable
{

  /**
   * Ensures that a given number of recyclable objects are available.
   * 
   * @param pNumberofPrealocatedRecyclablesNeeded
   *          number of preallocated recyclables
   * @param pRecyclerRequest
   *          request to use for instanciating the recyclable objects
   * @return effective number of recyclable objects allocated.
   */
  public abstract long ensurePreallocated(final long pNumberofPrealocatedRecyclablesNeeded,
                                          final P pRecyclerRequest);

  /**
   * Attempts to get a recyclable object from this recycler for a given request.
   * This call might fail in which case a null reference is returned.
   * 
   * @param pRecyclerRequest
   *          request
   * @return null if failed to get a recyclable.
   */
  public abstract R getOrFail(final P pRecyclerRequest);

  /**
   * Waits for a given amount of time to return a recyclable object given a
   * request.
   * 
   * @param pWaitTime
   *          wait time
   * @param pTimeUnit
   *          wait time unit
   * @param pRecyclerRequest
   *          request
   * @return null if fails to get a new recyclable object before timeout
   */
  public abstract R getOrWait(final long pWaitTime,
                              final TimeUnit pTimeUnit,
                              final P pRecyclerRequest);

  /**
   * Requests a recyclable object (all other get methods delegate to this one).
   * 
   * @param pWaitForLiveRecyclablesToComeBack
   *          as it names suggest, this flag causes this call to wait until an
   *          object is available.
   * @param pWaitTime
   *          wait time
   * @param pTimeUnit
   *          wait time unit
   * @param pRecyclerRequest
   *          request
   * @return null if fails or timeout
   */
  public abstract R get(final boolean pWaitForLiveRecyclablesToComeBack,
                        final long pWaitTime,
                        final TimeUnit pTimeUnit,
                        final P pRecyclerRequest);

  /**
   * Returns the maximum number of live objects allowed.
   * 
   * @return maximum number of live objects allowed
   */
  public abstract int getMaxNumberOfLiveObjects();

  /**
   * Returns the current number of live objects - these are objects that are
   * curently in use and thus not available for recycling.
   * 
   * @return number of live objects
   */
  public abstract int getNumberOfLiveObjects();

  /**
   * Returns the maximum number of available objects allowed.
   * 
   * @return max number of available objects
   */
  public abstract int getMaxNumberOfAvailableObjects();

  /**
   * Returns the current number of recyclable objects available for recycling.
   * 
   * @return number of available recyclable objects
   */
  public abstract int getNumberOfAvailableObjects();

  /**
   * Returns the number of failed requests.
   * 
   * @return number of failed requests
   */
  public abstract long getNumberOfFailedRequests();

  /**
   * Returns the calculated total size in bytes of live objects.
   * 
   * @return total siee of live objects in bytes
   */
  public abstract long computeLiveMemorySizeInBytes();

  /**
   * Returns the calculated total size in bytes of available objects.
   * 
   * @return calculated total size in bytes
   */
  public abstract long computeAvailableMemorySizeInBytes();

  /**
   * Releases the given object
   * 
   * @param pObject
   *          recyclable object to release
   */
  public abstract void release(final R pObject);

  /**
   * Clears the list of available objects and frees them if the 'autofree' flag
   * is set.
   */
  public abstract void clearReleased();

  /**
   * Clears the list of live objects and frees them if the 'autofree' flag is
   * set
   */
  public abstract void clearLive();

  /**
   * Adds a recycler listener to this recycler.
   * 
   * @param pRecyclerListener
   *          recycler listener
   */
  public abstract void addListener(RecyclerListenerInterface pRecyclerListener);

  /**
   * Remioves a recycler listener to this recycler.
   * 
   * @param pRecyclerListener
   *          recycler listener
   */
  public abstract void removeListener(RecyclerListenerInterface pRecyclerListener);

  /**
   * Prints debug info
   */
  public abstract void printDebugInfo();

}
