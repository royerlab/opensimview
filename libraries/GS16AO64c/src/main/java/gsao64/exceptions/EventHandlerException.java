package gsao64.exceptions;

public class EventHandlerException extends Exception
{

  public EventHandlerException(String pString)
  {
    super(pString);
  }

  public EventHandlerException(String pErrorMessage, Throwable pThrowable)
  {
    super(pErrorMessage, pThrowable);
  }
}