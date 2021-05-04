package coremem.rgc;

import coremem.exceptions.FreedException;

/**
 * Base class for all freeable objects
 *
 * @author royer
 */
public abstract class FreeableBase implements Freeable
{

  @Override
  public void complainIfFreed() throws FreedException
  {
    if (isFree())
    {
      final String lErrorMessage = "Underlying ressource has been freed!";
      throw new FreedException(lErrorMessage);
    }
  }

}
