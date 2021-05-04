package clearcontrol.devices.optomech.filterwheels.devices.ludl;

import clearcontrol.com.serial.SerialDevice;
import clearcontrol.core.concurrent.thread.ThreadSleep;
import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.devices.optomech.filterwheels.FilterWheelDeviceInterface;
import clearcontrol.devices.optomech.filterwheels.devices.ludl.adapters.FilterWheelPositionDeviceAdapter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class LudlFilterWheelDevice extends SerialDevice implements FilterWheelDeviceInterface
{

  private final Variable<Integer> mFilterPositionVariable, mFilterSpeedVariable;
  private volatile int mCachedPosition;
  private ConcurrentHashMap<Integer, String> mFilterPositionToNameMap;

  public LudlFilterWheelDevice(final int pDeviceIndex)
  {
    this(MachineConfiguration.get().getSerialDevicePort("filterwheel.ludl", pDeviceIndex, "NULL"));

    setName("LudlFilterWheel" + pDeviceIndex);

    for (int i : getValidPositions())
    {

      String lFilterPositionName = MachineConfiguration.get().getStringProperty("filterwheel.ludl." + pDeviceIndex + ".filter" + i, "filter " + i);

      if (lFilterPositionName != null) setPositionName(i, lFilterPositionName);
      else setPositionName(i, "Position" + i);
    }
  }

  public LudlFilterWheelDevice(final String pPortName)
  {
    super("LudlFilterWheel", pPortName, 1000000);

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
    setPosition(0);

    return lIsOpened;
  }

  @Override
  public int[] getValidPositions()
  {
    return new int[]{0, 1, 2, 3, 4, 5};
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
