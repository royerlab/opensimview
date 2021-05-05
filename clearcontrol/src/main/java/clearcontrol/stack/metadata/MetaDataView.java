package clearcontrol.stack.metadata;

import clearcontrol.state.AcquisitionType;

/**
 * Basic stack meta data entries
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public enum MetaDataView implements MetaDataEntryInterface<Integer>
{

  Camera(Integer.class), LightSheet(Integer.class);

  private final Class<Integer> mClass;

  private MetaDataView(Class<Integer> pClass)
  {
    mClass = pClass;
  }

  @Override
  public Class<Integer> getMetaDataClass()
  {
    return mClass;
  }

  public static final String getCxLyString(StackMetaData pStackMetaData)
  {
    Integer lCameraIndex = pStackMetaData.getValue(MetaDataView.Camera);

    if (pStackMetaData.getValue(MetaDataAcquisitionType.AcquisitionType) == AcquisitionType.TimeLapseInterleaved)
    {
      return "C" + pStackMetaData.getValue(MetaDataView.Camera) + "interleaved";
    } else if (pStackMetaData.getValue(MetaDataAcquisitionType.AcquisitionType) == AcquisitionType.TimeLapseOpticallyCameraFused)
    {
      return "C" + pStackMetaData.getValue(MetaDataView.Camera) + "opticallycamerafused";
    } else
    {
      Integer lLightSheetIndex = pStackMetaData.getValue(MetaDataView.LightSheet);

      if (lCameraIndex == null || lLightSheetIndex == null) return null;

      String lKey = String.format("C%dL%d", (int) lCameraIndex, (int) lLightSheetIndex);
      return lKey;
    }
  }

}
