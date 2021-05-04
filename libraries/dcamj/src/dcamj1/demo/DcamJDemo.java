package dcamj1.demo;

import dcamj1.DcamAcquisition;
import dcamj1.DcamAcquisition.TriggerType;
import dcamj1.DcamAcquisitionListener;
import dcamj1.DcamFrame;
import dcamj1.utils.StopWatch;
import org.bridj.Pointer;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class DcamJDemo
{

  final static int cNumberOfBuffers = 1000;

  private void getTime(final int pFramesAcquiredUntilNow, final StopWatch lStopWatch)
  {
    final long lElapsedTimeInSeconds = lStopWatch.time(TimeUnit.SECONDS);
    final double lFramerate = (double) pFramesAcquiredUntilNow / lElapsedTimeInSeconds;
    System.out.format("Framerate: %g \n", lFramerate);
  }

  @Test
  public void testDcamAcquisitionOneCamera() throws InterruptedException, IOException
  {

    final DcamAcquisition lDcamAcquisition = new DcamAcquisition(0);

    lDcamAcquisition.addListener(new DcamAcquisitionListener()
    {

      @Override
      public void frameArrived(final DcamAcquisition pDcamAquisition, final long pAbsoluteFrameIndex, final long pArrivalTimeStamp, final long pFrameIndexInBufferList, final DcamFrame pDcamFrame)
      {
        System.out.format("Frame %d in buffer %d arrived at %d \n", pAbsoluteFrameIndex, pFrameIndexInBufferList, pArrivalTimeStamp);
        System.out.format("frameArrived: hashcode= %d index=%d dimensions: (%d,%d) \n ", pDcamFrame.hashCode(), pDcamFrame.getIndex(), pDcamFrame.getWidth(), pDcamFrame.getHeight());
      }
    });

    assertTrue(lDcamAcquisition.open());
    lDcamAcquisition.getProperties().setOutputTriggerToProgrammable();
    assertTrue(lDcamAcquisition.getProperties().setBinning(2));
    assertTrue(lDcamAcquisition.getProperties().setCenteredROI(2048, 2048));

    lDcamAcquisition.startAcquisition();
    Thread.sleep(250);
    lDcamAcquisition.stopAcquisition();
    lDcamAcquisition.close();

  }

  @Test
  public void testDcamAcquisitionTwoCameras() throws InterruptedException, IOException
  {

    final DcamAcquisition lDcamAcquisition0 = new DcamAcquisition(0);
    final DcamAcquisition lDcamAcquisition1 = new DcamAcquisition(1);

    DcamAcquisitionListener lDcamAcquisitionListener = new DcamAcquisitionListener()
    {

      @Override
      public void frameArrived(final DcamAcquisition pDcamAquisition, final long pAbsoluteFrameIndex, final long pArrivalTimeStamp, final long pFrameIndexInBufferList, final DcamFrame pDcamFrame)
      {
        System.out.format("Camera %d \n", pDcamAquisition.getDeviceIndex());
        System.out.format("Frame %d in buffer %d arrived at %d \n", pAbsoluteFrameIndex, pFrameIndexInBufferList, pArrivalTimeStamp);
        System.out.format("frameArrived: hashcode= %d index= %d \n", pDcamFrame.hashCode(), pDcamFrame.getIndex());
      }
    };

    lDcamAcquisition0.addListener(lDcamAcquisitionListener);
    lDcamAcquisition1.addListener(lDcamAcquisitionListener);

    lDcamAcquisition0.open();
    lDcamAcquisition1.open();
    lDcamAcquisition0.getProperties().setOutputTriggerToProgrammable();
    lDcamAcquisition1.getProperties().setOutputTriggerToProgrammable();
    lDcamAcquisition0.setFrameWidthAndHeight(2048, 2048);
    lDcamAcquisition1.setFrameWidthAndHeight(2048, 2048);
    lDcamAcquisition0.startAcquisition();
    lDcamAcquisition1.startAcquisition();
    Thread.sleep(2500);
    lDcamAcquisition0.stopAcquisition();
    lDcamAcquisition1.stopAcquisition();
    lDcamAcquisition0.close();
    lDcamAcquisition1.close();

  }

  @Test
  public void testDcamAcquisitionWithExternalTriggering() throws InterruptedException, IOException
  {

    final DcamAcquisition lDcamAcquisition = new DcamAcquisition(0);
    lDcamAcquisition.setTriggerType(TriggerType.ExternalEdge);
    lDcamAcquisition.setExposureInSeconds(0.001);

    lDcamAcquisition.addListener(new DcamAcquisitionListener()
    {

      @Override
      public void frameArrived(final DcamAcquisition pDcamAquisition, final long pAbsoluteFrameIndex, final long pArrivalTimeStamp, final long pFrameIndexInBuffer, final DcamFrame pDcamFrame)
      {
        System.out.format("Frame %d in buffer %d arrived at %d \n", pAbsoluteFrameIndex, pFrameIndexInBuffer, pArrivalTimeStamp);
      }
    });

    lDcamAcquisition.open();

    System.out.format("Effective exposure is: %g s \n", lDcamAcquisition.getExposureInSeconds());

    lDcamAcquisition.startAcquisition();

    Thread.sleep(5000);
    lDcamAcquisition.stopAcquisition();
    lDcamAcquisition.close();

  }

  static int lDcamFrameCounter;

  @Test
  public void testDcamSequenceAcquisition() throws InterruptedException, IOException
  {

    final DcamAcquisition lDcamAcquisition = new DcamAcquisition(0);

    final int lNumerOfDcamFrames = 4;
    final int lNumberOfIterations = 10;
    final int lNumberOfFramesToCapture = 10;
    final int lImageResolution = 512;

    lDcamAcquisition.setFrameWidthAndHeight(lImageResolution, lImageResolution);
    lDcamAcquisition.setExposureInSeconds(0.0001);
    if (!lDcamAcquisition.open())
    {
      lDcamAcquisition.close();
      return;
    }

    lDcamAcquisition.getProperties().setOutputTriggerToProgrammable();

    final long lBufferCapacity = lDcamAcquisition.getBufferControl().computeTotalRequiredMemoryInBytes(lNumberOfFramesToCapture);
    System.out.format("RequiredMemory is: %d MB \n", lBufferCapacity / 1000000);

    assertTrue(lDcamAcquisition.getBufferControl().allocateInternalBuffers(lNumberOfFramesToCapture));

    final DcamFrame[] lDcamFrameArray = new DcamFrame[lNumerOfDcamFrames];
    for (int i = 0; i < lNumerOfDcamFrames; i++)
      lDcamFrameArray[i] = new DcamFrame(2, lImageResolution, lImageResolution, lNumberOfFramesToCapture);

    lDcamFrameCounter = 0;

    lDcamAcquisition.addListener(new DcamAcquisitionListener()
    {

      @Override
      public void frameArrived(final DcamAcquisition pDcamAquisition, final long pAbsoluteFrameIndex, final long pArrivalTimeStamp, final long pFrameIndexInBuffer, final DcamFrame pDcamFrame)
      {
        System.out.format("Frame %d of depth %d in buffer %d arrived at %d \n", pAbsoluteFrameIndex, pDcamFrame.getDepth(), pFrameIndexInBuffer, pArrivalTimeStamp);/**/
        assertTrue(pDcamFrame.getDepth() != 1);

      }
    });

    System.gc();
    final StopWatch lStopWatch = StopWatch.start();
    for (int i = 0; i < lNumberOfIterations; i++)
    {
      System.out.println("ITERATION=" + i);
      assertTrue(lDcamAcquisition.startAcquisition(false, true, lDcamFrameArray[lDcamFrameCounter]));
      // lDcamAcquisition.stopAcquisition();

      // Thread.sleep(1000);
      lDcamFrameCounter = (lDcamFrameCounter + 1) % lNumerOfDcamFrames;
      /*final DcamFrame lNewDcamFrame = lDcamFrameArray[lDcamFrameCounter];
      lDcamAcquisition.getBufferControl()
      								.attachExternalBuffers(lNewDcamFrame);/**/

    }
    final long lTimeInSeconds = lStopWatch.time(TimeUnit.SECONDS);
    final double lSpeed = lNumberOfIterations * lNumberOfFramesToCapture / (lTimeInSeconds);
    System.out.format("acquisition speed: %g frames/s \n", lSpeed);

    while (lDcamAcquisition.isAcquiring())
    {
      System.out.println(".");
      Thread.sleep(100);
    }

    for (int j = 0; j < lNumerOfDcamFrames; j++)
      for (int i = 0; i < lDcamFrameArray[j].getDepth(); i++)
      {
        final double average = computeAverageInBuffer(lDcamFrameArray[j].getPointerForSinglePlane(i));
        System.out.format("avg=%g \n", average);
        assertTrue(average != 0);
      }

    lDcamAcquisition.close();

  }

  private double computeAverageInBuffer(final Pointer<Byte> pPointer)
  {
    double average = 0;

    final long lLength = pPointer.getValidBytes();
    final double lInverse = 1 / (double) lLength;
    for (int i = 0; i < lLength; i++)
    {
      final byte lByte = pPointer.getByteAtIndex(i);
      average = average + lByte * lInverse;
    }
    return average;
  }

}
