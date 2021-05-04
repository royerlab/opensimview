package coremem.exceptions;

/**
 * Exception thrown for unsupported wrapping of a memory object
 *
 * @author royer
 */
public class UnsupportedWrappingException extends CoreMemException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instanciates with an error message.
   *
   * @param pErrorMessage error message
   */
  public UnsupportedWrappingException(String pErrorMessage)
  {
    super(pErrorMessage);
  }

  /**
   * Instanciates with error message and Throwable cause
   *
   * @param pErrorMessage error message
   * @param pThrowable    cause
   */
  public UnsupportedWrappingException(String pErrorMessage, Throwable pThrowable)
  {
    super(pErrorMessage, pThrowable);
  }

}
