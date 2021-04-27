package coremem.exceptions;

/**
 * Exception thrown when trying to write to a read-only memory object.
 *
 * @author royer
 */
public class InvalidWriteAtReadOnly extends CoreMemException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instanciates with error message and Throwable cause
   * 
   * @param pString
   *          error message
   */
  public InvalidWriteAtReadOnly(String pString)
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
  public InvalidWriteAtReadOnly(String pErrorMessage,
                                Throwable pThrowable)
  {
    super(pErrorMessage, pThrowable);
  }

}
