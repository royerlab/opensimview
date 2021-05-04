package coremem.interfaces;

/**
 * Memory objects implementing this interface can be resized.
 *
 * @author royer
 */
public interface Resizable
{
  /**
   * Resizes this memory object.
   *
   * @param pNewLength new length.
   * @return new length
   */
  public long resize(final long pNewLength);
}
