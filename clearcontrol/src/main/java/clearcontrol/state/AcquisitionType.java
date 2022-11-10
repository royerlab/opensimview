package clearcontrol.state;

/**
 * Acquisition type
 *
 * @author royer
 */
public enum AcquisitionType
{
  /**
   * Interactive acquisition stack
   */
  Interactive,

  /**
   * Timelapse stack
   */
  TimeLapse,

  TimelapseSequential,

  TimeLapseInterleaved,

  TimeLapseMultiColorInterleaved,

  TimeLapseOpticallyCameraFused,

  TimeLapseHybridInterleavedOpticsPrefused
}
