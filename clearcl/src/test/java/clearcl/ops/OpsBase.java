package clearcl.ops;

import clearcl.ClearCLContext;
import clearcl.ClearCLQueue;

/**
 * Ops base class providing common machinery to all ops.
 *
 * @author royer
 */
public class OpsBase
{
  private ClearCLContext mClearCLContext;
  private ClearCLQueue mClearCLQueue;

  /**
   * Instantiates an op.
   * 
   * @param pClearCLQueue
   *          queue
   */
  public OpsBase(ClearCLQueue pClearCLQueue)
  {
    super();
    setClearCLQueue(pClearCLQueue);
    setClearCLContext(pClearCLQueue.getContext());
  }

  /**
   * Returns queue used by op.
   * 
   * @return queue
   */
  public ClearCLQueue getQueue()
  {
    return mClearCLQueue;
  }

  /**
   * Sets queue used by op.
   * 
   * @param pClearCLQueue
   *          queueu
   */
  public void setClearCLQueue(ClearCLQueue pClearCLQueue)
  {
    mClearCLQueue = pClearCLQueue;
  }

  /**
   * Returns OpenCL context.
   * 
   * @return context
   */
  public ClearCLContext getContext()
  {
    return mClearCLContext;
  }

  /**
   * Returns OpenCL context.
   * 
   * @param pClearCLContext
   *          new context
   */
  public void setClearCLContext(ClearCLContext pClearCLContext)
  {
    mClearCLContext = pClearCLContext;
  }

}
