package coremem.interop;

import com.sun.jna.Pointer;

import coremem.ContiguousMemoryInterface;

/**
 * BridJ buffers interoperability
 *
 * @author royer
 */
public class JNAInterop
{
  /**
   * Gets JNA pointer given a native address. Important: there is no way to keep
   * a reference of the parent object in a JNA Pointer. Holding the 'long' value
   * of the pointer is no garantee that the corresponding buffer will not be
   * deleted (opposite of malloc) if the holding object is garbage collected...
   *
   * @param pAddress
   *          address
   * @return JNA pointer
   */
  public static Pointer getJNAPointer(long pAddress)
  {
    return new Pointer(pAddress);
  }

  /**
   * Returns a JNA Pointer
   *
   * Note: JNA memory have a BIG problem: the corresponding off-heap memory is
   * not freed when garbage collected... are not freed when the It is a real
   * problem.... So it is much better to allocate memory with CoreMem and then
   * get a JNA pointer, instead of allocating on the JNA side...
   *
   * @param pContiguousMemory
   *          contiguous memory object
   * @return JNA pointer
   */
  public static Pointer getJNAPointer(ContiguousMemoryInterface pContiguousMemory)
  {
    return new Pointer(pContiguousMemory.getAddress());
  }
}
