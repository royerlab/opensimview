package fastfuse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import coremem.ContiguousMemoryInterface;
import fastfuse.tasks.TaskInterface;

import org.apache.commons.lang3.tuple.MutablePair;

/**
 * Interface implemented by all stack fusion classes
 *
 * @author royer
 */
public interface FastFusionEngineInterface
{

  /**
   * Resets all image slots - a new computation can be started.
   * 
   * @param pCloseImages
   *          closes images
   */
  void reset(boolean pCloseImages);

  /**
   * Adds a task to this stack fusion engine.
   * 
   * @param pTask
   *          task to add
   */
  void addTask(TaskInterface pTask);

  default void addTasks(List<TaskInterface> pTaskList)
  {
    for (TaskInterface pTask : pTaskList)
      addTask(pTask);
  }

  /**
   * Returns the tasks in this Fast Fusion engine
   * 
   * @return list of tasks
   */
  ArrayList<TaskInterface> getTasks();

  /**
   * Returns the image for the given key
   * 
   * @param pSlotKey
   *          slot key
   * @return image
   */
  ClearCLImage getImage(String pSlotKey);

  void removeImage(String pSlotKey);

  /**
   * An image is available for computation once its data has been successfully
   * transfered
   * 
   * @param pSlotKey
   *          slot key
   * @return true -> available for computation
   */
  boolean isImageAvailable(String pSlotKey);

  /**
   * Set of slot keys for available (ready and already processed) images
   * 
   * @return available images
   */
  Set<String> getAvailableImagesSlotKeys();

  /**
   * Ensures allocation for the image of given slot key. If the image is already
   * allocated and of the right dimensions, it is reused.
   * 
   * @param pSlotKey
   *          slot key
   * @param pImageChannelDataType
   *          image channel data type
   * @param pDimensions
   *          image dimensions
   * @return (available flag, image (already or newly) allocated)
   */
  MutablePair<Boolean, ClearCLImage> ensureImageAllocated(String pSlotKey,
                                                          ImageChannelDataType pImageChannelDataType,
                                                          long... pDimensions);

  /**
   * This method is used for identity tasks that do not change the image and
   * simply copy the image reference from one slot to another.
   * 
   * @param pSrcSlotKey
   *          source slot
   * @param pDstSlotKey
   *          destination slot
   */
  void assignImageToAnotherSlotKey(String pSrcSlotKey,
                                   String pDstSlotKey);

  /**
   * Passes image data located in non GPU memory for a given key
   * 
   * @param pSlotKey
   *          image key
   * @param pImageData
   *          image data
   * @param pImageChannelDataType
   *          image channel data type
   * @param pDimensions
   *          corresponding dimensions
   */
  void passImage(String pSlotKey,
                 ContiguousMemoryInterface pImageData,
                 final ImageChannelDataType pImageChannelDataType,
                 long... pDimensions);

  /**
   * Passes image data located in GPU memory for a given key
   * 
   * @param pSlotKey
   *          image key
   * @param pImage
   *          image
   */
  void passImage(String pSlotKey, ClearCLImage pImage);

  /**
   * Executes one task that is ready (= all required images are available). If
   * no task is ready, null is returned.
   * 
   * @return number of executed tasks (0 or 1)
   * 
   * @throws ExecutionException
   *           thrown when computation encounters a problem
   */
  int executeOneTask() throws ExecutionException;

  /**
   * Executes tasks that are ready (= all required images are available). If no
   * task is ready, null is returned.
   * 
   * @param pMaxNumberOfTasks
   *          max number of tasks to execute
   * @return future of this task computation
   */
  int executeSeveralTasks(int pMaxNumberOfTasks);

  /**
   * Executes all tasks that are ready (= all required images are available). If
   * no task is ready, null is returned.
   * 
   * @return future of this task computation
   */
  default int executeAllTasks()
  {
    int lNumberOfExecutedTasks =
                               executeSeveralTasks(Integer.MAX_VALUE);
    if (lNumberOfExecutedTasks == 0)
      return lNumberOfExecutedTasks;

    return lNumberOfExecutedTasks + executeAllTasks();
  }

}
