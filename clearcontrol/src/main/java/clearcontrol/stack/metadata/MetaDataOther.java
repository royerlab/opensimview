package clearcontrol.stack.metadata;

/**
 * Other stack meta data entries
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public enum MetaDataOther implements MetaDataEntryInterface<Double>
{

  ColorWavelength(Double.class),
  ExposureC0(Double.class),
  ExposureC1(Double.class),
  ExposureC2(Double.class),
  ExposureC3(Double.class);


  private final Class<Double> mClass;

  private MetaDataOther(Class<Double> pClass)
  {
    mClass = pClass;
  }

  @Override
  public Class<Double> getMetaDataClass()
  {
    return mClass;
  }


  public static MetaDataOther getCameraExposure(int pCameraIndex)
  {
    switch (pCameraIndex)
    {
      case 0:
        return MetaDataOther.ExposureC0;
      case 1:
        return MetaDataOther.ExposureC1;
      case 2:
        return MetaDataOther.ExposureC2;
      case 3:
        return MetaDataOther.ExposureC3;
      default:
        throw new IllegalArgumentException("Camera index must be within [0,3]");
    }
  }
}
