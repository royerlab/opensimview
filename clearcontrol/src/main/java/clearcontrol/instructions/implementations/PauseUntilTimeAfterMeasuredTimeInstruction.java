package clearcontrol.instructions.implementations;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.util.TimeFormat;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class PauseUntilTimeAfterMeasuredTimeInstruction extends PauseInstruction implements
                                                                                 InstructionInterface,
                                                                                 LoggingFeature
{
  private final Variable<String>
      mMeasuredTimeKeyVariable =
      new Variable<String>("Time measurement key", "_");

  public PauseUntilTimeAfterMeasuredTimeInstruction(String pMeasuredTimeKey,
                                                    int pPauseTimeInMilliseconds)
  {
    super("Timing: Pause "
          + TimeFormat.humanReadableTime(pPauseTimeInMilliseconds)
          + " after time t_"
          + pMeasuredTimeKey
          + " measurement");
    getPauseTimeInMilliseconds().set(pPauseTimeInMilliseconds);
    mMeasuredTimeKeyVariable.set(pMeasuredTimeKey);
  }

  @Override public boolean initialize()
  {
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    if (!MeasureTimeInstruction.sMeasuredTime.containsKey(mMeasuredTimeKeyVariable.get()))
    {
      warning("Time measurement t_"
              + mMeasuredTimeKeyVariable.get()
              + " does not exist!");
      return false;
    }
    Long
        measuredTime =
        MeasureTimeInstruction.sMeasuredTime.get(mMeasuredTimeKeyVariable.get());
    long
        timeToWait =
        mPauseTimeInMilliseconds.get() - (System.currentTimeMillis() - measuredTime);
    if (timeToWait > 0)
    {

      try
      {
        Thread.sleep(timeToWait);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
    }
    return true;
  }

  @Override public PauseUntilTimeAfterMeasuredTimeInstruction copy()
  {
    return new PauseUntilTimeAfterMeasuredTimeInstruction(mMeasuredTimeKeyVariable.get(),
                                                          mPauseTimeInMilliseconds.get());
  }

  public Variable<String> getMeasuredTimeKeyVariable()
  {
    return mMeasuredTimeKeyVariable;
  }
}
