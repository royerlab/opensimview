package clearcontrol.microscope.lightsheet.warehouse.containers;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.stack.StackInterface;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This DataContainer is used to store several image stack which belong together because
 * they were acquired together.
 *
 * @author haesleinhuepf April 2018
 */
public abstract class StackInterfaceContainer extends DataContainerBase implements
                                                                        DataContainerInterface,
                                                                        Map<String, StackInterface>,
                                                                        LoggingFeature
{
  HashMap<String, StackInterface> mData = new HashMap<>();

  public StackInterfaceContainer(long pTimePoint)
  {
    super(pTimePoint);
  }

  @Override public int size()
  {
    return mData.size();
  }

  @Override public boolean isEmpty()
  {
    return mData.isEmpty();
  }

  @Override public boolean containsKey(Object key)
  {
    return mData.containsKey(key);
  }

  @Override public boolean containsValue(Object value)
  {
    return mData.containsValue(value);
  }

  @Override public StackInterface get(Object key)
  {
    return mData.get(key);
  }

  @Override public StackInterface put(String key, StackInterface value)
  {
    return mData.put(key, value);
  }

  @Override public StackInterface remove(Object key)
  {
    return mData.remove(key);
  }

  @Override public void putAll(@NotNull Map<? extends String, ? extends StackInterface> m)
  {
    mData.putAll(m);
  }

  @Override public void clear()
  {
    mData.clear();
  }

  @NotNull @Override public Set<String> keySet()
  {
    return mData.keySet();
  }

  @NotNull @Override public Collection<StackInterface> values()
  {
    return mData.values();
  }

  @NotNull @Override public Set<Entry<String, StackInterface>> entrySet()
  {
    return mData.entrySet();
  }

  public void dispose()
  {
    warning("This container should be recycled, not disposed!");
    /*for (String key : keySet()) {
      get(key).free();
    }*/
    for (String key : keySet())
    {
      get(key).release();
    }
    clear();
  }

  public String toString()
  {
    return this.getClass().getSimpleName() + " [" + this.keySet() + "]";
  }

  public String getKeyContainingString(String pSearchString)
  {

    for (String key : keySet())
    {
      if (key.toLowerCase().contains(pSearchString))
      {
        return key;
      }
    }
    return null;
  }

}
