package fastfuse.tasks;

import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;

import org.apache.commons.lang3.tuple.MutablePair;

/**
 * Fuses two stacks using the average method.
 *
 * @author royer
 */
public class AverageTask extends FusionTaskBase
                         implements TaskInterface
{
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
  public AverageTask(String pImageASlotKey,
                     String pImageBSlotKey,
                     String pDestImageKey)
  {
    super(pImageASlotKey, pImageBSlotKey, pDestImageKey);
    setupProgram(FusionTaskBase.class, "./kernels/fuseavg.cl");
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
  public AverageTask(String pImageASlotKey,
                     String pImageBSlotKey,
                     String pImageCSlotKey,
                     String pImageDSlotKey,
                     String pDestImageSlotKey)
  {
    super(pImageASlotKey,
          pImageBSlotKey,
          pImageCSlotKey,
          pImageDSlotKey,
          pDestImageSlotKey);
    setupProgram(FusionTaskBase.class, "./kernels/fuseavg.cl");
  }

  @Override
  public boolean fuse(ClearCLImage lImageA,
                      ClearCLImage lImageB,
                      ClearCLImage lImageC,
                      ClearCLImage lImageD,
                      MutablePair<Boolean, ClearCLImage> pImageAndFlag,
                      boolean pWaitToFinish)
  {
    ClearCLImage lImageFused = pImageAndFlag.getValue();

    ClearCLKernel lKernel = null;

    try
    {
      if (mInputImagesSlotKeys.length == 2)
        lKernel = getKernel(lImageFused.getContext(), "fuseavg2");
      else if (mInputImagesSlotKeys.length == 4)
        lKernel = getKernel(lImageFused.getContext(), "fuseavg4");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }

    lKernel.setArgument("imagea", lImageA);
    lKernel.setArgument("imageb", lImageB);
    if (mInputImagesSlotKeys.length == 4)
    {
      lKernel.setArgument("imagec", lImageC);
      lKernel.setArgument("imaged", lImageD);
    }
    lKernel.setArgument("imagedest", lImageFused);

    lKernel.setGlobalSizes(lImageFused);

    // System.out.println("running kernel");
    runKernel(lKernel, pWaitToFinish);
    pImageAndFlag.setLeft(true);

    return true;
  }

}
