package clearcl.exceptions;

/**
 * Exception thrown when memory (buffer or image) allocation fails
 *
 * @author royer
 */
public class ClearCLAllocationException extends ClearCLException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instanciates this exception
   */
  public ClearCLAllocationException()
  {
    super("Memory allocation exception");
  }

  /**
   * Instanciates this exception with a given message
   *
   * @param pMessage message
   */
  public ClearCLAllocationException(String pMessage)
  {
    super("Memory allocation exception: " + pMessage);
  }

}
