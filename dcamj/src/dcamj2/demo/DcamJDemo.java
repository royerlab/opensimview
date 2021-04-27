package dcamj2.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import coremem.recycling.BasicRecycler;
import dcamj2.DcamDevice;
import dcamj2.DcamLibrary;
import dcamj2.DcamSequenceAcquisition;
import dcamj2.imgseq.DcamImageSequence;
import dcamj2.imgseq.DcamImageSequenceFactory;
import dcamj2.imgseq.DcamImageSequenceRequest;

import org.junit.Test;

/**
 * DcamJ demo
 *
 * @author royer
 */
public class DcamJDemo
{

  /**
   * Tests single image acquisition
   * 
   * @throws InterruptedException
   *           NA
   * @throws ExecutionException
   *           NA
   */
  @Test
  public void testSingleImageAcquisition() throws InterruptedException,
                                           ExecutionException
  {
    int lWidth = 2048;
    int lHeight = 2048;
    int lDepth = 1;

    assertTrue(DcamLibrary.initialize());

    DcamDevice lDcamDevice = new DcamDevice(0, false, false);
    assertNotNull(lDcamDevice);

    assertTrue(lDcamDevice.open());
    lDcamDevice.setInputTriggerToInternal();


    System.out.println(lDcamDevice.getStatus());

    // lDcamDevice.setInputTriggerToExternalLevel();
    // lDcamDevice.setInputTriggerToInternal();

    lDcamDevice.printDeviceInfo();

    DcamSequenceAcquisition lDcamSequenceAcquisition =
                                                     new DcamSequenceAcquisition(lDcamDevice);

    // lDcamSequenceAcquisition.mDebug = true;

    DcamImageSequence lSequence1 = new DcamImageSequence(lDcamDevice,
                                                         2,
                                                         lWidth,
                                                         lHeight,
                                                         lDepth);

    for (int i = 0; i < 15; i++)
    {
      System.out.println("Acquiring single image: #" + i);
      assertTrue(lDcamSequenceAcquisition.acquireSequenceAsync(0.01,
                                                               100.0,
                                                               lSequence1)
                                         .get());
    }

    lDcamDevice.close();

    assertTrue(DcamLibrary.uninitialize());

  }

  /**
   * Tests sequence acquisition
   * 
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void testSequenceAcquisition() throws InterruptedException
  {
    int lWidth = 2048;
    int lHeight = 2048;
    int lDepth = 512;




    assertTrue(DcamLibrary.initialize());

    DcamDevice lDcamDevice = new DcamDevice(0, false, false);
    lDcamDevice.setInputTriggerToInternal();
    assertNotNull(lDcamDevice);

    assertTrue(lDcamDevice.open());

    lDcamDevice.setInputTriggerToInternal();
    lDcamDevice.mDebug =true;

    System.out.println(lDcamDevice.getStatus());

    lDcamDevice.printDeviceInfo();

    DcamSequenceAcquisition lDcamSequenceAcquisition =
                                                     new DcamSequenceAcquisition(lDcamDevice);
    lDcamSequenceAcquisition.mDebug=true;
    System.out.println("FIRST SEQUENCE");
    DcamImageSequence lSequence1 = new DcamImageSequence(lDcamDevice,
                                                         2,
                                                         lWidth,
                                                         lHeight,
                                                         lDepth);

    assertTrue(lDcamSequenceAcquisition.acquireSequence(0.01,
                                                        lSequence1));

    System.out.println("SECOND SEQUENCE");
    DcamImageSequence lSequence2 = new DcamImageSequence(lDcamDevice,
                                                         2,
                                                         lWidth / 2,
                                                         lHeight / 2,
                                                         lDepth / 2);
    lDcamSequenceAcquisition.mDebug=true;

    assertTrue(lDcamSequenceAcquisition.acquireSequence(0.01,
                                                        lSequence2));

    lDcamDevice.close();

    assertTrue(DcamLibrary.uninitialize());

  }

  /**
   * Tests sequence acquisition
   *
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void testSequenceAcquisitionTwoCameras() throws InterruptedException, ExecutionException {
    int lWidth = 2048;
    int lHeight = 2048;
    int lDepth = 1024;


    assertTrue(DcamLibrary.initialize());

    DcamDevice lDcamDevice0 = new DcamDevice(0, false, false);
    assertNotNull(lDcamDevice0);
    assertTrue(lDcamDevice0.open());
    lDcamDevice0.setInputTriggerToInternal();
    lDcamDevice0.mDebug =true;
    System.out.println(lDcamDevice0.getStatus());
    lDcamDevice0.printDeviceInfo();

    DcamDevice lDcamDevice1 = new DcamDevice(1, false, false);
    assertNotNull(lDcamDevice1);
    assertTrue(lDcamDevice1.open());
    lDcamDevice1.setInputTriggerToInternal();
    lDcamDevice1.mDebug =true;
    System.out.println(lDcamDevice1.getStatus());
    lDcamDevice1.printDeviceInfo();


    DcamSequenceAcquisition lDcamSequenceAcquisition0 =
            new DcamSequenceAcquisition(lDcamDevice0);
    lDcamSequenceAcquisition0.mDebug=true;
    System.out.println("FIRST CAMERA");
    DcamImageSequence lSequence0 = new DcamImageSequence(lDcamDevice0,
            2,
            lWidth,
            lHeight,
            lDepth);



    DcamSequenceAcquisition lDcamSequenceAcquisition1 =
            new DcamSequenceAcquisition(lDcamDevice1);
    lDcamSequenceAcquisition1.mDebug=true;
    System.out.println("SECOND CAMERA");
    DcamImageSequence lSequence1 = new DcamImageSequence(lDcamDevice1,
            2,
            lWidth,
            lHeight,
            lDepth);
    lDcamSequenceAcquisition1.mDebug=true;


    Future<Boolean> lFuture0 = lDcamSequenceAcquisition0.acquireSequenceAsync(0.01, lSequence0);
    Future<Boolean> lFuture1 = lDcamSequenceAcquisition1.acquireSequenceAsync(0.01,lSequence1);

    assertTrue(lFuture0.get());
    assertTrue(lFuture1.get());

    lDcamDevice0.close();
    lDcamDevice1.close();

    assertTrue(DcamLibrary.uninitialize());

  }

  /**
   * Tests sequence acquisition
   * 
   * @throws InterruptedException
   *           NA
   */
  // @Test
  public void testSequenceAcquisitionWithBinning() throws InterruptedException
  {
    int lWidth = 512;
    int lHeight = 512;
    int lDepth = 10;

    assertTrue(DcamLibrary.initialize());

    DcamDevice lDcamDevice = new DcamDevice(0, false, false);
    assertNotNull(lDcamDevice);

    assertTrue(lDcamDevice.open());

    System.out.println(lDcamDevice.getStatus());

    lDcamDevice.printDeviceInfo();

    DcamSequenceAcquisition lDcamSequenceAcquisition =
            new DcamSequenceAcquisition(lDcamDevice);
    lDcamSequenceAcquisition.mDebug=true;

    lDcamDevice.setBinning(2);

    assertEquals(2, lDcamDevice.getBinning());

    DcamImageSequence lSequence = new DcamImageSequence(lDcamDevice,
            2,
            lWidth,
            lHeight,
            lDepth);

    assertTrue(lDcamSequenceAcquisition.acquireSequence(0.01,
            lSequence));

    lDcamDevice.close();

    assertTrue(DcamLibrary.uninitialize());

  }

  /**
   * Tests sequence acquisition
   * 
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void testRepeatedSequenceAcquisition() throws InterruptedException
  {

    assertTrue(DcamLibrary.initialize());

    DcamDevice lDcamDevice = new DcamDevice(0, false, false);
    assertNotNull(lDcamDevice);

    assertTrue(lDcamDevice.open());

    System.out.println(lDcamDevice.getStatus());

    lDcamDevice.printDeviceInfo();

    DcamSequenceAcquisition lDcamSequenceAcquisition =
                                                     new DcamSequenceAcquisition(lDcamDevice);

    for (int i = 0; i < 15; i++)
    {
      System.out.println("SEQUENCE: " + i);

      int lWidth = (int) (512 + Math.random() * 128);
      int lHeight = (int) (512 + Math.random() * 128);
      int lDepth = (int) (10 + Math.random() * 300);
      DcamImageSequence lSequence = new DcamImageSequence(lDcamDevice,
                                                          2,
                                                          lWidth,
                                                          lHeight,
                                                          lDepth);

      System.out.println(lSequence);

      assertTrue(lDcamSequenceAcquisition.acquireSequence(0.01,
                                                          lSequence));

    }

    lDcamDevice.close();

    assertTrue(DcamLibrary.uninitialize());

  }



  /**
   * Tests sequence acquisition
   * 
   * @throws InterruptedException
   *           NA
   */
  @Test
  public void testRepeatedSequenceAcquisitionWithRecycler() throws InterruptedException
  {

    assertTrue(DcamLibrary.initialize());

    DcamDevice lDcamDevice = new DcamDevice(0, false, false);
    assertNotNull(lDcamDevice);

    assertTrue(lDcamDevice.open());

    System.out.println(lDcamDevice.getStatus());

    lDcamDevice.printDeviceInfo();

    DcamSequenceAcquisition lDcamSequenceAcquisition =
                                                     new DcamSequenceAcquisition(lDcamDevice);

    DcamImageSequenceFactory lDcamImageSequenceFactory =
                                                       new DcamImageSequenceFactory();
    BasicRecycler<DcamImageSequence, DcamImageSequenceRequest> lRecycler =
                                                                         new BasicRecycler<>(lDcamImageSequenceFactory,
                                                                                             10);

    for (int i = 0; i < 25; i++)
    {
      System.out.println("SEQUENCE: " + i);

      int lWidth = (int) (512); // + Math.random() * 2
      int lHeight = (int) (512);
      int lDepth = (int) (10);
      DcamImageSequenceRequest lRequest =
                                        DcamImageSequenceRequest.build(lDcamDevice,
                                                                       2,
                                                                       lWidth,
                                                                       lHeight,
                                                                       lDepth,
                                                                       true);

      System.out.println(lRequest);

      DcamImageSequence lSequence = lRecycler.getOrWait(1,
                                                        TimeUnit.SECONDS,
                                                        lRequest);

      assertNotNull(lSequence);

      System.out.println(lSequence);

      assertTrue(lDcamSequenceAcquisition.acquireSequence(0.01,
                                                          lSequence));

      // we are supposed to do something here with the image sequence, and then
      // release it..
      lSequence.release();

    }

    lDcamDevice.close();

    assertTrue(DcamLibrary.uninitialize());

  }

}
