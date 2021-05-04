package clearcontrol.stack.metadata;

/**
 * Other stack meta data entries
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public enum MetaDataOther implements MetaDataEntryInterface<Double>
{

  ColorWavelength(Double.class);

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

}
