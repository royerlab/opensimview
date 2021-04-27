package clearcontrol.microscope.lightsheet.adaptive;

import clearcontrol.core.device.task.TaskDevice;
import clearcontrol.microscope.adaptive.AdaptiveEngine;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.adaptive.modules.*;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;

/**
 * This class is a bit a placeholder for an empty engine. It is used to make another
 * Adaptive Panel accessible in the GUI
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public class AdaptationStateEngine extends TaskDevice
{
  AdaptiveEngine mAdaptiveEngine;
  LightSheetMicroscope mLightSheetMicroscope;
  InterpolatedAcquisitionState mInterpolatedAcquisitionState;

  public AdaptationStateEngine(String pDeviceName,
                               AdaptiveEngine pAdaptiveEngine,
                               LightSheetMicroscope pLightSheetMicroscope,
                               InterpolatedAcquisitionState pInterpolatedAcquisitionState)
  {
    super(pDeviceName);

    mAdaptiveEngine = pAdaptiveEngine;
    mLightSheetMicroscope = pLightSheetMicroscope;
    mInterpolatedAcquisitionState = pInterpolatedAcquisitionState;
  }

  public AdaptiveEngine getAdaptiveEngine()
  {
    return mAdaptiveEngine;
  }

  public LightSheetMicroscope getLightSheetMicroscope()
  {
    return mLightSheetMicroscope;
  }

  public InterpolatedAcquisitionState getInterpolatedAcquisitionState()
  {
    return mInterpolatedAcquisitionState;
  }

  /**
   * This static function allows to maintain setup code for Adaptation modules in one
   * place, which are used in several demos and main XWing code
   * <p>
   * This class may disappear again or refactored...
   */
  public static void setup(LightSheetMicroscope lLightSheetMicroscope,
                           InterpolatedAcquisitionState lAcquisitionState)
  {
    int lNumberOfLightSheets = lLightSheetMicroscope.getNumberOfLightSheets();

    AdaptiveEngine<InterpolatedAcquisitionState>
        lAdaptiveEngine =
        lLightSheetMicroscope.addAdaptiveEngine(lAcquisitionState);
    lAdaptiveEngine.getRunUntilAllModulesReadyVariable().set(true);

    lAdaptiveEngine.add(new AdaptationZ(9,
                                        3,
                                        0.95,
                                        2e-5,
                                        0.010,
                                        0.02,
                                        lNumberOfLightSheets));
    lAdaptiveEngine.add(new AdaptationZSlidingWindowDetectionArmSelection(7,
                                                                          3,
                                                                          true,
                                                                          3,
                                                                          0.95,
                                                                          2e-5,
                                                                          0.010,
                                                                          0.02));

    lAdaptiveEngine.add(new AdaptationZManualDetectionArmSelection(9,
                                                                   3,
                                                                   0.95,
                                                                   2e-5,
                                                                   0.010,
                                                                   0.02,
                                                                   lNumberOfLightSheets,
                                                                   lLightSheetMicroscope));

    lAdaptiveEngine.add(new AdaptationX(11,
                                        -300,
                                        300,
                                        0.94,
                                        2e-5,
                                        0.050,
                                        0.02));

    lAdaptiveEngine.add(new AdaptationA(15,
                                        11,
                                        0.95,
                                        2e-5,
                                        0.01,
                                        0.02));

    lAdaptiveEngine.add(new AdaptationP(0.5));

    lAdaptiveEngine.add(new AdaptationW(11,
                                        0.95,
                                        2e-5,
                                        0.01,
                                        0.02));

    lLightSheetMicroscope.addDevice(0,
                                    new AdaptationStateEngine("Microscope State",
                                                              lAdaptiveEngine,
                                                              lLightSheetMicroscope,
                                                              lAcquisitionState));
  }

  @Override public void run()
  {

  }
}
