package clearcl.enums;

/**
 * OpenCl host access type
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public enum HostAccessType
{
  ReadOnly(true, false), WriteOnly(false, true), ReadWrite(true, true), NoAccess(false, false), Undefined(true, true);

  private boolean mReadable, mWritable;

  private HostAccessType(boolean pRead, boolean pWrite)
  {
    mReadable = pRead;
    mWritable = pWrite;
  }

  public boolean isReadableFromHost()
  {
    return mReadable;
  }

  public boolean isWritableFromHost()
  {
    return mWritable;
  }
}
