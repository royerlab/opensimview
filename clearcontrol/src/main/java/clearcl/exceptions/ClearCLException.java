package clearcl.exceptions;

/**
 * Standard ClearCL exception. Wraps internal backend exception.
 *
 * @author royer
 */
public class ClearCLException extends RuntimeException
{
  private static final long serialVersionUID = 1L;

  /**
   * Instanciates a ClearCL exception from a message and a cause.
   *
   * @param pMessage message
   * @param pCause   cause
   */
  public ClearCLException(String pMessage, Throwable pCause)
  {
    super(pMessage, pCause);
  }

  /**
   * Instanciates a ClearCL exception from a message and a cause.
   *
   * @param pMessage message
   */
  public ClearCLException(String pMessage)
  {
    super(pMessage);
  }

}
