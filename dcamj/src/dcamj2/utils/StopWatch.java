package dcamj2.utils;

import java.util.concurrent.TimeUnit;

/**
 * StopWatch
 *
 * @author royer
 */
public class StopWatch
{
  volatile long mStartingTime;

  /**
   * Start stopwatch
   * 
   * @return this stopwatch
   */
  public static StopWatch start()
  {
    return new StopWatch();
  }

  private StopWatch()
  {
    reset();
  }

  /**
   * Resets stopwatch
   * 
   * @return this stopwatch
   */
  public StopWatch reset()
  {
    mStartingTime = System.nanoTime();
    return this;
  }

  /**
   * Elapsed time in nanoseconds
   * 
   * @return nanoseconds
   */
  public long timeInNanoseconds()
  {
    final long mEndingTime = System.nanoTime();
    return mEndingTime - mStartingTime;
  }

  /**
   * Returns elpased time in the given unit
   * 
   * @param unit
   *          time unit
   * @return time in given unit
   */
  public long time(final TimeUnit unit)
  {
    return unit.convert(timeInNanoseconds(), TimeUnit.NANOSECONDS);
  }

  /**
   * Returns the absolute time in nanoseconds
   * 
   * @return absolute time in nanoseconds
   */
  public static long absoluteTimeInNanoseconds()
  {
    return System.nanoTime();
  }

}