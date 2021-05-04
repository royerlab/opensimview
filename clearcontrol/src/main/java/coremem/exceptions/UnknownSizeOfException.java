package coremem.exceptions;

/**
 * Exception thrown when Size.of function canot determine the size in bytes of
 * Java object.
 *
 * @author royer
 */
public class UnknownSizeOfException extends CoreMemException
{

  private static final long serialVersionUID = 1L;

  /**
   * Constructs with object for which the size cannot be determined.
   *
   * @param pOffendingObject offending object for which the size could not be determined
   */
  public UnknownSizeOfException(Object pOffendingObject)
  {
    super("Unknown size-of for object:  " + pOffendingObject + " of class: " + pOffendingObject.getClass().toString());
  }

}
