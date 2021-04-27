package coremem.recycling;

import coremem.interfaces.SizedInBytes;
import coremem.rgc.Freeable;

/**
 * Interface for all recyclable objects. A recyclable object life-cycle is
 * handled by the recycling machinery. Recyclable objects are expensive to
 * Instantiate and/or garbage collect and thus are reused when possible to
 * reduce resource pressure on the system.
 *
 * @param <R>
 *          Recyclable type
 * @param <P>
 *          Request type
 * @author royer
 */
public interface RecyclableInterface<R extends RecyclableInterface<R, P>, P extends RecyclerRequestInterface>
                                    extends SizedInBytes, Freeable
{

  /**
   * Returns true if this object is compatible with the given request.
   * Compatibility means that the object can be reused as well as an object
   * newly instantiated with the given request.
   * 
   * @param pRequest
   *          request
   * @return true if compatible
   */
  boolean isCompatible(P pRequest);

  /**
   * Recycles the object by initializing this object with information from the
   * given request
   * 
   * @param pRequest
   *          request
   */
  void recycle(P pRequest);

  /**
   * Sets the recycler that should be used by this recyclable object
   * 
   * @param pRecycler
   *          recycler to use
   */
  void setRecycler(RecyclerInterface<R, P> pRecycler);

  /**
   * Sets whether this recyclable object has been released. An object is
   * released when it is not in use anymore and is waiting to be recycled.
   * 
   * @param pIsReleased
   */
  void setReleased(boolean pIsReleased);

  /**
   * Returns true if the object has been released. An object is released when it
   * is not in use anymore and is waiting to be recycled.
   * 
   * @return true if released
   */
  boolean isReleased();

  /**
   * Release this object. An object is released when it is not in use anymore
   * and is waiting to be recycled.
   */
  void release();

}
