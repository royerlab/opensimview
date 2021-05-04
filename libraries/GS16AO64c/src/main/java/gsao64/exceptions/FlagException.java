package gsao64.exceptions;

public class FlagException extends Exception
{

  public FlagException(String pString)
  {
    super(pString);
  }

  public FlagException(String pErrorMessage, Throwable pThrowable)
  {
    super(pErrorMessage, pThrowable);
  }

}