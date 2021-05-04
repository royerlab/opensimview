package coremem.rgc.test;

import coremem.rgc.*;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * testing the ressource garbage collector
 *
 * @author royer
 */
public class RessourceGarbageCollectorTests
{
  static AtomicInteger sCounter = new AtomicInteger(0);

  /**
   * Free ressource wth given Id
   *
   * @param pResourceId ressource id
   */
  public static final void freeRessource(long pResourceId)
  {
    sCounter.incrementAndGet();
    // System.out.println("freeing: " + pResourceId);
  }

  private static class ClassWithRessource extends FreeableBase implements Freeable, Cleanable
  {
    long mSomeRessource = (long) (1000 * Math.random());
    AtomicBoolean mFree = new AtomicBoolean(false);

    {
      ClassWithRessource lClassWithRessource = this;
      RessourceCleaner.register(lClassWithRessource);

      // double[] lGarbage = new double[10000000];
      // lGarbage[12345] = 1;
    }

    static class MyCleaner implements Cleaner
    {
      private long mSomeRessource2;

      public MyCleaner(long pSomeRessource)
      {
        mSomeRessource2 = pSomeRessource;
      }

      @Override
      public void run()
      {
        freeRessource(mSomeRessource2);
      }

    }

    @Override
    public Cleaner getCleaner()
    {
      return new MyCleaner(mSomeRessource);
    }

    @Override
    public void free()
    {
      mFree.set(false);
      freeRessource(mSomeRessource);
    }

    @Override
    public boolean isFree()
    {
      return mFree.get();
    }

  }

  /**
   * @throws InterruptedException NA
   */
  @Test
  public void testRessourceCleaner() throws InterruptedException
  {
    int N = 100;

    for (int i = 0; i < N; i++)
    {
      @SuppressWarnings("unused") ClassWithRessource a = new ClassWithRessource();
      // RessourceCleaner.cleanNow();
      sleep(1);
      /*System.out.println("registered: "
                         + RessourceCleaner.getNumberOfRegisteredObjects());/**/
    }

    for (int i = 0; i < 10000 || RessourceCleaner.getNumberOfRegisteredObjects() != 0; i++)
    {
      sleep(1);
      if (i % 100 == 0) System.gc();

      // RessourceCleaner.cleanNow();
      // System.out.println(i);
      /*System.out.println("still registered: "
                         + RessourceCleaner.getNumberOfRegisteredObjects());/**/
    }

    int lNumberOfRegisteredObjects = RessourceCleaner.getNumberOfRegisteredObjects();
    assertEquals(0, lNumberOfRegisteredObjects);
    // System.out.println("lNumberOfRegisteredObjects=" +
    // lNumberOfRegisteredObjects);

    // System.out.println(sCounter.get());
    assertEquals(N, sCounter.get());
  }

  private void sleep(int pMilliseconds)
  {
    try
    {
      Thread.sleep(pMilliseconds);
    } catch (InterruptedException e)
    {
      e.printStackTrace();
    }

  }
}
