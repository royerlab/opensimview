package coremem.interfaces;

/**
 * Memory objects implementing this interface can be wrapped into a JNA pointer.
 *
 * @author royer
 */
public interface JNAPointerWrappable
{
  /**
   * Returns a JNA pointer for this memory.
   *
   * @return JNA Pointer
   */
  public com.sun.jna.Pointer getJNAPointer();
}
