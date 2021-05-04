package coremem.interfaces;

/**
 * Memory objects implementing this interface have an underlying long pointer
 * address and length defined.
 *
 * @author royer
 */
public interface PointerAccessible extends SizedInBytes
{
  /**
   * Returns the memory's address.
   *
   * @return address
   */
  long getAddress();

}
