package clearcontrol.devices.optomech.filterwheels.devices.sutter;

import clearcontrol.com.serial.SerialDevice;
import clearcontrol.core.concurrent.thread.ThreadSleep;
import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.devices.optomech.filterwheels.FilterWheelDeviceInterface;
import clearcontrol.devices.optomech.filterwheels.devices.sutter.adapters.FilterWheelPositionDeviceAdapter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SutterFilterWheelDevice extends SerialDevice implements FilterWheelDeviceInterface
{

  private final Variable<Integer> mFilterPositionVariable, mFilterSpeedVariable;
  private volatile int mCachedPosition;
  private ConcurrentHashMap<Integer, String> mFilterPositionToNameMap;

  public SutterFilterWheelDevice(final int pDeviceIndex)
  {
    this(MachineConfiguration.get().getSerialDevicePort("filterwheel.sutter", pDeviceIndex, "NULL"));

    setName("SutterFilterWheel" + pDeviceIndex);

    for (int i : getValidPositions())
    {

      String lFilterPositionName = MachineConfiguration.get().getStringProperty("filterwheel.sutter." + pDeviceIndex + ".filter" + i, "filter " + i);

      if (lFilterPositionName != null) setPositionName(i, lFilterPositionName);
      else setPositionName(i, "Position" + i);
    }
  }

  public SutterFilterWheelDevice(final String pPortName)
  {
    super("SutterFilterWheel", pPortName, 115200);

    mFilterPositionToNameMap = new ConcurrentHashMap<>();

    final FilterWheelPositionDeviceAdapter lFilterWheelPosition = new FilterWheelPositionDeviceAdapter(this);
    mFilterPositionVariable = addSerialVariable("FilterWheelPosition", lFilterWheelPosition);

    mFilterPositionVariable.addSetListener(new VariableSetListener<Integer>()
    {
      @Override
      public void setEvent(final Integer pCurrentValue, final Integer pNewValue)
      {
        updateCache(pNewValue);
      }

      private void updateCache(final Integer pNewValue)
      {
        mCachedPosition = (int) (pNewValue == null ? 0 : pNewValue.doubleValue());
      }
    });

    mFilterSpeedVariable = new Variable<Integer>("FilterWheelSpeed", 1);

  }

  @Override
  public final Variable<Integer> getPositionVariable()
  {
    return mFilterPositionVariable;
  }

  @Override
  public final Variable<Integer> getSpeedVariable()
  {
    return mFilterSpeedVariable;
  }

  @Override
  public int getPosition()
  {
    return mFilterPositionVariable.get();
  }

  public int getCachedPosition()
  {
    return mCachedPosition;
  }

  @Override
  public void setPosition(final int pPosition)
  {
    mFilterPositionVariable.set(pPosition);
  }

  @Override
  public int getSpeed()
  {
    return mFilterSpeedVariable.get();
  }

  public int getCachedSpeed()
  {
    return mFilterSpeedVariable.get();
  }

  @Override
  public void setSpeed(final int pSpeed)
  {
    mFilterSpeedVariable.set(pSpeed);
  }

  @Override
  public boolean open()
  {
    final boolean lIsOpened = super.open();

    ThreadSleep.sleep(5, TimeUnit.SECONDS);
    setSpeed(6);
    setPosition(0);

    return lIsOpened;
  }

  @Override
  public int[] getValidPositions()
  {
    return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
  }

  @Override
  public void setPositionName(int pPositionIndex, String pPositionName)
  {
    mFilterPositionToNameMap.put(pPositionIndex, pPositionName);

  }

  @Override
  public String getPositionName(int pPositionIndex)
  {
    return mFilterPositionToNameMap.get(pPositionIndex);
  }

}
