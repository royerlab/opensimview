package clearcontrol.microscope.lightsheet.warehouse;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerInterface;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The DataWarehouse represents central data storage. It allows collecting a number of
 * DataContainers containing image data grouped per timepoint. It has its own recycler to
 * ensure memory stays under a certain limit.
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class DataWarehouse extends HashMap<String, DataContainerInterface> implements LoggingFeature
{

  @Override
  public DataContainerInterface put(String key, DataContainerInterface value)
  {
    if (containsKey(key))
    {
      warning(key + " already exists!");
    }
    super.put(key, value);
    return value;
  }

  public <DCI extends DataContainerInterface> DCI getOldestContainer(Class pClass)
  {
    long lMinimumTimePoint = Long.MAX_VALUE;
    DCI lOldestContainer = null;
    for (String key : keySet())
    {
      DataContainerInterface lContainer = get(key);
      if (pClass.isInstance(lContainer) && lContainer.getTimepoint() < lMinimumTimePoint)
      {
        lMinimumTimePoint = lContainer.getTimepoint();
        lOldestContainer = (DCI) lContainer;
      }
    }

    if (lOldestContainer != null)
    {
      info("Oldest container is from timepoint " + lOldestContainer.getTimepoint());
    } else
    {
      warning("Warning, no container to return!");
    }
    return lOldestContainer;
  }

  public <DCI extends DataContainerInterface> DCI getNewestContainer(Class pClass)
  {
    long lMaximumTimePoint = -Long.MAX_VALUE;
    DCI lNewestContainer = null;
    for (String key : keySet())
    {
      DataContainerInterface lContainer = get(key);
      if (pClass.isInstance(lContainer) && lContainer.getTimepoint() > lMaximumTimePoint)
      {
        lMaximumTimePoint = lContainer.getTimepoint();
        lNewestContainer = (DCI) lContainer;
      }
    }

    if (lNewestContainer != null)
    {
      info("Newest container is from timepoint " + lNewestContainer.getTimepoint());
    } else
    {
      warning("Warning, no container to return!");
    }
    return lNewestContainer;
  }

  public <DCI extends DataContainerInterface> ArrayList<DCI> getContainers(Class pClass)
  {
    return getContainers(pClass, true);
  }

  public <DCI extends DataContainerInterface> ArrayList<DCI> getContainers(Class pClass, boolean pSortedByTimePointAscending)
  {
    ArrayList<DCI> lContainerList = new ArrayList<DCI>();
    for (String key : keySet())
    {
      DataContainerInterface lContainer = get(key);
      if (pClass.isInstance(lContainer))
      {
        lContainerList.add((DCI) lContainer);
      }
    }

    if (pSortedByTimePointAscending)
    {
      lContainerList.sort((a, b) ->
      {
        if (a.getTimepoint() > b.getTimepoint())
        {
          return 1;
        }
        if (a.getTimepoint() < b.getTimepoint())
        {
          return -1;
        }
        return 0;
      });
    }

    return lContainerList;
  }

  public void disposeContainer(DataContainerInterface pContainer)
  {
    if (pContainer == null)
    {
      return;
    }

    pContainer.dispose();

    for (String key : keySet())
    {
      DataContainerInterface lContainer = get(key);
      if (lContainer == pContainer)
      {
        info("Disposing container: " + key);
        remove(key);
        return;
      }
    }
  }

  @Override
  public void clear()
  {
    for (DataContainerInterface lContainer : values())
    {
      lContainer.dispose();
    }
    super.clear();
  }

}
