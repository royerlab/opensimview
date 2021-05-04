package fastfuse.tasks;

import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.enums.ImageChannelDataType;
import fastfuse.FastFusionEngineInterface;
import org.apache.commons.lang3.tuple.MutablePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DownsampleXYbyHalfTask extends TaskBase implements TaskInterface
{

  private final String mSrcImageKey, mDstImageKey;
  private final ImageChannelDataType mDstDataType;
  private final Type mType;

  public enum Type
  {
    Nearest("downsample_xy_by_half_nearest"), Average("downsample_xy_by_half_average"), Median("downsample_xy_by_half_median");

    private final String mKernelName;

    private Type(String pKernelName)
    {
      mKernelName = pKernelName;
    }

    public String getKernelName()
    {
      return mKernelName;
    }

  }

  ;

  public static List<TaskInterface> applyAndReleaseInputs(Type pType, String pSuffix, String... pImageKeys)
  {
    return apply(true, pType, pSuffix, pImageKeys);
  }

  public static List<TaskInterface> applyAndKeepInputs(Type pType, String pSuffix, String... pImageKeys)
  {
    return apply(false, pType, pSuffix, pImageKeys);
  }

  public static List<TaskInterface> apply(boolean pReleaseInput, Type pType, String pSuffix, String... pImageKeys)
  {
    List<TaskInterface> lTaskList = new ArrayList<>();
    for (String lSrcKey : pImageKeys)
    {
      String lDstKey = lSrcKey + pSuffix;
      lTaskList.add(new DownsampleXYbyHalfTask(lSrcKey, lDstKey, pType));
      if (pReleaseInput) lTaskList.add(new MemoryReleaseTask(lDstKey, lSrcKey));
    }
    return lTaskList;
  }

  public DownsampleXYbyHalfTask(String pSrcImageKey, String pDstImageKey)
  {
    this(pSrcImageKey, pDstImageKey, Type.Median, null);
  }

  public DownsampleXYbyHalfTask(String pSrcImageKey, String pDstImageKey, Type pType)
  {
    this(pSrcImageKey, pDstImageKey, pType, null);
  }

  public DownsampleXYbyHalfTask(String pSrcImageKey, String pDstImageKey, Type pType, ImageChannelDataType pDstDataType)
  {
    super(pSrcImageKey);
    setupProgram(DownsampleXYbyHalfTask.class, "./kernels/downsampling.cl");
    mSrcImageKey = pSrcImageKey;
    mDstImageKey = pDstImageKey;
    mDstDataType = pDstDataType;
    mType = pType;
  }

  @Override
  public boolean enqueue(FastFusionEngineInterface pFastFusionEngine, boolean pWaitToFinish)
  {
    ClearCLImage lSrcImage, lDstImage;
    lSrcImage = pFastFusionEngine.getImage(mSrcImageKey);

    long[] lSrcDims = lSrcImage.getDimensions();
    assert lSrcDims.length == 3;
    assert lSrcDims[0] % 2 == 0 && lSrcDims[1] % 2 == 0;
    long[] lDstDims = new long[]{lSrcDims[0] / 2, lSrcDims[1] / 2, lSrcDims[2]};

    ImageChannelDataType lDstDataType = mDstDataType;
    if (lDstDataType == null) lDstDataType = lSrcImage.getChannelDataType();

    MutablePair<Boolean, ClearCLImage> lFlagAndDstImage = pFastFusionEngine.ensureImageAllocated(mDstImageKey, lDstDataType, lDstDims);
    lDstImage = lFlagAndDstImage.getRight();

    assert TaskHelper.allowedDataType(lSrcImage, lDstImage);

    try
    {
      // TODO: test
      ClearCLKernel lKernel;
      lKernel = getKernel(lSrcImage.getContext(), mType.getKernelName(), TaskHelper.getOpenCLDefines(lSrcImage, lDstImage));
      lKernel.setGlobalSizes(lDstImage.getDimensions());
      lKernel.setArguments(lDstImage, lSrcImage);
      runKernel(lKernel, pWaitToFinish);
      lFlagAndDstImage.setLeft(true);
      return true;
    } catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    }
  }

}
