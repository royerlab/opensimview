package ecc100;

import static java.lang.Math.abs;

import java.util.concurrent.TimeUnit;

import ecc100.bindings.EccLibrary;

import org.bridj.Pointer;

public class ECC100Axis
{
  private static final double cGoToEpsilon = 10;
  private Pointer<Integer> mPointerToDeviceHandle =
                                                  Pointer.allocateInt();
  private int mAxisIndex;
  private boolean mLocked = false;
  private volatile double mLastTargetPositionInMicrons;
  private volatile double pLastEpsilonInMicrons;

  public ECC100Axis(ECC100Controller pECC100Controller,
                    int pDeviceIndex,
                    int pAxisIndex)
  {
    super();

    mPointerToDeviceHandle.set(pECC100Controller.getControllerDeviceHandle(pDeviceIndex));
    mAxisIndex = pAxisIndex;
    // stopOnEOT(true);
    getReferencePosition();
  }

  private int isMovingState(Pointer<Integer> pPointerToDeviceHandle,
                            int i)
  {
    Pointer<Integer> lIsMovingState = Pointer.allocateInt();
    EccLibrary.ECC_getStatusMoving(pPointerToDeviceHandle.getInt(),
                                   i,
                                   lIsMovingState);
    int lInt = lIsMovingState.getInt();
    lIsMovingState.release();

    return lInt;
  }

  public boolean isMoving()
  {
    int lMovingState = isMovingState(mPointerToDeviceHandle,
                                     mAxisIndex);

    return lMovingState == 1;
  }

  public boolean isPending()
  {
    int lMovingState = isMovingState(mPointerToDeviceHandle,
                                     mAxisIndex);

    return lMovingState == 2;
  }

  public void singleStep(boolean lForward)
  {
    EccLibrary.ECC_setSingleStep(mPointerToDeviceHandle.getInt(),
                                 mAxisIndex,
                                 lForward ? 0 : 1);
  }

  public void reset()
  {
    EccLibrary.ECC_setReset(mPointerToDeviceHandle.getInt(),
                            mAxisIndex);
    printLastError();
  }

  public void home()
  {
    getReferencePosition();
    goToPosition(0, cGoToEpsilon);
  }

  public void enable()
  {
    controlAproachToTargetPosition(true);
    controlOutputRelais(true);
  }

  public boolean isReady()
  {
    return !isLocked() && !isPending();
  }

  public void stop()
  {
    controlAproachToTargetPosition(false);
  }

  public void goToPosition(double pTargetPositionInMicrons,
                           double pEpsilonInMicrons)
  {
    enable();
    setTargetPosition(pTargetPositionInMicrons);
  }

  public void goToPositionAndWait(double pTargetPositionInMicrons)
  {
    goToPositionAndWait(pTargetPositionInMicrons,
                        cGoToEpsilon,
                        1,
                        TimeUnit.MINUTES);
  }

  public boolean goToPositionAndWait(double pTargetPositionInMicrons,
                                     double pEpsilonInMicrons,
                                     long pTimeOut,
                                     TimeUnit pTimeUnit)
  {
    enable();
    setTargetPosition(pTargetPositionInMicrons);
    mLastTargetPositionInMicrons = pTargetPositionInMicrons;
    pLastEpsilonInMicrons = pEpsilonInMicrons;
    return waitToArriveAt(pTargetPositionInMicrons,
                          pEpsilonInMicrons,
                          pTimeOut,
                          pTimeUnit);
  }

  public boolean hasArrived()
  {
    return abs(getCurrentPosition()
               - mLastTargetPositionInMicrons) < pLastEpsilonInMicrons;
  }

  private boolean waitToArriveAt(double pTargetPositionInMicrons,
                                 double pEpsilonInMicrons,
                                 long pTimeOut,
                                 TimeUnit pTimeUnit)
  {
    if (isLocked())
      return false;

    long lDeadLine = System.nanoTime()
                     + TimeUnit.NANOSECONDS.convert(pTimeOut,
                                                    pTimeUnit);
    while (!isReady() && !hasArrived())
    {
      try
      {
        /*System.out.println("isMoving=" + isMoving());
        System.out.println("getCurrentPosition=" + getCurrentPosition());
        System.out.println("pTargetPosition=" + pTargetPosition);/**/
        Thread.sleep(100);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
      if (System.nanoTime() > lDeadLine)
        return false;
    }
    return true;
  }

  public void setTargetPosition(double lTargetPositionInMicrons)
  {
    Pointer<Integer> lPointerToTarget = Pointer.allocateInt();
    lPointerToTarget.set((int) (lTargetPositionInMicrons * 1000
                                + getReferencePosition() * 1000));
    EccLibrary.ECC_controlTargetPosition(mPointerToDeviceHandle.getInt(),
                                         mAxisIndex,
                                         lPointerToTarget,
                                         1);
    lPointerToTarget.release();
    printLastError();
  }

  public void continuous(boolean pEnable, boolean pForward)
  {
    Pointer<Integer> lPointerEnable = Pointer.allocateInt();
    lPointerEnable.set(pEnable ? 1 : 0);
    if (pForward)
      EccLibrary.ECC_controlContinousFwd(mPointerToDeviceHandle.getInt(),
                                         mAxisIndex,
                                         lPointerEnable,
                                         1);
    else
      EccLibrary.ECC_controlContinousBkwd(mPointerToDeviceHandle.getInt(),
                                          mAxisIndex,
                                          lPointerEnable,
                                          1);
    lPointerEnable.release();
    printLastError();
  }

  public void setFrequency(int lVoltagInMillihertz)
  {
    Pointer<Integer> lPointerToFrequency = Pointer.allocateInt();
    lPointerToFrequency.set(lVoltagInMillihertz);
    EccLibrary.ECC_controlFrequency(mPointerToDeviceHandle.getInt(),
                                    mAxisIndex,
                                    lPointerToFrequency,
                                    1);
    lPointerToFrequency.release();
    printLastError();
  }

  public void setVoltage(int lVoltagInMilliVolts)
  {
    Pointer<Integer> lPointerToAmplitude = Pointer.allocateInt();
    lPointerToAmplitude.set(lVoltagInMilliVolts);
    EccLibrary.ECC_controlAmplitude(mPointerToDeviceHandle.getInt(),
                                    mAxisIndex,
                                    lPointerToAmplitude,
                                    1);
    lPointerToAmplitude.release();
    printLastError();
  }

  public void stopOnEOT(boolean pStop)
  {
    Pointer<Integer> lPointerToStopOnEOT = Pointer.allocateInt();
    lPointerToStopOnEOT.set(pStop ? 1 : 0);
    EccLibrary.ECC_controlEotOutputDeactive(mPointerToDeviceHandle.getInt(),
                                            mAxisIndex,
                                            lPointerToStopOnEOT,
                                            1);
    lPointerToStopOnEOT.release();
    printLastError();
  }

  public void controlAproachToTargetPosition(boolean lEnable)
  {
    Pointer<Integer> lPointerEnable = Pointer.allocateInt();
    lPointerEnable.set(lEnable ? 1 : 0);
    EccLibrary.ECC_controlMove(mPointerToDeviceHandle.getInt(),
                               mAxisIndex,
                               lPointerEnable,
                               1);
    lPointerEnable.release();
    printLastError();
  }

  public void controlOutputRelais(boolean lEnable)
  {
    Pointer<Integer> lPointerEnable = Pointer.allocateInt();
    lPointerEnable.set(lEnable ? 1 : 0);
    EccLibrary.ECC_controlOutput(mPointerToDeviceHandle.getInt(),
                                 mAxisIndex,
                                 lPointerEnable,
                                 1);
    lPointerEnable.release();
    printLastError();
  }

  public String getActorName()
  {
    Pointer<Byte> lActorName = Pointer.allocateBytes(128);
    EccLibrary.ECC_getActorName(mPointerToDeviceHandle.getInt(),
                                mAxisIndex,
                                lActorName);
    printLastError();
    String lString = new String(lActorName.getBytes());
    lActorName.release();
    return lString;
  }

  @SuppressWarnings("unchecked")
  public int getActorType()
  {
    Pointer<Integer> lActorType = Pointer.allocateInt();
    EccLibrary.ECC_getActorType(mPointerToDeviceHandle.getInt(),
                                mAxisIndex,
                                (Pointer) lActorType);
    printLastError();
    int lActorTypeInt = lActorType.getInt();
    lActorType.release();
    return lActorTypeInt;
  }

  public double getCurrentPosition()
  {
    Pointer<Integer> lCurrentPosition = Pointer.allocateInt();
    EccLibrary.ECC_getPosition(mPointerToDeviceHandle.getInt(),
                               mAxisIndex,
                               lCurrentPosition);
    double lCurrentPositionInt = lCurrentPosition.getInt() * 0.001
                                 - getReferencePosition();
    lCurrentPosition.release();
    return lCurrentPositionInt;
  }

  public double getReferencePosition()
  {
    if (!isReferencePositionValid())
      return 0;

    Pointer<Integer> lReferencePosition = Pointer.allocateInt();
    EccLibrary.ECC_getReferencePosition(mPointerToDeviceHandle.getInt(),
                                        mAxisIndex,
                                        lReferencePosition);
    int lReferencePositionInt = lReferencePosition.getInt();
    double lReferencePositionInMicrons =
                                       lReferencePositionInt * 0.001;
    lReferencePosition.release();
    return lReferencePositionInMicrons;
  }

  public boolean isReferencePositionValid()
  {
    Pointer<Integer> lReferencePositionIsValid =
                                               Pointer.allocateInt();
    EccLibrary.ECC_getStatusReference(mPointerToDeviceHandle.getInt(),
                                      mAxisIndex,
                                      lReferencePositionIsValid);
    int lReferencePositionIsValidInt =
                                     lReferencePositionIsValid.getInt();
    lReferencePositionIsValid.release();
    return lReferencePositionIsValidInt > 0;
  }

  public void printLastError()
  {
    Pointer<Integer> lLastError = Pointer.allocateInt();
    EccLibrary.ECC_getStatusError(mPointerToDeviceHandle.getInt(),
                                  mAxisIndex,
                                  lLastError);
    int lLastErrorInt = lLastError.getInt();
    if (lLastErrorInt != 0)
    {
      System.out.println("ECC_getStatusError ->" + lLastErrorInt);
    }
  }

  @Override
  public String toString()
  {
    return "ECC100Axis [mDeviceIndex=" + mAxisIndex
           + ", mAxisIndex="
           + mAxisIndex
           + "]";
  }

  public boolean isLocked()
  {
    return mLocked;
  }

  public void setLocked(boolean pIsLocked)
  {
    mLocked = pIsLocked;
  }

}
