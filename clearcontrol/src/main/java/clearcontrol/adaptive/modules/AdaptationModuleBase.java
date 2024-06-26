package clearcontrol.adaptive.modules;

import clearcontrol.adaptive.AdaptiveEngine;
import clearcontrol.core.device.name.NameableBase;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.state.AcquisitionStateInterface;

import java.util.ArrayList;
import java.util.concurrent.Future;

/**
 * Base class providing common fields and methods for all adaptation modules
 *
 * @param <S> state type
 * @author royer
 */
public abstract class AdaptationModuleBase<S extends AcquisitionStateInterface<?, ?>> extends NameableBase implements AdaptationModuleInterface<S>, LoggingFeature
{

  private AdaptiveEngine<S> mAdaptiveEngine;

  private int mPriority = 1;

  protected ArrayList<Future<?>> mListOfFuturTasks = new ArrayList<>();

  private final Variable<Boolean> mIsActiveVariable = new Variable<>("IsActive", true);

  private final Variable<String> mStatusStringVariable = new Variable<>("Status", "");

  /**
   * Instanciate an adaptation module given a name
   *
   * @param pName name
   */
  public AdaptationModuleBase(String pName)
  {
    super(pName);
  }

  @Override
  public void setAdaptator(AdaptiveEngine<S> pAdaptiveEngine)
  {
    mAdaptiveEngine = pAdaptiveEngine;
  }

  @Override
  public AdaptiveEngine<S> getAdaptiveEngine()
  {
    return mAdaptiveEngine;
  }

  @Override
  public void setPriority(int pPriority)
  {
    mPriority = pPriority;
  }

  @Override
  public int getPriority()
  {
    return mPriority;
  }

  @Override
  public abstract Boolean apply(Void pVoid);

  @Override
  public Variable<Boolean> getIsActiveVariable()
  {
    return mIsActiveVariable;
  }

  @Override
  public Variable<String> getStatusStringVariable()
  {
    return mStatusStringVariable;
  }

  @Override
  public boolean isReady()
  {
    boolean lAllDone = true;
    for (Future<?> lTask : mListOfFuturTasks)
      if (lTask != null)
      {
        boolean lDone = lTask.isDone();
        lAllDone &= lDone;
        // if (!lDone)
        // info("Task: %s not done yet", lTask);
      }

    return lAllDone;
  }

  @Override
  public void reset()
  {
    mListOfFuturTasks.clear();
  }

  @Override
  public String toString()
  {
    return String.format("AdaptationModuleBase [getName()=%s, getPriority()=%s, getIsActiveVariable()=%s, isReady()=%s]", getName(), getPriority(), getIsActiveVariable(), isReady());
  }

}
