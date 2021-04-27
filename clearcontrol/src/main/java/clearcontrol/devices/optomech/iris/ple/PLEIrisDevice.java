package clearcontrol.devices.optomech.iris.ple;

import clearcontrol.com.serial.SerialDevice;
import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.devices.optomech.filterwheels.FilterWheelDeviceInterface;
import clearcontrol.devices.optomech.filterwheels.devices.fli.adapters.FilterWheelPositionDeviceAdapter;
import clearcontrol.devices.optomech.filterwheels.devices.fli.adapters.FilterWheelSpeedDeviceAdapter;

import java.util.concurrent.ConcurrentHashMap;

public class PLEIrisDevice extends SerialDevice
{

//  private final Variable<Float> mIrisPositionVariable;


  public PLEIrisDevice(final int pDeviceIndex)
  {
    this(MachineConfiguration.get().getSerialDevicePort(
                                                        "filterwheel.fli",
                                                        pDeviceIndex,
                                                        "NULL"));

  }

  public PLEIrisDevice(final String pPortName)
  {
    super("FLIFilterWheel", pPortName, 9600);


//    final FilterWheelPositionDeviceAdapter lFilterWheelPosition =
//                                                                new FilterWheelPositionDeviceAdapter(this);
//    mIrisPositionVariable = addSerialVariable("FilterWheelPosition",
//                                                lFilterWheelPosition);



  }

//  @Override
//  public final Variable<Float> getPositionVariable()
//  {
//    return mIrisPositionVariable;
//  }
//
//  @Override
//  public final Variable<Float> getSpeedVariable()
//  {
//    return mIrisPositionVariable;
//  }
//
//  @Override
//  public float getPosition()
//  {
//    return mIrisPositionVariable.get();
//  }
//
//
//  @Override
//  public void setPosition(final int pPosition)
//  {
//    mIrisPositionVariable.set(pPosition);
//  }
//
//  @Override
//  public boolean open()
//  {
//    final boolean lIsOpened = super.open();
//    // TODO: put here initialisation commands.
//    // if(lIsOpened)
//    //  sendCommand();
//    return lIsOpened;
//  }



}
