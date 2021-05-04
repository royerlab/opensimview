package dcamj1;

import dcamapi.DcamapiLibrary.DCAMERR;
import org.bridj.IntValuedEnum;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DcamBase
{
  private final ConcurrentLinkedQueue<IntValuedEnum<DCAMERR>> mErrorList = new ConcurrentLinkedQueue<IntValuedEnum<DCAMERR>>();

  boolean mDebug = false;
  boolean mShowErrors = false;

  /**
   * @return
   */
  public boolean isDebug()
  {
    return mDebug;
  }

  public void setDebug(boolean pDebug)
  {
    mDebug = pDebug;
  }

  public boolean isShowErrors()
  {
    return mShowErrors;
  }

  public void setShowErrors(boolean pShowErrors)
  {
    mShowErrors = pShowErrors;
  }

  protected final void addError(final IntValuedEnum<DCAMERR> pError)
  {
    if (isDebug())
    {
      mErrorList.add(pError);
      System.out.println(pError);
    }

    if (isShowErrors() && !DcamLibrary.hasSucceeded(pError))
    {
      System.err.println(pError);
    }
  }

  protected final boolean addErrorToListAndCheckHasSucceeded(final IntValuedEnum<DCAMERR> lError)
  {
    addError(lError);
    final boolean lSuccess = DcamLibrary.hasSucceeded(lError);
    return lSuccess;
  }

  public final Collection<IntValuedEnum<DCAMERR>> getErrorList()
  {
    return mErrorList;
  }

  public final void displayErrorList()
  {
    System.out.println(mErrorList);
  }

  public final void clearErrorList()
  {
    mErrorList.clear();
  }

  public final boolean haveAllSucceeded()
  {
    for (final IntValuedEnum<DCAMERR> lEntry : mErrorList)
    {
      final boolean lHasSucceeded = DcamLibrary.hasSucceeded(lEntry);
      if (!lHasSucceeded)
      {
        return false;
      }
    }
    return true;
  }

}
