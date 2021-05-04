package clearcl.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Asynchronous executor using a fixed pool of threads.
 *
 * @author royer
 */
public class AsynchronousExecutor
{
  private static final int cNumberOfThreads = 10;
  static Executor sExecutor = Executors.newFixedThreadPool(cNumberOfThreads);

  /**
   * Notify asynchronously
   *
   * @param pRunnable runnbale to execute
   */
  public static void notifyChange(Runnable pRunnable)
  {
    sExecutor.execute(pRunnable);
  }

}
