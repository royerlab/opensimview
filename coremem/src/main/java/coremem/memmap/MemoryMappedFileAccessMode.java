package coremem.memmap;

/**
 * Memory mapped file access modes
 *
 * @author royer
 */
public enum MemoryMappedFileAccessMode
{
 /**
  * Read only memory mapping
  */
 ReadOnly(0),

 /**
  * Read and only memory mapping
  */
 ReadWrite(1),

 /**
  * Private memory mapping
  */
 Private(2);

  private final int mValue;

  /**
   * @param pValue
   */
  private MemoryMappedFileAccessMode(final int pValue)
  {
    mValue = pValue;
  }

  /**
   * Returns corresponding int value
   * 
   * @return value
   */
  public int getValue()
  {
    return mValue;
  }
}
