package clearcontrol.adaptive.modules;

import clearcontrol.LightSheetDOF;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.LightSheetMicroscopeQueue;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.state.InterpolatedAcquisitionState;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.concurrent.Future;

/**
 * Adaptation module responsible for adjusting the X lightsheeet positions
 *
 * @author royer
 */
public class AdaptationX extends StandardAdaptationModule implements AdaptationModuleInterface<InterpolatedAcquisitionState>
{

  private final BoundedVariable<Double> mMinXVariable = new BoundedVariable<Double>("MinX", -1000.0);

  private final BoundedVariable<Double> mMaxXVariable = new BoundedVariable<Double>("MaxX", 1000.0);

  /**
   * Instantiates a X focus adaptation module given the number of samples, probability
   * threshold, and image metric threshold
   *
   * @param pNumberOfSamples      number of samples
   * @param pMinX                 min X
   * @param pMaxX                 max X
   * @param pProbabilityThreshold probability threshold
   * @param pImageMetricThreshold image metric threshold
   * @param pExposureInSeconds    exposure in seconds
   * @param pLaserPower           laser power
   */
  public AdaptationX(int pNumberOfSamples, double pMinX, double pMaxX, double pProbabilityThreshold, double pImageMetricThreshold, double pExposureInSeconds, double pLaserPower)
  {
    super("X*", LightSheetDOF.IX, pNumberOfSamples, pProbabilityThreshold, pImageMetricThreshold, pExposureInSeconds, pLaserPower);

    getMinXVariable().set(pMinX);
    getMaxXVariable().set(pMaxX);

    getIsActiveVariable().set(false);
  }

  @Override
  public Future<?> atomicStep(int... pStepCoordinates)
  {
    int lControlPlaneIndex = pStepCoordinates[0];
    int lLightSheetIndex = pStepCoordinates[1];

    LightSheetMicroscope lLightsheetMicroscope = (LightSheetMicroscope) getAdaptiveEngine().getMicroscope();

    int lNumberOfSamples = getNumberOfSamplesVariable().get();
    double lMinX = getMinXVariable().get().doubleValue();
    double lMaxX = getMaxXVariable().get().doubleValue();
    double lDeltaX = (lMaxX - lMinX) / (lNumberOfSamples - 1);

    LightSheetMicroscopeQueue lQueue = lLightsheetMicroscope.requestQueue();
    InterpolatedAcquisitionState lAcquisitionState = getAdaptiveEngine().getAcquisitionStateVariable().get();

    lQueue.clearQueue();

    lAcquisitionState.applyStateAtControlPlane(lQueue, lControlPlaneIndex);

    final TDoubleArrayList lIXList = new TDoubleArrayList();

    lQueue.setI(lLightSheetIndex);
    lQueue.setExp(getExposureInSecondsVariable().get());
    lQueue.setIP(lLightSheetIndex, getLaserPowerVariable().get());
    lQueue.setILO(false);
    lQueue.setC(false);
    lQueue.setIX(lLightSheetIndex, lMinX);
    lQueue.addCurrentStateToQueue();
    lQueue.addCurrentStateToQueue();

    int lLaserLineToUse = getLaserLineVariable().get().intValue();
    lQueue.setILO(lLightSheetIndex, lLaserLineToUse, true);
    lQueue.setC(true);
    for (int i = 0; i < lNumberOfSamples; i++)
    {
      double x = lMinX + lDeltaX * i;
      lIXList.add(x);
      lQueue.setIX(lLightSheetIndex, x);
      // Trash the first image to give time to the piezo actuator to move...
      lQueue.setC(false);
      lQueue.addCurrentStateToQueue();
      lQueue.setC(true);
      lQueue.addCurrentStateToQueue();
    }

    lQueue.setILO(false);
    lQueue.setC(false);
    lQueue.addCurrentStateToQueue();

    lQueue.setTransitionTime(0.75);
    lQueue.setFinalisationTime(0.001);

    lQueue.finalizeQueue();

    lQueue.addMetaDataEntry(MetaDataChannel.Channel, "NoDisplay");

    return findBestDOFValue(lControlPlaneIndex, lLightSheetIndex, lQueue, lAcquisitionState, lIXList);

  }

  @Override
  public void updateState(InterpolatedAcquisitionState pStateToUpdate)
  {
    // Makes no sense to have the max correction be inconsistent with the min and max...
    getMaxCorrectionVariable().set(Math.max(Math.abs(getMinXVariable().get()), Math.abs(getMaxXVariable().get())));
    updateStateInternal(pStateToUpdate, false, false);
  }

  /**
   * Returns the minimum X value
   *
   * @return minimum X value
   */
  public BoundedVariable<Double> getMinXVariable()
  {
    return mMinXVariable;
  }

  /**
   * Returns the maximum X value
   *
   * @return maximum X value
   */
  public BoundedVariable<Double> getMaxXVariable()
  {
    return mMaxXVariable;
  }

}
