package clearcontrol.microscope.lightsheet.adaptive.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.math.argmax.SmartArgMaxFinder;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.DiscreteConsinusTransformEntropyPerSliceEstimator;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.stack.StackInterface;
import ij.gui.Plot;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This instructions initializes focus (delta Z of illumination arms). It should be used
 * if the focus is really off.
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) March
 * 2018
 */
public class FocusFinderZInstruction extends LightSheetMicroscopeInstructionBase implements
                                                                                 InstructionInterface,
                                                                                 LoggingFeature,
                                                                                 PropertyIOableInstructionInterface
{
  private int mControlPlaneIndex;
  private int mLightSheetIndex;
  private int mDetectionArmIndex;

  private BoundedVariable<Double>
      mExposureTimeInSecondsVariable =
      new BoundedVariable<Double>("exp", 0.05, 0.0000001, Double.MAX_VALUE, 0.0000001);
  private BoundedVariable<Integer>
      mImageWidthVariable =
      new BoundedVariable<Integer>("image width", 2048, 16, 2048, 1);
  private BoundedVariable<Integer>
      mImageHeightVariable =
      new BoundedVariable<Integer>("image height", 2048, 16, 2048, 1);
  private BoundedVariable<Integer>
      mNumberOfImagesToTakeVariable =
      new BoundedVariable<Integer>("number of samples", 25, 3, 1000, 1);
  private BoundedVariable<Double>
      mDeltaZVariable =
      new BoundedVariable<Double>("deltaZ", 1.0, 0.01, Double.MAX_VALUE, 0.01);
  private Variable<Boolean>
      mResetAllTheTime =
      new Variable<Boolean>("resetAllTheTime", false);

  boolean mNeedsReset = true;

  public static boolean debug = false;

  public FocusFinderZInstruction(int pLightSheetIndex,
                                 int pDetectionArmIndex,
                                 int pControlPlaneIndex,
                                 LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Adaptation: Focus finder Z for C"
          + pDetectionArmIndex
          + "L"
          + pLightSheetIndex
          + "CPI"
          + pControlPlaneIndex, pLightSheetMicroscope);
    mControlPlaneIndex = pControlPlaneIndex;
    mLightSheetIndex = pLightSheetIndex;
    mDetectionArmIndex = pDetectionArmIndex;
  }

  @Override public boolean initialize()
  {
    mNeedsReset = true;
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    InterpolatedAcquisitionState
        lAcquisitionState =
        (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager()
                                                                .getCurrentState();

    int cpi = mControlPlaneIndex;
    int lLightSheetIndex = mLightSheetIndex;
    int lCameraIdx = mDetectionArmIndex;

    info("Control plane " + cpi);
    double lCPPositionZ = lAcquisitionState.getControlPlaneZ(cpi);
    info("Z position " + lCPPositionZ);

    // low Z values are on the back of XWing at camera 1
    // high Z values are better visible in camera 0 on the front

    double deltaZ = -determineBestDeltaZAtZ(lCPPositionZ, lLightSheetIndex, lCameraIdx);
    info("determined delta Z " + deltaZ);

    double
        lIZ =
        lAcquisitionState.getInterpolationTables()
                         .get(LightSheetDOF.IZ, cpi, lLightSheetIndex);
    info("old delta Z " + lIZ);

    lAcquisitionState.getInterpolationTables()
                     .set(LightSheetDOF.IZ, cpi, lLightSheetIndex, deltaZ);

    LightSheetMicroscopeQueue lQueue = getLightSheetMicroscope().requestQueue();
    System.out.println("Z1: " + lCPPositionZ);
    lAcquisitionState.applyAcquisitionStateAtZ(lQueue, lCPPositionZ);
    System.out.println("Z1: " + lQueue.getIZ(lLightSheetIndex));

    mNeedsReset = false;
    return false;
  }

  private double determineBestDeltaZAtZ(double lCPPositionZ,
                                        int lLightSheetIndex,
                                        int lCameraIdx)
  {

    double lDetectionZStep = mDeltaZVariable.get();
    int lNumberOfImagesToTake = mNumberOfImagesToTakeVariable.get();
    int lImageWidth = mImageWidthVariable.get();
    int lImageHeight = mImageHeightVariable.get();
    double lExposureTimeInSeconds = mExposureTimeInSecondsVariable.get();

    int lDetectionArmIndex = lCameraIdx;
    int lIlluminationArmIndex = lLightSheetIndex;

    double lIlluminationZStart = lCPPositionZ;
    double
        lDetectionZZStart =
        lIlluminationZStart - (lDetectionZStep * (lNumberOfImagesToTake - 1) / 2);

    // build a queue
    LightSheetMicroscopeQueue lQueue = getLightSheetMicroscope().requestQueue();

    // initialize queue
    lQueue.clearQueue();
    lQueue.setCenteredROI(lImageWidth, lImageHeight);

    lQueue.setExp(lExposureTimeInSeconds);

    // initial position
    goToInitialPosition(getLightSheetMicroscope(),
                        lQueue,
                        lIlluminationZStart,
                        lDetectionArmIndex,
                        lDetectionZZStart);
    // lQueue.addCurrentStateToQueue();

    // Do a break of three seconds before imaging
    // lQueue.setExp(3);
    // lQueue.addCurrentStateToQueue();

    // --------------------------------------------------------------------
    // build a queue
    double[] lXAxis = new double[lNumberOfImagesToTake];
    InterpolatedAcquisitionState
        lInterpolatedAcquisitionState =
        (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager()
                                                                .getCurrentState();

    for (int illuminationZIndex = 0; illuminationZIndex
                                     < lNumberOfImagesToTake; illuminationZIndex++)
    {
      lInterpolatedAcquisitionState.applyAcquisitionStateAtZ(lQueue, lIlluminationZStart);
      for (int l = 0; l < getLightSheetMicroscope().getNumberOfLightSheets(); l++)
      {
        lQueue.setI(l, l == lIlluminationArmIndex);
      }
      // #print(" i" + str(lIlluminationZStart))
      // #print(" d" + str(lDetectionZZStart + illuminationZIndex *
      // lDetectionZStep))

      // lQueue.setIW(lIlluminationArmIndex, 0.45);
      // lQueue.setI(lIlluminationArmIndex, true);
      System.out.println("A: " + lQueue.getIA(lIlluminationArmIndex));
      // lQueue.setIZ(lIlluminationArmIndex, lIlluminationZStart);

      double lPositionZ = lDetectionZZStart + illuminationZIndex * lDetectionZStep;
      if (mNeedsReset || mResetAllTheTime.get())
      {
        lQueue.setDZ(lDetectionArmIndex, lPositionZ);
      }
      else
      {
        lQueue.setDZ(lDetectionArmIndex, lQueue.getDZ(lDetectionArmIndex) + lPositionZ);
      }

      lQueue.setC(lDetectionArmIndex, true);
      lQueue.setExp(lExposureTimeInSeconds);
      lQueue.addCurrentStateToQueue();

      lQueue.setTransitionTime(0.5);
      lQueue.setFinalisationTime(0.005);

      lXAxis[illuminationZIndex] = lPositionZ - lCPPositionZ;
    }

    goToInitialPosition(getLightSheetMicroscope(),
                        lQueue,
                        lIlluminationZStart,
                        lDetectionArmIndex,
                        lDetectionZZStart);

    lQueue.finalizeQueue();

    // --------------------------------------------------------------------
    // Laser on!

    // Thread.sleep(20000);

    // programStep!
    boolean lPlayQueueAndWait = false;
    try
    {
      lPlayQueueAndWait =
          getLightSheetMicroscope().playQueueAndWaitForStacks(lQueue,
                                                              10000
                                                              + lQueue.getQueueLength(),
                                                              TimeUnit.SECONDS);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    catch (ExecutionException e)
    {
      e.printStackTrace();
    }
    catch (TimeoutException e)
    {
      e.printStackTrace();
    }
    if (!lPlayQueueAndWait)
    {
      warning("Error while imaging");
    }

    StackInterface
        lStack =
        getLightSheetMicroscope().getCameraStackVariable(lDetectionArmIndex).get();

    // measure quality
    DiscreteConsinusTransformEntropyPerSliceEstimator
        lImageQualityEstimator =
        new DiscreteConsinusTransformEntropyPerSliceEstimator(lStack);
    double[] lQualityPerSliceMeasurementsArray = lImageQualityEstimator.getQualityArray();

    if (debug)
    {
      info("arr " + Arrays.toString(lQualityPerSliceMeasurementsArray));
      Plot
          plot =
          new Plot("title", "quality", "z", lXAxis, lQualityPerSliceMeasurementsArray);
      plot.show();
    }
    SmartArgMaxFinder lSmartArgMaxFinder = new SmartArgMaxFinder();
    double
        lArgMaxQualityDeltaZ =
        lSmartArgMaxFinder.argmax(lXAxis, lQualityPerSliceMeasurementsArray);

    if (debug)
    {
      info("bef " + lQualityPerSliceMeasurementsArray[lNumberOfImagesToTake / 2]);
      info("max " + lArgMaxQualityDeltaZ);
      info("arg " + lArgMaxQualityDeltaZ);

      // --------------------------------------------------------------------
      // convert and show
      // RandomAccessibleInterval imgStack =
      // EasyScopyUtilities.stackToImg((OffHeapPlanarStack) lStack);
      // ImageJFunctions.show(imgStack);
      // IJ.run("Enhance Contrast", "saturated=0.35");
    }
    return lArgMaxQualityDeltaZ;
  }

  private void goToInitialPosition(LightSheetMicroscope lLightsheetMicroscope,
                                   LightSheetMicroscopeQueue lQueue,
                                   double lIlluminationZStart,
                                   int lDetectionArmIndex,
                                   double lDetectionZZStart)
  {
    ((InterpolatedAcquisitionState) lLightsheetMicroscope.getAcquisitionStateManager()
                                                         .getCurrentState()).applyAcquisitionStateAtZ(
        lQueue,
        lIlluminationZStart);

    for (int i = 0; i < lLightsheetMicroscope.getNumberOfLightSheets(); i++)
    {
      lQueue.setI(i, false);

      // lQueue.setIZ(lIlluminationZStart);
      // lQueue.setDZ(lDetectionArmIndex, lDetectionZZStart);
    }

    lQueue.setDZ(lDetectionArmIndex, lDetectionZZStart);
    lQueue.setC(lDetectionArmIndex, false);

    lQueue.addCurrentStateToQueue();
    lQueue.addCurrentStateToQueue();
  }

  /*
  private void goToInitialPosition(LightSheetMicroscope lLightsheetMicroscope, LightSheetMicroscopeQueue lQueue, double lIlluminationZStart, int lDetectionArmIndex, double lDetectionZZStart) {
    for (int i = 0; i < lLightsheetMicroscope.getNumberOfLightSheets(); i++) {
  
      System.out.println("TODO: XWing specific code!");
      lQueue.setI(i, false);
      // XWing specific:
      lQueue.setIW(i, 0.45);
      lQueue.setIH(i, 500);
      lQueue.setIX(i, 0);
      lQueue.setIY(i, 0);
  
      lQueue.setIZ(lIlluminationZStart);
      lQueue.setDZ(lDetectionArmIndex, lDetectionZZStart);
      lQueue.setC(lDetectionArmIndex, false);
    }
  
    lQueue.addCurrentStateToQueue();
  }
  */

  public BoundedVariable<Double> getExposureTimeInSecondsVariable()
  {
    return mExposureTimeInSecondsVariable;
  }

  public BoundedVariable<Integer> getImageWidthVariable()
  {
    return mImageWidthVariable;
  }

  public BoundedVariable<Integer> getImageHeightVariable()
  {
    return mImageHeightVariable;
  }

  public BoundedVariable<Integer> getNumberOfImagesToTakeVariable()
  {
    return mNumberOfImagesToTakeVariable;
  }

  public BoundedVariable<Double> getDeltaZVariable()
  {
    return mDeltaZVariable;
  }

  public Variable<Boolean> getResetAllTheTime()
  {
    return mResetAllTheTime;
  }

  @Override public FocusFinderZInstruction copy()
  {
    FocusFinderZInstruction
        copied =
        new FocusFinderZInstruction(mLightSheetIndex,
                                    mDetectionArmIndex,
                                    mControlPlaneIndex,
                                    getLightSheetMicroscope());
    copied.mDeltaZVariable.set(mDeltaZVariable.get());
    copied.mExposureTimeInSecondsVariable.set(mExposureTimeInSecondsVariable.get());
    copied.mImageHeightVariable.set(mImageHeightVariable.get());
    copied.mImageWidthVariable.set(mImageWidthVariable.get());
    copied.mNumberOfImagesToTakeVariable.set(mNumberOfImagesToTakeVariable.get());
    return copied;
  }

  @Override public Variable[] getProperties()
  {
    return new Variable[] { getDeltaZVariable(),
                            getExposureTimeInSecondsVariable(),
                            getImageHeightVariable(),
                            getImageWidthVariable(),
                            getNumberOfImagesToTakeVariable(),
                            getResetAllTheTime() };
  }
}
