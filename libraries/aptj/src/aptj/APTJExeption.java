package aptj;

/**
 * APTJ exception
 *
 * @author royer
 */
public class APTJExeption extends Exception
{

  private static final long serialVersionUID = 1L;

  private final APTJReturnCode mReturnCode;

  /**
   * Instantiates an APTJ exception given a APTJ error code.
   *
   * @param pReturnCode return code
   */
  public APTJExeption(long pReturnCode)
  {
    super();
    mReturnCode = APTJReturnCode.getByReturCodeInt(pReturnCode);
  }

  @Override
  public String toString()
  {
    return String.format("APTJExeption [mReturnCode=%s]", mReturnCode);
  }


}
