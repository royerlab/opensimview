package fastfuse.tasks;

import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import fastfuse.FastFusionEngineInterface;
import fastfuse.FastFusionException;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * This Task allows to split a stack with slices (0,1,2,3,4,5,6,7) into four
 * stacks with slices (0,4), (1,5), (2,6) and (3,7) This is necessary for
 * interleaved image acquisition in clearcontrol-lightsheet
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) February 2018
 */
public class StackSplitTask extends TaskBase implements TaskInterface
{

  private final String mInputImageSlotKey;
  private final String[] mDestImageSlotKeys;
  private boolean mDownsampleXYByHalf;

  public static ArrayList<TaskInterface> splitStackAndReleaseInputs(String pInputImageSlotKey, String[] pDestImageSlotKeys, boolean pDownsampleXYByHalf)
  {
    ArrayList<TaskInterface> lList = new ArrayList<TaskInterface>();

    lList.add(new StackSplitTask(pInputImageSlotKey, pDestImageSlotKeys, pDownsampleXYByHalf));
    lList.add(new MemoryReleaseTask(Arrays.asList(pDestImageSlotKeys), pInputImageSlotKey));

    return lList;
  }

  public StackSplitTask(String pInputImageSlotKey, String[] pDestImageSlotKeys)
  {
    this(pInputImageSlotKey, pDestImageSlotKeys, false);
  }

  public StackSplitTask(String pInputImageSlotKey, String[] pDestImageSlotKeys, boolean pDownsampleXYByHalf)
  {

    super(pInputImageSlotKey);
    mInputImageSlotKey = pInputImageSlotKey;
    mDestImageSlotKeys = pDestImageSlotKeys;
    mDownsampleXYByHalf = pDownsampleXYByHalf;
    setupProgram(FusionTaskBase.class, "./kernels/stacksplitting.cl");
  }

  @Override
  public boolean enqueue(FastFusionEngineInterface pFastFusionEngine, boolean pWaitToFinish)
  {

    ClearCLImage lInputImage = pFastFusionEngine.getImage(mInputImageSlotKey);

    if (lInputImage == null) throw new FastFusionException("Fusion task %s received a null image", this);

    assert TaskHelper.allowedDataType(lInputImage);

    ArrayList<MutablePair<Boolean, ClearCLImage>> lResultImagesAndFlags = new ArrayList<>();

    ClearCLKernel lKernel = null;

    Map<String, Object> lDefines = TaskHelper.getOpenCLDefines(lInputImage, lInputImage);

    try
    {
      if (!mDownsampleXYByHalf)
      {
        lKernel = getKernel(lInputImage.getContext(), "convert_interleaved_to_stacks_" + mDestImageSlotKeys.length, lDefines);
      } else
      {
        lKernel = getKernel(lInputImage.getContext(), "convert_interleaved_to_stacks_" + mDestImageSlotKeys.length + "_and_downsample_xy_by_half_nearest", lDefines);
      }
    } catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }

    lKernel.setArgument("src", lInputImage);

    long[] lSrcDims = lInputImage.getDimensions();
    assert lSrcDims.length == 3;
    assert lSrcDims[0] % 2 == 0 && lSrcDims[1] % 2 == 0;
    long[] lDstDims = new long[]{lSrcDims[0], lSrcDims[1], lSrcDims[2] / 4};
    if (mDownsampleXYByHalf)
    {
      lDstDims[0] = lDstDims[0] / 2;
      lDstDims[1] = lDstDims[1] / 2;
    }

    int lDestCount = 0;
    for (String lDestImageSlotKey : mDestImageSlotKeys)
    {
      MutablePair<Boolean, ClearCLImage> lDestImageAndFlag = pFastFusionEngine.ensureImageAllocated(lDestImageSlotKey, lInputImage.getChannelDataType(), lDstDims);
      lResultImagesAndFlags.add(lDestImageAndFlag);

      ClearCLImage lDestImage = lDestImageAndFlag.getValue();
      lKernel.setArgument("dst" + lDestCount, lDestImage);
      lKernel.setGlobalSizes(lDestImage);
      lDestCount++;
    }

    // System.out.println("running kernel");
    runKernel(lKernel, pWaitToFinish);

    for (MutablePair<Boolean, ClearCLImage> lDestImageAndFlag : lResultImagesAndFlags)
    {
      lDestImageAndFlag.setLeft(true);
    }

    return true;
  }

}
