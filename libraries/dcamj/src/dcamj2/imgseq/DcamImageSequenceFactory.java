package dcamj2.imgseq;

import coremem.recycling.RecyclableFactoryInterface;

/**
 * @author royer
 */
public class DcamImageSequenceFactory implements RecyclableFactoryInterface<DcamImageSequence, DcamImageSequenceRequest>
{

  @Override
  public DcamImageSequence create(DcamImageSequenceRequest pRequest)
  {

    return pRequest.newImageSequence();
  }

}
