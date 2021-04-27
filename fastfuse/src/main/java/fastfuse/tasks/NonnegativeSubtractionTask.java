package fastfuse.tasks;

import java.io.IOException;

import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.enums.ImageChannelDataType;
import fastfuse.FastFusionEngineInterface;

import org.apache.commons.lang3.tuple.MutablePair;

public class NonnegativeSubtractionTask extends TaskBase
                                        implements TaskInterface
{

  private final String mSrc1ImageKey, mSrc2ImageKey, mDstImageKey;
  private final Number mSrc2Number;
  private final ImageChannelDataType mDstDataType;

  public NonnegativeSubtractionTask(String pSrc1ImageKey,
                                    Number pSrc2Number,
                                    String pDstImageKey)
  {
    this(pSrc1ImageKey, pSrc2Number, pDstImageKey, null);
  }

  public NonnegativeSubtractionTask(String pSrc1ImageKey,
                                    Number pSrc2Number,
                                    String pDstImageKey,
                                    ImageChannelDataType pDstDataType)
  {
    super(pSrc1ImageKey);
    setupProgram(NonnegativeSubtractionTask.class,
                 "./kernels/arithmetic.cl");
    mSrc1ImageKey = pSrc1ImageKey;
    mSrc2ImageKey = null;
    mSrc2Number = pSrc2Number;
    mDstImageKey = pDstImageKey;
    mDstDataType = pDstDataType;
  }

  public NonnegativeSubtractionTask(String pSrc1ImageKey,
                                    String pSrc2ImageKey,
                                    String pDstImageKey)
  {
    this(pSrc1ImageKey, pSrc2ImageKey, pDstImageKey, null);
  }

  public NonnegativeSubtractionTask(String pSrc1ImageKey,
                                    String pSrc2ImageKey,
                                    String pDstImageKey,
                                    ImageChannelDataType pDstDataType)
  {
    super(pSrc1ImageKey, pSrc2ImageKey);
    setupProgram(NonnegativeSubtractionTask.class,
                 "./kernels/arithmetic.cl");
    mSrc1ImageKey = pSrc1ImageKey;
    mSrc2ImageKey = pSrc2ImageKey;
    mSrc2Number = null;
    mDstImageKey = pDstImageKey;
    mDstDataType = pDstDataType;
  }

  @Override
  public boolean enqueue(FastFusionEngineInterface pFastFusionEngine,
                         boolean pWaitToFinish)
  {

    ClearCLImage lSrc1Image, lDstImage;
    lSrc1Image = pFastFusionEngine.getImage(mSrc1ImageKey);

    ImageChannelDataType lDstDataType = mDstDataType;
    if (lDstDataType == null)
      lDstDataType = lSrc1Image.getChannelDataType();

    MutablePair<Boolean, ClearCLImage> lFlagAndDstImage =
                                                        pFastFusionEngine.ensureImageAllocated(mDstImageKey,
                                                                                               lDstDataType,
                                                                                               lSrc1Image.getDimensions());
    lDstImage = lFlagAndDstImage.getRight();
    assert TaskHelper.allowedDataType(lSrc1Image, lDstImage);

    try
    {
      if (mSrc2ImageKey == null)
      {
        assert mSrc2Number != null;
        float lConstant = mSrc2Number.floatValue();

        ClearCLKernel lKernel =
                              getKernel(lSrc1Image.getContext(),
                                        "subtract_constant",
                                        TaskHelper.getOpenCLDefines(lSrc1Image,
                                                                    lDstImage));
        lKernel.setGlobalSizes(lDstImage.getDimensions());
        lKernel.setArguments(lDstImage, lSrc1Image, lConstant, 0f);
        runKernel(lKernel, pWaitToFinish);
        lFlagAndDstImage.setLeft(true);
        return true;

      }
      else
      {
        assert mSrc2ImageKey != null;
        ClearCLImage lSrc2Image =
                                pFastFusionEngine.getImage(mSrc2ImageKey);
        assert lSrc2Image.getChannelDataType() == lSrc1Image.getChannelDataType();

        ClearCLKernel lKernel =
                              getKernel(lSrc1Image.getContext(),
                                        "subtract_image",
                                        TaskHelper.getOpenCLDefines(lSrc1Image,
                                                                    lDstImage));
        lKernel.setGlobalSizes(lDstImage.getDimensions());
        lKernel.setArguments(lDstImage, lSrc1Image, lSrc2Image, 0f);
        runKernel(lKernel, pWaitToFinish);
        lFlagAndDstImage.setLeft(true);
        return true;
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return false;
    }

  }

}
