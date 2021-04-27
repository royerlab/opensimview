package coremem.exceptions;

/**
 * Top-level CoreMem exception.
 * 
 * @author royer
 */
public class CoreMemException extends RuntimeException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instanciates with error message and Throwable cause
   * 
   * @param pString
   *          error message
   */
  public CoreMemException(String pString)
  {
    super(pString);
  }

  /**
   * Instanciates with error message and Throwable cause
   * 
   * @param pErrorMessage
   *          error message
   * @param pThrowable
   *          cause
   */
  public CoreMemException(String pErrorMessage, Throwable pThrowable)
  {
    super(pErrorMessage, pThrowable);
  }

}
