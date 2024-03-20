package fastfuse.stackgen;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import clearcl.util.Region3;
import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.synthoscopy.microscope.lightsheet.LightSheetMicroscopeSimulator;
import simbryo.synthoscopy.microscope.parameters.CameraParameter;
import simbryo.synthoscopy.microscope.parameters.DetectionParameter;
import simbryo.synthoscopy.microscope.parameters.IlluminationParameter;
import simbryo.synthoscopy.microscope.parameters.PhantomParameter;
import simbryo.synthoscopy.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.synthoscopy.phantom.scatter.impl.drosophila.DrosophilaScatteringPhantom;

/**
 * Stack Generator
 *
 * @author royer, weigert
 */
public class StackGenerator implements AutoCloseable
{

  private LightSheetMicroscopeSimulator mSimulator;
  private ClearCLContext mContext;

  private DrosophilaScatteringPhantom mDrosophilaScatteringPhantom;
  private DrosophilaHistoneFluorescence mDrosophilaFluorescencePhantom;
  private Drosophila mDrosophila;

  private float mLightSheetIntensity, mLightSheetHeight;

  private ClearCLImage mStack;

  /**
   * Instantiates a stack generator given stack dimensions
   *
   * @param pSimulator microscope simulator to use
   */
  public StackGenerator(LightSheetMicroscopeSimulator pSimulator)
  {
    mSimulator = pSimulator;
    try
    {
      long lPhantomWidth = mSimulator.getWidth();
      long lPhantomHeight = mSimulator.getHeight();
      long lPhantomDepth = mSimulator.getDepth();

      mDrosophila = Drosophila.getDeveloppedEmbryo(11, false);

      mContext = mSimulator.getContext();

      mDrosophilaFluorescencePhantom = new DrosophilaHistoneFluorescence(mContext, mDrosophila, lPhantomWidth, lPhantomHeight, lPhantomDepth);
      mDrosophilaFluorescencePhantom.render(true);

      mDrosophilaScatteringPhantom = new DrosophilaScatteringPhantom(mContext, mDrosophila, mDrosophilaFluorescencePhantom, lPhantomWidth / 2, lPhantomHeight / 2, lPhantomDepth / 2);

      mDrosophilaScatteringPhantom.render(true);

      mSimulator.setPhantomParameter(PhantomParameter.Fluorescence, mDrosophilaFluorescencePhantom.getImage());
      mSimulator.setPhantomParameter(PhantomParameter.Scattering, mDrosophilaScatteringPhantom.getImage());

    } catch (Throwable e)
    {
      e.printStackTrace();
    }

  }

  /**
   * Returns stack
   *
   * @return stack
   */
  public ClearCLImage getStack()
  {
    return mStack;
  }

  /**
   * Sets centered ROI width and height for all cameras
   *
   * @param pWidth  ROI width
   * @param pHeight ROI height
   */
  public void setCenteredROI(int pWidth, int pHeight)
  {
    for (int lCameraIndex = 0; lCameraIndex < mSimulator.getNumberOfDetectionArms(); lCameraIndex++)
    {
      mSimulator.setNumberParameter(CameraParameter.ROIWidth, lCameraIndex, pWidth);
      mSimulator.setNumberParameter(CameraParameter.ROIHeight, lCameraIndex, pHeight);
    }
  }

  /**
   * Sets light sheet(s) intensity
   *
   * @param pLightSheetIntensity lightsheet intensity
   */
  public void setLightSheetIntensity(final float pLightSheetIntensity)
  {
    mLightSheetIntensity = pLightSheetIntensity;
  }

  /**
   * Sets light sheet(s) height
   *
   * @param pLightSheetHeight lightsheet height
   */
  public void setLightSheetHeight(final float pLightSheetHeight)
  {
    mLightSheetHeight = pLightSheetHeight;
  }

  /**
   * Generates stack for a given set of parameters
   *
   * @param pCameraIndex     camera index
   * @param pLightsheetIndex lightsheet index
   * @param pLowZ            low z value
   * @param pHighZ           high z value
   * @param pNumberOfPlanes  number of planes
   */
  public void generateStack(final int pCameraIndex, final int pLightsheetIndex, final float pLowZ, final float pHighZ, final int pNumberOfPlanes)
  {

    int lImageWidth = mSimulator.getNumberParameter(CameraParameter.ROIWidth, pCameraIndex).intValue();
    int lImageHeight = mSimulator.getNumberParameter(CameraParameter.ROIHeight, pCameraIndex).intValue();

    if (mStack == null || mStack.getWidth() != lImageWidth || mStack.getHeight() != lImageHeight || mStack.getDepth() != pNumberOfPlanes)
      mStack = mContext.createSingleChannelImage(ImageChannelDataType.UnsignedInt16, lImageWidth, lImageHeight, pNumberOfPlanes);

    for (int i = 0; i < mSimulator.getNumberOfLightSheets(); i++)
    {
      mSimulator.setNumberParameter(IlluminationParameter.Height, i, mLightSheetHeight);
      mSimulator.setNumberParameter(IlluminationParameter.Intensity, i, 0.f);

    }

    mSimulator.setNumberParameter(IlluminationParameter.Intensity, pLightsheetIndex, mLightSheetIntensity);

    float dz = (pHighZ - pLowZ) / pNumberOfPlanes;

    for (int lPlaneIndex = 0; lPlaneIndex < pNumberOfPlanes; lPlaneIndex++)
    {
      final float z = pLowZ + lPlaneIndex * dz;
      System.out.format("generating image plane #%d at %g \n", lPlaneIndex, z);

      for (int i = 0; i < mSimulator.getNumberOfLightSheets(); i++)
        mSimulator.setNumberParameter(IlluminationParameter.Z, i, z);

      mSimulator.setNumberParameter(DetectionParameter.Z, 0, z);
      mSimulator.setNumberParameter(DetectionParameter.Z, 1, z);

      mDrosophilaFluorescencePhantom.render(false);

      mSimulator.render(true);

      ClearCLImage lCameraImage = mSimulator.getCameraImage(pCameraIndex);

      lCameraImage.copyTo(mStack, Region3.originZero(), Region3.origin(0, 0, lPlaneIndex), Region3.region(lImageWidth, lImageHeight), true);

    }

  }

  @Override
  public void close() throws Exception
  {
    mSimulator.close();
    mDrosophilaScatteringPhantom.close();
    mDrosophilaFluorescencePhantom.close();
  }

}
