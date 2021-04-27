package clearcontrol.microscope.lightsheet.postprocessing.containers;

/**
 * MeasurementInSpaceContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class MeasurementInSpaceContainer extends MeasurementContainer
{
  Double mX = null;
  Double mY = null;
  Double mZ = null;

  public MeasurementInSpaceContainer(long pTimePoint,
                                     double pX,
                                     double pY,
                                     double pZ,
                                     double pMeasurement)
  {
    super(pTimePoint, pMeasurement);
    mX = pX;
    mY = pY;
    mZ = pZ;
  }

  public Double getX()
  {
    return mX;
  }

  public Double getY()
  {
    return mY;
  }

  public Double getZ()
  {
    return mZ;
  }

  public String toString()
  {
    return this.getClass().getSimpleName()
           + " "
           + getX()
           + "/"
           + getY()
           + "/"
           + getZ()
           + " "
           + getMeasurement();
  }
}
