package clearcontrol.warehouse.containers.io;

import clearcl.util.ElapsedTime;
import clearcontrol.core.concurrent.thread.ThreadSleep;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.timelapse.LightSheetTimelapse;
import clearcontrol.warehouse.DataWarehouse;
import clearcontrol.warehouse.containers.StackInterfaceContainer;
import clearcontrol.timelapse.TimelapseInterface;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.sourcesink.sink.FileStackSinkInterface;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * This generalised IO instruction writes all images in a StackInterfaceContainer of a
 * given Class to disc.
 *
 * @author haesleinhuepf April 2018, Loic Royer July 2020
 */
public abstract class WriteStackInterfaceContainerAsRawToDiscInstructionBase extends LightSheetMicroscopeInstructionBase implements LoggingFeature
{
  class SaveWorkItem
  {
    public FileStackSinkInterface mSinkInterface;
    public String mChannelName;
    public StackInterface mStack;

    public SaveWorkItem(FileStackSinkInterface mSinkInterface, String mChannelName, StackInterface mStack)
    {
      this.mSinkInterface = mSinkInterface;
      this.mChannelName = mChannelName;
      this.mStack = mStack.duplicate();
    }
  }

  private static ConcurrentLinkedQueue<SaveWorkItem> sSaveQueue = new ConcurrentLinkedQueue();

  private static Thread sSavingThread = null;


  protected Class mContainerClass;
  protected String[] mImageKeys = null;
  protected String mChannelName = null;

  protected Boolean mAsyncSaving = true;
  protected int mMaxQueueOccupancy = 16;


  /**
   * INstanciates a virtual device with a given name
   *
   * @param pDeviceName device name
   */
  public WriteStackInterfaceContainerAsRawToDiscInstructionBase(String pDeviceName, Class pContainerClass, String[] pImageKeys, String pChannelName, LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pDeviceName, pLightSheetMicroscope);
    mContainerClass = pContainerClass;
    mImageKeys = pImageKeys;
    if (pChannelName != null && pChannelName.length() > 0)
    {
      mChannelName = pChannelName;
    }

    if (sSavingThread == null)
    {
      sSavingThread = new Thread(() ->
      {
        while (true)
        {
          SaveWorkItem lSaveWorkItem = sSaveQueue.poll();
          if (lSaveWorkItem != null)
          {
            info("Saving one stack asynchronously now, " + sSaveQueue.size() + " more stacks in queue to save!");
            FileStackSinkInterface lSinkInterface = lSaveWorkItem.mSinkInterface;
            String lChannelName = lSaveWorkItem.mChannelName;
            StackInterface lStack = lSaveWorkItem.mStack;
            ElapsedTime.measureForceOutput("Saving stack for channel " + lChannelName, () -> lSinkInterface.appendStack(lChannelName, lStack));
          }

          ThreadSleep.sleep(100, TimeUnit.MILLISECONDS);

        }
      }, "Stack Saving Thread");
      sSavingThread.setDaemon(true);
      sSavingThread.setPriority(Thread.NORM_PRIORITY - 2);
      sSavingThread.start();
    }
  }

  @Override
  public boolean initialize()
  {
    return false;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    LightSheetTimelapse lTimelapse = (LightSheetTimelapse) getLightSheetMicroscope().getDevice(TimelapseInterface.class, 0);
    FileStackSinkInterface lFileStackSinkInterface = lTimelapse.getCurrentFileStackSinkVariable().get();

    DataWarehouse lDataWarehouse = ((LightSheetMicroscope) getLightSheetMicroscope()).getDataWarehouse();

    StackInterfaceContainer lContainer = lDataWarehouse.getOldestContainer(mContainerClass);
    if (lContainer == null)
    {
      warning("No " + mContainerClass.getCanonicalName() + " found for saving");
      return false;
    }

    for (String key : mImageKeys)
    {
      StackInterface lStack = lContainer.get(key);
      if (mChannelName != null)
      {
        saveStack(lFileStackSinkInterface, mChannelName, lStack);
      } else
      {
        saveStack(lFileStackSinkInterface, key, lStack);
      }
    }
    return true;
  }

  private void saveStack(FileStackSinkInterface lSinkInterface, String pChannelName, StackInterface lStack)
  {
    if (mAsyncSaving)
    {
      info("Saving stack asynchronously.");
      SaveWorkItem lSaveWorkItem = new SaveWorkItem(lSinkInterface, pChannelName, lStack);
      sSaveQueue.add(lSaveWorkItem);
      int lNumberOfStacksInQueue = sSaveQueue.size();
      if (lNumberOfStacksInQueue > mMaxQueueOccupancy)
      {
        info("Too many stacks in queue waiting to be saved, waiting... (queue size = " + lNumberOfStacksInQueue + ")");
        ThreadSleep.sleepWhile(100, TimeUnit.MILLISECONDS, () -> sSaveQueue.size() > mMaxQueueOccupancy);
      }
    } else
    {
      ElapsedTime.measureForceOutput(this + "Saving stack synchronously", () -> lSinkInterface.appendStack(pChannelName, lStack));
    }

  }
}
