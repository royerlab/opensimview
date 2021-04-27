package coremem.exceptions;

/**
 * Exception thrown when memory mapping a file
 *
 * @author royer
 */
public class MemoryMapFileException extends MemoryMapException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instanciates with error message and Throwable cause
   * 
   * @param pErrorMessage
   *          error message
   * @param pThrowable
   *          cause
   */
  public MemoryMapFileException(String pErrorMessage,
                                Throwable pThrowable)
  {
    super(pErrorMessage, pThrowable);
  }

}
