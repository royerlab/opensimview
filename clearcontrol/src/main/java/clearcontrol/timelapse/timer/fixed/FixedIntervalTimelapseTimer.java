package clearcontrol.timelapse.timer.fixed;

import clearcontrol.timelapse.timer.TimelapseTimerBase;
import clearcontrol.timelapse.timer.TimelapseTimerInterface;

import java.util.concurrent.TimeUnit;

/**
 * Fixed interval timelapse timer
 *
 * @author royer
 */
public class FixedIntervalTimelapseTimer extends TimelapseTimerBase implements TimelapseTimerInterface
{

  /**
   * Instanciates a fixed interval timelapse timer
   */
  public FixedIntervalTimelapseTimer()
  {
    super(1, TimeUnit.SECONDS);
  }

}
