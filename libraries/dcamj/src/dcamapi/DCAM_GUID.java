package dcamapi;

import org.bridj.Pointer;
import org.bridj.StructObject;
import org.bridj.ann.Array;
import org.bridj.ann.CLong;
import org.bridj.ann.Field;
import org.bridj.ann.Library;

/**
 * <i>native declaration : lib\dcam\inc\dcamapi.h:324</i><br>
 * This file was autogenerated by
 * <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that
 * <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a
 * few opensource projects.</a>.<br>
 * For help, please visit
 * <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> or
 * <a href="http://bridj.googlecode.com/">BridJ</a> .
 */
@Library("dcamapi")
public class DCAM_GUID extends StructObject
{
  public DCAM_GUID()
  {
    super();
  }

  @CLong
  @Field(0)
  public long data1()
  {
    return this.io.getCLongField(this, 0);
  }

  @CLong
  @Field(0)
  public DCAM_GUID data1(final long data1)
  {
    this.io.setCLongField(this, 0, data1);
    return this;
  }

  @Field(1)
  public short data2()
  {
    return this.io.getShortField(this, 1);
  }

  @Field(1)
  public DCAM_GUID data2(final short data2)
  {
    this.io.setShortField(this, 1, data2);
    return this;
  }

  @Field(2)
  public short data3()
  {
    return this.io.getShortField(this, 2);
  }

  @Field(2)
  public DCAM_GUID data3(final short data3)
  {
    this.io.setShortField(this, 2, data3);
    return this;
  }

  // / C type : unsigned char[8]
  @Array({8})
  @Field(3)
  public Pointer<Byte> data4()
  {
    return this.io.getPointerField(this, 3);
  }

  public DCAM_GUID(final Pointer pointer)
  {
    super(pointer);
  }
}
