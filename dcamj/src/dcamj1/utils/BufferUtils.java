package dcamj1.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BufferUtils
{
  private static Executor sCleanerExecutor =
                                           Executors.newSingleThreadExecutor();

  /**
   * DirectByteBuffers are garbage collected by using a phantom reference and a
   * reference queue. Every once a while, the JVM checks the reference queue and
   * cleans the DirectByteBuffers. However, as this doesn't happen immediately
   * after discarding all references to a DirectByteBuffer, it's easy to
   * OutOfMemoryError yourself using DirectByteBuffers. This function explicitly
   * calls the Cleaner method of a DirectByteBuffer.
   * 
   * @param pToBeDestroyed
   *          The DirectByteBuffer that will be "cleaned". Utilizes reflection.
   * @throws IllegalArgumentException
   *           exception thrown
   * @throws IllegalAccessException
   *           exception thrown
   * @throws InvocationTargetException
   *           exception thrown
   * @throws SecurityException
   *           exception thrown
   * @throws NoSuchMethodException
   *           exception thrown
   * 
   */
  public static void destroyDirectByteBuffer(final ByteBuffer pToBeDestroyed) throws IllegalArgumentException,
                                                                              IllegalAccessException,
                                                                              InvocationTargetException,
                                                                              SecurityException,
                                                                              NoSuchMethodException
  {

    final Runnable lCleaner = new Runnable()
    {

      @Override
      public void run()
      {
        try
        {
          final Method cleanerMethod =
                                     pToBeDestroyed.getClass()
                                                   .getMethod("cleaner");
          cleanerMethod.setAccessible(true);
          final Object cleaner = cleanerMethod.invoke(pToBeDestroyed);
          final Method cleanMethod = cleaner.getClass()
                                            .getMethod("clean");
          cleanMethod.setAccessible(true);
          cleanMethod.invoke(cleaner);
        }
        catch (final Throwable e)
        {
          e.printStackTrace();
        }
      }
    };

    sCleanerExecutor.execute(lCleaner);

  }
}
