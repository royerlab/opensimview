package clearcontrol.processor;

import clearcl.ClearCLContext;
import clearcl.enums.ImageChannelDataType;
import clearcontrol.core.concurrent.executors.AsynchronousExecutorFeature;
import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface.ChartType;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.MetaDataView;
import clearcontrol.stack.metadata.StackMetaData;
import fastfuse.FastFusionEngine;
import fastfuse.FastFusionEngineInterface;
import fastfuse.pool.FastFusionMemoryPool;
import fastfuse.registration.AffineMatrix;
import fastfuse.tasks.*;
import fastfuse.tasks.DownsampleXYbyHalfTask.Type;

import java.util.List;

/**
 * Lightsheet fast fusion engine
 *
 * @author royer
 */
public class LightSheetFastFusionEngine extends FastFusionEngine implements FastFusionEngineInterface, AsynchronousExecutorFeature, RegistrationListener, LoggingFeature
{

  private VisualConsoleInterface mVisualConsoleInterface;
  private volatile int mCounter = 0;

  private volatile boolean mRegistration = MachineConfiguration.get().getBooleanProperty("fastfuse.register", true);

  private volatile boolean mBackgroundSubtraction = MachineConfiguration.get().getBooleanProperty("fastfuse.backgroundsubtraction", false);

  private volatile boolean mDownscale = MachineConfiguration.get().getBooleanProperty("fastfuse.downscale", true);

  private volatile double mMemRatio = MachineConfiguration.get().getDoubleProperty("fastfuse.memratio", 0.8);

  private Object mRunningLock = new Object();

  private boolean mTimeStepping = true;

  private StackMetaData mFusedStackMetaData = new StackMetaData();

  private RegistrationTask mRegistrationTask;

  /**
   * Instantiates a lightsheet fast fusion engine
   *
   * @param pContext                ClearCL context
   * @param pVisualConsoleInterface visual console interface for logging purposes
   * @param pNumberOfLightSheets    number of lightsheets
   * @param pNumberOfDetectionArms  number of detection arms
   */
  public LightSheetFastFusionEngine(ClearCLContext pContext, VisualConsoleInterface pVisualConsoleInterface, int pNumberOfLightSheets, int pNumberOfDetectionArms)
  {
    super(pContext);
    mVisualConsoleInterface = pVisualConsoleInterface;
    if (mVisualConsoleInterface != null)
    {
      mVisualConsoleInterface.configureChart("Registration error", "error", "time", "error", ChartType.Area);

      mVisualConsoleInterface.configureChart("Registration error", "error (actual)", "time", "error", ChartType.Area);

      mVisualConsoleInterface.configureChart("Translation", "Tx", "time", "Tx", ChartType.Line);
      mVisualConsoleInterface.configureChart("Translation", "Ty", "time", "Ty", ChartType.Line);
      mVisualConsoleInterface.configureChart("Translation", "Tz", "time", "Tz", ChartType.Line);

      mVisualConsoleInterface.configureChart("Translation", "Tx (used)", "time", "Tx", ChartType.Line);
      mVisualConsoleInterface.configureChart("Translation", "Ty (used)", "time", "Ty", ChartType.Line);
      mVisualConsoleInterface.configureChart("Translation", "Tz (used)", "time", "Tz", ChartType.Line);

      mVisualConsoleInterface.configureChart("Rotation", "Rx", "time", "Rx", ChartType.Line);
      mVisualConsoleInterface.configureChart("Rotation", "Ry", "time", "Ry", ChartType.Line);
      mVisualConsoleInterface.configureChart("Rotation", "Rz", "time", "Rz", ChartType.Line);

      mVisualConsoleInterface.configureChart("Rotation", "Rx (used)", "time", "Rx", ChartType.Line);
      mVisualConsoleInterface.configureChart("Rotation", "Ry (used)", "time", "Ry", ChartType.Line);
      mVisualConsoleInterface.configureChart("Rotation", "Rz (used)", "time", "Rz", ChartType.Line);
    }

    // setting up pool with max pool size:
    long lMaxMemoryInBytes = (long) (mMemRatio * pContext.getDevice().getGlobalMemorySizeInBytes());
    FastFusionMemoryPool.getInstance(pContext, lMaxMemoryInBytes);

    setup(pNumberOfLightSheets, pNumberOfDetectionArms);
  }

  public void setup(int pNumberOfLightSheets, int pNumberOfDetectionArms)
  {
    this.reset(true);
    this.getTasks().clear();

    int[] lKernelSizesRegistration = new int[]{3, 3, 3};
    float[] lKernelSigmasRegistration = new float[]{0.5f, 0.5f, 0.5f};

    float[] lKernelSigmasFusion = new float[]{15, 15, 5};

    float[] lKernelSigmasBackground = null;
    if (mBackgroundSubtraction)
    {
      lKernelSigmasBackground = new float[]{30, 30, 10};
    }

    if (pNumberOfLightSheets == 1)
    {
      if (pNumberOfDetectionArms == 1)
      {
        setupOneLightsheetOneDetectionArm();
      } else if (pNumberOfDetectionArms == 2)
      {
        setupOneLightsheetTwoDetectionArm(lKernelSizesRegistration, lKernelSigmasRegistration);
      }
    } else if (pNumberOfLightSheets == 2)
    {
      if (pNumberOfDetectionArms == 1)
      {
        setupTwoLightSheetsOneDetectionArm();

      } else if (pNumberOfDetectionArms == 2)
      {

        setupTwoLightsheetsTwoDetectionArms(lKernelSizesRegistration, lKernelSigmasRegistration);
      }
    } else if (pNumberOfLightSheets == 4)
    {
      if (pNumberOfDetectionArms == 1)
      {
        setupFourLightsheetsOneDetectionArm();
      } else if (pNumberOfDetectionArms == 2)
      {

        setupFourLightsheetsTwoDetectionArms(lKernelSizesRegistration, lKernelSigmasRegistration, lKernelSigmasFusion, lKernelSigmasBackground);
      }
    }

  }

  protected void setupFourLightsheetsTwoDetectionArms(int[] pKernelSizesRegistration, float[] pKernelSigmasRegistration, float[] pKernelSigmasFusion, float[] pKernelSigmasBackground)
  {
    /*
    if (isInterleaved()) {
      if (isDownscale()) {
      } else {
      }
    } else*/

    ImageChannelDataType lInitialFusionDataType = isRegistration() ? ImageChannelDataType.Float : ImageChannelDataType.UnsignedInt16;

    if (isDownscale())
    {
      // process optically camera-fused input
      addTask(new DownsampleXYbyHalfTask("C0opticallycamerafused", "C0", Type.Median, lInitialFusionDataType));
      addTask(new MemoryReleaseTask("C0", "C0opticallycamerafused"));
      addTask(new DownsampleXYbyHalfTask("C1opticallycamerafused", "C1", Type.Median, lInitialFusionDataType));
      addTask(new MemoryReleaseTask("C1", "C1opticallycamerafused"));

      // process interleaved acquisition input
      addTasks(StackSplitTask.splitStackAndReleaseInputs("C0interleaved", new String[]{"C0L0d", "C0L1d", "C0L2d", "C0L3d"}, true));
      addTasks(StackSplitTask.splitStackAndReleaseInputs("C1interleaved", new String[]{"C1L0d", "C1L1d", "C1L2d", "C1L3d"}, true));

      // process sequential acquisition input
      addTasks(DownsampleXYbyHalfTask.applyAndReleaseInputs(Type.Median, "d", "C0L0", "C0L1", "C0L2", "C0L3", "C1L0", "C1L1", "C1L2", "C1L3"));
    } else
    {
      // process optically camera-fused input
      addTask(new IdentityTask("C0opticallycamerafused", "C0"));
      addTask(new IdentityTask("C1opticallycamerafused", "C1"));

      // process interleaved acquisition input
      addTasks(StackSplitTask.splitStackAndReleaseInputs("C0interleaved", new String[]{"C0L0d", "C0L1d", "C0L2d", "C0L3d"}, false));
      addTasks(StackSplitTask.splitStackAndReleaseInputs("C1interleaved", new String[]{"C1L0d", "C1L1d", "C1L2d", "C1L3d"}, false));

      // process sequential acquisition input
      addTasks(IdentityTask.withSuffix("d", "C0L0", "C0L1", "C0L2", "C0L3", "C1L0", "C1L1", "C1L2", "C1L3"));
    }

    addTasks(CompositeTasks.fuseWithSmoothWeights("C0", lInitialFusionDataType, pKernelSigmasFusion, true, "C0L0d", "C0L1d", "C0L2d", "C0L3d"));

    addTasks(CompositeTasks.fuseWithSmoothWeights("C1", lInitialFusionDataType, pKernelSigmasFusion, true, "C1L0d", "C1L1d", "C1L2d", "C1L3d"));

    if (isRegistration())
    {
      List<TaskInterface> lRegistrationTaskList = CompositeTasks.registerWithBlurPreprocessing("C0", "C1", "C1adjusted", pKernelSigmasRegistration, pKernelSizesRegistration, AffineMatrix.scaling(-1, 1, 1), true);
      addTasks(lRegistrationTaskList);
      // extract registration task from list
      for (TaskInterface lTask : lRegistrationTaskList)
        if (lTask instanceof RegistrationTask)
        {
          mRegistrationTask = (RegistrationTask) lTask;
          break;
        }
    } else
    {
      addTask(FlipTask.flipX("C1", "C1adjusted"));
      addTask(new MemoryReleaseTask("C1adjusted", "C1"));
    }

    // addTasks(CompositeTasks.fuseWithSmoothWeights("fused",
    // ImageChannelDataType.UnsignedInt16,
    // pKernelSigmasFusion,
    // true,
    // "C0",
    // "C1adjusted"));

    if (pKernelSigmasBackground == null)
    {
      addTasks(CompositeTasks.fuseWithSmoothWeights("fused-preliminary", ImageChannelDataType.Float, pKernelSigmasFusion, true, "C0", "C1adjusted"));

      // following line just transforms the result image from Float to unsigned
      // int
      addTask(new NonnegativeSubtractionTask("fused-preliminary", 0, "fused", ImageChannelDataType.UnsignedInt16));

    } else
    {

      addTasks(CompositeTasks.fuseWithSmoothWeights("fused-preliminary", ImageChannelDataType.Float, pKernelSigmasFusion, true, "C0", "C1adjusted"));

      addTasks(CompositeTasks.subtractBlurredCopyFromFloatImage("fused-preliminary", "fused", pKernelSigmasBackground, true, ImageChannelDataType.UnsignedInt16));

    }

    // TODO here is the TimeStepper stuff

    // this.getFusedMetaData();
    /*if (mTimeStepping) {
      System.out.println("adding the stepper task now");
      try
      {
        Handler lTimeStepHandler =
                                 new Handler(this.getContext(),
                                             ImageChannelDataType.UnsignedInt16);
        addTask(new CacheStackTask("fused", lTimeStepHandler));
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }*/
    // }
  }

  protected void setupFourLightsheetsOneDetectionArm()
  {
    if (isDownscale())
    {
      addTask(new DownsampleXYbyHalfTask("C0L0", "C0L0d"));
      addTask(new DownsampleXYbyHalfTask("C0L1", "C0L1d"));
      addTask(new DownsampleXYbyHalfTask("C0L2", "C0L2d"));
      addTask(new DownsampleXYbyHalfTask("C0L3", "C0L3d"));
    } else
    {
      addTask(new IdentityTask("C0L0", "C0L0d"));
      addTask(new IdentityTask("C0L1", "C0L1d"));
      addTask(new IdentityTask("C0L2", "C0L2d"));
      addTask(new IdentityTask("C0L3", "C0L3d"));
    }

    addTask(new TenengradFusionTask("C0L0d", "C0L1d", "C0L2d", "C0L3d", "fused", ImageChannelDataType.UnsignedInt16));
  }

  protected void setupTwoLightsheetsTwoDetectionArms(int[] lKernelSizes, float[] lKernelSigmas)
  {
    if (isRegistration())
    {
      if (isDownscale())
      {
        addTask(new DownsampleXYbyHalfTask("C0L0", "C0L0d"));
        addTask(new DownsampleXYbyHalfTask("C0L1", "C0L1d"));
        addTask(new DownsampleXYbyHalfTask("C1L0", "C1L0d"));
        addTask(new DownsampleXYbyHalfTask("C1L1", "C1L1d"));
      } else
      {
        addTask(new IdentityTask("C0L0", "C0L0d"));
        addTask(new IdentityTask("C0L1", "C0L1d"));
        addTask(new IdentityTask("C1L0", "C1L0d"));
        addTask(new IdentityTask("C1L1", "C1L1d"));
      }

      addTask(new TenengradFusionTask("C0L0d", "C0L1d", "C0", ImageChannelDataType.Float));
      addTask(new TenengradFusionTask("C1L0d", "C1L1d", "C1", ImageChannelDataType.Float));

      addTask(new GaussianBlurTask("C0", "C0blur", lKernelSigmas, lKernelSizes));
      addTask(new GaussianBlurTask("C1", "C1blur", lKernelSigmas, lKernelSizes));

      mRegistrationTask = new RegistrationTask("C0blur", "C1blur", "C0", "C1", "C1reg");

      mRegistrationTask.getParameters().setZeroTransformMatrix(AffineMatrix.scaling(-1, 1, 1));

      addTask(mRegistrationTask);
      addTask(new TenengradFusionTask("C0", "C1reg", "fused", ImageChannelDataType.UnsignedInt16));
    } else
    {
      addTask(new TenengradFusionTask("C0L0", "C0L1", "C0", ImageChannelDataType.UnsignedInt16));
      addTask(new TenengradFusionTask("C1L0", "C1L1", "C1", ImageChannelDataType.UnsignedInt16));

      addTask(FlipTask.flipX("C1", "C1flipped"));

      addTask(new TenengradFusionTask("C0", "C1flipped", "fused", ImageChannelDataType.UnsignedInt16));
    }
  }

  protected void setupTwoLightSheetsOneDetectionArm()
  {
    if (isDownscale())
    {
      addTask(new DownsampleXYbyHalfTask("C0L0", "C0L0d"));
      addTask(new DownsampleXYbyHalfTask("C0L1", "C0L1d"));
    } else
    {
      addTask(new IdentityTask("C0L0", "C0L0d"));
      addTask(new IdentityTask("C0L1", "C0L1d"));
    }

    addTask(new TenengradFusionTask("C0L0d", "C0L1d", "fused", ImageChannelDataType.UnsignedInt16));
  }

  protected void setupOneLightsheetTwoDetectionArm(int[] lKernelSizes, float[] lKernelSigmas)
  {
    if (isRegistration())
    {
      if (isDownscale())
      {
        addTask(new DownsampleXYbyHalfTask("C0L0", "C0L0d"));
        addTask(new DownsampleXYbyHalfTask("C1L0", "C1L0d"));
      } else
      {
        addTask(new IdentityTask("C0L0", "C0L0d"));
        addTask(new IdentityTask("C1L0", "C1L0d"));
      }

      addTask(new GaussianBlurTask("C0L0d", "C0L0blur", lKernelSigmas, lKernelSizes));
      addTask(new GaussianBlurTask("C1L0d", "C1L0blur", lKernelSigmas, lKernelSizes));

      mRegistrationTask = new RegistrationTask("C0L0blur", "C1L0blur", "C0L0d", "C1L0d", "C1L0reg");
      mRegistrationTask.getParameters().setZeroTransformMatrix(AffineMatrix.scaling(-1, 1, 1));

      addTask(mRegistrationTask);
      addTask(new TenengradFusionTask("C0L0d", "C1L0reg", "fused", ImageChannelDataType.UnsignedInt16));
    } else
    {
      if (isDownscale())
      {
        addTask(new DownsampleXYbyHalfTask("C0L0", "C0L0d"));
        addTask(new DownsampleXYbyHalfTask("C1L0", "C1L0d"));
      } else
      {
        addTask(new IdentityTask("C0L0", "C0L0d"));
        addTask(new IdentityTask("C1L0", "C1L0d"));
      }

      addTask(FlipTask.flipX("C1L0d", "C1L0flipped"));

      addTask(new TenengradFusionTask("C0L0d", "C1flipped", "fused", ImageChannelDataType.UnsignedInt16));

    }
  }

  protected void setupOneLightsheetOneDetectionArm()
  {
    if (isDownscale())
    {
      addTask(new DownsampleXYbyHalfTask("C0L0", "fused"));
    } else
    {
      addTask(new IdentityTask("C0L0", "fused"));
    }
  }

  /**
   * Returns the fused metadata object
   *
   * @return fused metadata
   */
  public StackMetaData getFusedMetaData()
  {
    return mFusedStackMetaData.clone();
  }

  @Override
  public void reset(boolean pCloseImages)
  {
    super.reset(pCloseImages);
    mFusedStackMetaData.clear();
  }

  /**
   * Returns whether this fusion engine has received all the required stacks
   *
   * @return true -> ready to fuse
   */
  /*public boolean isReady()
  {
    return getAvailableImagesSlotKeys().contains("C0L0")
           && getAvailableImagesSlotKeys().contains("C0L1");
  }/**/

  /**
   * Passes a stack to this Fast Fusion engine.
   *
   * @param pWaitToFinish true ->
   * @param pStack        stack
   */
  public void passStack(boolean pWaitToFinish, StackInterface pStack)
  {
    try
    {
      StackMetaData lStackMetaData = pStack.getMetaData();

      // I commented this out, because I believe it's not necessary
      /*
      Integer lCameraIndex =
                           lStackMetaData.getValue(MetaDataView.Camera);
      Integer lLightSheetIndex =
                               lStackMetaData.getValue(MetaDataView.LightSheet);
      
      if (lCameraIndex == null || lLightSheetIndex == null)
      {
        pStack.release();
        return;
      }
      */

      if (mRegistrationTask != null && lStackMetaData != null && lStackMetaData.getVoxelDimX() != null && lStackMetaData.getVoxelDimZ() != null)
      {
        float lZAspectRatio = (float) (lStackMetaData.getVoxelDimZ() / lStackMetaData.getVoxelDimX());

        System.out.println("RegistrationTask: Setting aspect ratio: " + lZAspectRatio);

        mRegistrationTask.getParameters().setScaleZ(lZAspectRatio);

        mRegistrationTask.addListener(this);

        // mRegisteredFusionTask.setLowerBounds(pLowerBound);

      }

      String lKey = MetaDataView.getCxLyString(lStackMetaData);

      synchronized (mRunningLock)
      {
        info("Passing stack " + lKey + " " + pStack);
        Runnable lRunnable = () ->
        {
          passImage(lKey, pStack.getContiguousMemory(), ImageChannelDataType.UnsignedInt16, pStack.getDimensions());

          fuseMetaData(pStack);

          pStack.release();
        };

        if (pWaitToFinish) lRunnable.run();
        else executeAsynchronously(lRunnable);
      }
    } catch (Throwable e)
    {
      e.printStackTrace();
    }

  }

  private void fuseMetaData(StackInterface pStack)
  {
    StackMetaData lMetaData = pStack.getMetaData();

    mFusedStackMetaData.addAll(lMetaData);
    // System.out.println("passed:" + lMetaData);
  }

  /**
   * Returns true if the fusion is done
   *
   * @return true -> fusion done
   */
  public boolean isDone()
  {
    return isImageAvailable("fused");
  }

  /**
   * Is registration turned on?
   *
   * @return true if registration is turned on
   */
  public boolean isRegistration()
  {
    return mRegistration;
  }

  /**
   * Sets the registration flag
   *
   * @param pRegistration registration flag
   */
  public void setRegistration(boolean pRegistration)
  {
    mRegistration = pRegistration;
  }

  /**
   * Is background subtraction turned on?
   *
   * @return true if registration is turned on
   */
  public boolean isSubtractingBackground()
  {
    return mBackgroundSubtraction;
  }

  /**
   * Sets the background subtraction flag
   *
   * @param pBackgroundSubtraction registration flag
   */
  public void setSubtractingBackground(boolean pBackgroundSubtraction)
  {
    mBackgroundSubtraction = pBackgroundSubtraction;
  }

  /**
   * Returns true if downscale by a factor 2 along XY is active
   *
   * @return true if downscale isactive
   */
  public boolean isDownscale()
  {
    return mDownscale;
  }

  /**
   * Sets whether to downscale by a factor 2 along XY
   *
   * @param pDownscale downscale on or off
   */
  public void setDownscale(boolean pDownscale)
  {
    mDownscale = pDownscale;
  }

  @Override
  public void notifyListenersOfNewScoreForComputedTheta(double pScore)
  {
    mVisualConsoleInterface.addPoint("Registration error", "error", false, mCounter, pScore);
  }

  @Override
  public void notifyListenersOfNewScoreForUsedTheta(double pScore)
  {
    mVisualConsoleInterface.addPoint("Registration error", "error (actual)", false, mCounter, pScore);
  }

  @Override
  public void newComputedTheta(double[] pTheta)
  {
    double Tx = pTheta[0];
    double Ty = pTheta[1];
    double Tz = pTheta[2];
    double Rx = pTheta[3];
    double Ry = pTheta[4];
    double Rz = pTheta[5];

    mVisualConsoleInterface.addPoint("Translation", "Tx", false, mCounter, Tx);

    mVisualConsoleInterface.addPoint("Translation", "Ty", false, mCounter, Ty);

    mVisualConsoleInterface.addPoint("Translation", "Tz", false, mCounter, Tz);

    mVisualConsoleInterface.addPoint("Rotation", "Rx", false, mCounter, Rx);

    mVisualConsoleInterface.addPoint("Rotation", "Ry", false, mCounter, Ry);

    mVisualConsoleInterface.addPoint("Rotation", "Rz", false, mCounter, Rz);

  }

  @Override
  public void newUsedTheta(double[] pTheta)
  {
    double Tx = pTheta[0];
    double Ty = pTheta[1];
    double Tz = pTheta[2];
    double Rx = pTheta[3];
    double Ry = pTheta[4];
    double Rz = pTheta[5];

    mVisualConsoleInterface.addPoint("Translation", "Tx (used)", false, mCounter, Tx);

    mVisualConsoleInterface.addPoint("Translation", "Ty (used)", false, mCounter, Ty);

    mVisualConsoleInterface.addPoint("Translation", "Tz (used)", false, mCounter, Tz);

    mVisualConsoleInterface.addPoint("Rotation", "Rx (used)", false, mCounter, Rx);

    mVisualConsoleInterface.addPoint("Rotation", "Ry (used)", false, mCounter, Ry);

    mVisualConsoleInterface.addPoint("Rotation", "Rz (used)", false, mCounter, Rz);

    mCounter++;
  }

  /**
   * Returns registration task
   *
   * @return registration task
   */
  public RegistrationTask getRegistrationTask()
  {
    return mRegistrationTask;
  }

  public void setTimeStepping(boolean mTimeStepping)
  {
    this.mTimeStepping = mTimeStepping;
  }

}
