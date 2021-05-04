package clearcontrol.imaging;

import clearcontrol.LightSheetMicroscope;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class SingleViewPlaneImager extends SingleViewStackImager
{

  public SingleViewPlaneImager(LightSheetMicroscope pLightSheetMicroscope, double pZ)
  {
    super(pLightSheetMicroscope);
    mMinZ = pZ;
    mMaxZ = pZ;
  }

  @Override
  public void setMinZ(double pMinZ)
  {
    warning("In single views, minZ and maxZ are set equal");
    this.mMinZ = pMinZ;
    this.mMaxZ = pMinZ;
  }

  @Override
  public void setMaxZ(double pMaxZ)
  {
    warning("In single views, minZ and maxZ are set equal");
    this.mMinZ = pMaxZ;
    this.mMaxZ = pMaxZ;
  }

}
