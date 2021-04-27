package coremem.exceptions;

/**
 * Exception thrown when an invalid native memory access occurs.
 *
 * @author royer
 */
public class InvalidNativeMemoryAccessException extends
                                                CoreMemException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instanciates with error message and Throwable cause
   * 
   * @param pString
   *          error message
   */
  public InvalidNativeMemoryAccessException(String pString)
  {
    super(pString);
  }

}
