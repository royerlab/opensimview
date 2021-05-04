package asdk;

import asdk.bindings.ASDKLibrary;
import asdk.bindings.ASDKLibrary.DM;
import org.bridj.Pointer;

import java.io.IOException;

public class AlpaoDeformableMirror implements AutoCloseable
{
  private final Object mLock = new Object();

  private final String mAlpaoDeviceSerialName;
  private Pointer<DM> mDevicePointer;
  private boolean mDebugPrintout = false;

  private Pointer<Double> mRawMirrorShapeVector;
  private Pointer<Double> mRawMirrorShapeSequenceVector;

  public AlpaoDeformableMirror(String pAlpaoDeviceSerialName)
  {
    super();
    mAlpaoDeviceSerialName = pAlpaoDeviceSerialName;
  }

  public boolean open()
  {
    if (mDevicePointer != null) return false;
    final Pointer<Byte> lPointerToSerialNumber = Pointer.pointerToCString(mAlpaoDeviceSerialName);
    if (isDebugPrintout()) System.out.println("ASDKLibrary.asdkInit(...");
    mDevicePointer = ASDKLibrary.asdkInit(lPointerToSerialNumber);
    lPointerToSerialNumber.release();
    if (isDebugPrintout()) printLastError();

    final String lLastErrorString = getLastErrorString();
    if (isError(lLastErrorString)) return false;

    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      @Override
      public void run()
      {
        try
        {
          close();
        } catch (final Throwable e)
        {
          e.printStackTrace();
        }
      }
    });

    return true;
  }

  @Override
  public void close() throws IOException
  {
    synchronized (mLock)
    {
      if (mDevicePointer == null) return;

      if (isDebugPrintout()) System.out.println("ASDKLibrary.asdkRelease(...");
      ASDKLibrary.asdkRelease(mDevicePointer);
      mDevicePointer = null;
      if (isDebugPrintout()) printLastError();

      final String lLastErrorString = getLastErrorString();
      if (isError(lLastErrorString)) throw new AlpaoException("ALPAO:" + lLastErrorString);

    }
  }

  public int getNumberOfActuators()
  {
    final Pointer<Double> lPointerToNumberOfActuators = Pointer.allocateDouble();
    if (isDebugPrintout()) System.out.println("ASDKLibrary.asdkGet(...NbOfActuator...");
    ASDKLibrary.asdkGet(mDevicePointer, Pointer.pointerToCString("NbOfActuator"), lPointerToNumberOfActuators);
    if (isDebugPrintout()) printLastError();
    final int lNumberOfActuators = (int) lPointerToNumberOfActuators.getDouble();
    lPointerToNumberOfActuators.release();
    if (isDebugPrintout()) System.out.println("lNumberOfActuators=" + lNumberOfActuators);

    return lNumberOfActuators;
  }

  public boolean setInputTriggerMode(TriggerMode pTriggerMode)
  {
    if (isDebugPrintout()) System.out.println("ASDKLibrary.asdkSet(...TriggerIn..." + pTriggerMode);
    ASDKLibrary.asdkSet(mDevicePointer, Pointer.pointerToCString("TriggerIn"), pTriggerMode.ordinal());
    if (isDebugPrintout()) printLastError();

    final String lLastErrorString = getLastErrorString();
    if (isError(lLastErrorString)) return false;

    return true;
  }

  public boolean resetDAC()
  {
    if (isDebugPrintout()) System.out.println("ASDKLibrary.asdkSet(...DacReset...1");
    ASDKLibrary.asdkSet(mDevicePointer, Pointer.pointerToCString("DacReset"), 1);
    if (isDebugPrintout()) printLastError();

    final String lLastErrorString = getLastErrorString();
    if (isError(lLastErrorString)) return false;

    return true;
  }

  public boolean setLogPrintLevel(int level)
  {
    if (level < 0 || level > 4)
    {
      if (isDebugPrintout())
      {
        System.out.println("LogPrintLevel " + level + " must be between 0 and 4!");
        return false;
      }
    }

    if (isDebugPrintout()) System.out.println("ASDKLibrary.asdkSet(...LogPrintLevel..." + level);

    ASDKLibrary.asdkSet(mDevicePointer, Pointer.pointerToCString("LogPrintLevel"), level);


    final Pointer<Double> lLogPrintLevel = Pointer.allocateDouble();
    if (isDebugPrintout()) System.out.println("ASDKLibrary.asdkGet(...LogPrintLevel...");
    ASDKLibrary.asdkGet(mDevicePointer, Pointer.pointerToCString("LogPrintLevel"), lLogPrintLevel);

    if (isDebugPrintout()) printLastError();
    final int lNumberOfActuators = (int) lLogPrintLevel.getDouble();
    lLogPrintLevel.release();
    if (isDebugPrintout()) System.out.println("LogPrintLevel=" + lNumberOfActuators);

    if (isDebugPrintout()) printLastError();

    final String lLastErrorString = getLastErrorString();
    if (isError(lLastErrorString)) return false;

    return true;
  }

  public boolean sendFlatMirrorShapeVector()
  {
    final int lMatrixHeightWidth = AlpaoDeformableMirrorsSpecifications.getFullMatrixHeightWidth(getNumberOfActuators());
    final Pointer<Double> lPointerToDoubles = Pointer.pointerToDoubles(new double[lMatrixHeightWidth * lMatrixHeightWidth]);
    sendFullMatrixMirrorShapeVector(lPointerToDoubles);

    return true;
  }

  public boolean sendFullMatrixMirrorShapeVector(double[] pFullMatrixMirrorShapeVector)
  {
    final Pointer<Double> lPointerToDoubles = Pointer.pointerToDoubles(pFullMatrixMirrorShapeVector);
    final boolean lReturnValue = sendFullMatrixMirrorShapeVector(lPointerToDoubles);
    lPointerToDoubles.release();
    return lReturnValue;
  }

  public boolean sendFullMatrixMirrorShapeVector(Pointer<Double> pFullMatrixMirrorShapeVectorDoubleBuffer)
  {
    checkVectorDimensions(pFullMatrixMirrorShapeVectorDoubleBuffer, AlpaoDeformableMirrorsSpecifications.getFullMatrixLength(getNumberOfActuators()));
    final Pointer<Double> lRawMirrorShapeVectorDoubleBuffer = removeNonExistantCornerActuators(pFullMatrixMirrorShapeVectorDoubleBuffer);

    return sendRawMirrorShapeVector(lRawMirrorShapeVectorDoubleBuffer);

  }

  public boolean sendFullMatrixMirrorShapeSequenceVector(Pointer<Double> pFullMatrixMirrorShapeVectorDoubleBuffer, final int pNumberOfPatterns, final int pNumberOfRepeats)
  {
    // TODO: add the same kind of function for the MIRAO, we need a way to do
    // the conversion on sequences too.
    // use: mRawMirrorShapeSequenceVector
    return mDebugPrintout;
  }

  private Pointer<Double> removeNonExistantCornerActuators(Pointer<Double> pSquareMirrorShapeVectorDoubleBuffer)
  {
    checkVectorDimensions(pSquareMirrorShapeVectorDoubleBuffer, AlpaoDeformableMirrorsSpecifications.getFullMatrixLength(getNumberOfActuators()));
    if (mRawMirrorShapeVector == null || mRawMirrorShapeVector.getValidElements() != getNumberOfActuators())
      mRawMirrorShapeVector = Pointer.allocateDoubles(getNumberOfActuators());

    AlpaoDeformableMirrorsSpecifications.convertLayout(getNumberOfActuators(), pSquareMirrorShapeVectorDoubleBuffer, mRawMirrorShapeVector);
    return mRawMirrorShapeVector;
  }

  public boolean sendRawMirrorShapeVector(double[] pMirrorShape)
  {
    final Pointer<Double> lPointerToDoubleArray = Pointer.pointerToDoubles(pMirrorShape);
    final boolean lReturnValue = sendRawMirrorShapeVector(lPointerToDoubleArray);
    lPointerToDoubleArray.release();
    return lReturnValue;
  }

  public boolean sendRawMirrorShapeVector(Pointer<Double> pMirrorShape)
  {
    synchronized (mLock)
    {
      if (isDebugPrintout()) System.out.println("ASDKLibrary.asdkSend(...");
      ASDKLibrary.asdkSend(mDevicePointer, pMirrorShape);
      if (isDebugPrintout()) printLastError();

      final String lLastErrorString = getLastErrorString();
      if (isError(lLastErrorString)) return false;

      return true;
    }
  }

  public boolean sendMirrorShapeSequenceAsynchronously(double[] pMirrorShapeSequence, final int pNumberOfPatterns, final int pNumberOfRepeats)
  {

    final Pointer<Double> lPointerToDoubleArray = Pointer.pointerToDoubles(pMirrorShapeSequence);
    final boolean lReturnValue = sendMirrorShapeSequenceAsynchronously(lPointerToDoubleArray, pNumberOfPatterns, pNumberOfRepeats);
    lPointerToDoubleArray.release();
    return lReturnValue;
  }

  public boolean sendMirrorShapeSequenceAsynchronously(Pointer<Double> pMirrorShapeSequence, final int pNumberOfPatterns, final int pNumberOfRepeats)
  {
    synchronized (mLock)
    {
      if (isDebugPrintout()) System.out.println("ASDKLibrary.asdkSend(...");
      ASDKLibrary.asdkSendPattern(mDevicePointer, pMirrorShapeSequence, pNumberOfPatterns, pNumberOfRepeats);
      if (isDebugPrintout()) printLastError();

      final String lLastErrorString = getLastErrorString();
      if (isError(lLastErrorString)) return false;

      return true;
    }
  }

  private boolean isError(String pErrorString)
  {
    return !pErrorString.toLowerCase().contains("no error");
  }

  public String getLastErrorString()
  {
    synchronized (mLock)
    {
      final Pointer<Integer> errorNo = Pointer.allocateInt();
      final Pointer<Byte> errMsg = Pointer.allocateBytes(256);
      final long errSize = 256;
      ASDKLibrary.asdkGetLastError(errorNo, errMsg, errSize);
      final String lErrorString = new String(errMsg.getBytes());
      errorNo.release();
      errMsg.release();
      return lErrorString;
    }
  }

  public void printLastError()
  {
    synchronized (mLock)
    {
      ASDKLibrary.asdkPrintLastError();
    }
  }

  public boolean isDebugPrintout()
  {
    return mDebugPrintout;
  }

  public void setDebugPrintout(boolean pDebugPrintout)
  {
    mDebugPrintout = pDebugPrintout;
  }

  /**
   * Checks vector dimensions and throw an exception if the length is incorrect.
   *
   * @param pVector               vector to check for correct length (Java double array)
   * @param pExpectedVectorLength expected correct length
   */
  private void checkVectorDimensions(Pointer<Double> pVector, int pExpectedVectorLength)
  {
    if (pVector.getValidElements() != pExpectedVectorLength)
    {
      final String lExceptionMessage = String.format("Provided vector has wrong length %d should be %d", pVector.getValidElements(), pExpectedVectorLength);
      throw new AlpaoException(lExceptionMessage);
    }
  }

}
