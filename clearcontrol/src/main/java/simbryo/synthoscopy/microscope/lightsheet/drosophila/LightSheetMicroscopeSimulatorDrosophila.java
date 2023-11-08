package simbryo.synthoscopy.microscope.lightsheet.drosophila;

import clearcl.ClearCLContext;
import clearcl.viewer.ClearCLImageViewer;
import clearcontrol.core.configuration.MachineConfiguration;
import simbryo.dynamics.tissue.embryo.zoo.Drosophila;
import simbryo.dynamics.tissue.recorder.TissueRecorder;
import simbryo.synthoscopy.microscope.lightsheet.LightSheetMicroscopeSimulatorOrtho;
import simbryo.synthoscopy.microscope.parameters.PhantomParameter;
import simbryo.synthoscopy.phantom.fluo.impl.drosophila.DrosophilaHistoneFluorescence;
import simbryo.synthoscopy.phantom.scatter.impl.drosophila.DrosophilaScatteringPhantom;

import java.io.File;
import java.io.IOException;

/**
 * Light sheet microscope simulator with drosophila embryo
 *
 * @author royer
 */
public class LightSheetMicroscopeSimulatorDrosophila extends LightSheetMicroscopeSimulatorOrtho
{

  private final Drosophila mDrosophila;
  private final DrosophilaHistoneFluorescence mDrosophilaFluorescencePhantom;
  private final DrosophilaScatteringPhantom mDrosophilaScatteringPhantom;

  private volatile boolean mFreezedEmbryo = false;

  /**
   * Instanciates a light sheet microscope simulator with a simulated drosophila
   * embryo as sample
   *
   * @param pContext                  ClearCL context to use
   * @param pNumberOfDetectionArms    number of detection arms
   * @param pNumberOfIlluminationArms number of illumination arms
   * @param pMaxCameraResolution      max width and height of camera images
   * @param pInitialDivisionTime      initial 'division time' for embryo
   * @param pMainPhantomDimensions    main phantom dimensions
   */
  public LightSheetMicroscopeSimulatorDrosophila(ClearCLContext pContext, int pNumberOfDetectionArms, int pNumberOfIlluminationArms, int pMaxCameraResolution, float pInitialDivisionTime, long... pMainPhantomDimensions)
  {
    super(pContext, pNumberOfDetectionArms, pNumberOfIlluminationArms, pMaxCameraResolution, pMainPhantomDimensions);

    try
    {
      mDrosophila = Drosophila.getDeveloppedEmbryo(pInitialDivisionTime);

      // operating system dependent download folder for storing recordings:
      String lDefaultFolderPrefix = System.getProperty("user.home") + "/Downloads/";

      // get prefix for recording folder from machine configuration:
      String lRecorderFolderPrefix = MachineConfiguration.get().getStringProperty("simbryo.recorder.folderprefix", lDefaultFolderPrefix);

      // create recording folder from lRecorderFolderPrefix and with a subfolder that is the date and time in YYYY:MM:DD:HH:MM:SS format:
      String lRecorderFolderName = lRecorderFolderPrefix + File.separator + String.format("%1$tY:%1$tm:%1$td:%1$tH:%1$tM:%1$tS", System.currentTimeMillis());

      // Ensure folder exists:
      File lRecorderFolderFile = new File(lRecorderFolderName);
      lRecorderFolderFile.mkdirs();

      // set recorder:
      mDrosophila.setRecorder(new TissueRecorder(mDrosophila, lRecorderFolderName));

      // create embryo fluorescence phantom:
      mDrosophilaFluorescencePhantom = new DrosophilaHistoneFluorescence(pContext, mDrosophila, getWidth(), getHeight(), getDepth());
      mDrosophilaFluorescencePhantom.render(true);

      // create embryo scattering phantom:
      mDrosophilaScatteringPhantom = new DrosophilaScatteringPhantom(pContext, mDrosophila, mDrosophilaFluorescencePhantom, getWidth() / 2, getHeight() / 2, getDepth() / 2);
      mDrosophilaScatteringPhantom.render(true);

      // set embryo fluorescence and scattering phantoms:
      setPhantomParameter(PhantomParameter.Fluorescence, mDrosophilaFluorescencePhantom.getImage());
      setPhantomParameter(PhantomParameter.Scattering, mDrosophilaScatteringPhantom.getImage());
    } catch (IOException e)
    {
      e.printStackTrace();
      throw new RuntimeException("Problem while initializing phantoms", e);
    }

  }

  /**
   * Opens viewer for fluorescence phantom image.
   *
   * @return viewer
   */
  public ClearCLImageViewer openViewerForFluorescencePhantom()
  {
    return mDrosophilaFluorescencePhantom.openViewer();
  }

  /**
   * Opens viewer for scattering phantom image.
   *
   * @return viewer
   */
  public ClearCLImageViewer openViewerForScatteringPhantom()
  {
    return mDrosophilaScatteringPhantom.openViewer();
  }

  @Override
  public void simulationSteps(int pNumberOfSteps)
  {
    if (!isFreezedEmbryo()) mDrosophila.simulationSteps(pNumberOfSteps);
    super.simulationSteps(pNumberOfSteps);
  }

  public boolean advance()
  {
    boolean lAdvanced = super.advance(1.2f);
    if (lAdvanced)
    {
      mDrosophilaFluorescencePhantom.requestUpdate();
      mDrosophilaScatteringPhantom.requestUpdate();
    }

    return lAdvanced;
  }

  @Override
  public void render(boolean pWaitToFinish)
  {
    mDrosophilaFluorescencePhantom.render(false);
    mDrosophilaScatteringPhantom.render(true);
    setPhantomParameter(PhantomParameter.Fluorescence, mDrosophilaFluorescencePhantom.getImage());
    setPhantomParameter(PhantomParameter.Scattering, mDrosophilaScatteringPhantom.getImage());
    super.render(pWaitToFinish);
  }

  @Override
  public void render(int pDetectionIndex, boolean pWaitToFinish)
  {
    mDrosophilaFluorescencePhantom.render(false);
    mDrosophilaScatteringPhantom.render(true);
    setPhantomParameter(PhantomParameter.Fluorescence, mDrosophilaFluorescencePhantom.getImage());
    setPhantomParameter(PhantomParameter.Scattering, mDrosophilaScatteringPhantom.getImage());
    super.render(pDetectionIndex, pWaitToFinish);
  }

  @Override
  public void close() throws Exception
  {
    super.close();
    mDrosophilaScatteringPhantom.close();
    mDrosophilaFluorescencePhantom.close();
  }

  /**
   * Returns wether this embryo is 'frozen' - meaning that it is prevented from
   * developping.
   *
   * @return true -> frozen, false -> otherwise
   */
  public boolean isFreezedEmbryo()
  {
    return mFreezedEmbryo;
  }

  /**
   * Sets whther this embryo should be 'frozen' - prevented from developping.
   *
   * @param pFreezeEmbryo true -> frozen, false -> otherwise
   */
  public void setFreezedEmbryo(boolean pFreezeEmbryo)
  {
    mFreezedEmbryo = pFreezeEmbryo;
  }

}
