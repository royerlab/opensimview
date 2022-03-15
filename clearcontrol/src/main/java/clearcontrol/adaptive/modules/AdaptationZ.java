package clearcontrol.adaptive.modules;

import clearcontrol.LightSheetDOF;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.LightSheetMicroscopeQueue;
import clearcontrol.configurationstate.ConfigurationState;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.metadata.MetaDataView;
import clearcontrol.stack.metadata.MetaDataViewFlags;
import clearcontrol.stack.metadata.StackMetaData;
import clearcontrol.state.InterpolatedAcquisitionState;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.concurrent.Future;

/**
 * Adaptation module responsible for adjusting the Z focus
 *
 * @author royer
 */
public class AdaptationZ extends StandardAdaptationPerLightSheetModule implements AdaptationModuleInterface<InterpolatedAcquisitionState>
{

  private final BoundedVariable<Double> mDeltaZVariable = new BoundedVariable<>("Delta Z", 1.0, 0.0, Double.POSITIVE_INFINITY, 0.001);

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
  public AdaptationZ(int pNumberOfSamples, double pDeltaZ, double pProbabilityThreshold, double pImageMetricThreshold, double pExposureInSeconds, double pLaserPower, int pNumberOfLightSheets)
  {
    super("Z", LightSheetDOF.IZ, pNumberOfSamples, pProbabilityThreshold, pImageMetricThreshold, pExposureInSeconds, pLaserPower);
    getDeltaZVariable().set(pDeltaZ);

    setConfigurationState(ConfigurationState.UNINITIALIZED);

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

    LightSheetMicroscope lLightSheetMicroscope = (LightSheetMicroscope) getAdaptiveEngine().getMicroscope();
    LightSheetMicroscopeQueue lQueue = lLightSheetMicroscope.requestQueue();

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

    int lLaserLineToUse = getLaserLineVariable().get().intValue();
    lQueue.setILO(lLightSheetIndex, lLaserLineToUse, true);
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
    lQueue.setFinalisationTime(0.005);

    lQueue.finalizeQueue();

    // Setting metadata:
    for (int lCameraIndex = 0; lCameraIndex < lLightSheetMicroscope.getNumberOfDetectionArms(); lCameraIndex++)
    {
      StackMetaData lMetaData = lQueue.getCameraDeviceQueue(lCameraIndex).getMetaDataVariable().get();

      lMetaData.addEntry(MetaDataView.Camera, lCameraIndex);
      lMetaData.addEntry(MetaDataViewFlags.getLightSheet(lLightSheetIndex), true);
      lMetaData.addEntry(MetaDataChannel.Channel, String.format("AutoPilot_Z_P%d_C%d_L%d", lControlPlaneIndex, lCameraIndex, lLightSheetIndex));
    }

    Future<?> result = findBestDOFValue(lControlPlaneIndex, lLightSheetIndex, lQueue, lAcquisitionState, lDZList);

    return result;
    /**/

  }

  @Override
  public void updateState(InterpolatedAcquisitionState pStateToUpdate)
  {
    updateStateInternal(pStateToUpdate, true, true);
  }

  /**
   * Returns the variable holding the delta Z value
   *
   * @return delta Z variable
   */
  public BoundedVariable<Double> getDeltaZVariable()
  {
    return mDeltaZVariable;
  }

}
