package aptj.demo;

import aptj.APTJDevice;
import aptj.APTJDeviceFactory;
import aptj.APTJDeviceType;
import aptj.bindings.APTLibrary;
import org.bridj.Pointer;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * APTJ Tests
 *
 * @author royer
 */
public class APTJDemo
{
  @Test
  public void testKST101Device() throws Exception
  {

    for (int turn = 0; turn < 100; turn++)
    {

      System.out.println("A");
      APTLibrary.APTInit();
      System.out.println("B");
      APTLibrary.InitHWDevice(26000318);
      System.out.println("C");

      Pointer<Character> lZsModelPointer = Pointer.allocateAlignedArray(Character.class, 1024, 0);
      Pointer<Character> lZsSWVer = Pointer.allocateAlignedArray(Character.class, 1024, 0);
      Pointer<Character> lZsHWNotes = Pointer.allocateAlignedArray(Character.class, 1024, 0);

      APTLibrary.GetHWInfo(26000318, lZsModelPointer, 1024, lZsSWVer, 1024, lZsHWNotes, 1024);
      String lModel = lZsModelPointer.getCString();
      String lSWVer = lZsSWVer.getCString();
      String lHWNotes = lZsHWNotes.getCString();

      System.out.println("Model: " + lModel);
      System.out.println("SWVer: " + lSWVer);
      System.out.println("Model: " + lHWNotes);

      //if( true) return;

      //Pointer<Float> lPositionPointer = Pointer.allocateFloat();
      //APTJDeviceFactory.checkError(APTLibrary.MOT_GetPosition(getSerialNumber(),
      //                                                        lPositionPointer));

      try (APTJDeviceFactory lAPTJLibrary = new APTJDeviceFactory(APTJDeviceType.TST001))
      {

        int lNumberOfDevices = lAPTJLibrary.getNumberOfDevices();

        System.out.println(lNumberOfDevices);

        for (int i = 0; i < lAPTJLibrary.getNumberOfDevices(); i++)
        {
          APTJDevice lDevice = lAPTJLibrary.createDeviceFromIndex(i);
          System.out.println("Serial number: " + lDevice.getSerialNumber());
          System.out.println("Min position: " + lDevice.getMinPosition());
          System.out.println("Max position: " + lDevice.getMaxPosition());
          System.out.println("Cur position: " + lDevice.getCurrentPosition());
        }
      }
      APTLibrary.APTCleanUp();
      Thread.sleep(1000);
    }
  }

  /**
   * Tests TST001 device
   *
   * @throws Exception NA
   */
  @Test
  public void testTST001Device() throws Exception
  {
    try (APTJDeviceFactory lAPTJLibrary = new APTJDeviceFactory(APTJDeviceType.TST001))
    {

      int lNumberOfDevices = lAPTJLibrary.getNumberOfDevices();

      System.out.println(lNumberOfDevices);

      APTJDevice lDevice = lAPTJLibrary.createDeviceFromIndex(0);
      System.out.println("Serial number: " + lDevice.getSerialNumber());
      System.out.println("Min position: " + lDevice.getMinPosition());
      System.out.println("Max position: " + lDevice.getMaxPosition());
      System.out.println("Cur position: " + lDevice.getCurrentPosition());

      System.out.println("home()");
      lDevice.home();
      assertTrue(lDevice.waitWhileMoving(10, 30 * 1000, TimeUnit.MILLISECONDS));
      assertEquals(lDevice.getCurrentPosition(), 0, 0.01);

      System.out.println("moveTo(2)");
      lDevice.moveTo(2);
      assertTrue(lDevice.waitWhileMoving(10, 10 * 1000, TimeUnit.MILLISECONDS));
      assertEquals(lDevice.getCurrentPosition(), 2, 0.01);

      System.out.println("moveBy(-1)");
      lDevice.moveBy(-1);
      assertTrue(lDevice.waitWhileMoving(10, 10 * 1000, TimeUnit.MILLISECONDS));
      assertEquals(lDevice.getCurrentPosition(), 1, 0.01);

      System.out.println("move(1)");
      lDevice.move(1);
      Thread.sleep(20000);
      assertTrue(lDevice.getCurrentPosition() > lDevice.getMaxPosition() / 2);

      System.out.println("moveTo(1)");
      lDevice.moveTo(1);
      assertTrue(lDevice.waitWhileMoving(10, 10 * 1000, TimeUnit.MILLISECONDS));
      assertEquals(lDevice.getCurrentPosition(), 1, 0.01);

      System.out.println("move(-1)");
      lDevice.move(-1);
      Thread.sleep(3000);
      assertTrue(lDevice.getCurrentPosition() < 1);

      System.out.println("home()");
      lDevice.home();
      assertTrue(lDevice.waitWhileMoving(10, 10 * 1000, TimeUnit.MILLISECONDS));
      assertEquals(lDevice.getCurrentPosition(), 0, 0.5);

    }
  }

}
