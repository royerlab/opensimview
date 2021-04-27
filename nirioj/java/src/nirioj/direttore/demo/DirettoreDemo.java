package nirioj.direttore.demo;

import static org.junit.Assert.assertTrue;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import nirioj.direttore.Direttore;

import org.junit.Test;

public class DirettoreDemo
{

  @Test
  public void testSinus() throws UnknownHostException,
                          SocketException,
                          InterruptedException
  {
    Direttore lDirettore = new Direttore();

    assertTrue(lDirettore.open());
    assertTrue(lDirettore.start());

    final short lNumberOfTimePoints = 2048;
    final int lNumberOfChannels = lDirettore.getNumberOfChannels();
    final int lNumberOfMatrices = 10;
    final double lFrequency = 10;
    final double lAmplitude = 3000;

    // ByteBuffer lByteBuffer = ByteBuffer.allocateDirect(lNumberOfMatrices* 2);

    final IntBuffer lDeltaTimeBuffer = ByteBuffer
                                                 .allocateDirect(lNumberOfMatrices
                                                                 * 4)
                                                 .order(ByteOrder.nativeOrder())
                                                 .asIntBuffer();

    final ByteBuffer lSyncControlByteBuffer =
                                            ByteBuffer.allocateDirect(lNumberOfMatrices
                                                                      * 4)
                                                      .order(ByteOrder.nativeOrder());

    final IntBuffer lSyncControlShortBuffer =
                                            lSyncControlByteBuffer.asIntBuffer();

    final IntBuffer lNumberOfTimePointsBuffer =
                                              ByteBuffer.allocateDirect(lNumberOfMatrices
                                                                        * 4)
                                                        .order(ByteOrder.nativeOrder())
                                                        .asIntBuffer();

    final ShortBuffer lMatrixBuffer = ByteBuffer
                                                .allocateDirect(lNumberOfMatrices
                                                                * lNumberOfChannels
                                                                * lNumberOfTimePoints
                                                                * 2)
                                                .order(ByteOrder.nativeOrder())
                                                .asShortBuffer();

    for (int m = 0; m < lNumberOfMatrices; m++)
    {
      lDeltaTimeBuffer.put(lDirettore.convertMicroSecondsToTicks(3));
      lNumberOfTimePointsBuffer.put(lNumberOfTimePoints);
      lSyncControlShortBuffer.put(1);

      for (int t = 0; t < lNumberOfTimePoints; t++)
      {
        final double lValue = Math.sin(
                                       (2 * Math.PI
                                        / lNumberOfTimePoints)
                                       * t * lFrequency);
        final short lShortValue = (short) (Math.round(lAmplitude
                                                      * lValue));
        for (int c = 0; c < lNumberOfChannels; c++)
        {
          lMatrixBuffer.put(lShortValue);
        }
      }
    }

    // lSyncControlByteBuffer.put((byte) 1);
    // lSyncControlByteBuffer.put((byte) 8);

    for (int i = 0; i < 50000; i++)
    {
      System.out.println("Play #" + i);
      lDirettore.play(lDeltaTimeBuffer,
                      lNumberOfTimePointsBuffer,
                      lSyncControlShortBuffer,
                      lNumberOfMatrices,
                      lMatrixBuffer);

    }
    Thread.sleep(4000);
    // System.out.println("waiting a long time now....");
    // Thread.sleep(100000);

    assertTrue(lDirettore.stop());
    lDirettore.close();
  }

}
