package clearcontrol.timelapse;

import clearcontrol.MicroscopeInterface;
import clearcontrol.adaptive.AdaptiveEngine;
import clearcontrol.core.concurrent.timing.ElapsedTime;
import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.device.task.LoopTaskDevice;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.cameras.StackCameraDeviceInterface;
import clearcontrol.gui.jfx.var.combo.enums.TimeUnitEnum;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.processor.LightSheetFastFusionEngine;
import clearcontrol.processor.LightSheetFastFusionProcessor;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.sourcesink.StackSinkSourceInterface;
import clearcontrol.stack.sourcesink.sink.AsynchronousFileStackSinkAdapter;
import clearcontrol.stack.sourcesink.sink.CompressedStackSink;
import clearcontrol.stack.sourcesink.sink.FileStackSinkInterface;
import clearcontrol.state.AcquisitionStateInterface;
import clearcontrol.timelapse.io.ProgramWriter;
import clearcontrol.timelapse.timer.TimelapseTimerInterface;
import clearcontrol.timelapse.timer.fixed.FixedIntervalTimelapseTimer;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

/**
 * Timelapse implementations ? extends FileStackSinkInterface
 *
 * @author royer
 */
public class Timelapse extends LoopTaskDevice implements TimelapseInterface
{
  private static final int cMinimumNumberOfAvailableStacks = 16;
  private static final int cMaximumNumberOfAvailableStacks = 16;
  private static final int cMaximumNumberOfLiveStacks = 16;

  private static final DateTimeFormatter sDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SS");

  private final MicroscopeInterface<?> mMicroscope;

  private AdaptiveEngine<? extends AcquisitionStateInterface<?, ?>> mAdaptiveEngine;

  private final Variable<TimelapseTimerInterface> mTimelapseTimerVariable = new Variable<>("TimelapseTimer", null);

  private final Variable<Boolean> mEnforceMaxNumberOfTimePointsVariable = new Variable<>("LimitNumberOfTimePoints", true);

  private final Variable<Boolean> mEnforceMaxDurationVariable = new Variable<>("LimitTimelapseDuration", false);

  private final Variable<Boolean> mEnforceMaxDateTimeVariable = new Variable<>("LimitTimelapseDateTime", false);

  private final Variable<Long> mMaxNumberOfTimePointsVariable = new Variable<Long>("MaxNumberOfTimePoints", 1000L);

  private final Variable<Long> mMaxDurationVariable = new Variable<Long>("MaxDuration", 24L);

  private final Variable<TimeUnitEnum> mMaxDurationUnitVariable = new Variable<TimeUnitEnum>("MaxDurationUnit", TimeUnitEnum.Hours);

  private final Variable<LocalDateTime> mMaxDateTimeVariable = new Variable<LocalDateTime>("MaxDateTime", LocalDateTime.now());

  private final Variable<LocalDateTime> mStartDateTimeVariable = new Variable<LocalDateTime>("StartDateTime", LocalDateTime.now());

  private final Variable<Long> mTimePointCounterVariable = new Variable<Long>("TimePointCounter", 0L);

  private final ArrayList<Class<? extends FileStackSinkInterface>> mFileStackSinkTypesList = new ArrayList<>();

  private final Variable<Class<? extends FileStackSinkInterface>> mCurrentFileStackSinkTypeVariable = new Variable<>("CurrentFileStackSinkTypeVariable", CompressedStackSink.class);

  private final Variable<AsynchronousFileStackSinkAdapter> mCurrentFileStackSinkVariable = new Variable<>("CurrentFileStackSink", null);

  private final Variable<File> mRootFolderVariable = new Variable<>("RootFolder", null);

  private final Variable<String> mDataSetNamePostfixVariable = new Variable<>("DataSetNamePrefix", null);

  private final Variable<Boolean> mSaveStacksVariable = new Variable<Boolean>("SaveStacks", true);

  private final Variable<Boolean> mAdaptiveEngineOnVariable = new Variable<Boolean>("AdaptiveEngineOnVariable", true);

  private final BoundedVariable<Integer> mMinAdaptiveEngineStepsVariable = new BoundedVariable<Integer>("MinAdaptiveEngineSteps", 1, 1, Integer.MAX_VALUE, 1);

  private final BoundedVariable<Integer> mMaxAdaptiveEngineStepsVariable = new BoundedVariable<Integer>("MaxAdaptiveEngineSteps", 2, 1, Integer.MAX_VALUE, 1);

  private final Variable<StackInterface> mStackToSaveVariable = new Variable<>("StackToSaveVariable", null);

  private final VariableSetListener<StackInterface> mStackListener;

  protected ArrayList<InstructionInterface> mCurrentProgram = new ArrayList<InstructionInterface>();

  private Variable<Integer> mInstructionIndexVariable = new Variable<Integer>("instructions index", 0);

  ArrayList<InstructionInterface> mInitializedInstructionsList;


  /**
   * Instantiates a timelapse with a given timelapse timer
   *
   * @param pMicroscope     microscope
   * @param pTimelapseTimer timelapse timer
   */
  public Timelapse(MicroscopeInterface<?> pMicroscope, TimelapseTimerInterface pTimelapseTimer)
  {
    super("Timelapse");
    mMicroscope = pMicroscope;

    getMaxNumberOfTimePointsVariable().set(999999L);

    getTimelapseTimerVariable().set(pTimelapseTimer);

    MachineConfiguration lMachineConfiguration = MachineConfiguration.get();
    File lDefaultRootFolder = lMachineConfiguration.getFileProperty("timelapse.rootfolder", new File(System.getProperty("user.home") + "/Desktop"));
    getRootFolderVariable().set(lDefaultRootFolder);

    getDataSetNamePostfixVariable().addSetListener((o, n) -> info("New dataset name: %s \n", n));

    mStackListener = (o, n) ->
    {
      Variable<AsynchronousFileStackSinkAdapter> lStackSinkVariable = getCurrentFileStackSinkVariable();
      if (lStackSinkVariable != null && n != null)
      {
        if (getSaveStacksVariable().get())
        {
          info("Received new stack %s and appending it to the file sink %s", n, lStackSinkVariable);

          String lChannelInMetaData = n.getMetaData().getValue(MetaDataChannel.Channel);

          final String lChannel = lChannelInMetaData != null ? lChannelInMetaData : StackSinkSourceInterface.cDefaultChannel;

          lStackSinkVariable.get().appendStack(lChannel, n);
        }
        else
        {
          getMicroscope().getCleanupStackVariable().setAsync(n);
        }
      }
    };

    mStackToSaveVariable.addSetListener(mStackListener);

    getMaxAdaptiveEngineStepsVariable().addSetListener((o, n) ->
    {
      int lMin = getMinAdaptiveEngineStepsVariable().get().intValue();
      if (n.intValue() != o.intValue() && n < lMin) getMaxAdaptiveEngineStepsVariable().setAsync(lMin);
    });

    getMinAdaptiveEngineStepsVariable().addSetListener((o, n) ->
    {
      int lMax = getMaxAdaptiveEngineStepsVariable().get().intValue();
      if (n.intValue() != o.intValue() && n > lMax) getMaxAdaptiveEngineStepsVariable().setAsync(n);
    });
  }

  /**
   * Instantiates a timelapse with a fixed interval timer
   *
   * @param pMicroscope microscope
   */
  public Timelapse(MicroscopeInterface<?> pMicroscope)
  {
    this(pMicroscope, new FixedIntervalTimelapseTimer());
  }


  /**
   * Returns the parent microscope
   *
   * @return parent microscope
   */
  public MicroscopeInterface<?> getMicroscope()
  {
    return mMicroscope;
  }

  /**
   * Returns the stack sink type list
   *
   * @return stack sink type list
   */
  @Override
  public ArrayList<Class<? extends FileStackSinkInterface>> getFileStackSinkTypeList()
  {
    return mFileStackSinkTypesList;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void addFileStackSinkType(Class<?> pFileStackSinkType)
  {
    /*if (!pFileStackSinkType.toGenericString()
                           .contains(FileStackSinkInterface.class.getSimpleName()))
    {
      severe("Cannot add files stack sink type: %s must be of type Class<%s>. ",
             pFileStackSinkType.toGenericString(),
             FileStackSinkInterface.class.getSimpleName());
      return;
    }/**/

    mFileStackSinkTypesList.add((Class<? extends FileStackSinkInterface>) pFileStackSinkType);
  }

  @Override
  public void startTimelapse()
  {
    if (mMicroscope.getCurrentTask().get() != null)
    {
      warning("Another task (%s) is already running, please stop it first.", mMicroscope.getCurrentTask());
      return;
    }

    getStartSignalVariable().setEdgeAsync(false, true);


    File lProgramFile = new File(getWorkingDirectory(), "program.txt");
    ProgramWriter writer = new ProgramWriter(mCurrentProgram, lProgramFile);
    writer.write();

    mInitializedInstructionsList = new ArrayList<InstructionInterface>();

    LightSheetFastFusionProcessor lLightSheetFastFusionProcessor = mMicroscope.getDevice(LightSheetFastFusionProcessor.class, 0);
    LightSheetFastFusionEngine lLightSheetFastFusionEngine = lLightSheetFastFusionProcessor.getEngine();
    if (lLightSheetFastFusionEngine != null)
    {
      lLightSheetFastFusionEngine.reset(true);
    }
    mInstructionIndexVariable.set(0);
  }


  @Override
  public void stopTimelapse()
  {
    getStopSignalVariable().setEdgeAsync(false, true);
  }

  @Override
  public void run()
  {

    if (!getIsRunningVariable().get())
    {
      getTimePointCounterVariable().set(0L);
      getStartDateTimeVariable().set(LocalDateTime.now());
    }

    try
    {
      getTimePointCounterVariable().set(0L);
      getTimelapseTimerVariable().get().reset();

      if (getSaveStacksVariable().get())
      {
        if (mRootFolderVariable.get() == null)
        {
          severe("Root folder not defined.");
          return;
        }
        setupFileSink();
      }


      // Connecting stack cameras to stack sink:
      for (int c = 0; c < mMicroscope.getNumberOfDevices(StackCameraDeviceInterface.class); c++)
      {
        Variable<StackInterface> lStackVariable = mMicroscope.getTerminatorStackVariable(c);
        lStackVariable.sendUpdatesToInstead(mStackToSaveVariable);
      }

      initAdaptiveEngine();

      super.run();

      // Disconnecting stack cameras to stack sink:
      for (int c = 0; c < mMicroscope.getNumberOfDevices(StackCameraDeviceInterface.class); c++)
      {
        Variable<StackInterface> lStackVariable = mMicroscope.getTerminatorStackVariable(c);
        lStackVariable.doNotSendUpdatesTo(mStackToSaveVariable);
      }

    } catch (InstantiationException e)
    {
      severe("Cannot instanciate class %s (%s)", mCurrentFileStackSinkTypeVariable.get(), e.getMessage());
      return;
    } catch (IllegalAccessException e)
    {
      severe("Cannot access class %s (%s)", mCurrentFileStackSinkTypeVariable.get(), e.getMessage());
      return;
    } finally
    {
      mMicroscope.getCurrentTask().set(null);
    }

  }

  @Override
  public boolean startTask()
  {
    mMicroscope.getCurrentTask().set(this);
    return super.startTask();
  }

  @Override
  public void stopTask()
  {
    super.stopTask();
  }

  @Override
  public boolean loop()
  {
    if (getTimelapseTimerVariable() == null) return false;

    if (programStep())
    {

      if (getEnforceMaxNumberOfTimePointsVariable().get())
        if (getTimePointCounterVariable().get() >= getMaxNumberOfTimePointsVariable().get()) return false;

      if (getEnforceMaxDurationVariable().get() && getMaxDurationVariable().get() != null)
        if (checkMaxDuration()) return false;

      if (getEnforceMaxDateTimeVariable().get() && getMaxDateTimeVariable().get() != null)
        if (checkMaxDateTime()) return false;

      runAdaptiveEngine(getTimelapseTimerVariable().get());

    }

    return true;
  }

  public void waitForNextTimePoint()
  {
    TimelapseTimerInterface lTimelapseTimer = getTimelapseTimerVariable().get();
    lTimelapseTimer.waitToAcquire(1, TimeUnit.DAYS);
    lTimelapseTimer.notifyAcquisition();
  }

  protected void setupFileSink() throws InstantiationException, IllegalAccessException
  {
    if (getCurrentFileStackSinkTypeVariable().get() == null)
    {
      warning("No stack sink type defined!");

      if (getFileStackSinkTypeList().isEmpty())
      {
        severe("No stack sink types available! aborting timelapse acquisition!");
        return;
      }
      Class<? extends FileStackSinkInterface> lDefaultStackSink = getFileStackSinkTypeList().get(0);
      warning("Using the first stack sink available: %s !", lDefaultStackSink);

      getCurrentFileStackSinkTypeVariable().set(lDefaultStackSink);
    }

    FileStackSinkInterface lStackSink = getCurrentFileStackSinkTypeVariable().get().newInstance();

    AsynchronousFileStackSinkAdapter lAsyncStackSink = AsynchronousFileStackSinkAdapter.wrap(lStackSink, 64);
    lAsyncStackSink.start();

    if (getDataSetNamePostfixVariable().get() == null) getDataSetNamePostfixVariable().set("");

    String lNowDateTimeString = sDateTimeFormatter.format(LocalDateTime.now());

    lAsyncStackSink.setLocation(mRootFolderVariable.get(), lNowDateTimeString + "-" + getDataSetNamePostfixVariable().get());

    getCurrentFileStackSinkVariable().set(lAsyncStackSink);
  }

  @SuppressWarnings({"unchecked"})
  private void initAdaptiveEngine()
  {
    if (!mAdaptiveEngineOnVariable.get()) return;

    mAdaptiveEngine = mMicroscope.getDevice(AdaptiveEngine.class, 0);
    mAdaptiveEngine.getAcquisitionStateCounterVariable().set(0L);
    mAdaptiveEngine.reset();
  }

  private void runAdaptiveEngine(TimelapseTimerInterface pTimelapseTimer)
  {
    if (!mAdaptiveEngineOnVariable.get()) return;

    if (mAdaptiveEngine == null) initAdaptiveEngine();

    int lMinSteps = getMinAdaptiveEngineStepsVariable().get().intValue();
    int lMaxSteps = getMaxAdaptiveEngineStepsVariable().get().intValue();

    if (lMaxSteps < lMinSteps) lMaxSteps = lMinSteps;

    Boolean lMoreStepsNeeded = true;

    for (int i = 0; i < lMinSteps && lMoreStepsNeeded; i++)
    {
      lMoreStepsNeeded = mAdaptiveEngine.step();
    }

    for (int i = 0; i < (lMaxSteps - lMinSteps) && lMoreStepsNeeded; i++)
    {

      long lNextStepInMilliseconds = (long) (mAdaptiveEngine.estimateNextStepInSeconds() * 1000);

      if (pTimelapseTimer.enoughTimeFor(lNextStepInMilliseconds, lNextStepInMilliseconds / 10, TimeUnit.MILLISECONDS))
        lMoreStepsNeeded = mAdaptiveEngine.step();

    }

    if (!lMoreStepsNeeded) mAdaptiveEngine.reset();

  }

  private boolean checkMaxDuration()
  {
    LocalDateTime lStartDateTime = getStartDateTimeVariable().get();

    Duration lDuration = Duration.between(lStartDateTime, LocalDateTime.now());

    long lCurrentlMeasuredDurationInNanos = lDuration.toNanos();

    long lMaxDurationInNanos = TimeUnit.NANOSECONDS.convert(getMaxDurationVariable().get(), getMaxDurationUnitVariable().get().getTimeUnit());

    long lTimeLeft = lMaxDurationInNanos - lCurrentlMeasuredDurationInNanos;

    boolean lTimeIsOut = lTimeLeft < 0;

    return lTimeIsOut;
  }

  private boolean checkMaxDateTime()
  {
    LocalDateTime lMaxDateTime = getMaxDateTimeVariable().get();
    LocalDateTime lNowDateTime = LocalDateTime.now();

    return lNowDateTime.isAfter(lMaxDateTime);
  }

  @Override
  public boolean programStep()
  {

    try
    {
      info("Executing instruction: " + getInstructionIndexVariable().get());

      mMicroscope.useRecycler("3DTimelapse", cMinimumNumberOfAvailableStacks, cMaximumNumberOfAvailableStacks, cMaximumNumberOfLiveStacks);

      InstructionInterface lNextInstructionToRun = mCurrentProgram.get(mInstructionIndexVariable.get());

      // We stop if the program is empty:
      if (lNextInstructionToRun == null) return false;

      // if the instruction wasn't initialized yet, initialize it now!
      if (!mInitializedInstructionsList.contains(lNextInstructionToRun))
      {
        lNextInstructionToRun.initialize();
        mInitializedInstructionsList.add(lNextInstructionToRun);
      }

      info("Starting " + lNextInstructionToRun);
      double duration = ElapsedTime.measure("instructions execution", () ->
      {
        lNextInstructionToRun.execute(getTimePointCounterVariable().get());
      });
      info("Finished " + lNextInstructionToRun + " in " + duration + " ms");

      // Determine the next instruction
      mInstructionIndexVariable.set(mInstructionIndexVariable.get() + 1);
      if (mInstructionIndexVariable.get() > mCurrentProgram.size() - 1)
      {
        // At this point the program loop has finished... we will restart a loop.
        mInstructionIndexVariable.set(0);
        info("Finished time point:" + getTimePointCounterVariable());
        getTimePointCounterVariable().increment();
        if (getStopSignalVariable().get())
        {
          return false;
        } else
        {
          waitForNextTimePoint();
          info("Starting time point:" + getTimePointCounterVariable());
          return true;
        }


      }

    } catch (Throwable e)
    {
      e.printStackTrace();
    }

    return false;
  }

  @Override
  public Variable<Integer> getInstructionIndexVariable()
  {
    return mInstructionIndexVariable;
  }

  @Override
  public ArrayList<InstructionInterface> getCurrentProgram()
  {
    return mCurrentProgram;
  }

  @Override
  public Variable<Boolean> getAdaptiveEngineOnVariable()
  {
    return mAdaptiveEngineOnVariable;
  }

  @Override
  public Variable<TimelapseTimerInterface> getTimelapseTimerVariable()
  {
    return mTimelapseTimerVariable;
  }

  @Override
  public Variable<Boolean> getEnforceMaxNumberOfTimePointsVariable()
  {
    return mEnforceMaxNumberOfTimePointsVariable;
  }

  @Override
  public Variable<Boolean> getEnforceMaxDurationVariable()
  {
    return mEnforceMaxDurationVariable;
  }

  @Override
  public Variable<Boolean> getEnforceMaxDateTimeVariable()
  {
    return mEnforceMaxDateTimeVariable;
  }

  @Override
  public Variable<Long> getMaxNumberOfTimePointsVariable()
  {
    return mMaxNumberOfTimePointsVariable;
  }

  @Override
  public Variable<Long> getMaxDurationVariable()
  {
    return mMaxDurationVariable;
  }

  @Override
  public Variable<TimeUnitEnum> getMaxDurationUnitVariable()
  {
    return mMaxDurationUnitVariable;
  }

  @Override
  public Variable<LocalDateTime> getMaxDateTimeVariable()
  {
    return mMaxDateTimeVariable;
  }

  @Override
  public Variable<LocalDateTime> getStartDateTimeVariable()
  {
    return mStartDateTimeVariable;
  }

  @Override
  public Variable<Long> getTimePointCounterVariable()
  {
    return mTimePointCounterVariable;
  }

  @Override
  public Variable<Class<? extends FileStackSinkInterface>> getCurrentFileStackSinkTypeVariable()
  {
    return mCurrentFileStackSinkTypeVariable;
  }

  @Override
  public Variable<AsynchronousFileStackSinkAdapter> getCurrentFileStackSinkVariable()
  {
    return mCurrentFileStackSinkVariable;
  }

  @Override
  public Variable<File> getRootFolderVariable()
  {
    return mRootFolderVariable;
  }

  @Override
  public Variable<String> getDataSetNamePostfixVariable()
  {
    return mDataSetNamePostfixVariable;
  }

  @Override
  public Variable<Boolean> getSaveStacksVariable()
  {
    return mSaveStacksVariable;
  }

  @Override
  public BoundedVariable<Integer> getMinAdaptiveEngineStepsVariable()
  {
    return mMinAdaptiveEngineStepsVariable;
  }

  @Override
  public BoundedVariable<Integer> getMaxAdaptiveEngineStepsVariable()
  {
    return mMaxAdaptiveEngineStepsVariable;
  }

  @Override
  public File getWorkingDirectory()
  {
    if (mCurrentFileStackSinkVariable.get() == null)
    {
      return null;
    }
    return mCurrentFileStackSinkVariable.get().getLocation();
  }

  public ArrayList<InstructionInterface> getListOfAvailableInstructions(String... pMustContainStrings)
  {
    ArrayList<InstructionInterface> lListOfAvailabeSchedulers = new ArrayList<>();
    for (InstructionInterface lScheduler : mMicroscope.getDevices(InstructionInterface.class))
    {
      boolean lNamePatternMatches = true;
      for (String part : pMustContainStrings)
      {
        if (!lScheduler.toString().toLowerCase().contains(part.toLowerCase()))
        {
          lNamePatternMatches = false;
          break;
        }
      }
      if (lNamePatternMatches)
      {
        lListOfAvailabeSchedulers.add(lScheduler);
      }
    }

    lListOfAvailabeSchedulers.sort(new Comparator<InstructionInterface>()
    {
      @Override
      public int compare(InstructionInterface o1, InstructionInterface o2)
      {
        return o1.getName().compareTo(o2.getName());
      }
    });

    return lListOfAvailabeSchedulers;
  }

  private static final long cTimeOut = 1000;

  public long getTimeOut()
  {
    return cTimeOut;
  }

}
