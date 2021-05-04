package fastfuse.tasks;

import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.enums.ImageChannelDataType;
import fastfuse.FastFusionEngineInterface;
import org.apache.commons.lang3.tuple.MutablePair;

import java.io.IOException;

public class TenengradWeightTask extends TaskBase implements TaskInterface
{

  private final String mSrcImageKey, mDstImageKey;

  public TenengradWeightTask(String pSrcImageKey, String pDstImageKey)
  {
    super(pSrcImageKey);
    setupProgram(TenengradWeightTask.class, "./kernels/fusion.cl");
    mSrcImageKey = pSrcImageKey;
    mDstImageKey = pDstImageKey;
  }

  @Override
  public boolean enqueue(FastFusionEngineInterface pFastFusionEngine, boolean pWaitToFinish)
  {
    ClearCLImage lSrcImage, lDstImage;
    lSrcImage = pFastFusionEngine.getImage(mSrcImageKey);
    assert TaskHelper.allowedDataType(lSrcImage);

    MutablePair<Boolean, ClearCLImage> lFlagAndDstImage = pFastFusionEngine.ensureImageAllocated(mDstImageKey, ImageChannelDataType.Float, lSrcImage.getDimensions());
    lDstImage = lFlagAndDstImage.getRight();

    try
    {
      ClearCLKernel lKernel;
      lKernel = getKernel(lSrcImage.getContext(), "tenengrad_weight_unnormalized", TaskHelper.getOpenCLDefines(lSrcImage, lDstImage));
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
