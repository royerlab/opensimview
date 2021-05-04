package coremem.exceptions;

/**
 * Exception passed when a fragmented emory is in an invalid state.
 *
 * @author royer
 */
public class InvalidFragmentedMemoryStateException extends CoreMemException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instanciates with error message and Throwable cause
   *
   * @param pString error message
   */
  public InvalidFragmentedMemoryStateException(String pString)
  {
    super(pString);
  }

  /**
   * Instanciates with error message and Throwable cause
   *
   * @param pErrorMessage error message
   * @param pThrowable    cause
   */
  public InvalidFragmentedMemoryStateException(String pErrorMessage, Throwable pThrowable)
  {
    super(pErrorMessage, pThrowable);
  }

}
