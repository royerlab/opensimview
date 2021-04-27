package fastfuse.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.vecmath.Matrix4f;

import clearcl.enums.ImageChannelDataType;
import fastfuse.tasks.DownsampleXYbyHalfTask.Type;

import org.apache.commons.lang3.ArrayUtils;

public class CompositeTasks
{

  public static List<TaskInterface> fuseWithSmoothDownsampledWeights(String pDstImageKey,
                                                                     ImageChannelDataType pDstImageDataType,
                                                                     int pNumDownsample,
                                                                     float[] pKernelSigmas,
                                                                     boolean pReleaseSrcImages,
                                                                     String... pSrcImageKeys)
  {
    assert pNumDownsample >= 0;
    String lRandomSuffix = UUID.randomUUID().toString();
    List<TaskInterface> lTaskList = new ArrayList<>();

    int lNumImages = pSrcImageKeys.length;
    String[] lSrcImageAndWeightKeys = new String[2 * lNumImages];

    for (int i = 0; i < lNumImages; i++)
    {
      // define names for temporary images
      String lWeightRawKey = String.format("%s w    %s",
                                           pSrcImageKeys[i],
                                           lRandomSuffix);
      String lWeightSmoothKey = String.format("%s wb   %s",
                                              pSrcImageKeys[i],
                                              lRandomSuffix);
      String lWeightSmoothDownsampledKey =
                                         String.format("%s wbd%%d %s",
                                                       pSrcImageKeys[i],
                                                       lRandomSuffix);
      // record keys for fusion call
      lSrcImageAndWeightKeys[i] = pSrcImageKeys[i];
      lSrcImageAndWeightKeys[lNumImages
                             + i] =
                                  pNumDownsample == 0 ? lWeightSmoothKey
                                                      : String.format(lWeightSmoothDownsampledKey,
                                                                      pNumDownsample);
      // compute unnormalized weight from src image
      lTaskList.add(new TenengradWeightTask(pSrcImageKeys[i],
                                            lWeightRawKey));
      // blur raw weight to obtain smooth weight
      lTaskList.add(new GaussianBlurTask(lWeightRawKey,
                                         lWeightSmoothKey,
                                         pKernelSigmas,
                                         null,
                                         true));
      // release raw weight
      lTaskList.add(new MemoryReleaseTask(lWeightSmoothKey,
                                          lWeightRawKey));
      // downsample
      for (int j = 0; j < pNumDownsample; j++)
      {
        String lInputImage = j == 0 ? lWeightSmoothKey
                                    : String.format(lWeightSmoothDownsampledKey,
                                                    j);
        String lOutputImage =
                            String.format(lWeightSmoothDownsampledKey,
                                          j + 1);
        lTaskList.add(new DownsampleXYbyHalfTask(lInputImage,
                                                 lOutputImage,
                                                 Type.Average));
        lTaskList.add(new MemoryReleaseTask(lOutputImage,
                                            lInputImage));
      }
    }

    // fuse images with smooth and potentially downsampled weights
    lTaskList.add(new TenengradAdvancedFusionTask(pDstImageKey,
                                                  pDstImageDataType,
                                                  lSrcImageAndWeightKeys));
    // release fusion weights
    lTaskList.add(new MemoryReleaseTask(pDstImageKey,
                                        ArrayUtils.subarray(lSrcImageAndWeightKeys,
                                                            lNumImages,
                                                            2 * lNumImages)));
    // release src images
    if (pReleaseSrcImages)
      lTaskList.add(new MemoryReleaseTask(pDstImageKey,
                                          pSrcImageKeys));
    return lTaskList;
  }

  public static List<TaskInterface> fuseWithSmoothWeights(String pDstImageKey,
                                                          ImageChannelDataType pDstImageDataType,
                                                          float[] pKernelSigmas,
                                                          boolean pReleaseSrcImages,
                                                          String... pSrcImageKeys)
  {
    return fuseWithSmoothDownsampledWeights(pDstImageKey,
                                            pDstImageDataType,
                                            0,
                                            pKernelSigmas,
                                            pReleaseSrcImages,
                                            pSrcImageKeys);
  }

  public static List<TaskInterface> registerWithBlurPreprocessing(String pImageReferenceKey,
                                                                  String pImageToRegisterKey,
                                                                  String pImageTransformedKey,
                                                                  float[] pKernelSigmas,
                                                                  int[] pKernelSizes,
                                                                  Matrix4f pZeroTransformMatrix,
                                                                  boolean pReleaseImageToRegister)
  {
    // TODO: have a task that checks the data type of the input image (must be
    // Float here)
    String lRandomSuffix = UUID.randomUUID().toString();
    String lImageReferenceBlurredKey = String.format("%s-blurred-%s",
                                                     pImageReferenceKey,
                                                     lRandomSuffix);
    String lImageToRegisterBlurredKey = String.format("%s-blurred-%s",
                                                      pImageToRegisterKey,
                                                      lRandomSuffix);
    String[] lImageKeysToRelease =
                                 pReleaseImageToRegister ? new String[]
                                 { lImageReferenceBlurredKey, lImageToRegisterBlurredKey, pImageToRegisterKey } : new String[]
                                 { lImageReferenceBlurredKey, lImageToRegisterBlurredKey };

    RegistrationTask lRegistrationTask =
                                       new RegistrationTask(lImageReferenceBlurredKey,
                                                            lImageToRegisterBlurredKey,
                                                            pImageReferenceKey,
                                                            pImageToRegisterKey,
                                                            pImageTransformedKey);
    lRegistrationTask.getParameters()
                     .setZeroTransformMatrix(pZeroTransformMatrix);

    return Arrays.asList(new GaussianBlurTask(pImageReferenceKey,
                                              lImageReferenceBlurredKey,
                                              pKernelSigmas,
                                              pKernelSizes),
                         new GaussianBlurTask(pImageToRegisterKey,
                                              lImageToRegisterBlurredKey,
                                              pKernelSigmas,
                                              pKernelSizes),
                         lRegistrationTask,
                         new MemoryReleaseTask(pImageTransformedKey,
                                               lImageKeysToRelease));
  }

  public static List<TaskInterface> subtractBlurredCopyFromFloatImage(String pSrcImageKey,
                                                                      String pDstImageKey,
                                                                      float[] pSigmas,
                                                                      boolean pReleaseSrcImage)
  {
    return subtractBlurredCopyFromFloatImage(pSrcImageKey,
                                             pDstImageKey,
                                             pSigmas,
                                             pReleaseSrcImage,
                                             null);
  }

  public static List<TaskInterface> subtractBlurredCopyFromFloatImage(String pSrcImageKey,
                                                                      String pDstImageKey,
                                                                      float[] pSigmas,
                                                                      boolean pReleaseSrcImage,
                                                                      ImageChannelDataType pDstDataType)
  {
    // TODO: have a task that checks the data type of the input image (must be
    // Float here)
    String lTmpImageKey = String.format("%s-blurred-%s",
                                        pSrcImageKey,
                                        UUID.randomUUID().toString());
    String[] lImageKeysToRelease = pReleaseSrcImage ? new String[]
    { lTmpImageKey, pSrcImageKey } : new String[]
    { lTmpImageKey };

    return Arrays.asList(new GaussianBlurTask(pSrcImageKey,
                                              lTmpImageKey,
                                              pSigmas,
                                              null,
                                              true),
                         new NonnegativeSubtractionTask(pSrcImageKey,
                                                        lTmpImageKey,
                                                        pDstImageKey,
                                                        pDstDataType),
                         new MemoryReleaseTask(pDstImageKey,
                                               lImageKeysToRelease));
  }

}
