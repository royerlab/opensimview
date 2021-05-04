package clearcl.exceptions;

/**
 * Exception thrown when kernels are created for a non built program.
 *
 * @author royer
 */
public class ClearCLProgramNotBuiltException extends ClearCLException
{
  private static final long serialVersionUID = 1L;

  /**
   * Instanciates the exception.
   */
  public ClearCLProgramNotBuiltException()
  {
    super("Program must be built before creating kernels");
  }
}
