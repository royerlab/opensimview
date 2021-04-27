package fastfuse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import coremem.ContiguousMemoryInterface;
import fastfuse.pool.FastFusionMemoryPool;
import fastfuse.tasks.TaskInterface;

import org.apache.commons.lang3.tuple.MutablePair;

/**
 * Fast fusion engine.
 *
 * @author Loic Royer
 */
public class FastFusionEngine implements FastFusionEngineInterface
{
  private final ClearCLContext mContext;

  private final ConcurrentHashMap<String, MutablePair<Boolean, ClearCLImage>> mImageSlotsMap =
                                                                                             new ConcurrentHashMap<>();

  private final ArrayList<TaskInterface> mFusionTasks =
                                                      new ArrayList<>();

  private final HashSet<TaskInterface> mExecutedFusionTasks =
                                                            new HashSet<>();

  /**
   * Instantiates a StackFusion object given a CLearCL context
   * 
   * @param pContext
   *          ClearCL context
   */
  public FastFusionEngine(ClearCLContext pContext)
  {
    super();
    mContext = pContext;
  }

  /**
   * Instantiates a fast fusion engine given an existing engine - all tasks are
   * copied over
   * 
   * @param pFastFusionEngine
   *          fast fusion engine
   */
  public FastFusionEngine(FastFusionEngine pFastFusionEngine)
  {
    this(pFastFusionEngine.getContext());

    mFusionTasks.addAll(pFastFusionEngine.getTasks());
  }

  @Override
  public void reset(boolean pCloseImages)
  {
    mContext.getDefaultQueue().waitToFinish();
    FastFusionMemoryPool lMemoryPool = FastFusionMemoryPool.get();

    for (Entry<String, MutablePair<Boolean, ClearCLImage>> lEntry : mImageSlotsMap.entrySet())
    {
      ClearCLImage lImage = lEntry.getValue().getRight();
      if (lMemoryPool.isImageInUse(lImage))
      {
        lMemoryPool.releaseImage(lEntry.getKey(), lImage);
      }
      lEntry.getValue().setRight(null);
      lEntry.getValue().setLeft(false);
    }
    mExecutedFusionTasks.clear();
    if (pCloseImages)
      lMemoryPool.free();
  }

  @Override
  public void addTask(TaskInterface pTask)
  {
    addTask(pTask, false);
  }

  /**
   *
   * @param pTask
   *          task to add to the list
   * @param pPriority
   *          this flag allows adding an entry at the beginning of the list
   */
  public void addTask(TaskInterface pTask, boolean pPriority)
  {
    if (pPriority)
    {
      mFusionTasks.add(0, pTask);
    }
    else
    {
      mFusionTasks.add(pTask);
    }
  }

  @Override
  public ArrayList<TaskInterface> getTasks()
  {
    return mFusionTasks;
  }

  @Override
  public void passImage(String pSlotKey,
                        ContiguousMemoryInterface pImageData,
                        final ImageChannelDataType pImageChannelDataType,
                        long... pDimensions)
  {
    MutablePair<Boolean, ClearCLImage> lPair =
                                             ensureImageAllocated(pSlotKey,
                                                                  pImageChannelDataType,
                                                                  pDimensions);

    FastFusionMemoryPool.get()
                        .freeMemoryIfNecessaryAndRun(() -> lPair.getRight()
                                                                .readFrom(pImageData,
                                                                          true));
    lPair.setLeft(true);
  }

  @Override
  public void passImage(String pSlotKey, ClearCLImage pImage)
  {
    MutablePair<Boolean, ClearCLImage> lPair =
                                             ensureImageAllocated(pSlotKey,
                                                                  pImage.getChannelDataType(),
                                                                  pImage.getDimensions());

    FastFusionMemoryPool.get()
                        .freeMemoryIfNecessaryAndRun(() -> pImage.copyTo(lPair.getRight(),
                                                                         true));
    lPair.setLeft(true);
  }

  @Override
  public MutablePair<Boolean, ClearCLImage> ensureImageAllocated(final String pSlotKey,
                                                                 final ImageChannelDataType pImageChannelDataType,
                                                                 final long... pDimensions)
  {

    MutablePair<Boolean, ClearCLImage> lPair =
                                             getImageSlotsMap().get(pSlotKey);

    if (lPair == null)
    {
      lPair = MutablePair.of(false, (ClearCLImage) null);
      getImageSlotsMap().put(pSlotKey, lPair);
    }

    FastFusionMemoryPool lMemoryPool = FastFusionMemoryPool.get();
    ClearCLImage lImage = lPair.getRight();

    if (lImage == null)
    {
      lImage = lMemoryPool.requestImage(pSlotKey,
                                        pImageChannelDataType,
                                        pDimensions);
      lPair.setRight(lImage);
      lPair.setLeft(false);
    }

    assert lMemoryPool.isImageInUse(lImage);
    assert !lPair.getLeft();

    return lPair;
  }

  @Override
  public void assignImageToAnotherSlotKey(final String pSrcSlotKey,
                                          final String pDstSlotKey)
  {
    MutablePair<Boolean, ClearCLImage> lDstPair =
                                                getImageSlotsMap().get(pDstSlotKey);

    if (lDstPair == null)
    {
      lDstPair = MutablePair.of(false, (ClearCLImage) null);
      getImageSlotsMap().put(pDstSlotKey, lDstPair);
    }

    MutablePair<Boolean, ClearCLImage> lSrcPair =
                                                getImageSlotsMap().get(pSrcSlotKey);

    lDstPair.setRight(lSrcPair.getRight());
    lDstPair.setLeft(lSrcPair.getLeft());

  }

  @Override
  public ClearCLImage getImage(String pSlotKey)
  {
    return getImageSlotsMap().get(pSlotKey).getRight();
  }

  @Override
  public void removeImage(String pSlotKey)
  {
    MutablePair<Boolean, ClearCLImage> lMutablePair =
                                                    getImageSlotsMap().remove(pSlotKey);
    assert lMutablePair != null;
    if (lMutablePair != null)
    {
      FastFusionMemoryPool.get()
                          .releaseImage(pSlotKey,
                                        lMutablePair.getRight());
    }
  }

  @Override
  public boolean isImageAvailable(String pSlotKey)
  {
    MutablePair<Boolean, ClearCLImage> lMutablePair =
                                                    getImageSlotsMap().get(pSlotKey);
    if (lMutablePair == null)
      return false;
    return lMutablePair.getLeft();
  }

  @Override
  public Set<String> getAvailableImagesSlotKeys()
  {
    HashSet<String> lAvailableImagesKeys = new HashSet<String>();
    for (Entry<String, MutablePair<Boolean, ClearCLImage>> lEntry : mImageSlotsMap.entrySet())
    {
      if (lEntry.getValue().getKey())
      {
        lAvailableImagesKeys.add(lEntry.getKey());
      }
    }
    return lAvailableImagesKeys;
  }

  @Override
  public int executeOneTask()
  {
    return executeSeveralTasks(1);
  }

  @Override
  public int executeSeveralTasks(int pMaxNumberOfTasks)
  {
    return executeSeveralTasks(0, pMaxNumberOfTasks);
  }

  private int executeSeveralTasks(int pExecutedNumberOfTasks,
                                  int pMaxNumberOfTasks)
  {
    assert 0 <= pExecutedNumberOfTasks
           && pExecutedNumberOfTasks <= pMaxNumberOfTasks;
    if (pExecutedNumberOfTasks == pMaxNumberOfTasks)
      return pExecutedNumberOfTasks;
    Set<String> lAvailableImageKeys = getAvailableImagesSlotKeys();
    for (TaskInterface lTask : mFusionTasks)
    {
      if (!mExecutedFusionTasks.contains(lTask))
      {
        if (lTask.checkIfRequiredImagesAvailable(lAvailableImageKeys))
        {
          lTask.enqueue(this, true);
          mExecutedFusionTasks.add(lTask);
          return executeSeveralTasks(pExecutedNumberOfTasks + 1,
                                     pMaxNumberOfTasks);
        }
        else
        {
          // System.out.println("Cannot execute " + lTask + " because not the
          // right available images: " + lAvailableImageKeys );
        }
      }
    }
    return pExecutedNumberOfTasks;
  }

  /**
   * Waits for the currently
   */
  public void waitFusionTasksToComplete()
  {
    getContext().getDefaultQueue().waitToFinish();
  }

  /**
   * Returns ClearCL context
   * 
   * @return context
   */
  public ClearCLContext getContext()
  {
    return mContext;
  }

  private Map<String, MutablePair<Boolean, ClearCLImage>> getImageSlotsMap()
  {
    return mImageSlotsMap;
  }

}
