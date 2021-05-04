package clearcontrol.postprocessing.measurements;

import clearcontrol.warehouse.DataWarehouse;
import clearcontrol.warehouse.containers.DataContainerBase;
import clearcontrol.stack.StackInterface;

/**
 * TimeStampContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class TimeStampContainer extends DataContainerBase
{

  private final long mTimeStampInNanoSeconds;

  public TimeStampContainer(long pTimepoint, long pTimeStampInNanoSeconds)
  {
    super(pTimepoint);

    mTimeStampInNanoSeconds = pTimeStampInNanoSeconds;
  }

  @Override
  public boolean isDataComplete()
  {
    return true;
  }

  @Override
  public void dispose()
  {

  }

  public long getTimeStampInNanoSeconds()
  {
    return mTimeStampInNanoSeconds;
  }

  public static TimeStampContainer getGlobalTimeSinceStart(DataWarehouse pDataWarehouse, long pTimePoint, StackInterface pStack)
  {
    TimeStampContainer lStartTimeInNanoSecondsContainer = pDataWarehouse.getOldestContainer(TimeStampContainer.class);
    if (lStartTimeInNanoSecondsContainer == null)
    {
      lStartTimeInNanoSecondsContainer = new TimeStampContainer(pTimePoint, pStack.getMetaData().getTimeStampInNanoseconds());
      pDataWarehouse.put("timestamp" + pTimePoint, lStartTimeInNanoSecondsContainer);
    }
    return lStartTimeInNanoSecondsContainer;
  }
}
