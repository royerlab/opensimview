package gsao64.exceptions;

public class BoardInitializeException extends Exception {

    public BoardInitializeException(String pString)
    {
        super(pString);
    }

    public BoardInitializeException(String pErrorMessage, Throwable pThrowable)
    {
        super(pErrorMessage, pThrowable);
    }

}