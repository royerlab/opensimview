package clearcontrol.microscope.lightsheet.timelapse;

import clearcontrol.core.concurrent.timing.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionEngine;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionProcessor;
import clearcontrol.microscope.lightsheet.timelapse.containers.InstructionDurationContainer;
import clearcontrol.microscope.lightsheet.timelapse.io.ProgramWriter;
import clearcontrol.microscope.timelapse.TimelapseBase;
import clearcontrol.microscope.timelapse.TimelapseInterface;

import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * A LightSheetTimelapse is a list of instructions, which are executed one by one as long
 * as the timelapse is running.
 *
 * @author royer
 * @author haesleinhuepf
 */
public class LightSheetTimelapse extends TimelapseBase implements TimelapseInterface,
                                                                  LoggingFeature
{

  private static final long cTimeOut = 1000;
  private static final int cMinimumNumberOfAvailableStacks = 16;
  private static final int cMaximumNumberOfAvailableStacks = 16;
  private static final int cMaximumNumberOfLiveStacks = 16;

  private final LightSheetMicroscope mLightSheetMicroscope;

  private ArrayList<InstructionInterface>
      mCurrentProgram =
      new ArrayList<InstructionInterface>();

  private Variable<Integer>
          mInstructionIndexVariable =
      new Variable<Integer>("instructions index", 0);

  ArrayList<InstructionInterface> mInitializedInstructionsList;


  /**
   * @param pLightSheetMicroscope microscope
   */
  public LightSheetTimelapse(LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pLightSheetMicroscope);
    mLightSheetMicroscope = pLightSheetMicroscope;

    this.getMaxNumberOfTimePointsVariable().set(999999L);
  }

  @Override
  public void startTimelapse() {
    super.startTimelapse();

    mLightSheetMicroscope.getDataWarehouse().clear();

    File lProgramFile = new File(getWorkingDirectory(), "program.txt");
    ProgramWriter writer = new ProgramWriter(mCurrentProgram, lProgramFile);
    writer.write();

    mInitializedInstructionsList = new ArrayList<InstructionInterface>();

    LightSheetFastFusionProcessor
            lLightSheetFastFusionProcessor =
            mLightSheetMicroscope.getDevice(LightSheetFastFusionProcessor.class, 0);
    LightSheetFastFusionEngine
            lLightSheetFastFusionEngine =
            lLightSheetFastFusionProcessor.getEngine();
    if (lLightSheetFastFusionEngine != null)
    {
      lLightSheetFastFusionEngine.reset(true);
    }
    mInstructionIndexVariable.set(0);
  }

  @Override public boolean programStep()
  {

    if (getStopSignalVariable().get())
    {
      return false;
    }

    try
    {
      //LightSheetFastFusionProcessor
      //    lLightSheetFastFusionProcessor =
      //    mLightSheetMicroscope.getDevice(LightSheetFastFusionProcessor.class, 0);
      info("Executing timepoint: "
           + getTimePointCounterVariable().get()
           + " data warehouse holds "
           + mLightSheetMicroscope.getDataWarehouse().size()
           + " items");

      mLightSheetMicroscope.useRecycler("3DTimelapse",
                                        cMinimumNumberOfAvailableStacks,
                                        cMaximumNumberOfAvailableStacks,
                                        cMaximumNumberOfLiveStacks);



      InstructionInterface
          lNextInstructionToRun =
          mCurrentProgram.get(mInstructionIndexVariable.get());

      // if the instruction wasn't initialized yet, initialize it now!
      if (!mInitializedInstructionsList.contains(lNextInstructionToRun))
      {
        lNextInstructionToRun.initialize();
        mInitializedInstructionsList.add(lNextInstructionToRun);
      }

      info("Starting " + lNextInstructionToRun);
      double duration = ElapsedTime.measure("instructions execution", () -> {
        lNextInstructionToRun.enqueue(getTimePointCounterVariable().get());
      });
      info("Finished " + lNextInstructionToRun + "in "+duration+" ms");

      // store how long the execution took
//      InstructionDurationContainer
//          lContainer =
//          new InstructionDurationContainer(getTimePointCounterVariable().get(),
//                                           lNextInstructionToRun,
//                                           duration);
//      mLightSheetMicroscope.getDataWarehouse()
//                           .put("duration_" + getTimePointCounterVariable().get()+"_"+mInstructionIndexVariable.get(),
//                                lContainer);

      // Determine the next instruction
      mInstructionIndexVariable.set(mInstructionIndexVariable.get()
              + 1);
      if (mInstructionIndexVariable.get() > mCurrentProgram.size() - 1)
      {
        mInstructionIndexVariable.set(0);
        info("Finished time point:" + getTimePointCounterVariable());
        getTimePointCounterVariable().increment();
        return true;
      }

    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }

    return false;
  }



  public long getTimeOut()
  {
    return cTimeOut;
  }

  /**
   * Deprecated: use getCurrentProgram() instead
   *
   * @return current program as list of instructions
   */
  @Deprecated public ArrayList<InstructionInterface> getListOfActivatedSchedulers()
  {
    return getCurrentProgram();
  }

  /**
   * @return current program as list of instructions
   */
  public ArrayList<InstructionInterface> getCurrentProgram()
  {
    return mCurrentProgram;
  }

  public ArrayList<InstructionInterface> getListOfAvailableSchedulers(String... pMustContainStrings)
  {
    ArrayList<InstructionInterface> lListOfAvailabeSchedulers = new ArrayList<>();
    for (InstructionInterface lScheduler : mLightSheetMicroscope.getDevices(
        InstructionInterface.class))
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
      @Override public int compare(InstructionInterface o1, InstructionInterface o2)
      {
        return o1.getName().compareTo(o2.getName());
      }
    });

    return lListOfAvailabeSchedulers;
  }

  public Variable<Integer> getLastExecutedSchedulerIndexVariable()
  {
    return mInstructionIndexVariable;
  }

}
