package clearcontrol.stack.sourcesink.sink;

import clearcontrol.core.concurrent.asyncprocs.AsynchronousProcessorBase;
import clearcontrol.core.variable.Variable;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.MetaDataView;
import coremem.ContiguousMemoryInterface;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Asynchronous stack sink adapter. This sink adapter can wrap another sink an
 * provides asynchronous decoupling via an elastic queue.
 *
 * @author royer
 */
public class AsynchronousFileStackSinkAdapter implements FileStackSinkInterface
{

  private final ConcurrentHashMap.KeySetView<String, Boolean> mStackKeySet;
  private FileStackSinkInterface mStackSink;

  private AsynchronousProcessorBase<Pair<String, StackInterface>, StackInterface> mAsynchronousConversionProcessor;

  private Variable<StackInterface> mFinishedProcessingStackVariable;
  private static final int cZeroLevel = 100;

  /**
   * Wraps an existing stack sink to provide asynchronous capability
   *
   * @param pStackSink    sink to wrap
   * @param pMaxQueueSize max queue size
   * @return wrapped sink
   */
  public static AsynchronousFileStackSinkAdapter wrap(FileStackSinkInterface pStackSink, final int pMaxQueueSize)
  {
    return new AsynchronousFileStackSinkAdapter(pStackSink, pMaxQueueSize);
  }

  /**
   * Instanciates an asynchronous stack sink adapter for a given existing sink
   * and max queue size.
   *
   * @param pStackSink    sink to wrap
   * @param pMaxQueueSize max queue size
   */
  public AsynchronousFileStackSinkAdapter(final FileStackSinkInterface pStackSink, final int pMaxQueueSize)
  {
    super();
    mStackSink = pStackSink;

    mStackKeySet = ConcurrentHashMap.newKeySet();

    mAsynchronousConversionProcessor = new AsynchronousProcessorBase<Pair<String, StackInterface>, StackInterface>("AsynchronousStackSinkAdapter", pMaxQueueSize)
    {
      @Override
      public StackInterface process(final Pair<String, StackInterface> pPair)
      {
        String lChannel = pPair.getLeft();
        long lTimePointns = pPair.getRight().getMetaData().getTimeStampInNanoseconds();
        String lCameraLightSheet = MetaDataView.getCxLyString(pPair.getRight().getMetaData());
        String lKey = lChannel + lCameraLightSheet + lTimePointns;

        // We make sure to filter duplicates
        if (!mStackKeySet.contains(lKey))
        {
          mStackKeySet.add(lKey);
          StackInterface lStack = pPair.getRight();
          removeZeroLevel(lStack.getContiguousMemory());
          mStackSink.appendStack(lChannel, lStack);
          lStack.release();
          if (mFinishedProcessingStackVariable != null)
          {
            mFinishedProcessingStackVariable.set(lStack);
          }
        }
        return null;
      }
    };
  }

  /**
   * Starts the thread that passes the stacks to the sink
   *
   * @return true -> success
   */
  public boolean start()
  {
    return mAsynchronousConversionProcessor.start();
  }

  /**
   * Stops the thread that passes the stacks to the sink
   *
   * @return true -> success
   */
  public boolean stop()
  {
    return mAsynchronousConversionProcessor.stop();
  }

  @Override
  public boolean appendStack(final StackInterface pStack)
  {
    return appendStack(cDefaultChannel, pStack);
  }

  @Override
  public boolean appendStack(String pChannel, final StackInterface pStack)
  {
    return mAsynchronousConversionProcessor.passOrWait(Pair.of(pChannel, pStack));
  }

  /**
   * Waits for this asynchronous sink adapter to pass all pending stacks to the
   * delegated sink.
   *
   * @param pTimeOut  time out
   * @param pTimeUnit time unit
   * @return true -> success (= no timeout)
   */
  public boolean waitToFinish(final long pTimeOut, TimeUnit pTimeUnit)
  {
    return mAsynchronousConversionProcessor.waitToFinish(pTimeOut, pTimeUnit);
  }

  /**
   * Returns queue length
   *
   * @return queue length
   */
  public int getQueueLength()
  {
    return mAsynchronousConversionProcessor.getInputQueueLength();
  }

  /**
   * Sets the variable that receives stacks once they have been successfully
   * passed to the sink.
   *
   * @param pVariable variable that received stacks
   */
  public void setFinishedProcessingStackVariable(final Variable<StackInterface> pVariable)
  {
    mFinishedProcessingStackVariable = pVariable;
  }

  @Override
  public void setLocation(File pRootFolder, String pDataSetName)
  {
    mStackSink.setLocation(pRootFolder, pDataSetName);
  }

  @Override
  public File getLocation()
  {
    return mStackSink.getLocation();
  }

  @Override
  public void close() throws Exception
  {
    waitToFinish(5, TimeUnit.MINUTES);
    stop();
    mStackSink.close();
  }

  private void removeZeroLevel(ContiguousMemoryInterface lContiguousMemory)
  {
    long lLengthInUINT16 = lContiguousMemory.getSizeInBytes() / 2;
    for (long i = 0; i < lLengthInUINT16; i++)
    {
      int value = (0xFFFF & lContiguousMemory.getCharAligned(i));
      char lValue = (char) (Math.max(cZeroLevel, value) - cZeroLevel);
      lContiguousMemory.setCharAligned(i, lValue);
    }
  }

}
