package asdk.bindings.demo;

import asdk.bindings.ASDKLibrary;
import asdk.bindings.ASDKLibrary.COMPL_STAT;
import asdk.bindings.ASDKLibrary.DM;
import org.bridj.IntValuedEnum;
import org.bridj.Pointer;
import org.junit.Test;

import static java.lang.Math.random;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ASDKLibraryDemo
{

  @Test
  public void demo()
  {
    String lSerialName = "BIL118\0";
    Pointer<Byte> lPointerToSerialNumber = Pointer.pointerToCString(lSerialName);
    System.out.println("ASDKLibrary.asdkInit(...");
    Pointer<DM> lDevicePointer = ASDKLibrary.asdkInit(lPointerToSerialNumber);
    ASDKLibrary.asdkPrintLastError();
    assertNotNull(lDevicePointer);


    Pointer<Double> lPointerToNumberOfActuators = Pointer.allocateDouble();
    System.out.println("ASDKLibrary.asdkGet(...");
    ASDKLibrary.asdkGet(lDevicePointer, Pointer.pointerToCString("NbOfActuator"), lPointerToNumberOfActuators);
    ASDKLibrary.asdkPrintLastError();
    int lNumberOfActuators = (int) lPointerToNumberOfActuators.getDouble();
    System.out.println("lNumberOfActuators=" + lNumberOfActuators);
    assertTrue(lNumberOfActuators > 0);

    Pointer<Double> lData = Pointer.allocateDoubles(lNumberOfActuators);
    for (int i = 0; i < lNumberOfActuators; i++)
      lData.set(i, 0.1 * (2 * random() - 1));

    System.out.println("ASDKLibrary.asdkSend(...");
    IntValuedEnum<COMPL_STAT> lAsdkSend = ASDKLibrary.asdkSend(lDevicePointer, lData);
    System.out.println(lAsdkSend);
    ASDKLibrary.asdkPrintLastError();

    System.out.println("ASDKLibrary.asdkRelease(...");
    ASDKLibrary.asdkRelease(lDevicePointer);
    ASDKLibrary.asdkPrintLastError();

  }

}
