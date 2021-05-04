package fastfuse.tasks;

import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import fastfuse.FastFusionEngineInterface;
import fastfuse.FastFusionException;
import org.apache.commons.lang3.tuple.MutablePair;

/**
 * @author royer
 */
public class FlipTask extends TaskBase implements TaskInterface
{
  private String mInputImageSlotKey, mDestImageSlotKey;

  private boolean mFlipX, mFlipY, mFlipZ;

  /**
   * Instantiates stack X axis flip task given the keys for two input images and
   * destination image
   *
   * @param pInputImageSlotKey input image slot key
   * @param pDestImageSlotKey  destination image slot key
   * @return x axis flip task
   */
  public static FlipTask flipX(String pInputImageSlotKey, String pDestImageSlotKey)
  {
    FlipTask lFlipTask = new FlipTask(pInputImageSlotKey, pDestImageSlotKey);
    lFlipTask.setFlipX(true);
    return lFlipTask;
  }

  /**
   * Instantiates stack Y axis flip task given the keys for two input images and
   * destination image
   *
   * @param pInputImageSlotKey input image slot key
   * @param pDestImageSlotKey  destination image slot key
   * @return y axis flip task
   */
  public static FlipTask flipY(String pInputImageSlotKey, String pDestImageSlotKey)
  {
    FlipTask lFlipTask = new FlipTask(pInputImageSlotKey, pDestImageSlotKey);
    lFlipTask.setFlipY(true);
    return lFlipTask;
  }

  /**
   * Instantiates stack Z flip task given the keys for two input images and
   * destination image
   *
   * @param pInputImageSlotKey input image slot key
   * @param pDestImageSlotKey  destination image slot key
   * @return z axis flip task
   */
  public static FlipTask flipZ(String pInputImageSlotKey, String pDestImageSlotKey)
  {
    FlipTask lFlipTask = new FlipTask(pInputImageSlotKey, pDestImageSlotKey);
    lFlipTask.setFlipZ(true);
    return lFlipTask;
  }

  /**
   * Instantiates stack X,Y,Z axis flip task given the keys for two input images
   * and destination image
   *
   * @param pInputImageSlotKey input image slot key
   * @param pDestImageSlotKey  destination image slot key
   */
  public FlipTask(String pInputImageSlotKey, String pDestImageSlotKey)
  {
    super(pInputImageSlotKey);
    mInputImageSlotKey = pInputImageSlotKey;
    mDestImageSlotKey = pDestImageSlotKey;
    setupProgram(FusionTaskBase.class, "./kernels/flip.cl");
  }

  @Override
  public boolean enqueue(FastFusionEngineInterface pFastFusionEngine, boolean pWaitToFinish)
  {
    // First we prepare the images

    ClearCLImage lInputImage = pFastFusionEngine.getImage(mInputImageSlotKey);

    if (lInputImage == null) throw new FastFusionException("Fusion task %s received a null image", this);

    assert TaskHelper.allowedDataType(lInputImage);

    MutablePair<Boolean, ClearCLImage> lDestImageAndFlag = pFastFusionEngine.ensureImageAllocated(mDestImageSlotKey, lInputImage.getChannelDataType(), lInputImage.getDimensions());

    ClearCLImage lDestImage = lDestImageAndFlag.getValue();

    ClearCLKernel lKernel = null;

    try
    {
      if (lInputImage.isFloat()) lKernel = getKernel(lDestImage.getContext(), "flip_f");
      else if (lInputImage.isInteger()) lKernel = getKernel(lDestImage.getContext(), "flip_ui");
    } catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }

    lKernel.setArgument("flipx", isFlipX() ? 1 : 0);
    lKernel.setArgument("flipy", isFlipY() ? 1 : 0);
    lKernel.setArgument("flipz", isFlipZ() ? 1 : 0);
    lKernel.setArgument("imagein", lInputImage);
    lKernel.setArgument("imageout", lDestImage);

    lKernel.setGlobalSizes(lDestImage);

    // System.out.println("running kernel");
    runKernel(lKernel, pWaitToFinish);
    lDestImageAndFlag.setLeft(true);

    return true;

  }

  public boolean isFlipX()
  {
    return mFlipX;
  }

  public void setFlipX(boolean pFlipX)
  {
    mFlipX = pFlipX;
  }

  public boolean isFlipY()
  {
    return mFlipY;
  }

  public void setFlipY(boolean pFlipY)
  {
    mFlipY = pFlipY;
  }

  public boolean isFlipZ()
  {
    return mFlipZ;
  }

  public void setFlipZ(boolean pFlipZ)
  {
    mFlipZ = pFlipZ;
  }

}
