package coremem.exceptions;

/**
 * Exception thrown during memory mapping
 *
 * @author royer
 */
public class MemoryMapException extends CoreMemException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instanciates with error message and Throwable cause
   * 
   * @param pErrorMessage
   *          error message
   */
  public MemoryMapException(String pErrorMessage)
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
  public MemoryMapException(String pErrorMessage,
                            Throwable pThrowable)
  {
    super(pErrorMessage, pThrowable);
  }

}
