package coremem.interop;

import coremem.MemoryBase;

import org.bridj.Pointer;
import org.bridj.Pointer.Releaser;
import org.bridj.PointerIO;

/**
 * BridJ buffers interoperability
 *
 * @author royer
 */
public class BridJInterop
{
  /**
   * Gets Bridj pointer given a target class, native address, size in bytes, and
   * releaser
   * 
   * @param pTargetClass
   *          target class
   * @param pAddress
   *          address
   * @param pSizeInBytes
   *          size in bytes
   * @param pReleaser
   *          releaser
   * @return BridJ pointer
   */
  public static <T> Pointer<T> getBridJPointer(Class<T> pTargetClass,
                                               long pAddress,
                                               long pSizeInBytes,
                                               Releaser pReleaser)
  {

    PointerIO<T> lPointerIO = PointerIO.getInstance(pTargetClass);

    Pointer<T> lPointerToAddress =
                                 Pointer.pointerToAddress(pAddress,
                                                          pSizeInBytes,
                                                          lPointerIO,
                                                          pReleaser);

    return lPointerToAddress;

  }

  @SuppressWarnings(
  { "unchecked", "rawtypes" })
  public static Pointer getBridJPointer(MemoryBase pMemoryBase,
                                        Class pTargetClass)
  {

    final MemoryBase mThis = pMemoryBase;
    final Releaser lReleaser = new Releaser()
    {
      @SuppressWarnings("unused")
      volatile MemoryBase mMemoryBase = mThis;

      @Override
      public void release(Pointer<?> pP)
      {
        mMemoryBase = null;
      }
    };

    final Pointer<?> lPointerToAddress =
                                       BridJInterop.getBridJPointer(pTargetClass,
                                                                    pMemoryBase.getAddress(),
                                                                    pMemoryBase.getSizeInBytes(),
                                                                    lReleaser);

    return lPointerToAddress;
  }

}
