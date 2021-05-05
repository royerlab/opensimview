package clearcontrol;

import clearcontrol.core.concurrent.executors.AsynchronousSchedulerFeature;
import clearcontrol.core.concurrent.future.FutureBooleanList;
import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.device.active.ActivableDeviceInterface;
import clearcontrol.core.device.change.ChangeListener;
import clearcontrol.core.device.change.HasChangeListenerInterface;
import clearcontrol.core.device.openclose.OpenCloseDeviceInterface;
import clearcontrol.core.device.queue.QueueDeviceInterface;
import clearcontrol.core.device.queue.QueueInterface;
import clearcontrol.core.device.startstop.StartStopDeviceInterface;
import clearcontrol.core.gc.GarbageCollector;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.devices.cameras.StackCameraDeviceInterface;
import clearcontrol.devices.stages.StageDeviceInterface;
import clearcontrol.stack.CleanupStackVariable;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRecyclerManager;
import clearcontrol.stack.StackRequest;
import clearcontrol.stack.metadata.StackMetaData;
import clearcontrol.state.AcquisitionStateManager;
import coremem.recycling.RecyclerInterface;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.max;

/**
 * Microscope base class providing common fields and methods for all microscopes
 *
 * @param <M> microscope type
 * @param <Q> queue type
 * @author royer
 */
public abstract class MicroscopeBase<M extends MicroscopeBase<M, Q>, Q extends MicroscopeQueueBase<M, Q>> extends VirtualDevice implements MicroscopeInterface<Q>, StartStopDeviceInterface, AsynchronousSchedulerFeature, LoggingFeature
{

  protected final StackRecyclerManager mStackRecyclerManager;
  protected final MicroscopeDeviceLists mDeviceLists;
  private final ArrayList<Variable<StackInterface>> mTerminatorStackVariableArray;
  private final CleanupStackVariable mCleanupStackVariable;

  protected volatile long mAverageTimeInNS;
  private volatile boolean mSimulation;

  private final ArrayList<Variable<Double>> mCameraPixelSizeInNanometerVariableList = new ArrayList<>();

  // Played queue variable:
  private final Variable<Q> mPlayedQueueVariable = new Variable<Q>("LastPlayedQueue", null);


  // Lock:
  protected ReentrantLock mMasterLock = new ReentrantLock();

  //
  protected AtomicReference<Object> mCurrentTask = new AtomicReference<>();

  /**
   * Instantiates the microscope base class.
   *
   * @param pDeviceName                    device name
   * @param pMaxStackProcessingQueueLength max stack processing queue lengths
   * @param pThreadPoolSize                number of threads in execution pool
   */
  public MicroscopeBase(String pDeviceName, int pMaxStackProcessingQueueLength, int pThreadPoolSize)
  {
    super(pDeviceName);

    mDeviceLists = new MicroscopeDeviceLists();

    mStackRecyclerManager = new StackRecyclerManager();
    mDeviceLists.addDevice(0, mStackRecyclerManager);

    for (int i = 0; i < 128; i++)
    {
      Double lPixelSizeInNanometers = MachineConfiguration.get().getDoubleProperty("device.camera" + i + ".pixelsizenm", null);

      if (lPixelSizeInNanometers == null) break;

      Variable<Double> lPixelSizeInNanometersVariable = new Variable<Double>("Camera" + i + "PixelSizeNm", lPixelSizeInNanometers);
      mCameraPixelSizeInNanometerVariableList.add(lPixelSizeInNanometersVariable);
    }

    mTerminatorStackVariableArray = new ArrayList<>();

    mCleanupStackVariable = new CleanupStackVariable("CleanupStackVariable", 3);
  }

  @Override
  public ReentrantLock getMasterLock()
  {
    return mMasterLock;
  }

  @Override
  public AtomicReference<Object> getCurrentTask()
  {
    return mCurrentTask;
  }

  /**
   * Executes a portion of code while holding the master lock.
   *
   * @param pCallable callable to execute
   * @return return of the callable
   */
  public <R> R lock(Callable<R> pCallable)
  {
    getMasterLock().lock();
    try
    {
      return pCallable.call();
    } catch (Exception e)
    {
      String lMessage = "Exception wile executing locked code";
      severe(lMessage);
      throw new RuntimeException(lMessage, e);
    } finally
    {
      if (getMasterLock().isHeldByCurrentThread()) getMasterLock().unlock();
    }
  }

  @Override
  public boolean isSimulation()
  {
    return mSimulation;
  }

  @Override
  public void setSimulation(boolean pSimulation)
  {
    mSimulation = pSimulation;
  }

  @Override
  public Variable<Double> getCameraPixelSizeInNanometerVariable(int pCameraIndex)
  {
    return mCameraPixelSizeInNanometerVariableList.get(pCameraIndex);
  }

  @Override
  public MicroscopeDeviceLists getDeviceLists()
  {
    return mDeviceLists;
  }

  @Override
  public <T> void addDevice(int pDeviceIndex, T pDevice)
  {
    mDeviceLists.addDevice(pDeviceIndex, pDevice);

    if (pDevice instanceof StackCameraDeviceInterface)
    {
      Variable<StackInterface> lTerminatorStackVariable = new Variable<StackInterface>("TerminatorStackVariable-" + ((StackCameraDeviceInterface<?>) pDevice).getName());
      mTerminatorStackVariableArray.add(lTerminatorStackVariable);
    }

  }

  @Override
  public <T> int getNumberOfDevices(Class<T> pClass)
  {
    return mDeviceLists.getNumberOfDevices(pClass);
  }

  @Override
  public <T> T getDevice(Class<T> pClass, int pIndex)
  {
    return mDeviceLists.getDevice(pClass, pIndex);
  }

  @Override
  public <T> ArrayList<T> getDevices(Class<T> pClass)
  {
    return mDeviceLists.getDevices(pClass);
  }

  @Override
  public void addChangeListener(ChangeListener<VirtualDevice> pChangeListener)
  {
    super.addChangeListener(pChangeListener);
    for (final Object lDevice : mDeviceLists.getAllDeviceList())
    {
      if (lDevice instanceof HasChangeListenerInterface)
      {
        @SuppressWarnings("unchecked") final HasChangeListenerInterface<VirtualDevice> lHasChangeListenersInterface = (HasChangeListenerInterface<VirtualDevice>) lDevice;
        lHasChangeListenersInterface.addChangeListener(pChangeListener);
      }
    }
  }/**/

  @Override
  public void removeChangeListener(ChangeListener<VirtualDevice> pChangeListener)
  {
    super.removeChangeListener(pChangeListener);
    for (final Object lDevice : mDeviceLists.getAllDeviceList())
    {

      if (lDevice instanceof HasChangeListenerInterface)
      {
        @SuppressWarnings("unchecked") final HasChangeListenerInterface<VirtualDevice> lHasChangeListenersInterface = (HasChangeListenerInterface<VirtualDevice>) lDevice;
        lHasChangeListenersInterface.removeChangeListener(pChangeListener);
      }
    }
  }

  @Override
  public AcquisitionStateManager<?> addAcquisitionStateManager()
  {
    AcquisitionStateManager<?> lAcquisitionStateManager = new AcquisitionStateManager<>(this);
    addDevice(0, lAcquisitionStateManager);
    return lAcquisitionStateManager;
  }

  @Override
  public AcquisitionStateManager<?> getAcquisitionStateManager()
  {
    return getDevice(AcquisitionStateManager.class, 0);
  }


  @Override
  public boolean open()
  {
    return lock(() ->
    {
      boolean lIsOpen = true;

      for (final Object lDevice : mDeviceLists.getAllDeviceList())
      {

        if (lDevice instanceof OpenCloseDeviceInterface)
        {
          final OpenCloseDeviceInterface lOpenCloseDevice = (OpenCloseDeviceInterface) lDevice;

          info("Opening device: " + lDevice);
          final boolean lResult = lOpenCloseDevice.open();
          if (lResult) info("Successfully opened device: " + lDevice);
          else warning("Failed to open device: " + lDevice);

          lIsOpen &= lResult;
        }
      }

      return lIsOpen;
    });
  }

  @Override
  public boolean close()
  {
    return lock(() ->
    {
      boolean lIsClosed = true;
      for (final Object lDevice : mDeviceLists.getAllDeviceList())
      {
        if (lDevice instanceof OpenCloseDeviceInterface)
        {
          final OpenCloseDeviceInterface lOpenCloseDevice = (OpenCloseDeviceInterface) lDevice;

          info("Closing device: " + lDevice);
          boolean lResult = lOpenCloseDevice.close();

          if (lResult) info("Successfully closed device: " + lDevice);
          else warning("Failed to close device: " + lDevice);

          lIsClosed &= lResult;
        }
      }

      mStackRecyclerManager.clearAll();

      return lIsClosed;
    });
  }

  @Override
  public boolean start()
  {
    return lock(() ->
    {
      boolean lIsStarted = true;
      for (final Object lDevice : mDeviceLists.getAllDeviceList())
      {
        if (lDevice instanceof StartStopDeviceInterface)
        {
          final StartStopDeviceInterface lStartStopDevice = (StartStopDeviceInterface) lDevice;

          info("Starting device: " + lDevice);
          boolean lResult = lStartStopDevice.start();

          if (lResult) info("Successfully started device: " + lDevice);
          else warning("Failed to start device: " + lDevice);

          lIsStarted &= lResult;
        }
      }

      return lIsStarted;
    });
  }

  @Override
  public boolean stop()
  {
    return lock(() ->
    {
      boolean lIsStopped = true;
      for (final Object lDevice : mDeviceLists.getAllDeviceList())
      {
        if (lDevice instanceof StartStopDeviceInterface)
        {
          final StartStopDeviceInterface lStartStopDevice = (StartStopDeviceInterface) lDevice;

          info("Stoping device: " + lDevice);
          boolean lResult = lStartStopDevice.stop();

          if (lResult) info("Successfully stopped device: " + lDevice);
          else warning("Failed to stop device: " + lDevice);

          lIsStopped &= lResult;
        }
      }

      return lIsStopped;
    });
  }

  boolean isActiveDevice(final Object lDevice)
  {
    boolean lIsActive = true;
    if (lDevice instanceof ActivableDeviceInterface)
    {
      ActivableDeviceInterface lActivableDeviceInterface = (ActivableDeviceInterface) lDevice;
      lIsActive = lActivableDeviceInterface.isActive();
    }
    return lIsActive;
  }

  @Override
  public long lastAcquiredStacksTimeStampInNS()
  {
    return mAverageTimeInNS;
  }

  @Override
  public Variable<Q> getPlayedQueueVariable()
  {
    return mPlayedQueueVariable;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Variable<StackInterface> getCameraStackVariable(int pIndex)
  {
    return mDeviceLists.getDevice(StackCameraDeviceInterface.class, pIndex).getStackVariable();

  }

  @Override
  public Variable<StackInterface> getTerminatorStackVariable(int pIndex)
  {
    return mTerminatorStackVariableArray.get(pIndex);
  }

  @Override
  public void resetTerminatorStackVariables()
  {
    for (Variable<StackInterface> lStackInterfaceVariable : mTerminatorStackVariableArray)
    {
      lStackInterfaceVariable.sendUpdatesToInstead(mCleanupStackVariable);
    }
  }

  @Override
  public void useRecycler(final String pName, final int pMinimumNumberOfAvailableStacks, final int pMaximumNumberOfAvailableObjects, final int pMaximumNumberOfLiveObjects)
  {
    int lNumberOfStackCameraDevices = getNumberOfDevices(StackCameraDeviceInterface.class);
    RecyclerInterface<StackInterface, StackRequest> lRecycler = mStackRecyclerManager.getRecycler(pName, max(1, lNumberOfStackCameraDevices * pMaximumNumberOfAvailableObjects), max(1, 1 + lNumberOfStackCameraDevices * pMaximumNumberOfLiveObjects));

    // for (int i = 0; i < lNumberOfStackCameraDevices; i++)
    // getDevice(StackCameraDeviceInterface.class,
    // i).setMinimalNumberOfAvailableStacks(pMinimumNumberOfAvailableStacks);

    setRecycler(lRecycler);
  }

  @Override
  public void setRecycler(RecyclerInterface<StackInterface, StackRequest> pRecycler)
  {
    int lNumberOfStackCameraDevices = getDeviceLists().getNumberOfDevices(StackCameraDeviceInterface.class);
    for (int i = 0; i < lNumberOfStackCameraDevices; i++)
      setRecycler(i, pRecycler);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setRecycler(int pStackCameraDeviceIndex, RecyclerInterface<StackInterface, StackRequest> pRecycler)
  {
    getDeviceLists().getDevice(StackCameraDeviceInterface.class, pStackCameraDeviceIndex).setStackRecycler(pRecycler);
  }

  @SuppressWarnings("unchecked")
  @Override
  public RecyclerInterface<StackInterface, StackRequest> getRecycler(int pStackCameraDeviceIndex)
  {
    return getDeviceLists().getDevice(StackCameraDeviceInterface.class, pStackCameraDeviceIndex).getStackRecycler();
  }

  @Override
  public void clearRecycler(String pName)
  {
    mStackRecyclerManager.clear(pName);
  }

  @Override
  public void clearAllRecyclers()
  {
    mStackRecyclerManager.clearAll();
  }

  @Override
  public abstract Q requestQueue();

  @Override
  public FutureBooleanList playQueue(Q pQueue)
  {
    return lock(() ->
    {
      GarbageCollector.trigger();

      getPlayedQueueVariable().set(pQueue);

      final FutureBooleanList lFutureBooleanList = new FutureBooleanList();

      for (final Object lDevice : mDeviceLists.getAllDeviceList())
      {

        if (lDevice instanceof QueueDeviceInterface)
        {
          /*info("playQueue() on device: %s \n", lDevice);/**/
          @SuppressWarnings("unchecked") final QueueDeviceInterface<QueueInterface> lStateQueueDeviceInterface = (QueueDeviceInterface<QueueInterface>) lDevice;

          QueueInterface lDeviceQueue = pQueue.getDeviceQueue(lStateQueueDeviceInterface);

          final Future<Boolean> lPlayQueueFuture = lStateQueueDeviceInterface.playQueue(lDeviceQueue);

          if (lPlayQueueFuture != null) lFutureBooleanList.addFuture(lDevice.toString(), lPlayQueueFuture);
        }
      }

      return lFutureBooleanList;
    });
  }

  @Override
  public Boolean playQueueAndWait(Q pQueue, long pTimeOut, TimeUnit pTimeUnit) throws InterruptedException, ExecutionException, TimeoutException
  {
    return lock(() ->
    {
      final FutureBooleanList lPlayQueue = playQueue(pQueue);
      return lPlayQueue.get(pTimeOut, pTimeUnit);
    });
  }

  @Override
  public Boolean playQueueAndWaitForStacks(Q pQueue, long pTimeOut, TimeUnit pTimeUnit) throws InterruptedException, ExecutionException, TimeoutException
  {
    return lock(() ->
    {

      int lNumberOfStackCameras = getDeviceLists().getNumberOfDevices(StackCameraDeviceInterface.class);
      CountDownLatch[] lStacksReceivedLatches = new CountDownLatch[lNumberOfStackCameras];

      mAverageTimeInNS = 0;

      ArrayList<VariableSetListener<StackInterface>> lListenerList = new ArrayList<>();
      for (int i = 0; i < lNumberOfStackCameras; i++)
      {
        lStacksReceivedLatches[i] = new CountDownLatch(1);

        final int fi = i;

        VariableSetListener<StackInterface> lVariableSetListener = new VariableSetListener<StackInterface>()
        {

          @Override
          public void setEvent(StackInterface pCurrentValue, StackInterface pNewValue)
          {
            lStacksReceivedLatches[fi].countDown();
            if (pNewValue == null) return;
            StackMetaData lMetaData = pNewValue.getMetaData();
            if (lMetaData == null || lMetaData.getTimeStampInNanoseconds() == null) return;

            mAverageTimeInNS += lMetaData.getTimeStampInNanoseconds() / lNumberOfStackCameras;

          }
        };

        lListenerList.add(lVariableSetListener);

        getCameraStackVariable(i).addSetListener(lVariableSetListener);

      }

      info("Playing queue of length: " + pQueue.getQueueLength());
      final FutureBooleanList lPlayQueue = playQueue(pQueue);

      info("Waiting for: " + pTimeUnit.toSeconds(pTimeOut) + " seconds ");
      Boolean lBoolean = lPlayQueue.get(pTimeOut, pTimeUnit);

      if (lBoolean != null && lBoolean)
      {
        for (int i = 0; i < lNumberOfStackCameras; i++)
        {
          lStacksReceivedLatches[i].await(pTimeOut, pTimeUnit);
        }
      }

      for (VariableSetListener<StackInterface> lVariableSetListener : lListenerList)
        for (int i = 0; i < lNumberOfStackCameras; i++)
        {
          getCameraStackVariable(i).removeSetListener(lVariableSetListener);
        }

      return lBoolean;
    });
  }

  @Override
  public StageDeviceInterface getMainStage()
  {
    StageDeviceInterface lDevice = getDevice(StageDeviceInterface.class, 0);
    return lDevice;
  }

  @Override
  public void setStageX(double pXValue)
  {
    Variable<Double> lTargetPositionVariable = getMainStage().getTargetPositionVariable(getMainStage().getDOFIndexByName("X"));
    if (lTargetPositionVariable != null) lTargetPositionVariable.set(pXValue);
  }

  @Override
  public void setStageY(double pYValue)
  {
    Variable<Double> lTargetPositionVariable = getMainStage().getTargetPositionVariable(getMainStage().getDOFIndexByName("Y"));
    if (lTargetPositionVariable != null) lTargetPositionVariable.set(pYValue);
  }

  @Override
  public void setStageZ(double pZValue)
  {
    Variable<Double> lTargetPositionVariable = getMainStage().getTargetPositionVariable(getMainStage().getDOFIndexByName("Z"));
    if (lTargetPositionVariable != null) lTargetPositionVariable.set(pZValue);
  }

  @Override
  public void setStageR(double pZValue)
  {
    Variable<Double> lTargetPositionVariable = getMainStage().getTargetPositionVariable(getMainStage().getDOFIndexByName("R"));
    if (lTargetPositionVariable != null) lTargetPositionVariable.set(pZValue);
  }

  @Override
  public double getStageX()
  {
    Variable<Double> lTargetPositionVariable = getMainStage().getTargetPositionVariable(getMainStage().getDOFIndexByName("X"));
    if (lTargetPositionVariable != null) return lTargetPositionVariable.get();
    return 0;
  }

  @Override
  public double getStageY()
  {
    Variable<Double> lTargetPositionVariable = getMainStage().getTargetPositionVariable(getMainStage().getDOFIndexByName("Y"));
    if (lTargetPositionVariable != null) return lTargetPositionVariable.get();
    return 0;
  }

  @Override
  public double getStageZ()
  {
    Variable<Double> lTargetPositionVariable = getMainStage().getTargetPositionVariable(getMainStage().getDOFIndexByName("Z"));
    if (lTargetPositionVariable != null) return lTargetPositionVariable.get();
    return 0;
  }

  @Override
  public double getStageR()
  {
    Variable<Double> lTargetPositionVariable = getMainStage().getTargetPositionVariable(getMainStage().getDOFIndexByName("R"));
    if (lTargetPositionVariable != null) return lTargetPositionVariable.get();
    return 0;
  }

}
