package coremem.recycling;

/**
 * Interface for all recyclable object factories.
 *
 * @param <R> recyclable type
 * @param <P> request type
 * @author royer
 */
public interface RecyclableFactoryInterface<R extends RecyclableInterface<R, P>, P extends RecyclerRequestInterface>
{
  /**
   * Creates (instanciates) a recyclable object given a request. the request
   * entirely defines the parameters nescessary for instanciating the
   * recyclable. in a sense, the requests act as the parameters for the
   * recyclable constructor (and can in practice be implemented as such)
   *
   * @param pRecyclerRequest request
   * @return recyclable instanciated according to the request
   */
  R create(P pRecyclerRequest);
}
