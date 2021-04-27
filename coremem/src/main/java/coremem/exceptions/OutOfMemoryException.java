package coremem.exceptions;

/**
 * Out-of-memory exception.
 *
 * @author royer
 */
public class OutOfMemoryException extends CoreMemException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instanciates with error message and Throwable cause
   * 
   * @param pErrorMessage
   *          error message
   */
  public OutOfMemoryException(String pErrorMessage)
  {
    super(pErrorMessage);
  }

  /**
   * 
   * Instanciates with error message and Throwable cause
   * 
   * @param pErrorMessage
   *          error message
   * @param pThrowable
   *          cause
   */
  public OutOfMemoryException(String pErrorMessage,
                              Throwable pThrowable)

  {
    super(pErrorMessage, pThrowable);
  }

}
