package clearcontrol.adaptive.modules;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.configurationstate.ConfigurationState;
import clearcontrol.state.InterpolatedAcquisitionState;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public class AdaptationZManualDetectionArmSelection extends AdaptationZ
{
  private int mNumberOfControlPlanes;

  private BoundedVariable<Integer>[] mDetectionArmChoiceVariables;

  private final Variable<Boolean> mFirstAndLastControlPlaneZero = new Variable<Boolean>("pFirstAndLastControlPlaneZero", true);

  /**
   * Instantiates a Z focus adaptation module given the delta Z parameter, number of
   * samples, probability threshold and image metric threshold
   *
   * @param pNumberOfSamples      number of samples
   * @param pDeltaZ               delta z parameter
   * @param pProbabilityThreshold probability threshold
   * @param pImageMetricThreshold image metric threshold
   * @param pExposureInSeconds    expsoure in seconds
   * @param pLaserPower           laser power
   */
  public AdaptationZManualDetectionArmSelection(int pNumberOfSamples, double pDeltaZ, double pProbabilityThreshold, double pImageMetricThreshold, double pExposureInSeconds, double pLaserPower, int pNumberOfLightSheets, LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pNumberOfSamples, pDeltaZ, pProbabilityThreshold, pImageMetricThreshold, pExposureInSeconds, pLaserPower, pNumberOfLightSheets);
    setName("Z**");

    // Todo: The following block contains some casts which should be
    // solved in a better way... It's XWing specific

    InterpolatedAcquisitionState lInterpolatedAcquisitionState = (InterpolatedAcquisitionState) pLightSheetMicroscope.getAcquisitionStateManager().getCurrentState();

    mNumberOfControlPlanes = lInterpolatedAcquisitionState.getNumberOfControlPlanes();

    mDetectionArmChoiceVariables = new BoundedVariable[mNumberOfControlPlanes];
    for (int i = 0; i < mDetectionArmChoiceVariables.length; i++)
    {
      // todo: the default values of the following variables may be XWing
      // specific
      mDetectionArmChoiceVariables[i] = new BoundedVariable<Integer>("Control plane " + i + " camera", (i < mDetectionArmChoiceVariables.length / 2) ? 1 : 0, 0, pLightSheetMicroscope.getNumberOfDetectionArms() - 1);
    }

    getIsActiveVariable().set(false);
  }

  protected void updateStateInternal(InterpolatedAcquisitionState pStateToUpdate, boolean pRelativeCorrection, boolean pFlipCorrectionSign)
  {
    info("Update new state...");

    int lNumberOfControlPlanes = getAdaptiveEngine().getAcquisitionStateVariable().get().getNumberOfControlPlanes();
    int lNumberOfLightSheets = getAdaptiveEngine().getAcquisitionStateVariable().get().getNumberOfLightSheets();

    int lNumberOfDetectionArms = getAdaptiveEngine().getAcquisitionStateVariable().get().getNumberOfDetectionArms();

    int lMissingInfoCount = 0;
    for (int cpi = 0; cpi < lNumberOfControlPlanes; cpi++)
    {
      for (int l = 0; l < lNumberOfLightSheets; l++)
      {
        int lSelectedDetectionArm = mDetectionArmChoiceVariables[cpi].get();
        Result lResult = getResult(cpi, l, lSelectedDetectionArm);

        if (lResult == null)
        {
          severe("Found null result for cpi=%d, l=%d \n", cpi, l);
          continue;
        }

        double lCorrection = (pFlipCorrectionSign ? -1 : 1) * lResult.argmax;

        boolean lMissingInfo = checkAdaptationQuality(l, cpi, lCorrection, lResult.metricmax, lResult.probability, lSelectedDetectionArm);

        if (lMissingInfo)
        {
          lCorrection = computeCorrectionBasedOnNeighbooringControlPlanes(pRelativeCorrection, pStateToUpdate, cpi, l);

        }

        info("Applying correction: %g \n", lCorrection);

        getAdaptiveEngine().addEntry(getName(), false, "LS", "CPI", 9, l, cpi, String.format("argmax=%g\nmetricmax=%g\nprob=%g\ncorr=%g\nmissing=%s\nselected=%d", lResult.argmax, lResult.metricmax, lResult.probability, lCorrection, lMissingInfo, lSelectedDetectionArm));

        if (mFirstAndLastControlPlaneZero.get() && (cpi == 0 || cpi == lNumberOfControlPlanes - 1))
        {
          pStateToUpdate.getInterpolationTables().add(mLightSheetDOF, cpi, l, 0);
          info("Set first/last control plane to zero adaptation as configured.");
        } else
        {
          pStateToUpdate.getInterpolationTables().add(mLightSheetDOF, cpi, l, lCorrection);
        }
        /*
        if (pRelativeCorrection)
          pStateToUpdate.getInterpolationTables()
                        .add(mLightSheetDOF, cpi, l, lCorrection);
        else
          pStateToUpdate.getInterpolationTables()
                        .set(mLightSheetDOF, cpi, l, lCorrection);
        */
        invokeControlPlaneStateChangeListeners(l, cpi);
      }
    }
    setConfigurationState(ConfigurationState.fromProgressValue((double) lMissingInfoCount / (lNumberOfControlPlanes * lNumberOfLightSheets)));
  }

  public int getNumberOfControlPlanes()
  {
    return mNumberOfControlPlanes;
  }

  public BoundedVariable<Integer> getDetectionArmChoiceVariable(int pControlPlane)
  {
    return mDetectionArmChoiceVariables[pControlPlane];
  }

  public Variable<Boolean> getFirstAndLastControlPlaneZero()
  {
    return mFirstAndLastControlPlaneZero;
  }
}
