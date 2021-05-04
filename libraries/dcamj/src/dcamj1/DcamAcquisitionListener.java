package dcamj1;

public interface DcamAcquisitionListener
{

  void frameArrived(DcamAcquisition pDcamAquisition, long pAbsoluteFrameIndex, long pArrivalTimeStampInNanoseconds, long pFrameIndexInBufferList, DcamFrame pDcamFrame);

}
