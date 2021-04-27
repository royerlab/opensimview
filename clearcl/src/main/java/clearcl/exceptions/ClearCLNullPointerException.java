package clearcl.exceptions;

/**
 * Exception thrown when a OpenCL function returns a null pointer when they
 * should not.
 *
 * @author royer
 */
public class ClearCLNullPointerException extends ClearCLException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instanciates the exception.
   */
  public ClearCLNullPointerException()
  {
    super("Opencl function returned null pointer.");
  }

}
