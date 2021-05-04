package clearcontrol.core.device;

import clearcontrol.core.device.queue.QueueDeviceInterface;
import clearcontrol.core.device.queue.QueueInterface;

import java.util.concurrent.Future;

/**
 * Queuable virtual device base class. Devices deriving from this base class
 * have the built-in machinery to handle state queues for variables
 *
 * @param <Q> queue type
 * @author royer
 */
public abstract class QueueableVirtualDevice<Q extends QueueInterface> extends VirtualDevice implements QueueDeviceInterface<Q>
{

  /**
   * Instanciates a queueable virtual device given a name.
   *
   * @param pDeviceName device name
   */
  public QueueableVirtualDevice(final String pDeviceName)
  {
    super(pDeviceName);
  }

  @Override
  public boolean open()
  {
    return true;
  }

  @Override
  public boolean close()
  {
    return true;
  }

  @Override
  public abstract Q requestQueue();

  @Override
  public abstract Future<Boolean> playQueue(Q pQueue);
}
