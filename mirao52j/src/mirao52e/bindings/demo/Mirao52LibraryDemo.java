package mirao52e.bindings.demo;

import static java.lang.Math.random;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import mirao52e.bindings.Mirao52eLibrary;

import org.bridj.Pointer;
import org.junit.Test;

public class Mirao52LibraryDemo
{

  @Test
  public void testOpenClose() throws InterruptedException
  {
    Pointer<Integer> status = Pointer.allocateInt();
    byte lMroOpen = Mirao52eLibrary.mroOpen(status);
    if (lMroOpen == Mirao52eLibrary.MRO_FALSE)
    {
      System.err.println("Could not open status=" + status.getByte());
      fail();
    }

    double[] lCommandDoubleArray = new double[52];

    for (int i = 0; i < 100; i++)
    {
      System.out.println("sending shape " + i);
      for (int j = 0; j < 52; j++)
        lCommandDoubleArray[j] = 0.01 * (random() - 0.5);
      Pointer<Double> lCommand =
                               Pointer.pointerToDoubles(lCommandDoubleArray);
      assertTrue(Mirao52eLibrary.mroApplyCommand(lCommand,
                                                 (byte) Mirao52eLibrary.MRO_TRUE,
                                                 null) == Mirao52eLibrary.MRO_TRUE);
      Thread.sleep(100);
    }

    byte lMroClose = Mirao52eLibrary.mroClose(status);
    if (lMroOpen == Mirao52eLibrary.MRO_FALSE)
    {
      System.err.println("Could not close status="
                         + status.getByte());
      fail();
    }
  }

}
