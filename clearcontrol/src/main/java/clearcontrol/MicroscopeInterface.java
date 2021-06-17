package clearcontrol;

import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.device.change.HasChangeListenerInterface;
import clearcontrol.core.device.name.NameableInterface;
import clearcontrol.core.device.queue.QueueDeviceInterface;
import clearcontrol.core.device.queue.QueueInterface;
import clearcontrol.core.variable.Variable;
import clearcontrol.devices.stages.StageDeviceInterface;
import clearcontrol.stack.CleanupStackVariable;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import clearcontrol.state.AcquisitionStateManager;
import coremem.recycling.RecyclerInterface;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MicroscopeInterface is a generic interface for all microscopes types.
 *
 * @param <Q> queue type
 * @author royer
 */
public interface MicroscopeInterface<Q extends QueueInterface> extends NameableInterface, QueueDeviceInterface<Q>, HasChangeListenerInterface<VirtualDevice>

{

  /**
   * Returns the microscopes name.
   *
   * @return microscope's name.
   */
  @Override
  String getName();

  /**
   * Returns the master lock, which can be used to make sure that opening,
   * closing, playing queues don't overlapp.
   *
   * @return master lock
   */
  ReentrantLock getMasterLock();

  /**
   * Current task. This is used to prevent multiple tasks executions don't
   * overlap, for example, starting an acquisition while some other task is
   * already running...
   *
   * @return current task atomic reference
   */
  AtomicReference<Object> getCurrentTask();

  /**
   * Sets the simulation flag.
   *
   * @param pSimulation true if simulation, false otherwise.
   */
  void setSimulation(boolean pSimulation);

  /**
   * Returns whether the microscope is in simulation mode.
   *
   * @return true if simulation mode, or false otherwise
   */
  default boolean isSimulation()
  {
    return false;
  }

  /**
   * Adds a device of a given type. Devices are uniquely identified by their
   * class and index: (class,index) -> device
   *
   * @param pDeviceIndex device index
   * @param pDevice      device
   */
  <T> void addDevice(int pDeviceIndex, T pDevice);

  /**
   * Returns the number of devices of a given class. Devices are uniquely
   * identified by their class and index: (class,index) -> device
   *
   * @param pClass class
   * @return number of devices of a given type
   */
  <T> int getNumberOfDevices(Class<T> pClass);

  /**
   * Returns a device for a given type (class) and index. Devices are uniquely
   * identified by their class and index: (class,index) -> device
   *
   * @param pClass class
   * @param pIndex index
   * @return device for given pair; (class,index)
   */
  <T> T getDevice(Class<T> pClass, int pIndex);

  /**
   * Returns all devices for a given type (class). Devices are uniquely
   * identified by their class and index: (class,index) -> device
   *
   * @param pClass class
   * @return device for given pair: (class,index)
   */
  <T> ArrayList<T> getDevices(Class<T> pClass);

  /**
   * Returns the device list object from which all devices can be queried.
   *
   * @return device list object
   */
  MicroscopeDeviceLists getDeviceLists();

  /**
   * Adds acquisition state manager
   *
   * @return acquisition manager
   */
  AcquisitionStateManager<?> addAcquisitionStateManager();

  /**
   * Returns the acquisition state manager for this microscope
   *
   * @return acquisition state manager
   */
  AcquisitionStateManager<?> getAcquisitionStateManager();

  /**
   * Sets the recycler that should be used by the stack camera device of given
   * id.
   *
   * @param pStackCameraDeviceIndex stack camera device index
   * @param pRecycler               recycler
   */
  void setRecycler(int pStackCameraDeviceIndex, RecyclerInterface<StackInterface, StackRequest> pRecycler);

  /**
   * Sets the recycler that should be used by _all_ stack camera devices.
   *
   * @param pRecycler recyler to be used by all camera devices
   */
  void setRecycler(RecyclerInterface<StackInterface, StackRequest> pRecycler);

  /**
   * Returns the recycler currently b the stack camera device of given id.
   *
   * @param pStackCameraDeviceIndex stack camera index id.
   * @return recycler.
   */
  RecyclerInterface<StackInterface, StackRequest> getRecycler(int pStackCameraDeviceIndex);

  /**
   * Uses a recycler with given parameters. This recycler will be used for all
   * subsequent plays. if teh recycler does not exist yet, it is created.
   *  @param pName                           recycler name
   * @param pMinimumNumberOfAvailableStacks minimum number of available stacks
   * @param pMaximumNumberOfAvailableStacks maximum number of available stacks
   * @param pMaximumNumberOfLiveStacks      maximum number of live stacks
   * @return
   */
  RecyclerInterface<StackInterface, StackRequest> useRecycler(String pName, int pMinimumNumberOfAvailableStacks, int pMaximumNumberOfAvailableStacks, int pMaximumNumberOfLiveStacks);

  /**
   * Clears a given recycler.
   *
   * @param pName recycler name
   */
  void clearRecycler(String pName);

  /**
   * Clears all recyclers.
   */
  void clearAllRecyclers();

  /**
   * Plays queue for all devices, and waits for playback to finish.
   *
   * @param pQueue    queue to play
   * @param pTimeOut  timeout
   * @param pTimeUnit time unit for timeout
   * @return true if successful
   * @throws InterruptedException if interupted
   * @throws ExecutionException   if execution occured during async execution
   * @throws TimeoutException     if timeout
   */
  Boolean playQueueAndWait(Q pQueue, long pTimeOut, TimeUnit pTimeUnit) throws InterruptedException, ExecutionException, TimeoutException;

  /**
   * Plays queue for all devices, waits for playback to finish as well as waits
   * for stacks to be delivered.
   *
   * @param pQueue    queue to play
   * @param pTimeOut  timeout time
   * @param pTimeUnit timeout unit
   * @return true if successfull
   * @throws InterruptedException if interupted
   * @throws ExecutionException   if execution occured during async execution
   * @throws TimeoutException     if timeout
   */
  Boolean playQueueAndWaitForStacks(Q pQueue, long pTimeOut, TimeUnit pTimeUnit) throws InterruptedException, ExecutionException, TimeoutException;

  /**
   * Returns the variable hlding the last played queue
   *
   * @return last played queue variable
   */
  Variable<Q> getPlayedQueueVariable();

  /**
   * Returns the average timestamp for all stacks acquired during for last
   * played queue.
   *
   * @return timestamp in nanoseconds
   */
  long lastAcquiredStacksTimeStampInNS();

  /**
   * Returns Camera Stack Variable for given stack camera index
   *
   * @param pIndex stack camera index
   * @return Stack Variable
   */
  Variable<StackInterface> getCameraStackVariable(int pIndex);

  /**
   * Returns Terminator Stack Variable for given stack camera index
   *
   * @param pIndex stack camera index
   * @return Stack Variable
   */
  Variable<StackInterface> getTerminatorStackVariable(int pIndex);


  /**
   * Returns Cleanup Stack Variable
   *
   * @return Stack Variable
   */
  CleanupStackVariable getCleanupStackVariable();

  /**
   * Resets Terminator Stack Variables so that stacks get released.
   */
  void resetTerminatorStackVariables();

  /**
   * Returns the size in nanometer (anisotropic XY) of a pixel. this is the
   * actual physical size in the sample - thus taking into account overall
   * magnification. The size is returned wrapped into a Variable.
   *
   * @param pCameraIndex camera index
   * @return size in nanometer as a Variable
   */
   Variable<Double> getCameraPixelSizeInNanometerVariable(int pCameraIndex);

  /**
   * Sets the main sample stage X position.
   *
   * @param pXValue x position value
   */
  void setStageX(double pXValue);

  /**
   * Sets the main sample stage Y position.
   *
   * @param pYValue y position value
   */
  void setStageY(double pYValue);

  /**
   * Sets the sample's main stage Z position.
   *
   * @param pZValue z position value
   */
   void setStageZ(double pZValue);

  /**
   * Sets the sample's main stage R position.
   *
   * @param pRValue r position value
   */
   void setStageR(double pRValue);

  /**
   * Returns the main sample stage X position.
   *
   * @return x position value
   */
   double getStageX();

  /**
   * Returns the main sample stage Y position.
   *
   * @return y position value
   */
   double getStageY();

  /**
   * Returns the sample's main stage Z position.
   *
   * @return z position value
   */
   double getStageZ();

  /**
   * Returns the sample's main stage R position.
   *
   * @return r position value
   */
   double getStageR();

  /**
   * Returns the main XYZR stage of this microscope.
   *
   * @return main XYZR stage
   */
   StageDeviceInterface getMainStage();

}
