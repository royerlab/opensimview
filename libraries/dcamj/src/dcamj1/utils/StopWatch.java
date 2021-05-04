package dcamj1.utils;

import java.util.concurrent.TimeUnit;

public class StopWatch
{
  volatile long mStartingTime;

  public static StopWatch start()
  {
    return new StopWatch();
  }

  private StopWatch()
  {
    reset();
  }

  public StopWatch reset()
  {
    mStartingTime = System.nanoTime();
    return this;
  }

  public long timeInNanoseconds()
  {
    final long mEndingTime = System.nanoTime();
    return mEndingTime - mStartingTime;
  }

  public long time(final TimeUnit unit)
  {
    return unit.convert(timeInNanoseconds(), TimeUnit.NANOSECONDS);
  }

  public static long absoluteTimeInNanoseconds()
  {
    return System.nanoTime();
  }

}