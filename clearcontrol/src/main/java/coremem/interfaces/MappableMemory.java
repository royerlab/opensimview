package coremem.interfaces;

/**
 * Memory objects implementing this interface are 'mapped' and require calls to
 * map/unmap methods for access.
 *
 * @author royer
 */
public interface MappableMemory
{
  /**
   * Maps this memory object.
   *
   * @return mapping address
   */
  public long map();

  /**
   * TODO: figue out what this is really about
   */
  public void force();

  /**
   * Unmaps this memory object.
   */
  public void unmap();

  /**
   * Returns true if this object is mapped, false otherwise.
   *
   * @return true if mapped, false otherwise.
   */
  public boolean isCurrentlyMapped();
}
