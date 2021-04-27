package clearcontrol.microscope.lightsheet.warehouse.containers;

/**
 * This class contains some convenience methods for working with DataContainers.
 *
 * @author haesleinhuepf April 2018
 */
public abstract class DataContainerBase implements DataContainerInterface
{
  private long mTimePoint;

  /**
   * Create a new data container
   *
   * @param pTimepoint the time point when the container was created
   */
  protected DataContainerBase(long pTimepoint)
  {
    mTimePoint = pTimepoint;
  }

  /**
   * @return the time point when the container was created
   */
  @Override public long getTimepoint()
  {
    return mTimePoint;
  }
}
