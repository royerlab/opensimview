package coremem.interfaces;

/**
 * Memory objects implementing this interface have a defined length in bytes.
 *
 * @author royer
 */
public interface SizedInBytes
{
  /**
   * Return this memory object's size in bytes.
   * 
   * @return size in bytes.
   */
  public long getSizeInBytes();
}
