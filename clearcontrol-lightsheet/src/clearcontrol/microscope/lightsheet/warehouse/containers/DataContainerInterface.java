package clearcontrol.microscope.lightsheet.warehouse.containers;

/**
 * This interface describes a general DataContainer which can be stored in the
 * DataWarehouse. This intermediate layer is to ensure that not Objects of any kind can be
 * stored in the DataWarehouse and to ensure some systematic interface to access stored
 * objects.
 *
 * @author haesleinhuepf April 2018
 */
public interface DataContainerInterface
{
  /**
   * @return the time point when the container was created
   */
  public long getTimepoint();

  /**
   * Deprecated: This method was never really needed and will be removed soon.
   *
   * @return true if the container is finished
   */
  @Deprecated public boolean isDataComplete();

  /**
   * Dispose the container and release memory if needed.
   */
  public void dispose();
}
