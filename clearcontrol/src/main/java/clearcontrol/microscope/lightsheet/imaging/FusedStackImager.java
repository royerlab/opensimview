package clearcontrol.microscope.lightsheet.imaging;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedAcquisitionInstruction;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedFusionInstruction;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.OpticsPrefusedAcquisitionInstruction;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.OpticsPrefusedFusionInstruction;
import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialAcquisitionInstruction;
import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialFusionInstruction;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionProcessor;
import clearcontrol.microscope.lightsheet.processor.fusion.FusedImageDataContainer;
import clearcontrol.microscope.lightsheet.processor.fusion.FusionInstruction;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.stack.StackInterface;

/**
 * The fused imager is a sychronous imager. After calling its programStep method, it will e.g.
 * take 8 image stacks in sequential acquisition mode, fuse them and return the resulting
 * stack
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) March
 * 2018
 */
public class FusedStackImager implements ImagerInterface, LoggingFeature
{
  private LightSheetMicroscope mLightSheetMicroscope;

  private AcquisitionType mAcquisitionType = AcquisitionType.TimeLapseInterleaved;
  private double mMinZ = 0;
  private double mMaxZ = 0;
  private double mSliceDistance = 2.0;
  private double mExposureTimeInSeconds = 0.01;
  private int mImageHeight;
  private int mImageWidth;

  public FusedStackImager(LightSheetMicroscope pLightSheetMicroscope)
  {
    mLightSheetMicroscope = pLightSheetMicroscope;
    mMinZ = pLightSheetMicroscope.getDetectionArm(0).getZVariable().getMin().doubleValue();
    mMaxZ = pLightSheetMicroscope.getDetectionArm(0).getZVariable().getMax().doubleValue();

  }

  public StackInterface acquire()
  {

    // set the imaging state
    InterpolatedAcquisitionState lCurrentState = (InterpolatedAcquisitionState) mLightSheetMicroscope.getAcquisitionStateManager().getCurrentState();
    lCurrentState.getExposureInSecondsVariable().set(mExposureTimeInSeconds);
    lCurrentState.getStackZLowVariable().set(mMinZ);
    lCurrentState.getStackZHighVariable().set(mMaxZ);
    lCurrentState.getNumberOfZPlanesVariable().set((mMaxZ - mMinZ) / mSliceDistance + 1);
    lCurrentState.getImageWidthVariable().set(mImageWidth);
    lCurrentState.getImageHeightVariable().set(mImageHeight);

    LightSheetFastFusionProcessor lProcessor = mLightSheetMicroscope.getDevice(LightSheetFastFusionProcessor.class, 0);
    lProcessor.initializeEngine();
    lProcessor.reInitializeEngine();
    lProcessor.getEngine().reset(true);

    AbstractAcquistionInstruction lAcquisitionScheduler;
    FusionInstruction lFusionScheduler;
    switch (mAcquisitionType)
    {
      case TimelapseSequential:
        lAcquisitionScheduler = mLightSheetMicroscope.getDevice(SequentialAcquisitionInstruction.class, 0);
        lFusionScheduler = mLightSheetMicroscope.getDevice(SequentialFusionInstruction.class, 0);
        break;
      case TimeLapseOpticallyCameraFused:
        lAcquisitionScheduler = mLightSheetMicroscope.getDevice(OpticsPrefusedAcquisitionInstruction.class, 0);
        lFusionScheduler = mLightSheetMicroscope.getDevice(OpticsPrefusedFusionInstruction.class, 0);
        break;
      case TimeLapseInterleaved:
      default:
        lAcquisitionScheduler = mLightSheetMicroscope.getDevice(InterleavedAcquisitionInstruction.class, 0);
        lFusionScheduler = mLightSheetMicroscope.getDevice(InterleavedFusionInstruction.class, 0);
        break;
    }

    lAcquisitionScheduler.initialize();
    lAcquisitionScheduler.enqueue(0);

    lFusionScheduler.initialize();
    lFusionScheduler.enqueue(0);

    StackInterface lStack = ((FusedImageDataContainer) mLightSheetMicroscope.getDataWarehouse().getOldestContainer(FusedImageDataContainer.class)).get("fused");
    return lStack;
  }

  public LightSheetMicroscope getLightSheetMicroscope()
  {
    return mLightSheetMicroscope;
  }

  public void setAcquisitionType(AcquisitionType pAcquisitionType)
  {
    if (pAcquisitionType == AcquisitionType.Interactive || pAcquisitionType == AcquisitionType.TimeLapse)
    {
      warning("Acquistion type " + pAcquisitionType + " is not supported!");
      return;
    }
    this.mAcquisitionType = pAcquisitionType;
  }

  public void setMinZ(double pMinZ)
  {
    this.mMinZ = pMinZ;
  }

  public void setMaxZ(double pMaxZ)
  {
    this.mMaxZ = pMaxZ;
  }

  public void setSliceDistance(double pSliceDistance)
  {
    this.mSliceDistance = pSliceDistance;
  }

  public void setExposureTimeInSeconds(double pExposureTimeInSeconds)
  {
    this.mExposureTimeInSeconds = pExposureTimeInSeconds;
  }

  public void setImageHeight(int pImageHeight)
  {
    this.mImageHeight = pImageHeight;
  }

  public void setImageWidth(int pImageWidth)
  {
    this.mImageWidth = pImageWidth;
  }
}
