package clearcontrol.processor;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.util.ElapsedTime;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import clearcontrol.stack.metadata.*;
import clearcontrol.stack.processor.StackProcessorInterface;
import clearcontrol.stack.processor.clearcl.ClearCLStackProcessorBase;
import clearcontrol.state.AcquisitionType;
import coremem.recycling.RecyclerInterface;
import org.apache.commons.lang3.tuple.Triple;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Lightsheet fusion processor
 *
 * @author royer
 */
public class LightSheetFastFusionProcessor extends ClearCLStackProcessorBase implements StackProcessorInterface, VisualConsoleInterface, LoggingFeature
{
  private final LightSheetMicroscope mLightSheetMicroscope;
  private LightSheetFastFusionEngine mEngine;

  ConcurrentLinkedQueue<Triple<Integer, StackMetaData, ClearCLImage>> mFusedStackQueue = new ConcurrentLinkedQueue<>();

  private volatile StackInterface mFusedStack;

  private final Variable<Integer> mNumberOfRestartsVariable = new Variable<Integer>("NumberOfRestarts", 5);

  private final Variable<Integer> mMaxNumberOfEvaluationsVariable = new Variable<Integer>("MaxNumberOfEvaluations", 200);

  private final BoundedVariable<Double> mTranslationSearchRadiusVariable = new BoundedVariable<Double>("TranslationSearchRadius", 15.0);
  private final BoundedVariable<Double> mRotationSearchRadiusVariable = new BoundedVariable<Double>("RotationSearchRadius", 3.0);

  private final BoundedVariable<Double> mSmoothingConstantVariable = new BoundedVariable<Double>("SmoothingConstant", 0.05);

  private final Variable<Boolean> mTransformLockSwitchVariable = new Variable<Boolean>("TransformLockSwitch", true);

  private final Variable<Integer> mTransformLockThresholdVariable = new Variable<Integer>("TransformLockThreshold", 20);

  private final Variable<Boolean> mBackgroundSubtractionSwitchVariable = new Variable<Boolean>("BackgroundSubtractionSwitch", false);

  /**
   * Instantiates a lightsheet stack processor
   *
   * @param pProcessorName        processor name
   * @param pLightSheetMicroscope lightsheet microscope
   * @param pContext              ClearCL context to use
   */
  public LightSheetFastFusionProcessor(String pProcessorName, LightSheetMicroscope pLightSheetMicroscope, ClearCLContext pContext)
  {
    super(pProcessorName, pContext);
    mLightSheetMicroscope = pLightSheetMicroscope;
  }

  public void initializeEngine()
  {
    if (mEngine == null)
    {
      mEngine = new LightSheetFastFusionEngine(getContext(), (VisualConsoleInterface) this, mLightSheetMicroscope.getNumberOfLightSheets(), mLightSheetMicroscope.getNumberOfDetectionArms());
    }
  }

  public void reInitializeEngine()
  {
    mEngine.setSubtractingBackground(mBackgroundSubtractionSwitchVariable.get());
    mEngine.setup(mLightSheetMicroscope.getNumberOfLightSheets(), mLightSheetMicroscope.getNumberOfDetectionArms());
  }

  public synchronized StackInterface process(StackInterface pStack, RecyclerInterface<StackInterface, StackRequest> pStackRecycler)
  {
    info("forwarding " + pStack.getMetaData().getValue(MetaDataChannel.Channel));
    boolean lEngineNeedsInitialisation = false;
    if (mEngine == null)
    {
      initializeEngine();

      lEngineNeedsInitialisation = true;
    }

    if (mEngine.isSubtractingBackground() != mBackgroundSubtractionSwitchVariable.get())
      /* // todo: there is no checkbox for registration and downscaline mEngine.isRegistration() != ... || */
    {
      lEngineNeedsInitialisation = true;
    }

    if (lEngineNeedsInitialisation)
    {
      reInitializeEngine();
    }

    if (isPassThrough(pStack))
    {
      info("pass-through mode on, passing stack untouched: %s", pStack);
      return pStack;
    } else
    {
      info("Received stack for processing: %s", pStack);
      info("mets: " + pStack.getMetaData().toString());
    }

    if (mEngine.isDownscale())
    {
      if (pStack != null && pStack.getMetaData() != null && pStack.getMetaData().getVoxelDimX() != null && pStack.getMetaData().getVoxelDimY() != null)
      {
        System.out.println("pStack" + pStack);
        System.out.println("pStack.getMetaData()" + pStack.getMetaData());
        System.out.println("pStack.getMetaData().getVoxelDimX()" + pStack.getMetaData().getVoxelDimX());
        double lVoxelDimX = pStack.getMetaData().getVoxelDimX();
        double lVoxelDimY = pStack.getMetaData().getVoxelDimY();

        pStack.getMetaData().setVoxelDimX(2 * lVoxelDimX);
        pStack.getMetaData().setVoxelDimY(2 * lVoxelDimY);
      }
    }

    info("Passing " + MetaDataView.getCxLyString(pStack.getMetaData()));
    mEngine.passStack(true, pStack);

    if (mEngine.getRegistrationTask() != null)
    {
      try
      {

        if (getTransformLockSwitchVariable().get().booleanValue() && pStack.getMetaData().getValue(MetaDataOrdinals.TimePoint) > getTransformLockThresholdVariable().get().intValue())
        {
          getSmoothingConstantVariable().set(0.02);
          getTransformLockSwitchVariable().set(false);
        }

        mEngine.getRegistrationTask().getParameters().setNumberOfRestarts(getNumberOfRestartsVariable().get().intValue());

        mEngine.getRegistrationTask().getParameters().setTranslationSearchRadius(getTranslationSearchRadiusVariable().get().doubleValue());

        mEngine.getRegistrationTask().getParameters().setRotationSearchRadius(getRotationSearchRadiusVariable().get().doubleValue());

        mEngine.getRegistrationTask().getParameters().setMaxNumberOfEvaluations((int) getMaxNumberOfEvaluationsVariable().get().intValue());

        mEngine.getRegistrationTask().setSmoothingConstant(getSmoothingConstantVariable().get().doubleValue());

      } catch (Throwable e)
      {
        severe("Problem while setting fast fusion parameters: %s", e.toString());
      }
    }

    // if (mEngine.isReady())
    {
      ElapsedTime.measureForceOutput("FastFuseTaskExecution", () ->
      {
        int lNumberOfTasksExecuted = mEngine.executeAllTasks();
        info("executed %d fusion tasks", lNumberOfTasksExecuted);
      });

      for (String key : mEngine.getAvailableImagesSlotKeys())
      {
        info("Available: " + key);
      }
    }

    if (pStack.getMetaData().hasEntry(MetaDataFusion.RequestPerCameraFusion))
    {
      int lNumberOfDetectionArms = mLightSheetMicroscope.getNumberOfDetectionArms();
      for (int c = 0; c < lNumberOfDetectionArms; c++)
      {
        String lKey = "C" + c;
        ClearCLImage lImage = mEngine.getImage(lKey);
        if (lImage != null) mFusedStackQueue.add(Triple.of(c, mEngine.getFusedMetaData().clone(), lImage));
      }

      Triple<Integer, StackMetaData, ClearCLImage> lImageFromQueue = mFusedStackQueue.poll();

      if (lImageFromQueue != null)
      {
        StackInterface lStack = copyFusedStack(pStackRecycler, lImageFromQueue.getRight(), lImageFromQueue.getMiddle(), "C" + lImageFromQueue.getLeft());
        lStack.getMetaData().addEntry(MetaDataView.Camera, lImageFromQueue.getLeft());
        return lStack;
      }

    } else if (mEngine.isDone())
    {
      ClearCLImage lFusedImage = mEngine.getImage("fused");

      return copyFusedStack(pStackRecycler, lFusedImage, mEngine.getFusedMetaData(), null);

      /*
      StackInterface lReturnStack = copyFusedStack(pStackRecycler,
                            lFusedImage,
                            mEngine.getFusedMetaData(),
                            null);
      mEngine.removeImage("fused");
      return lReturnStack;*/
    }

    return null;
  }

  /**
   * This function has been marked as deprecated, because it does a lot more things than
   * its name suggests. Stack saving has been moved to a new class, SaveImageStackTask
   *
   * @param pStackRecycler
   * @param lFusedImage
   * @param pStackMetaData
   * @param pChannel
   * @return
   */
  @Deprecated
  protected StackInterface copyFusedStack(RecyclerInterface<StackInterface, StackRequest> pStackRecycler, ClearCLImage lFusedImage, StackMetaData pStackMetaData, String pChannel)
  {
    mFusedStack = pStackRecycler.getOrWait(1000, TimeUnit.SECONDS, StackRequest.build(lFusedImage.getDimensions()));

    mFusedStack.setMetaData(pStackMetaData);
    mFusedStack.getMetaData().addEntry(MetaDataFusion.Fused, true);
    if (pChannel != null) mFusedStack.getMetaData().addEntry(MetaDataChannel.Channel, pChannel);
    mFusedStack.getMetaData().removeAllEntries(MetaDataView.class);
    mFusedStack.getMetaData().removeAllEntries(MetaDataViewFlags.class);
    mFusedStack.getMetaData().removeEntry(MetaDataOrdinals.Index);

    info("Resulting fused stack metadata:" + mFusedStack.getMetaData());

    lFusedImage.writeTo(mFusedStack.getContiguousMemory(), true);

    mEngine.reset(false);

    return mFusedStack;
  }

  private boolean isPassThrough(StackInterface pStack)
  {
    AcquisitionType lAcquisitionType = pStack.getMetaData().getValue(MetaDataAcquisitionType.AcquisitionType);

    if (lAcquisitionType == AcquisitionType.Interactive) return true;

    if (pStack.getMetaData().hasEntry(MetaDataFusion.RequestFullFusion)) return false;

    if (pStack.getMetaData().hasEntry(MetaDataFusion.RequestPerCameraFusion)) return false;

    return true;
  }

  /**
   * Returns the variable holding the translation search radius.
   *
   * @return translation search radius variable.
   */
  public BoundedVariable<Double> getTranslationSearchRadiusVariable()
  {
    return mTranslationSearchRadiusVariable;
  }

  /**
   * Returns the variable holding the rotation search radius
   *
   * @return rotation search radius
   */
  public BoundedVariable<Double> getRotationSearchRadiusVariable()
  {
    return mRotationSearchRadiusVariable;
  }

  /**
   * Returns the variable holding the number of optimization restarts
   *
   * @return number of optimization restarts variable
   */
  public Variable<Integer> getNumberOfRestartsVariable()
  {
    return mNumberOfRestartsVariable;
  }

  /**
   * Returns the max number of evaluations variable
   *
   * @return max number of evaluations variable
   */
  public Variable<Integer> getMaxNumberOfEvaluationsVariable()
  {
    return mMaxNumberOfEvaluationsVariable;
  }

  /**
   * Returns the variable holding the smoothing constant
   *
   * @return smoothing constant variable
   */
  public BoundedVariable<Double> getSmoothingConstantVariable()
  {
    return mSmoothingConstantVariable;
  }

  /**
   * Returns the switch that decides whether to lock the transformation after a certain
   * number of time points has elapsed
   *
   * @return Transform lock switch variable
   */
  public Variable<Boolean> getTransformLockSwitchVariable()
  {
    return mTransformLockSwitchVariable;
  }

  /**
   * Returns the variable holding the number of timepoints until the transformation should
   * be 'locked' with more stringent temporal filtering
   *
   * @return transform lock timer variable
   */
  public Variable<Integer> getTransformLockThresholdVariable()
  {
    return mTransformLockThresholdVariable;
  }

  public Variable<Boolean> getBackgroundSubtractionSwitchVariable()
  {
    return mBackgroundSubtractionSwitchVariable;
  }

  /*
   *  This function was just introduced for debugging and will soon be delete
   */
  @Deprecated
  public LightSheetFastFusionEngine getEngine()
  {
    return mEngine;
  }

}
