package clearcl.exceptions;

/**
 * Exception thrown when argument is illegal.
 *
 * @author royer
 */
public class ClearCLIllegalArgumentException extends ClearCLException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instanciates an exception with a given error message
   * 
   * @param pMessage
   *          error message
   */
  public ClearCLIllegalArgumentException(String pMessage)
  {
    super(pMessage);
  }

}
