package clearcl.abs;

import clearcl.ClearCLPeerPointer;
import clearcl.backend.ClearCLBackendInterface;

/**
 * ClearCLBase offers basic functionality shared by several ClearCL classes,
 * such as holding a reference to the backed and peer pointer for OpenCL objects
 *
 * @author royer
 */
public abstract class ClearCLBase implements AutoCloseable
{

  private final ClearCLBackendInterface mClearCLBackendInterface;
  private ClearCLPeerPointer mPeerPointer;

  /**
   * Constructs a ClearCLBase given a ClearCL backend and OpenCL peer pointer
   *
   * @param pClearCLBackendInterface backend
   * @param pPointer                 peer pointer
   */
  public ClearCLBase(ClearCLBackendInterface pClearCLBackendInterface, ClearCLPeerPointer pPointer)
  {
    mClearCLBackendInterface = pClearCLBackendInterface;
    setPeerPointer(pPointer);
  }

  /**
   * Returns this object's ClearCL backend
   *
   * @return backend
   */
  public ClearCLBackendInterface getBackend()
  {
    return mClearCLBackendInterface;
  }

  /**
   * Returns this objects peer pointer
   *
   * @return peer pointer
   */
  public ClearCLPeerPointer getPeerPointer()
  {
    return mPeerPointer;
  }

  /**
   * Sets this object's peer pointer
   *
   * @param pPeerPointer peer pointer
   */
  public void setPeerPointer(ClearCLPeerPointer pPeerPointer)
  {
    mPeerPointer = pPeerPointer;
  }

  /* (non-Javadoc)
   * @see java.lang.AutoCloseable#close()
   */
  @Override
  public abstract void close();

  @Override
  public String toString()
  {
    return String.format("ClearCLBase [mClearCLBackendInterface=%s, mPeerPointer=%s]", mClearCLBackendInterface, mPeerPointer);
  }

}
