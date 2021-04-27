package clearcontrol.microscope.lightsheet.state.io;

import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;

/**
 * This class serves as backend for acquisition state file input/output
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class InterpolatedAcquisitionStateData
{
  public Double[][] mIlluminationX;
  public Double[][] mIlluminationY;
  public Double[][] mIlluminationZ;
  public Double[][] mIlluminationA;
  public Double[][] mIlluminationB;
  public Double[][] mIlluminationW;
  public Double[][] mIlluminationH;
  public Double[][] mIlluminationP;
  public Double[][] mDetectionZ;

  /**
   * This constructor is needed for file I/O it should not be used by the programmer
   */
  @Deprecated public InterpolatedAcquisitionStateData()
  {
  }

  public InterpolatedAcquisitionStateData(InterpolatedAcquisitionState pState)
  {
    mDetectionZ = new Double[pState.getNumberOfDetectionArms()][];
    for (int d = 0; d < pState.getNumberOfDetectionArms(); d++)
    {
      mDetectionZ[d] = new Double[pState.getNumberOfControlPlanes()];
      for (int cpi = 0; cpi < pState.getNumberOfControlPlanes(); cpi++)
      {
        mDetectionZ[d][cpi] = pState.get(LightSheetDOF.DZ, cpi, d);
      }
    }

    mIlluminationX = new Double[pState.getNumberOfLightSheets()][];
    mIlluminationY = new Double[pState.getNumberOfLightSheets()][];
    mIlluminationZ = new Double[pState.getNumberOfLightSheets()][];
    mIlluminationA = new Double[pState.getNumberOfLightSheets()][];
    mIlluminationB = new Double[pState.getNumberOfLightSheets()][];
    mIlluminationW = new Double[pState.getNumberOfLightSheets()][];
    mIlluminationH = new Double[pState.getNumberOfLightSheets()][];
    mIlluminationP = new Double[pState.getNumberOfLightSheets()][];
    for (int l = 0; l < pState.getNumberOfLightSheets(); l++)
    {
      mIlluminationX[l] = new Double[pState.getNumberOfControlPlanes()];
      mIlluminationY[l] = new Double[pState.getNumberOfControlPlanes()];
      mIlluminationZ[l] = new Double[pState.getNumberOfControlPlanes()];
      mIlluminationA[l] = new Double[pState.getNumberOfControlPlanes()];
      mIlluminationB[l] = new Double[pState.getNumberOfControlPlanes()];
      mIlluminationW[l] = new Double[pState.getNumberOfControlPlanes()];
      mIlluminationH[l] = new Double[pState.getNumberOfControlPlanes()];
      mIlluminationP[l] = new Double[pState.getNumberOfControlPlanes()];
      for (int cpi = 0; cpi < pState.getNumberOfControlPlanes(); cpi++)
      {
        mIlluminationX[l][cpi] =
            pState.getInterpolationTables().get(LightSheetDOF.IX, cpi, l);
        mIlluminationY[l][cpi] =
            pState.getInterpolationTables().get(LightSheetDOF.IY, cpi, l);
        mIlluminationZ[l][cpi] =
            pState.getInterpolationTables().get(LightSheetDOF.IZ, cpi, l);
        mIlluminationA[l][cpi] =
            pState.getInterpolationTables().get(LightSheetDOF.IA, cpi, l);
        mIlluminationB[l][cpi] =
            pState.getInterpolationTables().get(LightSheetDOF.IB, cpi, l);
        mIlluminationW[l][cpi] =
            pState.getInterpolationTables().get(LightSheetDOF.IW, cpi, l);
        mIlluminationH[l][cpi] =
            pState.getInterpolationTables().get(LightSheetDOF.IH, cpi, l);
        mIlluminationP[l][cpi] =
            pState.getInterpolationTables().get(LightSheetDOF.IP, cpi, l);
      }
    }
  }

  public void copyTo(InterpolatedAcquisitionState pState)
  {
    for (int d = 0; d < pState.getNumberOfDetectionArms(); d++)
    {
      for (int cpi = 0; cpi < pState.getNumberOfControlPlanes(); cpi++)
      {
        pState.getInterpolationTables()
              .set(LightSheetDOF.DZ, cpi, d, mDetectionZ[d][cpi]);
      }
    }

    for (int l = 0; l < pState.getNumberOfLightSheets(); l++)
    {
      for (int cpi = 0; cpi < pState.getNumberOfControlPlanes(); cpi++)
      {
        pState.getInterpolationTables()
              .set(LightSheetDOF.IX, cpi, l, mIlluminationX[l][cpi]);
        pState.getInterpolationTables()
              .set(LightSheetDOF.IY, cpi, l, mIlluminationY[l][cpi]);
        pState.getInterpolationTables()
              .set(LightSheetDOF.IZ, cpi, l, mIlluminationZ[l][cpi]);
        pState.getInterpolationTables()
              .set(LightSheetDOF.IA, cpi, l, mIlluminationA[l][cpi]);
        pState.getInterpolationTables()
              .set(LightSheetDOF.IB, cpi, l, mIlluminationB[l][cpi]);
        pState.getInterpolationTables()
              .set(LightSheetDOF.IW, cpi, l, mIlluminationW[l][cpi]);
        pState.getInterpolationTables()
              .set(LightSheetDOF.IH, cpi, l, mIlluminationH[l][cpi]);
        pState.getInterpolationTables()
              .set(LightSheetDOF.IP, cpi, l, mIlluminationP[l][cpi]);
      }
    }
  }
}
