package ecc100.bindings.demo;

import ecc100.bindings.EccInfo;
import ecc100.bindings.EccLibrary;
import org.bridj.Pointer;
import org.junit.Test;

import static java.lang.Math.abs;
import static java.lang.Math.random;

public class Ecc100LibraryDemo
{

  @Test
  public void demo() throws InterruptedException
  {
    System.out.println("BEGIN");

    // Thread.sleep(1000);
    Pointer<Pointer<EccInfo>> info = Pointer.allocatePointers(EccInfo.class, 4);
    for (int i = 0; i < 4; i++)
    {
      info.set(Pointer.allocate(EccInfo.class));
    }

    int lNumberOfDevicesFound = EccLibrary.ECC_Check(info);

    System.out.println("lNumberOfDevicesFound=" + lNumberOfDevicesFound);

    for (int i = 0; i < lNumberOfDevicesFound; i++)
    {
      Pointer<EccInfo> lPointer = info.get(i);

      if (lPointer != null)
      {
        EccInfo lEccInfo = lPointer.get();
        System.out.println("lEccInfo" + i + "->" + lEccInfo);
      } else
      {
        System.out.println("NULL");
      }
    } /**/

    Pointer<Integer> lPointerToDeviceHandle = Pointer.allocate(Integer.class);
    Pointer<Integer> lPointerToDeviceId = Pointer.allocateInt();
    Pointer<Integer> lPointerToIsLocked = Pointer.allocateInt();

    for (int i = 0; i < lNumberOfDevicesFound; i++)
    {
      EccLibrary.ECC_getDeviceInfo(i, lPointerToDeviceId, lPointerToIsLocked);

      System.out.println("devId=" + lPointerToDeviceId.getInt() + " locked=" + lPointerToIsLocked.getInt());

      connectToDevice(lPointerToDeviceHandle, i);
      printActorName(lPointerToDeviceHandle, i);
      printActorType(lPointerToDeviceHandle, i);
      printCurrentPosition(lPointerToDeviceHandle, 2);

      reset(lPointerToDeviceHandle, i);

      boolean lEnable = true;

      controlOutputRelais(lPointerToDeviceHandle, i, lEnable);

      int lVoltagInMilliVolts = 30000;

      setVoltage(lPointerToDeviceHandle, i, lVoltagInMilliVolts);

      int lVoltagInMillihertz = 440000;

      setFrequency(lPointerToDeviceHandle, i, lVoltagInMillihertz);

      stopOnEOT(lPointerToDeviceHandle, i, true);

      boolean lForward = true;

      for (int step = 0; step < 1000; step++)
      {
        singleStep(lPointerToDeviceHandle, i, lForward);
      }

      Thread.sleep(1000);

      continuous(lPointerToDeviceHandle, i, lForward);

      Thread.sleep(5000);

      int lTargetPosition = (int) (150000 * (random() - 0.5));
      System.out.println("lTargetPosition=" + lTargetPosition);

      controlAproachToTargetPosition(lPointerToDeviceHandle, i, lEnable);

      setTargetPosition(lPointerToDeviceHandle, i, lTargetPosition);
      printCurrentPosition(lPointerToDeviceHandle, i);

      System.out.print("Waiting for movement to finish...");
      int epsilon = 1000;
      while (isMoving(lPointerToDeviceHandle, i) && abs(getCurrentPosition(lPointerToDeviceHandle, i) - lTargetPosition) > epsilon)
      {
        System.out.println(isMovingState(lPointerToDeviceHandle, i));
        printCurrentPosition(lPointerToDeviceHandle, i);
        Thread.sleep(10);
      }

      System.out.println("done");

      EccLibrary.ECC_Close(lPointerToDeviceHandle.getInt());
    }

  }

  private int isMovingState(Pointer<Integer> pPointerToDeviceHandle, int i)
  {
    Pointer<Integer> lIsMovingState = Pointer.allocateInt();
    EccLibrary.ECC_getStatusMoving(pPointerToDeviceHandle.getInt(), i, lIsMovingState);
    // printLastError(pPointerToDeviceHandle, i);

    return lIsMovingState.getInt();
  }

  private boolean isMoving(Pointer<Integer> pPointerToDeviceHandle, int i)
  {
    int lMovingState = isMovingState(pPointerToDeviceHandle, i);

    return lMovingState == 1;
  }

  private void singleStep(Pointer<Integer> lPointerToDeviceHandle, int i, boolean lForward)
  {
    EccLibrary.ECC_setSingleStep(lPointerToDeviceHandle.getInt(), i, lForward ? 0 : 1);
  }

  private void reset(Pointer<Integer> lPointerToDeviceHandle, int i)
  {
    EccLibrary.ECC_setReset(lPointerToDeviceHandle.getInt(), i);
    printLastError(lPointerToDeviceHandle, i);
  }

  private void setTargetPosition(Pointer<Integer> lPointerToDeviceHandle, int i, int lTargetPosition)
  {
    Pointer<Integer> lPointerToTarget = Pointer.allocateInt();
    lPointerToTarget.set(lTargetPosition);
    EccLibrary.ECC_controlTargetPosition(lPointerToDeviceHandle.getInt(), i, lPointerToTarget, 1);
    printLastError(lPointerToDeviceHandle, i);
  }

  private void continuous(Pointer<Integer> lPointerToDeviceHandle, int i, boolean pForward)
  {
    Pointer<Integer> lPointerEnable3 = Pointer.allocateInt();
    lPointerEnable3.set(1);
    if (pForward) EccLibrary.ECC_controlContinousFwd(lPointerToDeviceHandle.getInt(), i, lPointerEnable3, 1);
    else EccLibrary.ECC_controlContinousBkwd(lPointerToDeviceHandle.getInt(), i, lPointerEnable3, 1);
    printLastError(lPointerToDeviceHandle, i);
  }

  private void setFrequency(Pointer<Integer> lPointerToDeviceHandle, int i, int lVoltagInMillihertz)
  {
    Pointer<Integer> lPointerToFrequency = Pointer.allocateInt();
    lPointerToFrequency.set(lVoltagInMillihertz);
    EccLibrary.ECC_controlFrequency(lPointerToDeviceHandle.getInt(), i, lPointerToFrequency, 1);
    printLastError(lPointerToDeviceHandle, i);
  }

  private void setVoltage(Pointer<Integer> lPointerToDeviceHandle, int i, int lVoltagInMilliVolts)
  {
    Pointer<Integer> lPointerToAmplitude = Pointer.allocateInt();
    lPointerToAmplitude.set(lVoltagInMilliVolts);
    EccLibrary.ECC_controlAmplitude(lPointerToDeviceHandle.getInt(), i, lPointerToAmplitude, 1);
    printLastError(lPointerToDeviceHandle, i);
  }

  private void stopOnEOT(Pointer<Integer> lPointerToDeviceHandle, int i, boolean pStop)
  {
    Pointer<Integer> lPointerToStopOnEOT = Pointer.allocateInt();
    lPointerToStopOnEOT.set(pStop ? 1 : 0);
    EccLibrary.ECC_controlEotOutputDeactive(lPointerToDeviceHandle.getInt(), i, lPointerToStopOnEOT, 1);
    printLastError(lPointerToDeviceHandle, i);
  }

  private void controlAproachToTargetPosition(Pointer<Integer> lPointerToDeviceHandle, int i, boolean lEnable)
  {
    Pointer<Integer> lPointerEnable2 = Pointer.allocateInt();
    lPointerEnable2.set(lEnable ? 1 : 0);
    EccLibrary.ECC_controlMove(lPointerToDeviceHandle.getInt(), i, lPointerEnable2, 1);
    printLastError(lPointerToDeviceHandle, i);
  }

  private void controlOutputRelais(Pointer<Integer> lPointerToDeviceHandle, int i, boolean lEnable)
  {
    Pointer<Integer> lPointerEnable1 = Pointer.allocateInt();
    lPointerEnable1.set(lEnable ? 1 : 0);
    EccLibrary.ECC_controlOutput(lPointerToDeviceHandle.getInt(), i, lPointerEnable1, 1);
    printLastError(lPointerToDeviceHandle, i);
  }

  private void connectToDevice(Pointer<Integer> lPointerToDeviceHandle, int i)
  {
    EccLibrary.ECC_Connect(i, lPointerToDeviceHandle);
    System.out.println("deviceHandle=" + lPointerToDeviceHandle.getInt());
  }

  private void printActorName(Pointer<Integer> lPointerToDeviceHandle, int i)
  {
    Pointer<Byte> lActorName = Pointer.allocateBytes(128);
    EccLibrary.ECC_getActorName(lPointerToDeviceHandle.getInt(), i, lActorName);
    System.out.println("ECC_getActorName ->" + new String(lActorName.getBytes()));
    printLastError(lPointerToDeviceHandle, i);
  }

  private void printActorType(Pointer<Integer> lPointerToDeviceHandle, int i)
  {
    Pointer<Integer> lActorType = Pointer.allocateInt();
    EccLibrary.ECC_getActorType(lPointerToDeviceHandle.getInt(), i, (Pointer) lActorType);
    System.out.println("ECC_getActorType ->" + lActorType.getInt());
    printLastError(lPointerToDeviceHandle, i);
  }

  private int getCurrentPosition(Pointer<Integer> lPointerToDeviceHandle, int i)
  {
    Pointer<Integer> lCurrentPosition = Pointer.allocateInt();
    EccLibrary.ECC_getPosition(lPointerToDeviceHandle.getInt(), i, lCurrentPosition);
    return lCurrentPosition.getInt();
  }

  private void printCurrentPosition(Pointer<Integer> lPointerToDeviceHandle, int i)
  {
    System.out.println("ECC_getPosition ->" + getCurrentPosition(lPointerToDeviceHandle, i));
  }

  private void printLastError(Pointer<Integer> lPointerToDeviceHandle, int i)
  {
    Pointer<Integer> lLastError = Pointer.allocateInt();
    EccLibrary.ECC_getStatusError(lPointerToDeviceHandle.getInt(), i, lLastError);
    System.out.println("ECC_getStatusError ->" + lLastError.getInt());
  }

}
