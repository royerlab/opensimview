package clearcontrol.microscope.lightsheet.imaging;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.imglib2.StackToImgConverter;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This is the base class for all Imagers. Imager is a convenience layer for sychronous
 * image acquisition, e.g. from within scripts.
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) February
 * 2018
 */
public abstract class ImagerBase implements ImagerInterface, LoggingFeature
{
  // input
  private LightSheetMicroscope mLightSheetMicroscope;

  protected double mIlluminationZ = 0;
  protected double mDetectionZ = 0;

  protected double mIlluminationX = 0;
  protected double mIlluminationY = 0;
  protected double mIlluminationH = 500;
  protected double mIlluminationW = 0.45;

  protected double mIlluminationA = 0;

  protected int mLightSheetIndex = 0;
  protected int mDetectionArmIndex = 0;

  protected double mExposureTimeInSeconds = 0.05;

  private int mImageWidth = 512;
  private int mImageHeight = 512;

  // output
  StackInterface mResultImage = null;

  public ImagerBase(LightSheetMicroscope pLightSheetMicroscope)
  {
    mLightSheetMicroscope = pLightSheetMicroscope;
  }

  private synchronized boolean image()
  {

    LightSheetMicroscopeQueue lQueue = mLightSheetMicroscope.requestQueue();
    lQueue.clearQueue();
    // lQueue.zero();

    if (mInterpolatedAcquisitionState != null)
    {
      mInterpolatedAcquisitionState.applyAcquisitionStateAtZ(lQueue, mAcquisitionZ);
      info("Imaging at I "
           + lQueue.getIZ(mLightSheetIndex)
           + " instead of "
           + mIlluminationZ);
    }

    lQueue.setFullROI();
    lQueue.setCenteredROI(mImageWidth, mImageHeight);

    lQueue.setExp(mExposureTimeInSeconds);

    // reset everything
    for (int i = 0; i < mLightSheetMicroscope.getNumberOfLightSheets(); i++)
    {
      lQueue.setI(i, false);
    }

    lQueue.setI(mLightSheetIndex, true);
    lQueue.setIX(mLightSheetIndex, mIlluminationX);
    lQueue.setIY(mLightSheetIndex, mIlluminationY);
    lQueue.setIZ(mLightSheetIndex, mIlluminationZ);
    lQueue.setIW(mLightSheetIndex, mIlluminationW);
    lQueue.setIH(mLightSheetIndex, mIlluminationH);
    lQueue.setIA(mLightSheetIndex, mIlluminationA);

    lQueue.setDZ(mDetectionArmIndex, mDetectionZ);

    /*mQueue.setIPattern(i,
                       0,
                       new BinaryStructuredIlluminationPattern());
    mQueue.setIPattern(i,
                       0,
                       new BinaryStructuredIlluminationPattern());
    */

    lQueue.setC(mDetectionArmIndex, false);

    lQueue.addCurrentStateToQueue();

    // programStep the actual image
    configureQueue(lQueue);

    lQueue.setTransitionTime(0.1);

    lQueue.finalizeQueue();

    mLightSheetMicroscope.useRecycler("adaptation", 1, 4, 4);
    final Boolean lPlayQueueAndWait;
    try
    {
      lPlayQueueAndWait =
          mLightSheetMicroscope.playQueueAndWaitForStacks(lQueue,
                                                          100 + lQueue.getQueueLength(),
                                                          TimeUnit.SECONDS);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
      return false;
    }
    catch (ExecutionException e)
    {
      e.printStackTrace();
      return false;
    }
    catch (TimeoutException e)
    {
      e.printStackTrace();
      return false;
    }

    if (!lPlayQueueAndWait)
    {
      return false;
    }

    mResultImage = mLightSheetMicroscope.getCameraStackVariable(mDetectionArmIndex).get();

    return true;
  }

  public StackInterface acquire()
  {
    image();
    return mResultImage;
  }

  public RandomAccessibleInterval<UnsignedShortType> getRandomAccessibleInterval()
  {
    image();
    if (mResultImage == null)
    {
      return null;
    }

    StackToImgConverter<UnsignedShortType>
        lStackToImgConverter =
        new StackToImgConverter<UnsignedShortType>(mResultImage);
    RandomAccessibleInterval<UnsignedShortType>
        img =
        lStackToImgConverter.getRandomAccessibleInterval();

    return img;
  }

  public void invalidate()
  {
    mResultImage = null;
  }

  protected abstract boolean configureQueue(LightSheetMicroscopeQueue pQueue);

  public void setIlluminationZ(double pIlluminationZ)
  {
    invalidate();
    mInterpolatedAcquisitionState = null;
    mIlluminationZ = pIlluminationZ;
  }

  public void setIlluminationX(double pIlluminationX)
  {
    this.mIlluminationX = pIlluminationX;
  }

  public void setIlluminationY(double pIlluminationY)
  {
    this.mIlluminationY = pIlluminationY;
  }

  public void setIlluminationH(double pIlluminationH)
  {
    this.mIlluminationH = pIlluminationH;
  }

  public void setIlluminationW(double pIlluminationW)
  {
    this.mIlluminationW = pIlluminationW;
  }

  public void setIlluminationA(double pIlluminationA)
  {
    this.mIlluminationA = pIlluminationA;
  }

  public void setDetectionZ(double pDetectionZ)
  {
    invalidate();
    mDetectionZ = pDetectionZ;
  }

  public void setLightSheetIndex(int pLightSheetIndex)
  {
    invalidate();
    this.mLightSheetIndex = pLightSheetIndex;
  }

  public void setDetectionArmIndex(int pDetectionArmIndex)
  {
    invalidate();
    this.mDetectionArmIndex = pDetectionArmIndex;
  }

  public void setImageHeight(int pImageHeight)
  {
    this.mImageHeight = pImageHeight;
  }

  public void setImageWidth(int pImageWidth)
  {
    this.mImageWidth = pImageWidth;
  }

  InterpolatedAcquisitionState mInterpolatedAcquisitionState;
  Double mAcquisitionZ;

  public void applyInterpolatedAcquisitionState(InterpolatedAcquisitionState pInterpolatedAcquisitionState,
                                                Double pAcquisitionZ)
  {
    invalidate();
    mInterpolatedAcquisitionState = pInterpolatedAcquisitionState;
    mAcquisitionZ = pAcquisitionZ;
  }

  public LightSheetMicroscope getLightSheetMicroscope()
  {
    return mLightSheetMicroscope;
  }

  public void setExposureTimeInSeconds(double pExposureTimeInSeconds)
  {
    mExposureTimeInSeconds = pExposureTimeInSeconds;
  }

}
