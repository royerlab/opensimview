package clearcl.exceptions;

import clearcl.ClearCLKernel;

/**
 * Exception thrown when unknown argument name is provided to a kernel.
 *
 * @author royer
 */
public class ClearCLUnknownArgumentNameException extends
                                                 ClearCLException
{

  private static final long serialVersionUID = 1L;

  /**
   * Instanciates an unknown kernel argument exception .
   * 
   * @param pKernel
   *          kernel
   * @param pArgumentName
   *          argument name
   * @param pObject
   *          corresponding object
   */
  public ClearCLUnknownArgumentNameException(ClearCLKernel pKernel,
                                             String pArgumentName,
                                             Object pObject)
  {
    super(String.format("Argument name unknow: '%s' for object: %s in kernel: '%s'",
                        pArgumentName,
                        pObject == null ? "null"
                                        : pObject.getClass()
                                                 .getName(),
                        pKernel.getName()));
  }

}
