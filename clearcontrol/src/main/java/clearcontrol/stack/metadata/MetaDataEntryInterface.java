package clearcontrol.stack.metadata;

/**
 * Interface for individual metadata entries
 *
 * @param <T> entry type
 * @author royer
 */
public interface MetaDataEntryInterface<T>
{
  /**
   * Returns the meta data value class
   *
   * @return meta data value class
   */
  Class<T> getMetaDataClass();

}
