package coremem.interfaces;

/**
 * Memory objects implementing this interface can copy a range of their contents
 * to a generic destination.
 *
 * @param <M> object type for copy target
 * @author royer
 */
public interface RangeCopyable<M>
{
  /**
   * Copies range (offset_src,length) from this memory object to the range
   * (offset_src,length) of a generic destination.
   *
   * @param pSourceOffset      source offset
   * @param pTo                destination
   * @param pDestinationOffset destination offset
   * @param pLengthToCopy      length to copy in bytes.
   */
  public void copyRangeTo(long pSourceOffset, M pTo, long pDestinationOffset, long pLengthToCopy);
}
