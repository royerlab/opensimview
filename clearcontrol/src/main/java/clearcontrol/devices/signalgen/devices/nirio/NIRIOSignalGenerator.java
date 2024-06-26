package clearcontrol.devices.signalgen.devices.nirio;

import clearcontrol.core.concurrent.timing.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.devices.signalgen.SignalGeneratorBase;
import clearcontrol.devices.signalgen.SignalGeneratorInterface;
import clearcontrol.devices.signalgen.SignalGeneratorQueue;
import clearcontrol.devices.signalgen.devices.nirio.compiler.NIRIOCompiledScore;
import clearcontrol.devices.signalgen.devices.nirio.compiler.NIRIOScoreCompiler;
import clearcontrol.devices.signalgen.measure.MeasureInterface;
import clearcontrol.devices.signalgen.score.ScoreInterface;
import nirioj.direttore.Direttore;

import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.toIntExact;

/**
 * @author royer
 */
public class NIRIOSignalGenerator extends SignalGeneratorBase implements SignalGeneratorInterface, LoggingFeature

{

  double mWaitTimeInMilliseconds = 0;
  private final Direttore mDirettore;
  private final NIRIOCompiledScore mNIRIOCompiledScore = new NIRIOCompiledScore();

  /**
   *
   */
  public NIRIOSignalGenerator()
  {
    super("NIRIOSignalGenerator");
    mDirettore = new Direttore();

  }

  @Override
  public double getTemporalGranularityInMicroseconds()
  {
    return mDirettore.getTemporalGranularityInMicroseconds();
  }

  @Override
  public Future<Boolean> playQueue(SignalGeneratorQueue pSignalGeneratorQueue)
  {
    return super.playQueue(pSignalGeneratorQueue);
  }

  @Override
  public boolean playScore(ScoreInterface pScore)
  {
    final Thread lCurrentThread = Thread.currentThread();
    final int lCurrentThreadPriority = lCurrentThread.getPriority();
    lCurrentThread.setPriority(Thread.MAX_PRIORITY);
    mTriggerVariable.set(true);

    prependTransitionMeasure(pScore, getTransitionDurationInNanosecondsVariable().get(), TimeUnit.NANOSECONDS);

    info("Start compiling score...");
    double compilation_duration = ElapsedTime.measure("score compilation", () ->
    {
      NIRIOScoreCompiler.compile(mNIRIOCompiledScore, pScore);
    });
    info("Finished compiling score in " + compilation_duration + " ms");

    Boolean lPlayed = mDirettore.play(mNIRIOCompiledScore.getDeltaTimeBuffer().getContiguousMemory().getBridJPointer(Integer.class), mNIRIOCompiledScore.getNumberOfTimePointsBuffer().getContiguousMemory().getBridJPointer(Integer.class), mNIRIOCompiledScore.getSyncBuffer().getContiguousMemory().getBridJPointer(Integer.class), toIntExact(mNIRIOCompiledScore.getNumberOfMeasures()), mNIRIOCompiledScore.getScoreBuffer().getContiguousMemory().getBridJPointer(Short.class));

    lCurrentThread.setPriority(lCurrentThreadPriority);
    mTriggerVariable.set(false);

    return lPlayed && super.playScore(pScore);
  }

  @Override
  public boolean open()
  {
    try
    {
      if (!mDirettore.open())
      {
        return false;
      }

      return mDirettore.start();
    } catch (final Throwable e)
    {
      e.printStackTrace();
      return false;
    }

  }

  public boolean resume()
  {
    System.out.println(this.getClass().getSimpleName() + ": resume()");
    return true;
  }

  @Override
  public boolean close()
  {
    try
    {
      System.out.println(this.getClass().getSimpleName() + ": close()");
      mDirettore.stop();
      mDirettore.close();
      return true;
    } catch (final Throwable e)
    {
      e.printStackTrace();
      return false;
    }

  }

}
