package fastfuse.tasks;

import java.util.Arrays;

import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import clearcl.viewer.ClearCLImageViewer;
import fastfuse.FastFusionEngineInterface;
import fastfuse.FastFusionException;

import org.apache.commons.lang3.tuple.MutablePair;

/**
 * Base class providing common machinery for fusing 2 or 4 stacks together.
 *
 * @author royer
 */
public abstract class FusionTaskBase extends TaskBase
                                     implements TaskInterface
{
  protected final String[] mInputImagesSlotKeys;
  protected final String mDestImageSlotKey;
  protected ClearCLImageViewer mViewA, mViewB, mViewFused;
  protected volatile boolean mDebugDisplay = false;
  protected ImageChannelDataType mDestinationImageDataType;

  /**
   * Instantiates an average fusion task given the keys for two input images and
   * destination image
   * 
   * @param pImageASlotKey
   *          image A slot key
   * @param pImageBSlotKey
   *          image B slot key
   * @param pDestImageKey
   *          destination image key
   */
  public FusionTaskBase(String pImageASlotKey,
                        String pImageBSlotKey,
                        String pDestImageKey)
  {
    super(pImageASlotKey, pImageBSlotKey);
    mInputImagesSlotKeys = new String[]
    { pImageASlotKey, pImageBSlotKey };
    mDestImageSlotKey = pDestImageKey;

    mDestinationImageDataType = ImageChannelDataType.UnsignedInt16;
  }

  /**
   * Instantiates an average fusion task given the keys for the four input
   * images and destination image.
   * 
   * @param pImageASlotKey
   *          image A key
   * @param pImageBSlotKey
   *          image B key
   * @param pImageCSlotKey
   *          image C key
   * @param pImageDSlotKey
   *          image D key
   * @param pDestImageSlotKey
   *          destination image key
   */
  public FusionTaskBase(String pImageASlotKey,
                        String pImageBSlotKey,
                        String pImageCSlotKey,
                        String pImageDSlotKey,
                        String pDestImageSlotKey)
  {
    super(pImageASlotKey,
          pImageBSlotKey,
          pImageCSlotKey,
          pImageDSlotKey);
    mInputImagesSlotKeys = new String[]
    { pImageASlotKey,
      pImageBSlotKey,
      pImageCSlotKey,
      pImageDSlotKey };
    mDestImageSlotKey = pDestImageSlotKey;

    mDestinationImageDataType = ImageChannelDataType.UnsignedInt16;
  }

  @Override
  public boolean enqueue(FastFusionEngineInterface pFastFusionEngine,
                         boolean pWaitToFinish)
  {
    // First we prepare the images

    ClearCLImage lImageA, lImageB, lImageC = null, lImageD = null;

    lImageA = pFastFusionEngine.getImage(mInputImagesSlotKeys[0]);
    lImageB = pFastFusionEngine.getImage(mInputImagesSlotKeys[1]);

    if (lImageA == null || lImageB == null)
      throw new FastFusionException("Fusion task %s received a null image",
                                    this);

    if (!Arrays.equals(lImageA.getDimensions(),
                       lImageB.getDimensions()))
      throw new FastFusionException("Fusion task %s received two images of incompatible dimensions: %s and %s",
                                    this,
                                    Arrays.toString(lImageA.getDimensions()),
                                    Arrays.toString(lImageB.getDimensions()));

    if (mInputImagesSlotKeys.length == 4)
    {
      lImageC = pFastFusionEngine.getImage(mInputImagesSlotKeys[2]);
      lImageD = pFastFusionEngine.getImage(mInputImagesSlotKeys[3]);

      if (lImageC == null || lImageD == null)
        throw new FastFusionException("Fusion task %s received a null image",
                                      this);

      if (!Arrays.equals(lImageC.getDimensions(),
                         lImageD.getDimensions()))
        throw new FastFusionException("Fusion task %s received two images of incompatible dimensions: %s and %s",
                                      this,
                                      Arrays.toString(lImageC.getDimensions()),
                                      Arrays.toString(lImageD.getDimensions()));

      if (!Arrays.equals(lImageA.getDimensions(),
                         lImageC.getDimensions()))
        throw new FastFusionException("Fusion task %s received two images of incompatible dimensions: %s and %s",
                                      this,
                                      Arrays.toString(lImageA.getDimensions()),
                                      Arrays.toString(lImageC.getDimensions()));
    }

    MutablePair<Boolean, ClearCLImage> lImageAndFlag =
                                                     pFastFusionEngine.ensureImageAllocated(mDestImageSlotKey,
                                                                                            mDestinationImageDataType,
                                                                                            lImageA.getDimensions());
    // Then we do the actual work:

    boolean lResult = fuse(lImageA,
                           lImageB,
                           lImageC,
                           lImageD,
                           lImageAndFlag,
                           pWaitToFinish);

    // Debug display:

    if (mDebugDisplay)
    {
      ClearCLImage lImageFused = lImageAndFlag.getValue();

      String lWindowTitlePrefix =
                                this.getClass().getSimpleName() + ":";
      if (mViewA == null)
      {

        mViewA = ClearCLImageViewer.view(lImageA,
                                         lWindowTitlePrefix
                                                  + mInputImagesSlotKeys[0],
                                         512,
                                         512);
      }
      if (mViewB == null)
        mViewB = ClearCLImageViewer.view(lImageB,
                                         lWindowTitlePrefix
                                                  + mInputImagesSlotKeys[1],
                                         512,
                                         512);
      if (mViewFused == null)
        mViewFused = ClearCLImageViewer.view(lImageFused,
                                             lWindowTitlePrefix + ":"
                                                          + mDestImageSlotKey,
                                             512,
                                             512);

      mViewA.setImage(lImageA);
      mViewB.setImage(lImageB);
      mViewFused.setImage(lImageFused);

      lImageA.notifyListenersOfChange(lImageA.getContext()
                                             .getDefaultQueue());
      lImageB.notifyListenersOfChange(lImageB.getContext()
                                             .getDefaultQueue());
      lImageFused.notifyListenersOfChange(lImageFused.getContext()
                                                     .getDefaultQueue());
    }

    return lResult;

  }

  /**
   * Given the 2 or 4 input images, this method runs the actual fusion
   * 
   * @param pImageA
   *          input image A
   * @param pImageB
   *          input image B
   * @param pImageC
   *          input image C
   * @param pImageD
   *          input image D
   * @param pImageAndFlag
   *          Destination image and 'ready' flag.
   * @param pWaitToFinish
   *          true -> waits to finish
   * @return true for success, false otherwise.
   */
  public abstract boolean fuse(ClearCLImage pImageA,
                               ClearCLImage pImageB,
                               ClearCLImage pImageC,
                               ClearCLImage pImageD,
                               MutablePair<Boolean, ClearCLImage> pImageAndFlag,
                               boolean pWaitToFinish);

}
