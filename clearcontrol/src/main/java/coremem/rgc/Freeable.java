package coremem.rgc;

import coremem.exceptions.FreedException;

/**
 * Freeable class. Objects implementing this interface have some resource that
 * can be used until freed.
 *
 * @author royer
 */
public interface Freeable
{
  /**
   * Free this object, which means that the underlying ressources are released.
   */
  public void free();

  /**
   * Returns true if the object has been freed, which usually means that the
   * underlying ressources are not longer available.
   *
   * @return true if free
   */
  public boolean isFree();

  /**
   * Throws an exception when the object is freed more than once.
   *
   * @throws FreedException exception thrown
   */
  public void complainIfFreed() throws FreedException;

}
