package clearcl.exceptions;

import clearcl.ClearCLKernel;

/**
 * Exception thrown when kernel argument missing.
 *
 * @author royer
 */
public class ClearCLArgumentMissingException extends ClearCLException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instanciates this exception for a given kernel, argument name, and argument
   * index.
   *
   * @param pClearCLKernel kernel
   * @param pArgumentName  argument name
   * @param pArgumentIndex argument index
   */
  public ClearCLArgumentMissingException(ClearCLKernel pClearCLKernel, String pArgumentName, Integer pArgumentIndex)
  {
    super(String.format("Argument: '%s' missing at index: %s in kernel: '%s'", pArgumentName, pArgumentIndex, pClearCLKernel.getName()));
  }

}
