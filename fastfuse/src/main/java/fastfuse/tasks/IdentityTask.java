package fastfuse.tasks;

import java.util.ArrayList;
import java.util.List;

import fastfuse.FastFusionEngineInterface;

/**
 * Identity task - this task does nothing, just instantaneously passes the image
 * from a source to a destination slot.
 *
 * @author royer
 */
public class IdentityTask extends TaskBase implements TaskInterface
{

  private final String mSrcImageSlotKey, mDstImageSlotKey;

  public static List<TaskInterface> withSuffix(String pSuffix,
                                               String... pImageKeys)
  {
    List<TaskInterface> lTaskList = new ArrayList<>();
    for (String lSrcKey : pImageKeys)
    {
      String lDstKey = lSrcKey + pSuffix;
      lTaskList.add(new IdentityTask(lSrcKey, lDstKey));
    }
    return lTaskList;
  }

  /**
   * Instantiates an identity task.
   * 
   * @param pSrcImageSlotKey
   *          source slot key
   * @param pDstImageSlotKey
   *          destination slot key
   */
  public IdentityTask(String pSrcImageSlotKey,
                      String pDstImageSlotKey)
  {
    super(pSrcImageSlotKey);
    mSrcImageSlotKey = pSrcImageSlotKey;
    mDstImageSlotKey = pDstImageSlotKey;
  }

  @Override
  public boolean enqueue(FastFusionEngineInterface pFastFusionEngine,
                         boolean pWaitToFinish)
  {
    pFastFusionEngine.assignImageToAnotherSlotKey(mSrcImageSlotKey,
                                                  mDstImageSlotKey);
    return true;
  }

}
