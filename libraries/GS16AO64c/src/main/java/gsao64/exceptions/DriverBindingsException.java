package gsao64.exceptions;

public class DriverBindingsException extends Exception
{

  public DriverBindingsException(String pString)
  {
    super(pString);
  }

  public DriverBindingsException(String pErrorMessage, Throwable pThrowable)
  {
    super(pErrorMessage, pThrowable);
  }

}
