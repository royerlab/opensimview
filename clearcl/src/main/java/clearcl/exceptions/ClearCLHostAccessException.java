package clearcl.exceptions;

/**
 * Exception thrown when trying to access an image or buffer that is not
 * declared at creation time as being readable or writable from he host.
 *
 * @author royer
 */
public class ClearCLHostAccessException extends ClearCLException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instanciates an exception given a m,essage
   * 
   * @param pMessage
   *          message
   */
  public ClearCLHostAccessException(String pMessage)
  {
    super(pMessage);
  }

}
