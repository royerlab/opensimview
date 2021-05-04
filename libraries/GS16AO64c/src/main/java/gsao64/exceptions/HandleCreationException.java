package gsao64.exceptions;

public class HandleCreationException extends Exception
{

  public HandleCreationException(String pString)
  {
    super(pString);
  }

  public HandleCreationException(String pErrorMessage, Throwable pThrowable)
  {
    super(pErrorMessage, pThrowable);
  }
}