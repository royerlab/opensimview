package clearcl.exceptions;

/**
 * Exception thrown when the execution range ( global/local sizes or offsets)
 * are invalid or undefined.
 *
 * @author royer
 */
public class ClearCLInvalidExecutionRange extends ClearCLException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instanciates the exception with a given message.
   *
   * @param pMessage message
   */
  public ClearCLInvalidExecutionRange(String pMessage)
  {
    super(pMessage);
  }
}
