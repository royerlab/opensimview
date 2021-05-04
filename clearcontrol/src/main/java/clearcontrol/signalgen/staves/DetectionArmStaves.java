package clearcontrol.signalgen.staves;

import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.math.functions.UnivariateAffineFunction;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.signalgen.measure.Measure;
import clearcontrol.devices.signalgen.staves.ConstantStave;
import clearcontrol.component.detection.DetectionArmQueue;

/**
 * Detection arm staves
 *
 * @author royer
 */
public class DetectionArmStaves
{
  private final DetectionArmQueue mDetectionArmQueue;

  private final ConstantStave mDetectionZStave;

  private final int mStaveIndex;

  /**
   * Instantiates an object holding detection arm staves
   *
   * @param pDetectionArmQueue detection arm queue
   */
  public DetectionArmStaves(DetectionArmQueue pDetectionArmQueue)
  {
    super();
    mDetectionArmQueue = pDetectionArmQueue;

    mDetectionZStave = new ConstantStave("detection.z", 0);

    mStaveIndex = MachineConfiguration.get().getIntegerProperty("device.lsm.detection." + pDetectionArmQueue.getDetectionArm().getName() + ".z.index", 0);

  }

  /**
   * Returns detection arm
   *
   * @return detection arm
   */
  public DetectionArmQueue getDetectionArmQueue()
  {
    return mDetectionArmQueue;
  }

  /**
   * Adds staves to measures
   *
   * @param pBeforeExposureMeasure before exp measure
   * @param pExposureMeasure       exposure measure
   * @param pFinalMeasure          final measure
   */
  public void addStavesToMeasures(Measure pBeforeExposureMeasure, Measure pExposureMeasure, Measure pFinalMeasure)
  {
    // Analog outputs before exposure:
    pBeforeExposureMeasure.setStave(mStaveIndex, mDetectionZStave);

    // Analog outputs at exposure:
    pExposureMeasure.setStave(mStaveIndex, mDetectionZStave);

    // Analog outputs at finalization:
    pFinalMeasure.setStave(mStaveIndex, mDetectionZStave);
  }

  /**
   * Updates the staves based on the information from detection arm queue
   *
   * @param pBeforeExposureMeasure before exposure measure
   * @param pExposureMeasure       exposure measure
   * @param pFinalMeasure          final measure
   */
  public void update(Measure pBeforeExposureMeasure, Measure pExposureMeasure, Measure pFinalMeasure)
  {

    BoundedVariable<Number> lZVariable = mDetectionArmQueue.getZVariable();

    Variable<UnivariateAffineFunction> lZFunction = mDetectionArmQueue.getDetectionArm().getZFunction();

    double lZFocus = lZVariable.get().doubleValue();
    float lZFocusTransformed = (float) lZFunction.get().value(lZFocus);
    mDetectionZStave.setValue(lZFocusTransformed);

  }

  /**
   * Returns detection stave
   *
   * @return detection stave
   */
  public ConstantStave getDetectionZStave()
  {
    return mDetectionZStave;
  }

}
