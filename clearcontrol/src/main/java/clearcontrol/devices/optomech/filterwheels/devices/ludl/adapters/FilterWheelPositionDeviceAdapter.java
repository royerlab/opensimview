package clearcontrol.devices.optomech.filterwheels.devices.ludl.adapters;

import clearcontrol.com.serial.adapters.SerialDeviceAdapterAdapter;
import clearcontrol.com.serial.adapters.SerialTextDeviceAdapter;
import clearcontrol.devices.optomech.filterwheels.devices.ludl.LudlFilterWheelDevice;

import java.util.Scanner;

public class FilterWheelPositionDeviceAdapter implements SerialTextDeviceAdapter<Integer>
{

  private LudlFilterWheelDevice mLudlFilterWheelDevice;

  public FilterWheelPositionDeviceAdapter(final LudlFilterWheelDevice pLudlFilterWheelDevice)
  {

    mLudlFilterWheelDevice = pLudlFilterWheelDevice;
  }

  @Override public byte[] getGetValueCommandMessage()
  {
    String lMessage = "#g\n";
    return lMessage.getBytes();
  }

  @Override
  public Integer parseValue(final byte[] pMessage)
  {
    Scanner lScanner = new Scanner(String.valueOf(pMessage));
    if (lScanner.hasNextInt())
      return lScanner.nextInt();
    else return null;
  }

  @Override public long getGetValueReturnWaitTimeInMilliseconds()
  {
    return 0;
  }

  @Override public boolean hasResponseForGet()
  {
    return true;
  }

  @Override public boolean purgeAfterGet()
  {
    return true;
  }

  @Override public Integer clampSetValue(Integer pNewValue)
  {
    int lNumberOfvalidPositions = mLudlFilterWheelDevice.getValidPositions().length;
    return pNewValue%lNumberOfvalidPositions;
  }

  @Override
  public byte[] getSetValueCommandMessage(final Integer pOldPosition,
                                          final Integer pNewPosition)
  {
    String lMessage = "#s"+pNewPosition.intValue()+"\n";
    return lMessage.getBytes();
  }

  @Override public long getSetValueReturnWaitTimeInMilliseconds()
  {
    return 0;
  }

  @Override public boolean hasResponseForSet()
  {
    return true;
  }

  @Override
  public boolean checkAcknowledgementSetValueReturnMessage(final byte[] pMessage)
  {
    String lAcknowledgments =  new String(pMessage);
    return lAcknowledgments.contains("done!");
  }

  @Override public boolean purgeAfterSet()
  {
    return true;
  }

  @Override public Character getGetValueReturnMessageTerminationCharacter()
  {
    return '\n';
  }

  @Override public Character getSetValueReturnMessageTerminationCharacter()
  {
    return '\n';
  }
}
