package clearcontrol.imaging;

import clearcontrol.LightSheetMicroscope;
import clearcontrol.LightSheetMicroscopeQueue;

/**
 * This imager takes images of a whole stack (e.g. in Z) and returns it.
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) February
 * 2018
 */
public class SingleStackImager extends ImagerBase
{
  private double mIlluminationZStepDistance = 1;
  private double mDetectionZStepDistance = 1;
  private int mNumberOfRequestedImages = 1;

  public SingleStackImager(LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pLightSheetMicroscope);
  }

  @Override
  protected boolean configureQueue(LightSheetMicroscopeQueue pQueue)
  {
    // Todo: use acquisition state
    for (int lImageCount = 0; lImageCount < mNumberOfRequestedImages; lImageCount++)
    {
      if (lImageCount == 0)
      {
        pQueue.setIZ(mLightSheetIndex, mIlluminationZ);
        pQueue.setDZ(mDetectionArmIndex, mDetectionZ);
      } else
      {
        pQueue.setIZ(mLightSheetIndex, pQueue.getIZ(mLightSheetIndex) + mIlluminationZStepDistance);
        pQueue.setDZ(mDetectionArmIndex, pQueue.getDZ(mDetectionArmIndex) + mDetectionZStepDistance);
      }
      pQueue.setC(mDetectionArmIndex, true);
      pQueue.addCurrentStateToQueue();
    }
    return true;
  }

  /**
   * Set the stack slice distance of the illumination plane in Z.
   *
   * @param mIlluminationZStepDistance DeltaZ in microns
   */
  public void setIlluminationZStepDistance(double mIlluminationZStepDistance)
  {
    this.mIlluminationZStepDistance = mIlluminationZStepDistance;
  }

  /**
   * Set the stack slice distance of the detection/focal plane in Z.
   *
   * @param mDetectionZStepDistance DeltaZ in microns
   */
  public void setDetectionZStepDistance(double mDetectionZStepDistance)
  {
    this.mDetectionZStepDistance = mDetectionZStepDistance;
  }

  /**
   * Configure how many slices you want to image
   *
   * @param mNumberOfRequestedImages
   */
  public void setNumberOfRequestedImages(int mNumberOfRequestedImages)
  {
    this.mNumberOfRequestedImages = mNumberOfRequestedImages;
  }
}
