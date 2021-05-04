package coremem.exceptions;

/**
 * Exception thrown when ressource is freed more than once.
 *
 * @author royer
 */
public class FreedException extends RuntimeException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instanciates a 'freed' exception with a given error message
   *
   * @param pErrorMessage error message
   */
  public FreedException(String pErrorMessage)
  {
    super(pErrorMessage);
  }

}
