package coremem.exceptions;

/**
 * Exception thrown when invalid parameters are passed during memory allocation.
 *
 * @author royer
 */
public class InvalidAllocationParameterException extends CoreMemException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instanciates with error message and Throwable cause
   *
   * @param pString error message
   */
  public InvalidAllocationParameterException(String pString)
  {
    super(pString);
  }

  /**
   * Instanciates with error message and Throwable cause
   *
   * @param pErrorMessage error message
   * @param pThrowable    cause
   */
  public InvalidAllocationParameterException(String pErrorMessage, Throwable pThrowable)
  {
    super(pErrorMessage, pThrowable);
  }

}
