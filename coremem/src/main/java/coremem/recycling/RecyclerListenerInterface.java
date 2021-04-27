package coremem.recycling;

/**
 *
 *
 * @author royer
 */
public interface RecyclerListenerInterface
{
  /**
   * @param pNumberOfLiveObjects
   * @param pNumberOfAvailableObjects
   * @param pNumberOfFailedRequest
   */
  void update(int pNumberOfLiveObjects,
              int pNumberOfAvailableObjects,
              long pNumberOfFailedRequest);

}
