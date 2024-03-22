package clearcontrol.devices.optomech.filterwheels.devices.sutter.adapters;

import clearcontrol.com.serial.adapters.SerialTextDeviceAdapter;
import clearcontrol.devices.optomech.filterwheels.devices.sutter.SutterFilterWheelDevice;

import java.util.Scanner;

public class FilterWheelPositionDeviceAdapter implements SerialTextDeviceAdapter<Integer>
{

  private SutterFilterWheelDevice mSutterFilterWheelDevice;

  public FilterWheelPositionDeviceAdapter(final SutterFilterWheelDevice pSutterFilterWheelDevice)
  {
    mSutterFilterWheelDevice = pSutterFilterWheelDevice;
  }

  @Override
  public byte[] getGetValueCommandMessage()
  {
    String lMessage = "#g\n";
    return lMessage.getBytes();
  }

  @Override
  public Integer parseValue(final byte[] pMessage)
  {
    Scanner lScanner = new Scanner(String.valueOf(pMessage));
    if (lScanner.hasNextInt()) return lScanner.nextInt();
    else return null;
  }

  @Override
  public long getGetValueReturnWaitTimeInMilliseconds()
  {
    return 0;
  }

  @Override
  public boolean hasResponseForGet()
  {
    return true;
  }

  @Override
  public boolean purgeAfterGet()
  {
    return true;
  }

  @Override
  public Integer clampSetValue(Integer pNewValue)
  {
    int lNumberOfvalidPositions = mSutterFilterWheelDevice.getValidPositions().length;
    return pNewValue % lNumberOfvalidPositions;
  }

  @Override
  public byte[] getSetValueCommandMessage(final Integer pOldPosition, final Integer pNewPosition)
  {
    int lSpeed = mSutterFilterWheelDevice.getSpeed();
    lSpeed = lSpeed<0? 0 : lSpeed;
    lSpeed = lSpeed>=7? 7 : lSpeed;

    // Make sure that higher numbers are faster speeds:
    lSpeed = 7-lSpeed;

    int lPosition = pNewPosition.intValue();
    lPosition = lPosition<0? 0 : lPosition;
    lPosition = lPosition>=9? 9 : lPosition;

    byte[] lMessage = new byte[2];
    lMessage[0] = (byte)(0+lSpeed*16+lPosition);
    lMessage[1] = (byte)(128+lSpeed*16+lPosition);

    return lMessage;
  }

  @Override
  public long getSetValueReturnWaitTimeInMilliseconds()
  {
    return 0;
  }

  @Override
  public boolean hasResponseForSet()
  {
    return false;
  }

  @Override
  public boolean checkAcknowledgementSetValueReturnMessage(final byte[] pMessage)
  {
    return true;
  }

  @Override
  public boolean purgeAfterSet()
  {
    return true;
  }

  @Override
  public Character getGetValueReturnMessageTerminationCharacter()
  {
    return '\n';
  }

  @Override
  public Character getSetValueReturnMessageTerminationCharacter()
  {
    return '\n';
  }
}
