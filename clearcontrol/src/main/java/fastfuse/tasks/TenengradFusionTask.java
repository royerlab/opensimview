package fastfuse.tasks;

import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.enums.ImageChannelDataType;
import org.apache.commons.lang3.tuple.MutablePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Fuses two stacks by weighted average, the weights are obtained by computing
 * Tenengrad image quality metric
 *
 * @author royer, uschmidt
 */
public class TenengradFusionTask extends FusionTaskBase implements TaskInterface
{

  public static List<TaskInterface> applyAndReleaseInputs(String pImageASlotKey, String pImageBSlotKey, String pImageCSlotKey, String pImageDSlotKey, String pDestImageSlotKey, ImageChannelDataType pDestinationImageDataType)
  {
    return apply(true, pImageASlotKey, pImageBSlotKey, pImageCSlotKey, pImageDSlotKey, pDestImageSlotKey, pDestinationImageDataType);
  }

  public static List<TaskInterface> applyAndReleaseInputs(String pImageASlotKey, String pImageBSlotKey, String pDestImageSlotKey, ImageChannelDataType pDestinationImageDataType)
  {
    return apply(true, pImageASlotKey, pImageBSlotKey, pDestImageSlotKey, pDestinationImageDataType);
  }

  public static List<TaskInterface> apply(boolean pReleaseInputs, String pImageASlotKey, String pImageBSlotKey, String pDestImageSlotKey, ImageChannelDataType pDestinationImageDataType)
  {
    List<TaskInterface> lTaskList = new ArrayList<>();
    lTaskList.add(new TenengradFusionTask(pImageASlotKey, pImageBSlotKey, pDestImageSlotKey, pDestinationImageDataType));
    if (pReleaseInputs) lTaskList.add(new MemoryReleaseTask(pDestImageSlotKey, pImageASlotKey, pImageBSlotKey));
    return lTaskList;
  }

  public static List<TaskInterface> apply(boolean pReleaseInputs, String pImageASlotKey, String pImageBSlotKey, String pImageCSlotKey, String pImageDSlotKey, String pDestImageSlotKey, ImageChannelDataType pDestinationImageDataType)
  {
    List<TaskInterface> lTaskList = new ArrayList<>();
    lTaskList.add(new TenengradFusionTask(pImageASlotKey, pImageBSlotKey, pImageCSlotKey, pImageDSlotKey, pDestImageSlotKey, pDestinationImageDataType));
    if (pReleaseInputs)
      lTaskList.add(new MemoryReleaseTask(pDestImageSlotKey, pImageASlotKey, pImageBSlotKey, pImageCSlotKey, pImageDSlotKey));
    return lTaskList;
  }

  /**
   * Instantiates a Tenengrad fusion task given the keys for two input images
   * and destination image
   *
   * @param pImageASlotKey            image A slot key
   * @param pImageBSlotKey            image B slot key
   * @param pDestImageSlotKey         destination image key
   * @param pDestinationImageDataType destination image channel data type
   */
  public TenengradFusionTask(String pImageASlotKey, String pImageBSlotKey, String pDestImageSlotKey, ImageChannelDataType pDestinationImageDataType)
  {
    super(pImageASlotKey, pImageBSlotKey, pDestImageSlotKey);
    setupProgram(TenengradFusionTask.class, "./kernels/fusion.cl");
    mDestinationImageDataType = pDestinationImageDataType;
  }

  /**
   * Instantiates an] Tenengrad fusion task given the keys for the four input
   * images and destination image.
   *
   * @param pImageASlotKey            image A key
   * @param pImageBSlotKey            image B key
   * @param pImageCSlotKey            image C key
   * @param pImageDSlotKey            image D key
   * @param pDestImageSlotKey         destination image key
   * @param pDestinationImageDataType image channel data type
   */
  public TenengradFusionTask(String pImageASlotKey, String pImageBSlotKey, String pImageCSlotKey, String pImageDSlotKey, String pDestImageSlotKey, ImageChannelDataType pDestinationImageDataType)
  {
    super(pImageASlotKey, pImageBSlotKey, pImageCSlotKey, pImageDSlotKey, pDestImageSlotKey);
    setupProgram(TenengradFusionTask.class, "./kernels/fusion.cl");
    mDestinationImageDataType = pDestinationImageDataType;
  }

  @Override
  public boolean fuse(ClearCLImage lImageA, ClearCLImage lImageB, ClearCLImage lImageC, ClearCLImage lImageD, MutablePair<Boolean, ClearCLImage> pImageAndFlag, boolean pWaitToFinish)
  {
    ClearCLImage lImageFused = pImageAndFlag.getValue();

    ClearCLKernel lKernel = null;

    // check image data types
    assert TaskHelper.allowedDataType(lImageFused);
    if (mInputImagesSlotKeys.length == 2)
    {
      assert TaskHelper.allSameAllowedDataType(lImageA, lImageB);
      assert TaskHelper.allSameDimensions(lImageA, lImageB, lImageFused);
    } else
    {
      assert TaskHelper.allSameAllowedDataType(lImageA, lImageB, lImageC, lImageD);
      assert TaskHelper.allSameDimensions(lImageA, lImageB, lImageC, lImageD, lImageFused);
    }

    try
    {
      String lKernelName = String.format("tenengrad_fusion_%d_images", mInputImagesSlotKeys.length);
      lKernel = getKernel(lImageFused.getContext(), lKernelName, TaskHelper.getOpenCLDefines(lImageA, lImageFused));

      // kernel arguments are given by name
      lKernel.setArgument("src1", lImageA);
      lKernel.setArgument("src2", lImageB);
      if (mInputImagesSlotKeys.length == 4)
      {
        lKernel.setArgument("src3", lImageC);
        lKernel.setArgument("src4", lImageD);
      }
      lKernel.setArgument("dst", lImageFused);

      lKernel.setGlobalSizes(lImageFused);

      runKernel(lKernel, pWaitToFinish);
      pImageAndFlag.setLeft(true);

      return true;
    } catch (IOException e)
    {
      e.printStackTrace();
      return false;
    }

  }
}
