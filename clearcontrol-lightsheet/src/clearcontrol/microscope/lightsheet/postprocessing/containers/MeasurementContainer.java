package clearcontrol.microscope.lightsheet.postprocessing.containers;

import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerBase;

/**
 * MeasurementContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class MeasurementContainer extends DataContainerBase
{
  Double mMeasurement = null;

  public MeasurementContainer(long pTimePoint, double pMeasurement)
  {
    super(pTimePoint);
    mMeasurement = pMeasurement;
  }

  @Override public boolean isDataComplete()
  {
    return true;
  }

  @Override public void dispose()
  {
  }

  public Double getMeasurement()
  {
    return mMeasurement;
  }

  public String toString()
  {
    return this.getClass().getSimpleName() + " " + getMeasurement();
  }
}
