package bindings;

import bindings.AO64_64b_Driver_CLibrary.U64;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * <i>native declaration : DriverFiles/66-16AO64/AO64eintface.h</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class GS_PHYSICAL_MEM extends Structure
{
  /**
   * User-mode virtual address<br>
   * C type : U64
   */
  public U64 UserAddr;
  /**
   * Bus physical address<br>
   * C type : U64
   */
  public U64 PhysicalAddr;
  /**
   * CPU physical address<br>
   * C type : U64
   */
  public U64 CpuPhysical;
  /**
   * Size of the buffer<br>
   * C type : U32
   */
  public NativeLong Size;

  public GS_PHYSICAL_MEM()
  {
    super();
  }

  protected List<String> getFieldOrder()
  {
    return Arrays.asList("UserAddr", "PhysicalAddr", "CpuPhysical", "Size");
  }

  /**
   * @param UserAddr     User-mode virtual address<br>
   *                     C type : U64<br>
   * @param PhysicalAddr Bus physical address<br>
   *                     C type : U64<br>
   * @param CpuPhysical  CPU physical address<br>
   *                     C type : U64<br>
   * @param Size         Size of the buffer<br>
   *                     C type : U32
   */
  public GS_PHYSICAL_MEM(U64 UserAddr, U64 PhysicalAddr, U64 CpuPhysical, NativeLong Size)
  {
    super();
    this.UserAddr = UserAddr;
    this.PhysicalAddr = PhysicalAddr;
    this.CpuPhysical = CpuPhysical;
    this.Size = Size;
  }

  public GS_PHYSICAL_MEM(Pointer peer)
  {
    super(peer);
  }

  public static class ByReference extends GS_PHYSICAL_MEM implements Structure.ByReference
  {

  }

  ;

  public static class ByValue extends GS_PHYSICAL_MEM implements Structure.ByValue
  {

  }

  ;
}
