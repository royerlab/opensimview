package coremem.enums;

import coremem.interfaces.SizedInBytes;

/**
 * Enum listing the different native primitive data types.
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public enum NativeTypeEnum implements SizedInBytes
{

  Byte(1), UnsignedByte(1), Short(2), UnsignedShort(2), Int(4), UnsignedInt(4), Long(8), UnsignedLong(8), HalfFloat(2), Float(4), Double(8);

  private final long mSizeInBytes;

  NativeTypeEnum(long pSizeInBytes)
  {
    mSizeInBytes = pSizeInBytes;
  }

  @Override
  public long getSizeInBytes()
  {
    return mSizeInBytes;
  }

}
