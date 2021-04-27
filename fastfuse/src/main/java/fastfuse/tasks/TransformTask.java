package fastfuse.tasks;

import javax.vecmath.Matrix4f;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.util.MatrixUtils;
import fastfuse.FastFusionEngineInterface;
import fastfuse.FastFusionException;

import org.apache.commons.lang3.tuple.MutablePair;
import simbryo.util.geom.GeometryUtils;

/**
 * Fuses two stacks using the average method.
 *
 * @author royer
 */
public class TransformTask extends TaskBase implements TaskInterface
{
  private String mInputImageSlotKey, mDestImageSlotKey;
  private ClearCLBuffer mTransformMatrixBuffer;

  private Matrix4f mTransformMatrix = GeometryUtils.getIdentity();

  /**
   * Instantiates an average fusion task given the keys for two input images and
   * destination image
   * 
   * @param pInputImageSlotKey
   *          input image slot key
   * @param pDestImageSlotKey
   *          destination image slot key
   */
  public TransformTask(String pInputImageSlotKey,
                       String pDestImageSlotKey)
  {
    super(pInputImageSlotKey);
    mInputImageSlotKey = pInputImageSlotKey;
    mDestImageSlotKey = pDestImageSlotKey;
    setupProgram(FusionTaskBase.class, "./kernels/transform.cl");
  }

  @Override
  public boolean enqueue(FastFusionEngineInterface pFastFusionEngine,
                         boolean pWaitToFinish)
  {
    // First we prepare the images

    ClearCLImage lInputImage =
                             pFastFusionEngine.getImage(mInputImageSlotKey);

    if (lInputImage == null)
      throw new FastFusionException("Fusion task %s received a null image",
                                    this);

    MutablePair<Boolean, ClearCLImage> lImageAndFlag =
                                                     pFastFusionEngine.ensureImageAllocated(mDestImageSlotKey,
                                                                                            lInputImage.getChannelDataType(),
                                                                                            lInputImage.getDimensions());

    ClearCLImage lImageFused = lImageAndFlag.getValue();

    ClearCLKernel lKernel = null;

    try
    {

      lKernel = getKernel(lImageFused.getContext(), "transform");

    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }

    Matrix4f lInverseTransformMatrix = new Matrix4f(mTransformMatrix);

    lInverseTransformMatrix.invert();

    mTransformMatrixBuffer =
                           MatrixUtils.matrixToBuffer(lImageFused.getContext(),
                                                      mTransformMatrixBuffer,
                                                      lInverseTransformMatrix);

    lKernel.setArgument("imagein", lInputImage);
    lKernel.setArgument("imagedest", lImageFused);

    lKernel.setGlobalSizes(lImageFused);

    // System.out.println("running kernel");
    runKernel(lKernel, pWaitToFinish);
    lImageAndFlag.setLeft(true);

    return true;

  }

}
