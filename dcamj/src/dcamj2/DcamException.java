package dcamj2;

/**
 * Runtime exception thrown when problems occurs with DcamJ
 *
 * @author royer
 */
public class DcamException extends RuntimeException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a Dcam exception given an error message
   * 
   * @param pMessage
   *          error message
   * 
   */
  public DcamException(String pMessage)
  {
    super(pMessage);

  }

  /**
   * Instantiates a Dcam exception given a message and cause
   * 
   * @param pMessage
   *          error message
   * @param pCause
   *          cause
   */
  public DcamException(String pMessage, Throwable pCause)
  {
    super(pMessage, pCause);

  }

}
