package coremem.exceptions;

/**
 * Exception thrown when trying to resize a memory object that cannot be
 * resized.
 *
 * @author royer
 */
public class UnsupportedMemoryResizingException extends
                                                CoreMemException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instanciates with error message
   * 
   * @param pErrorMessage
   *          error message
   */
  public UnsupportedMemoryResizingException(String pErrorMessage)
  {
    super(pErrorMessage);
  }

  /**
   * Instanciates with error message and Throwable cause
   * 
   * @param pErrorMessage
   *          error message
   * @param pThrowable
   *          cause
   */
  public UnsupportedMemoryResizingException(String pErrorMessage,
                                            Throwable pThrowable)
  {
    super(pErrorMessage, pThrowable);
  }

}
