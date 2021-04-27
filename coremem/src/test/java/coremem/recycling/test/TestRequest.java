package coremem.recycling.test;

import coremem.recycling.RecyclerRequestInterface;

/**
 * Example request used for testing
 *
 * @author royer
 */
class TestRequest implements RecyclerRequestInterface
{

  public long size;

  /**
   * Instanciates a request with a single long 'size' parameter
   * 
   * @param pSize
   *          size parameter
   */
  public TestRequest(long pSize)
  {
    size = pSize;
  }

}