package fastfuse.tasks;

import java.io.IOException;
import java.util.stream.IntStream;

import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.enums.ImageChannelDataType;
import fastfuse.FastFusionEngineInterface;
import fastfuse.pool.FastFusionMemoryPool;

import org.apache.commons.lang3.tuple.MutablePair;

public class GaussianBlurTask extends TaskBase
                              implements TaskInterface
{
  private final String mSrcImageKey, mDstImageKey;
  private final int[] mKernelSizes;
  private final float[] mKernelSigmas;
  private final Boolean mSeparable;

  public GaussianBlurTask(String pSrcImageKey,
                          String pDstImageKey,
                          float[] pKernelSigmas,
                          int[] pKernelSizes,
                          Boolean pSeparable)
  {
    super(pSrcImageKey);
    setupProgram(GaussianBlurTask.class, "./kernels/blur.cl");
    mSrcImageKey = pSrcImageKey;
    mDstImageKey = pDstImageKey;
    assert pKernelSigmas != null && pKernelSigmas.length == 3;
    assert pKernelSizes == null || pKernelSizes.length == 3;
    if (pKernelSizes == null)
      pKernelSizes = IntStream.range(0, 3)
                              .map(i -> getKernelSize(pKernelSigmas[i]))
                              .toArray();
    for (int i = 0; i < 3; i++)
    {
      assert pKernelSizes[i] % 2 == 1;
      assert pKernelSigmas[i] > 0;
    }
    mKernelSizes = pKernelSizes;
    mKernelSigmas = pKernelSigmas;
    mSeparable = pSeparable;
  }

  public GaussianBlurTask(String pSrcImageKey,
                          String pDstImageKey,
                          float[] pKernelSigmas,
                          int[] pKernelSizes)
  {
    this(pSrcImageKey,
         pDstImageKey,
         pKernelSigmas,
         pKernelSizes,
         null);
  }

  public GaussianBlurTask(String pSrcImageKey,
                          String pDstImageKey,
                          float[] pKernelSigmas)
  {
    this(pSrcImageKey, pDstImageKey, pKernelSigmas, null, null);
  }

  private int getKernelSize(float sigma)
  {
    int lSize = Math.max(1, (int) Math.round(2 * 3.5 * sigma));
    return (lSize % 2 == 1) ? lSize : lSize + 1;
  }

  @Override
  public boolean enqueue(FastFusionEngineInterface pFastFusionEngine,
                         boolean pWaitToFinish)
  {

    ClearCLImage lSrcImage, lDstImage, lTmpImage = null;
    lSrcImage = pFastFusionEngine.getImage(mSrcImageKey);
    assert TaskHelper.allowedDataType(lSrcImage);

    boolean lSeparable;
    if (mSeparable != null)
                           // specifically requested
                           lSeparable = mSeparable;
    else
    {
      // check requirements for separable
      lSeparable =
                 lSrcImage.getChannelDataType() == ImageChannelDataType.Float
                   && (mKernelSizes[0] * mKernelSizes[1]
                       * mKernelSizes[2] > 100);
    }

    if (lSeparable)
    {
      assert lSrcImage.getChannelDataType() == ImageChannelDataType.Float;
      // get temporary image
      lTmpImage =
                FastFusionMemoryPool.get()
                                    .requestImage(ImageChannelDataType.Float,
                                                  lSrcImage.getDimensions());
    }

    MutablePair<Boolean, ClearCLImage> lFlagAndDstImage =
                                                        pFastFusionEngine.ensureImageAllocated(mDstImageKey,
                                                                                               lSrcImage.getChannelDataType(),
                                                                                               lSrcImage.getDimensions());
    lDstImage = lFlagAndDstImage.getRight();

    try
    {
      // TODO: test
      ClearCLKernel lKernel;
      if (lSeparable)
      {
        lKernel = getKernel(lSrcImage.getContext(),
                            "gaussian_blur_sep_image3d",
                            TaskHelper.getOpenCLDefines(lSrcImage,
                                                        lDstImage));
        lKernel.setGlobalSizes(lSrcImage.getDimensions());
        lKernel.setArguments(lDstImage,
                             lSrcImage,
                             0,
                             mKernelSizes[0],
                             mKernelSigmas[0]);
        runKernel(lKernel, pWaitToFinish);
        lKernel.setArguments(lTmpImage,
                             lDstImage,
                             1,
                             mKernelSizes[1],
                             mKernelSigmas[1]);
        runKernel(lKernel, pWaitToFinish);
        lKernel.setArguments(lDstImage,
                             lTmpImage,
                             2,
                             mKernelSizes[2],
                             mKernelSigmas[2]);
        runKernel(lKernel, pWaitToFinish);
        FastFusionMemoryPool.get().releaseImage(lTmpImage);
        lFlagAndDstImage.setLeft(true);
        return true;
      }
      else
      {
        lKernel = getKernel(lSrcImage.getContext(),
                            "gaussian_blur_image3d",
                            TaskHelper.getOpenCLDefines(lSrcImage,
                                                        lDstImage));
        lKernel.setGlobalSizes(lSrcImage.getDimensions());
        lKernel.setArguments(lDstImage,
                             lSrcImage,
                             mKernelSizes[0],
                             mKernelSizes[1],
                             mKernelSizes[2],
                             mKernelSigmas[0],
                             mKernelSigmas[1],
                             mKernelSigmas[2]);
        runKernel(lKernel, pWaitToFinish);
        lFlagAndDstImage.setLeft(true);
        return true;
      }
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    }
  }

}
