package coremem.recycling.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import coremem.recycling.BasicRecycler;
import coremem.recycling.RecyclableFactoryInterface;
import coremem.recycling.RecyclerInterface;

import org.junit.Test;

/**
 *
 *
 * @author royer
 */
public class RecyclerTests
{

  /**
   * Basic tests
   */
  @Test
  public void testBasics()
  {

    final RecyclableFactoryInterface<TestRecyclable, TestRequest> lRecyclableFactory =
                                                                                     new RecyclableFactoryInterface<TestRecyclable, TestRequest>()
                                                                                     {
                                                                                       @Override
                                                                                       public TestRecyclable create(TestRequest pParameters)
                                                                                       {
                                                                                         return new TestRecyclable(pParameters);
                                                                                       }
                                                                                     };

    final RecyclerInterface<TestRecyclable, TestRequest> lRecycler =
                                                                   new BasicRecycler<TestRecyclable, TestRequest>(lRecyclableFactory,
                                                                                                                  200,
                                                                                                                  200,
                                                                                                                  true);

    assertEquals(100,
                 lRecycler.ensurePreallocated(100,
                                              new TestRequest(1L)));

    assertEquals(100, lRecycler.getNumberOfAvailableObjects());
    assertEquals(0, lRecycler.getNumberOfLiveObjects());

    lRecycler.clearReleased();

    assertEquals(0, lRecycler.getNumberOfAvailableObjects());
    assertEquals(0, lRecycler.getNumberOfLiveObjects());

    assertEquals(100,
                 lRecycler.ensurePreallocated(100,
                                              new TestRequest(1L)));

    assertEquals(100, lRecycler.getNumberOfAvailableObjects());
    assertEquals(0, lRecycler.getNumberOfLiveObjects());

    final HashSet<TestRecyclable> lRecyclableObjectSet =
                                                       new HashSet<TestRecyclable>();
    for (int i = 0; i < 200; i++)
    {
      final TestRecyclable lRecyclableObject =
                                             lRecycler.getOrFail(new TestRequest(1L));
      assertTrue(lRecyclableObject != null);
      lRecyclableObjectSet.add(lRecyclableObject);
    }

    for (int i = 0; i < 10; i++)
    {
      final TestRecyclable lFailOrRequestRecyclableObject =
                                                          lRecycler.getOrFail(new TestRequest(1L));
      // System.out.println(lFailOrRequestRecyclableObject);
      assertTrue(lFailOrRequestRecyclableObject == null);
    }

    assertEquals(0, lRecycler.getNumberOfAvailableObjects());
    assertEquals(200, lRecycler.getNumberOfLiveObjects());

    final long lStartTimeNs = System.nanoTime();
    for (int i = 0; i < 10; i++)
    {
      final TestRecyclable lFailOrRequestRecyclableObject =
                                                          lRecycler.getOrWait(100,
                                                                              TimeUnit.MILLISECONDS,
                                                                              new TestRequest(1L));
      // System.out.println(lFailOrRequestRecyclableObject);
      assertTrue(lFailOrRequestRecyclableObject == null);
    }
    final long lStopTimeNs = System.nanoTime();

    assertTrue(10 * 100 * 1e6 < lStopTimeNs - lStartTimeNs);

    assertEquals(1, lRecycler.getNumberOfAvailableObjects());
    assertEquals(200, lRecycler.getNumberOfLiveObjects());

    for (final TestRecyclable lTestRecyclable : lRecyclableObjectSet)
    {
      lRecycler.release(lTestRecyclable);
    }

    assertEquals(200, lRecycler.getNumberOfAvailableObjects());
    assertEquals(0, lRecycler.getNumberOfLiveObjects());

    lRecycler.clearReleased();

    assertEquals(0, lRecycler.getNumberOfAvailableObjects());
    assertEquals(0, lRecycler.getNumberOfLiveObjects());

    for (int i = 0; i < 100; i++)
    {
      // System.out.println(i);
      final TestRecyclable lFailOrRequestRecyclableObject =
                                                          lRecycler.getOrWait(1,
                                                                              TimeUnit.MICROSECONDS,
                                                                              new TestRequest(1L));
      assertTrue(lFailOrRequestRecyclableObject != null);
      lRecycler.release(lFailOrRequestRecyclableObject);
    }

    assertEquals(1, lRecycler.getNumberOfAvailableObjects());
    assertEquals(0, lRecycler.getNumberOfLiveObjects());

    for (int i = 0; i < 200; i++)
    {
      // System.out.println(i);
      final TestRecyclable lRecyclableObject =
                                             lRecycler.getOrWait(1,
                                                                 TimeUnit.MICROSECONDS,
                                                                 new TestRequest(1L));
      assertTrue(lRecyclableObject != null);
      lRecyclableObjectSet.add(lRecyclableObject);
    }

    assertEquals(0, lRecycler.getNumberOfAvailableObjects());
    assertEquals(200, lRecycler.getNumberOfLiveObjects());

    for (int i = 0; i < 10; i++)
    {
      final TestRecyclable lFailOrRequestRecyclableObject =
                                                          lRecycler.getOrFail(new TestRequest(1L));
      // System.out.println(lFailOrRequestRecyclableObject);
      assertTrue(lFailOrRequestRecyclableObject == null);
    }

    assertEquals(0, lRecycler.getNumberOfAvailableObjects());
    assertEquals(200, lRecycler.getNumberOfLiveObjects());

    lRecycler.free();

    assertEquals(0, lRecycler.getNumberOfAvailableObjects());
    assertEquals(200, lRecycler.getNumberOfLiveObjects());
  }

  /**
   * Tests tight recycling
   */
  @Test
  public void testTightRecycling()
  {
    final RecyclableFactoryInterface<TestRecyclable, TestRequest> lRecyclableFactory =
                                                                                     new RecyclableFactoryInterface<TestRecyclable, TestRequest>()
                                                                                     {
                                                                                       @Override
                                                                                       public TestRecyclable create(TestRequest pParameters)
                                                                                       {
                                                                                         return new TestRecyclable(pParameters);
                                                                                       }
                                                                                     };

    final BasicRecycler<TestRecyclable, TestRequest> lRecycler =
                                                               new BasicRecycler<TestRecyclable, TestRequest>(lRecyclableFactory,
                                                                                                              1000);

    for (int i = 0; i < 100000; i++)
    {
      final TestRecyclable lRecyclableObject =
                                             lRecycler.getOrWait(1,
                                                                 TimeUnit.SECONDS,
                                                                 new TestRequest(1L));
      assertTrue(lRecyclableObject != null);

      // if (i % 2 == 0)
      lRecycler.release(lRecyclableObject);
    }

  }

  /**
   * tests tight recycling with changes in request parameters
   */
  @Test
  public void testTightRecyclingWithRequestChanges()
  {
    final RecyclableFactoryInterface<TestRecyclable, TestRequest> lRecyclableFactory =
                                                                                     new RecyclableFactoryInterface<TestRecyclable, TestRequest>()
                                                                                     {
                                                                                       @Override
                                                                                       public TestRecyclable create(TestRequest pParameters)
                                                                                       {
                                                                                         return new TestRecyclable(pParameters);
                                                                                       }
                                                                                     };

    final BasicRecycler<TestRecyclable, TestRequest> lRecycler =
                                                               new BasicRecycler<TestRecyclable, TestRequest>(lRecyclableFactory,
                                                                                                              10,
                                                                                                              10,
                                                                                                              true);
    TestRecyclable lRecyclableObject;

    assertEquals(0, lRecycler.getNumberOfAvailableObjects());
    assertEquals(0, lRecycler.getNumberOfLiveObjects());

    for (int i = 0; i < 10; i++)
    {
      lRecyclableObject = lRecycler.getOrWait(1,
                                              TimeUnit.SECONDS,
                                              new TestRequest(1024L));
      assertTrue(lRecyclableObject != null);
      assertEquals(1024L, lRecyclableObject.getSizeInBytes());
      lRecycler.release(lRecyclableObject);
    }

    assertEquals(1, lRecycler.getNumberOfAvailableObjects());
    assertEquals(0, lRecycler.getNumberOfLiveObjects());

    for (int i = 0; i < 10; i++)
    {
      lRecyclableObject = lRecycler.getOrWait(1,
                                              TimeUnit.SECONDS,
                                              new TestRequest(2048L));
      // System.out.println(lRecyclableObject);
      // System.out.println("lRecycler.getNumberOfAvailableObjects()" +
      // lRecycler.getNumberOfAvailableObjects());
      // System.out.println("lRecycler.getNumberOfLiveObjects()" +
      // lRecycler.getNumberOfLiveObjects());

      assertTrue(lRecyclableObject != null);
      assertEquals(2048L, lRecyclableObject.getSizeInBytes());
    }

    assertEquals(0, lRecycler.getNumberOfAvailableObjects());
    assertEquals(10, lRecycler.getNumberOfLiveObjects());

    assertEquals(null,
                 lRecycler.getOrWait(1,
                                     TimeUnit.SECONDS,
                                     new TestRequest(2048L)));

    lRecycler.clearLive();

    for (int i = 0; i < 100000; i++)
    {

      if ((i % 100) == 0)
      {
        lRecyclableObject =
                          lRecycler.getOrWait(1,
                                              TimeUnit.SECONDS,
                                              new TestRequest(1024L));
        assertEquals(1024L, lRecyclableObject.getSizeInBytes());

      }
      else
      {
        lRecyclableObject =
                          lRecycler.getOrWait(1,
                                              TimeUnit.SECONDS,
                                              new TestRequest(2048L));
        assertEquals(2048L, lRecyclableObject.getSizeInBytes());

      }

      assertTrue(lRecyclableObject != null);

      // if (i % 2 == 0)
      lRecycler.release(lRecyclableObject);
    }

  }

  /**
   * Tests async recycling with requests changes
   * 
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void testAsynchronousRecyclingWithRequestChanges() throws InterruptedException
  {
    final RecyclableFactoryInterface<TestRecyclable, TestRequest> lRecyclableFactory =
                                                                                     new RecyclableFactoryInterface<TestRecyclable, TestRequest>()
                                                                                     {
                                                                                       @Override
                                                                                       public TestRecyclable create(TestRequest pParameters)
                                                                                       {
                                                                                         return new TestRecyclable(pParameters);
                                                                                       }
                                                                                     };

    final BasicRecycler<TestRecyclable, TestRequest> lRecycler =
                                                               new BasicRecycler<TestRecyclable, TestRequest>(lRecyclableFactory,
                                                                                                              10,
                                                                                                              10,
                                                                                                              true);

    final ArrayBlockingQueue<TestRecyclable> lQueue =
                                                    new ArrayBlockingQueue<TestRecyclable>(10);

    final Runnable lRunnableProducer = new Runnable()
    {

      @Override
      public void run()
      {
        for (int i = 0; i < 1000; i++)
        {
          // System.out.println("PRODUCER:" + i);
          TestRecyclable lRecyclable;
          if (i % 10 == 0)
          {
            lRecyclable = lRecycler.getOrWait(1,
                                              TimeUnit.SECONDS,
                                              new TestRequest(1024L));
          }
          else
          {
            lRecyclable = lRecycler.getOrWait(1,
                                              TimeUnit.SECONDS,
                                              new TestRequest(2048L));
          }

          // System.out.println("SENDING...");
          lQueue.offer(lRecyclable);
          // System.out.println("SENT:" + lRecyclable);
          try
          {
            Thread.sleep(1);
          }
          catch (final InterruptedException e)
          {
          }
        }
      }
    };

    final Runnable lRunnableConsumer = new Runnable()
    {

      @Override
      public void run()
      {
        for (int i = 0; i < 1000; i++)
        {
          // System.out.println("CONSUMER:" + i);
          try
          {

            // System.out.println("RECEIVING...");
            final TestRecyclable lRecyclable = lQueue.take();
            // System.out.println("RECEIVED:" + lRecyclable);
            if (lRecyclable != null)
            {
              try
              {
                Thread.sleep(3);
              }
              catch (final InterruptedException e)
              {
              }
              lRecyclable.release();
            }
          }
          catch (final InterruptedException e)
          {
            e.printStackTrace();
          }
        }
      }
    };

    final ExecutorService lProducerExecutor =
                                            Executors.newSingleThreadExecutor();
    final ExecutorService lConsumerExecutor =
                                            Executors.newSingleThreadExecutor();

    lConsumerExecutor.execute(lRunnableConsumer);
    lProducerExecutor.execute(lRunnableProducer);

    lConsumerExecutor.shutdown();
    lConsumerExecutor.awaitTermination(100, TimeUnit.SECONDS);

    lProducerExecutor.shutdown();
    lProducerExecutor.awaitTermination(100, TimeUnit.SECONDS);

    /*System.out.println("lRecycler.getNumberOfAvailableObjects()="
                       + lRecycler.getNumberOfAvailableObjects());
    System.out.println("lRecycler.getNumberOfLiveObjects()="
                       + lRecycler.getNumberOfLiveObjects());/**/

    // assertEquals(0, lRecycler.getNumberOfAvailableObjects());
    assertEquals(0, lRecycler.getNumberOfLiveObjects());
  }
}
