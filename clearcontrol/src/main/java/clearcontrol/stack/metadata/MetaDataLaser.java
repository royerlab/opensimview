package clearcontrol.stack.metadata;

/**
 * Basic laser metadata
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public enum MetaDataLaser implements MetaDataEntryInterface<Double>
{
  LaserPower0(Double.class),
  LaserPower1(Double.class),
  LaserPower2(Double.class),
  LaserPower3(Double.class),
  LaserPower4(Double.class),
  LaserPower5(Double.class),
  LaserPower6(Double.class),
  LaserPower7(Double.class);

  private final Class<Double> mClass;

  private MetaDataLaser(Class<Double> pClass)
  {
    mClass = pClass;
  }

  @Override
  public Class<Double> getMetaDataClass()
  {
    return mClass;
  }

  public static MetaDataLaser getLaserPower(int pLaserLineIndex)
  {
    switch (pLaserLineIndex)
    {
      case 0:
        return MetaDataLaser.LaserPower0;
      case 1:
        return MetaDataLaser.LaserPower1;
      case 2:
        return MetaDataLaser.LaserPower2;
      case 3:
        return MetaDataLaser.LaserPower3;
      case 4:
        return MetaDataLaser.LaserPower4;
      case 5:
        return MetaDataLaser.LaserPower5;
      case 6:
        return MetaDataLaser.LaserPower6;
      case 7:
        return MetaDataLaser.LaserPower7;
      default:
        throw new IllegalArgumentException("Laser line index must be within [0,7]");
    }
  }


}
