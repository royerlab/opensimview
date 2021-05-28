package clearcontrol.timelapse.instructions;

import clearcontrol.LightSheetMicroscope;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;

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
public class SingleViewAcquisitionInstruction extends SequentialAcquisitionInstruction implements InstructionInterface, LoggingFeature
{
  int mCameraIndex;
  int mLightSheetIndex;
  String mChannelName;

  public SingleViewAcquisitionInstruction(int pCameraIndex, int pLightSheetIndex, LightSheetMicroscope pLightSheetMicroscope, String pChannelName)
  {
    super("Acquisition: Single view C" + pCameraIndex + "L" + pLightSheetIndex, pLightSheetMicroscope, pChannelName);
    mCameraIndex = pCameraIndex;
    mLightSheetIndex = pLightSheetIndex;
    mChannelName = pChannelName;
  }

  protected boolean isLightSheetOn(int pLightIndex)
  {
    return mLightSheetIndex == pLightIndex;
  }

  protected boolean isCameraOn(int pCameraIndex)
  {
    return mCameraIndex == pCameraIndex;
  }

  public int getLightSheetIndex()
  {
    return mLightSheetIndex;
  }

  public int getCameraIndex()
  {
    return mCameraIndex;
  }


  @Override
  public SingleViewAcquisitionInstruction copy()
  {
    return new SingleViewAcquisitionInstruction(mCameraIndex, mLightSheetIndex, getLightSheetMicroscope(), mChannelName);
  }
}
