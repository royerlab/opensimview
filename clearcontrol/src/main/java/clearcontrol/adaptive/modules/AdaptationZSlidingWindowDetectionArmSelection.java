package clearcontrol.adaptive.modules;

import clearcontrol.LightSheetDOF;
import clearcontrol.LightSheetMicroscopeQueue;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.state.InterpolatedAcquisitionState;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.Arrays;
import java.util.concurrent.Future;

/**
 * Adaptation module responsible for adjusting the Z focus
 *
 * @author royer
 * @author Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public class AdaptationZSlidingWindowDetectionArmSelection extends StandardAdaptationModule implements AdaptationModuleInterface<InterpolatedAcquisitionState>
{

  private final Variable<Double> mDeltaZVariable = new Variable<>("DeltaZ", 1.0);

  private final BoundedVariable<Integer> mSlidingWindowWidthVariable = new BoundedVariable<Integer>("SlidingWindowWidth", 1, 0, Integer.MAX_VALUE);

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
  public AdaptationZSlidingWindowDetectionArmSelection(int pNumberOfSamples, int pSlidingWindowWidth, boolean pFirstAndLastControlPlaneZero, double pDeltaZ, double pProbabilityThreshold, double pImageMetricThreshold, double pExposureInSeconds, double pLaserPower)
  {
    super("Z*", LightSheetDOF.IZ, pNumberOfSamples, pProbabilityThreshold, pImageMetricThreshold, pExposureInSeconds, pLaserPower);
    getDeltaZVariable().set(pDeltaZ);
    mSlidingWindowWidthVariable.set(pSlidingWindowWidth);

    mFirstAndLastControlPlaneZero.set(pFirstAndLastControlPlaneZero);

    getIsActiveVariable().set(false);
  }

  @Override
  public Future<?> atomicStep(int... pStepCoordinates)
  {
    info("Atomic step...");

    int lControlPlaneIndex = pStepCoordinates[0];
    int lLightSheetIndex = pStepCoordinates[1];

    double lDeltaZ = getDeltaZVariable().get();
    int lNumberOfSamples = getNumberOfSamplesVariable().get();
    int lHalfSamples = (lNumberOfSamples - 1) / 2;
    double lMinZ = -lDeltaZ * lHalfSamples;

    final TDoubleArrayList lDZList = new TDoubleArrayList();

    InterpolatedAcquisitionState lAcquisitionState = getAdaptiveEngine().getAcquisitionStateVariable().get();

    LightSheetMicroscopeQueue lQueue = (LightSheetMicroscopeQueue) getAdaptiveEngine().getMicroscope().requestQueue();

    lQueue.clearQueue();

    // here we set IZ:
    lAcquisitionState.applyStateAtControlPlane(lQueue, lControlPlaneIndex);
    double lCurrentDZ = lQueue.getDZ(0);

    lQueue.setI(lLightSheetIndex);
    lQueue.setExp(getExposureInSecondsVariable().get());
    lQueue.setIP(lLightSheetIndex, getLaserPowerVariable().get());
    lQueue.setILO(false);
    lQueue.setC(false);
    lQueue.setDZ(lCurrentDZ + lMinZ);
    lQueue.addCurrentStateToQueue();
    lQueue.addCurrentStateToQueue();

    lQueue.setILO(true);
    lQueue.setC(true);
    for (int i = 0; i < lNumberOfSamples; i++)
    {
      double z = lMinZ + lDeltaZ * i;
      lDZList.add(z);
      lQueue.setDZ(lCurrentDZ + z);
      lQueue.addCurrentStateToQueue();
    }

    lQueue.setILO(false);
    lQueue.setC(false);
    lQueue.setDZ(lCurrentDZ);
    lQueue.addCurrentStateToQueue();

    lQueue.setTransitionTime(0.5);
    lQueue.setFinalisationTime(0.001);

    lQueue.finalizeQueue();

    lQueue.addMetaDataEntry(MetaDataChannel.Channel, "NoDisplay");

    return findBestDOFValue(lControlPlaneIndex, lLightSheetIndex, lQueue, lAcquisitionState, lDZList);

    /**/

  }

  @Override
  public void updateState(InterpolatedAcquisitionState pStateToUpdate)
  {
    updateStateInternal(pStateToUpdate, true, true);
  }

  @Override
  protected void updateStateInternal(InterpolatedAcquisitionState pStateToUpdate, boolean pRelativeCorrection, boolean pFlipCorrectionSign)
  {
    info("Update new state...");

    int lNumberOfControlPlanes = getAdaptiveEngine().getAcquisitionStateVariable().get().getNumberOfControlPlanes();
    int lNumberOfLightSheets = getAdaptiveEngine().getAcquisitionStateVariable().get().getNumberOfLightSheets();

    int lNumberOfDetectionArms = getAdaptiveEngine().getAcquisitionStateVariable().get().getNumberOfDetectionArms();

    // correct choosing the wrong detection arm in single slices or for single
    // light sheets
    int[][] selectedDetectionArms = new int[lNumberOfControlPlanes][lNumberOfLightSheets];

    for (int cpi = 0; cpi < lNumberOfControlPlanes; cpi++)
    {
      for (int l = 0; l < lNumberOfLightSheets; l++)
      {
        int lSelectedDetectionArm = 0;
        Result lResult = getResult(cpi, l, 0);

        if (lResult == null)
        {
          severe("Found null result for cpi=%d, l=%d \n", cpi, l);
          continue;
        }

        for (int d = 1; d < lNumberOfDetectionArms; d++)
        {
          Result lOneResult = getResult(cpi, l, d);

          if (lOneResult != null)
          {
            if (lOneResult.metricmax * lOneResult.probability > lResult.metricmax * lResult.probability)
            {
              lResult = lOneResult;
              lSelectedDetectionArm = d;
            }
          }
        }
        selectedDetectionArms[cpi][l] = lSelectedDetectionArm;
      }

      info("Best detection arms for control plane " + cpi + ": " + Arrays.toString(selectedDetectionArms[cpi]));
    }

    int[] popularDetectionArms = new int[lNumberOfControlPlanes];

    // determination of popular detection arms
    for (int cpi = 0; cpi < lNumberOfControlPlanes; cpi++)
    {
      int[] detectionArmPopularity = new int[lNumberOfDetectionArms];

      for (int l = 0; l < lNumberOfLightSheets; l++)
      {
        detectionArmPopularity[selectedDetectionArms[cpi][l]]++;
      }
      int maxPopularity = -1;
      int mostPopularDetectionArm = 0;
      for (int i = 0; i < lNumberOfDetectionArms; i++)
      {
        if (detectionArmPopularity[i] > maxPopularity)
        {
          maxPopularity = detectionArmPopularity[i];
          mostPopularDetectionArm = i;
        }
      }

      popularDetectionArms[cpi] = mostPopularDetectionArm;
    }

    info("Popular detection arms: " + Arrays.toString(popularDetectionArms));

    // todo: the following block may not work in general:
    // the idea is to make the chosen detection arms equal in larger regions of
    // the sample. The scope should not switch
    // between cameras from control plane to control plane. It should in
    // principle image the half sample with one
    // camera and the other half with the other camera.
    int lSlidingWindowWidth = mSlidingWindowWidthVariable.get();
    int[] lSelectedDetectionsArms = new int[lNumberOfControlPlanes];
    for (int cpi = 0; cpi < lNumberOfControlPlanes; cpi++)
    {
      int lSlidingWindowEnd = Math.min(cpi + (int) (lSlidingWindowWidth * 0.5), lNumberOfControlPlanes - 1);
      int lSlidingWindowStart = Math.max(lSlidingWindowEnd - lSlidingWindowWidth + 1, 0);

      if (lSlidingWindowEnd - lSlidingWindowStart + 1 != lSlidingWindowWidth)
      {
        lSlidingWindowEnd = Math.min(lSlidingWindowStart + lSlidingWindowWidth - 1, lNumberOfControlPlanes - 1);
      }

      // info("Sliding window "+ cpi + ": " + lSlidingWindowStart + " - " +
      // lSlidingWindowEnd);
      lSelectedDetectionsArms[cpi] = findPopularDetectionArm(popularDetectionArms, lNumberOfDetectionArms, lSlidingWindowStart, lSlidingWindowEnd);
    }
    info("Selected detection arms: " + Arrays.toString(lSelectedDetectionsArms));

    // int mostPopularDetectionArmInFirstBlock =
    // findPopularDetectionArm(popularDetectionArms, lNumberOfDetectionArms, 0,
    // blockSize);

    // int lSelectedDetectionArm = 0;
    for (int cpi = 0; cpi < lNumberOfControlPlanes; cpi++)
    {
      /// int blockIndex = cpi / blockSize;
      // if (blockIndex != formerBlockIndex) {
      // get popular detection arm index for this block
      // int blockStart = cpi;

      int lSelectedDetectionArm = lSelectedDetectionsArms[cpi];
      // info("Selected detection arm: " + lSelectedDetectionArm);

      for (int l = 0; l < lNumberOfLightSheets; l++)
      {
        Result lResult = getResult(cpi, l, lSelectedDetectionArm);

        if (lResult == null)
        {
          severe("Found null result for cpi=%d, l=%d \n", cpi, l);
          continue;
        }

        double lCorrection = (pFlipCorrectionSign ? -1 : 1) * lResult.argmax;

        boolean lProbabilityInsufficient = lResult.probability < getProbabilityThresholdVariable().get();

        boolean lMetricMaxInsufficient = lResult.metricmax < getImageMetricThresholdVariable().get();

        if (lMetricMaxInsufficient)
        {
          warning("Metric maximum too low (%g < %g) for cpi=%d, l=%d using neighbooring values\n", lResult.metricmax, getImageMetricThresholdVariable().get(), cpi, l);
        }

        if (lProbabilityInsufficient)
        {
          warning("Probability too low (%g < %g) for cpi=%d, l=%d using neighbooring values\n", lResult.probability, getProbabilityThresholdVariable().get(), cpi, l);
        }

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
        // ?
        if (pRelativeCorrection)
          pStateToUpdate.getInterpolationTables()
                        .add(mLightSheetDOF, cpi, l, lCorrection);
        else
          pStateToUpdate.getInterpolationTables()
                        .set(mLightSheetDOF, cpi, l, lCorrection);
         */
      }
    }
  }

  /**
   * Returns the variable holding the delta Z value
   *
   * @return delta Z variable
   */
  public Variable<Double> getDeltaZVariable()
  {
    return mDeltaZVariable;
  }

  private int findPopularDetectionArm(int[] popularDetectionArms, int pNumberOfDetectionArms, int start, int end)
  {
    int[] countPerArm = new int[pNumberOfDetectionArms];
    for (int i = start; i <= end; i++)
    {
      int detectionArm = popularDetectionArms[i];
      countPerArm[detectionArm]++;
    }

    int maxPopular = 0;
    int chosenDetectionArm = 0;

    for (int i = 0; i < countPerArm.length; i++)
    {
      if (countPerArm[i] > maxPopular)
      {
        maxPopular = countPerArm[i];
        chosenDetectionArm = i;
      }
    }
    return chosenDetectionArm;
  }

  public BoundedVariable<Integer> getSlidingWindowWidthVariable()
  {
    return mSlidingWindowWidthVariable;
  }

  public Variable<Boolean> getFirstAndLastControlPlaneZero()
  {
    return mFirstAndLastControlPlaneZero;
  }
}
