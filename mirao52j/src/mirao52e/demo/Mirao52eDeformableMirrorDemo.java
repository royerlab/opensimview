package mirao52e.demo;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import mirao52e.Mirao52eDeformableMirror;

import org.junit.Test;

public class Mirao52eDeformableMirrorDemo
{

  /**
   * Start the Mirao52 UDP server on the localhost and fire this demo.
   * 
   * @throws IOException
   *           exception
   * @throws InterruptedException
   *           exception
   */
  @Test
  public void demo() throws IOException, InterruptedException
  {
    final Mirao52eDeformableMirror lMirao52eDeformableMirror =
                                                             new Mirao52eDeformableMirror();

    lMirao52eDeformableMirror.open();

    for (int i = 1; i <= 100; i++)
    {
      System.out.println("sending shape i=" + i);
      final double[] lSquareMirrorShapeVector =
                                              generateRandomVector(64);
      assertTrue(lMirao52eDeformableMirror.sendFullMatrixMirrorShapeVector(lSquareMirrorShapeVector));

      Thread.sleep(100);
    }

    lMirao52eDeformableMirror.close();

  }

  /**
   * generates a random shape vector
   * 
   * @param pLength
   * @return
   */
  private double[] generateRandomVector(int pLength)
  {
    final double[] lRawMirrorShapeVector = new double[pLength];

    final double lOffset = 0.01 * (2 * Math.random() - 1);
    for (int i = 0; i < lRawMirrorShapeVector.length; i++)
      lRawMirrorShapeVector[i] =
                               lOffset + 0.1 * (Math.random() - 0.5);
    return lRawMirrorShapeVector;
  }
}
