package clearcontrol.devices.signalgen.score;

import clearcontrol.core.device.update.UpdatableInterface;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Compiled score interface
 *
 * @author royer
 */
public interface CompiledScoreInterface extends UpdatableInterface
{
  /**
   * Returns lock
   *
   * @return lock
   */
  ReentrantLock getLock();
}
