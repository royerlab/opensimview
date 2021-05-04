package fastfuse.tasks;

import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.enums.ImageChannelDataType;
import fastfuse.FastFusionEngineInterface;
import org.apache.commons.lang3.tuple.MutablePair;

import java.io.IOException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TenengradAdvancedFusionTask extends TaskBase implements TaskInterface
{
  private final String[] mSrcImageKeys, mSrcWeightKeys;
  private final String mDstImageKey;
  private final ImageChannelDataType mDstImageDataType;

  public TenengradAdvancedFusionTask(String pDstImageKey, ImageChannelDataType pDstImageDataType, String... pSrcImageAndWeightKeys)
  {
    super(pSrcImageAndWeightKeys);
    assert pSrcImageAndWeightKeys != null && pSrcImageAndWeightKeys.length % 2 == 0;
    int lNumImages = pSrcImageAndWeightKeys.length / 2;
    mSrcImageKeys = IntStream.range(0, lNumImages).mapToObj(i -> pSrcImageAndWeightKeys[i]).toArray(String[]::new);
    mSrcWeightKeys = IntStream.range(lNumImages, 2 * lNumImages).mapToObj(i -> pSrcImageAndWeightKeys[i]).toArray(String[]::new);
    mDstImageKey = pDstImageKey;
    mDstImageDataType = pDstImageDataType;
    setupProgram(TenengradAdvancedFusionTask.class, "./kernels/fusion.cl");
  }

  @Override
  public boolean enqueue(FastFusionEngineInterface pFastFusionEngine, boolean pWaitToFinish)
  {
    int lNumImages = mSrcImageKeys.length;
    ClearCLImage[] lSrcImages, lSrcWeights;

    lSrcImages = Stream.of(mSrcImageKeys).map(pFastFusionEngine::getImage).toArray(ClearCLImage[]::new);
    lSrcWeights = Stream.of(mSrcWeightKeys).map(pFastFusionEngine::getImage).toArray(ClearCLImage[]::new);

    ImageChannelDataType lDstImageDataType = mDstImageDataType;
    if (lDstImageDataType == null) lDstImageDataType = lSrcImages[0].getChannelDataType();

    // check data types
    assert TaskHelper.allSameAllowedDataType(lSrcImages);
    assert TaskHelper.allowedDataType(lDstImageDataType);
    assert TaskHelper.allSameDataType(ImageChannelDataType.Float, lSrcWeights);
    // get and check dimensions
    assert TaskHelper.allSameDimensions(lSrcImages);
    assert TaskHelper.allSameDimensions(lSrcWeights);
    long[] lImageDims = lSrcImages[0].getDimensions();
    long[] lWeightDims = lSrcWeights[0].getDimensions();
    assert lImageDims[0] % lWeightDims[0] == 0 && lImageDims[1] % lWeightDims[1] == 0;
    assert (lImageDims[0] / lWeightDims[0]) == (lImageDims[1] / lWeightDims[1]);
    assert lImageDims[2] == lWeightDims[2];
    int lDimRatio = (int) (lImageDims[0] / lWeightDims[0]);

    MutablePair<Boolean, ClearCLImage> lFlagAndDstImage = pFastFusionEngine.ensureImageAllocated(mDstImageKey, lDstImageDataType, lSrcImages[0].getDimensions());
    ClearCLImage lDstImage = lFlagAndDstImage.getRight();

    try
    {
      String lKernelName = String.format("tenengrad_fusion_with_provided_weights_%d_images", lNumImages);
      ClearCLKernel lKernel = getKernel(lDstImage.getContext(), lKernelName, TaskHelper.getOpenCLDefines(lSrcImages[0], lDstImage));
      int i = 0;
      lKernel.setArgument(i++, lDstImage);
      lKernel.setArgument(i++, lDimRatio);
      for (ClearCLImage lImage : lSrcImages)
        lKernel.setArgument(i++, lImage);
      for (ClearCLImage lWeight : lSrcWeights)
        lKernel.setArgument(i++, lWeight);

      lKernel.setGlobalSizes(lDstImage.getDimensions());
      runKernel(lKernel, pWaitToFinish);
      lFlagAndDstImage.setLeft(true);
      return true;
    } catch (IOException e)
    {
      e.printStackTrace();
      return false;
    }

  }

}
