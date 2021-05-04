package coremem.interfaces;

import coremem.enums.MemoryType;

/**
 * Memory objects implementing this interface have a memory type.
 *
 * @author royer
 */
public interface MemoryTyped
{
  /**
   * Returns this memory object type.
   *
   * @return memory type.
   */
  MemoryType getMemoryType();
}
