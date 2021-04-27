package fastfuse.tasks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;

public class TaskHelper
{
  private static boolean allowedDataType(ImageChannelDataType pDataType)
  {
    return pDataType == ImageChannelDataType.Float
           || pDataType == ImageChannelDataType.UnsignedInt16;
  }

  private static boolean allowedDataType(ClearCLImage pImage)
  {
    return allowedDataType(pImage.getChannelDataType());
  }

  public static boolean allowedDataType(ImageChannelDataType... pDataTypes)
  {
    if (pDataTypes == null || pDataTypes.length == 0)
      return true;
    return Stream.of(pDataTypes)
                 .allMatch(TaskHelper::allowedDataType);
  }

  public static boolean allowedDataType(ClearCLImage... pImages)
  {
    if (pImages == null || pImages.length == 0)
      return true;
    return Stream.of(pImages).allMatch(TaskHelper::allowedDataType);
  }

  public static boolean allSameDataType(ImageChannelDataType pDataType,
                                        ClearCLImage... pImages)
  {
    if (pImages == null || pImages.length == 0)
      return true;
    return Stream.of(pImages)
                 .allMatch(x -> x.getChannelDataType() == pDataType);
  }

  public static boolean allSameDataType(ClearCLImage... pImages)
  {
    if (pImages == null || pImages.length == 0)
      return true;
    return allSameDataType(pImages[0].getChannelDataType(), pImages);
  }

  public static boolean allSameAllowedDataType(ClearCLImage... pImages)
  {
    if (pImages == null || pImages.length == 0)
      return true;
    ImageChannelDataType lDataType = pImages[0].getChannelDataType();
    return allowedDataType(lDataType)
           && allSameDataType(lDataType, pImages);
  }

  public static boolean allSameDimensions(ClearCLImage... pImages)
  {
    if (pImages == null || pImages.length == 0)
      return true;
    long[] lDims = pImages[0].getDimensions();
    return Stream.of(pImages)
                 .allMatch(x -> Arrays.equals(lDims,
                                              x.getDimensions()));
  }

  public static Map<String, Object> getOpenCLDefines(ImageChannelDataType pDTypeIn,
                                                     ImageChannelDataType pDTypeOut)
  {
    assert allowedDataType(pDTypeIn, pDTypeOut);
    Map<String, Object> lDefines = new HashMap<>();
    lDefines.put("DTYPE_IN",
                 pDTypeIn.isInteger() ? "ushort" : "float");
    lDefines.put("DTYPE_OUT",
                 pDTypeOut.isInteger() ? "ushort" : "float");
    lDefines.put("READ_IMAGE",
                 pDTypeIn.isInteger() ? "read_imageui"
                                      : "read_imagef");
    lDefines.put("WRITE_IMAGE",
                 pDTypeOut.isInteger() ? "write_imageui"
                                       : "write_imagef");
    return lDefines;
  }

  public static Map<String, Object> getOpenCLDefines(ClearCLImage pImageIn,
                                                     ClearCLImage pImageOut)
  {
    return getOpenCLDefines(pImageIn.getChannelDataType(),
                            pImageOut.getChannelDataType());
  }

}
