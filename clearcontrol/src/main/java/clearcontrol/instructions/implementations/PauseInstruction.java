package clearcontrol.instructions.implementations;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.util.TimeFormat;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) February
 * 2018
 */
public class PauseInstruction extends InstructionBase implements InstructionInterface
{
  BoundedVariable<Integer>
      mPauseTimeInMilliseconds =
      new BoundedVariable<>("Pause in ms", 0, 0, Integer.MAX_VALUE);

  /**
   * INstanciates a virtual device with a given name
   */
  public PauseInstruction()
  {
    this(0);
  }

  public PauseInstruction(int pPauseTimeInMilliseconds)
  {
    super("Timing: Pause " + TimeFormat.humanReadableTime(pPauseTimeInMilliseconds));
    mPauseTimeInMilliseconds.set(pPauseTimeInMilliseconds);
  }

  PauseInstruction(String pName)
  {
    super(pName);
  }

  @Override public boolean initialize()
  {
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    try
    {
      Thread.sleep(mPauseTimeInMilliseconds.get());
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    return true;
  }

  @Override public PauseInstruction copy()
  {
    return new PauseInstruction(mPauseTimeInMilliseconds.get());
  }

  @Override public String toString()
  {
    return "Timing: Pause " + TimeFormat.humanReadableTime(mPauseTimeInMilliseconds.get());
  }

  @Override public String getName()
  {
    return toString();
  }

  public BoundedVariable<Integer> getPauseTimeInMilliseconds()
  {
    return mPauseTimeInMilliseconds;
  }
}
