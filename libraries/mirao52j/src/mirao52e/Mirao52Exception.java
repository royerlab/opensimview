package mirao52e;

/**
 * Exception Mirao52Exception
 * <p>
 * Instances of this exception are thrown when there is a problem with the
 * MIRAO52e deformable mirror.
 *
 * @author Loic Royer 2014
 */
public class Mirao52Exception extends RuntimeException
{

  private static final long serialVersionUID = 1L;

  /**
   * Constructs an instance of Mirao52Exception class given an error message.
   *
   * @param pExceptionMessage exception message
   */
  public Mirao52Exception(String pExceptionMessage)
  {
    super(pExceptionMessage);
  }

  /**
   * Constructs an instance of the Mirao52Exception class given an error message
   * and a cause.
   *
   * @param pExceptionMessage exception message
   * @param pException        causative exception
   */
  public Mirao52Exception(String pExceptionMessage, Throwable pException)
  {
    super(pExceptionMessage, pException);
  }

}
