package asdk;

/**
 * Exception AlpaoException
 * 
 * Instances of this exception are thrown when there is a problem with the Alpao
 * SDK Java bindings for deformable mirrors.
 * 
 * @author Loic Royer 2014
 *
 */
public class AlpaoException extends RuntimeException
{

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an instance of AlpaoException class given an error message.
	 * 
	 * @param pExceptionMessage
	 *          exception message
	 */
	public AlpaoException(String pExceptionMessage)
	{
		super(pExceptionMessage);
	}

	/**
	 * Constructs an instance of the AlpaoException class given an error message
	 * and a cause.
	 * 
	 * @param pExceptionMessage
	 *          exception message
	 * @param pException
	 *          causative exception
	 */
	public AlpaoException(String pExceptionMessage,
													Throwable pException)
	{
		super(pExceptionMessage, pException);
	}

}
