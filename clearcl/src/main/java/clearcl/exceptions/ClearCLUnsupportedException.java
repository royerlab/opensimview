package clearcl.exceptions;

/**
 * Exception thrown when a stumbling on a unsupported functionality (typically a
 * backend does not support something)
 *
 * @author royer
 */
public class ClearCLUnsupportedException extends ClearCLException
{

  private static final long serialVersionUID = 1L;

  /**
   * Exception thrown for unsupported OpenCL functionality
   */
  public ClearCLUnsupportedException()
  {
    super("Unsupported OpenCL functionality");
  }

  /**
   * Exception thrown for a particular unsupported OpenCL functionality
   * 
   * @param pMessage
   *          additional message
   */
  public ClearCLUnsupportedException(String pMessage)
  {
    super("Unsupported OpenCL functionality: '" + pMessage + "'");
  }

}
