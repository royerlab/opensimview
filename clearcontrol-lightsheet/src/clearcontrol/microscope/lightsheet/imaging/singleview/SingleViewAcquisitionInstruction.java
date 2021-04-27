package clearcontrol.microscope.lightsheet.imaging.singleview;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialAcquisitionInstruction;
import clearcontrol.stack.StackInterface;

/**
 * This instructions acquires a single image stack for a defined camera and light sheet.
 * The image stacks are stored in the DataWarehouse in an StackInterfaceContainer with a
 * key like:
 * <p>
 * C0L0
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) February
 * 2018
 */
public class SingleViewAcquisitionInstruction extends SequentialAcquisitionInstruction implements
                                                                                       InstructionInterface,
                                                                                       LoggingFeature
{
  int mCameraIndex;
  int mLightSheetIndex;

  public SingleViewAcquisitionInstruction(int pCameraIndex,
                                          int pLightSheetIndex,
                                          LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Acquisition: Single view C" + pCameraIndex + "L" + pLightSheetIndex,
          pLightSheetMicroscope);
    mCameraIndex = pCameraIndex;
    mLightSheetIndex = pLightSheetIndex;

    mImageKeyToSave = "C" + pCameraIndex + "L" + pLightSheetIndex;
    mChannelName.set(mImageKeyToSave);
  }

  protected boolean isLightSheetOn(int pLightIndex)
  {
    return mLightSheetIndex == pLightIndex;
  }

  protected boolean isCameraOn(int pCameraIndex)
  {
    return mCameraIndex == pCameraIndex;
  }

  protected boolean isFused()
  {
    return true;
  }

  public int getLightSheetIndex()
  {
    return mLightSheetIndex;
  }

  public int getCameraIndex()
  {
    return mCameraIndex;
  }

  @Override public StackInterface getLastAcquiredStack()
  {
    return mLastAcquiredStack;
  }

  @Override public SingleViewAcquisitionInstruction copy()
  {
    return new SingleViewAcquisitionInstruction(mCameraIndex,
                                                mLightSheetIndex,
                                                getLightSheetMicroscope());
  }
}
