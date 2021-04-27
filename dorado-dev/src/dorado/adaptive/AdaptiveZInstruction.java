package dorado.adaptive;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.DiscreteConsinusTransformEntropyPerSliceEstimator;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.sourcesink.sink.RawFileStackSink;
import coremem.ContiguousMemoryInterface;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class AdaptiveZInstruction extends LightSheetMicroscopeInstructionBase implements
                                                      LoggingFeature
{

  private final BoundedVariable<Integer> mNumberOfSamplesVariable =
      new BoundedVariable<Integer>("NumberOfSamples", 11, 3, Integer.MAX_VALUE, 1);
  private final BoundedVariable<Double> mDeltaZVariable =
      new BoundedVariable<Double>("DeltaZ", 5.0, 0.1, Double.MAX_VALUE, 0.1);
  private final BoundedVariable<Double> mExposureInSecondsVariable =
      new BoundedVariable<Double>("ExposureInSeconds", 0.01, 0.001, Double.MAX_VALUE, 0.1);
  private final BoundedVariable<Double> mLaserPowerVariable =
      new BoundedVariable<Double>("LaserPower", 0.5, 1.0,0.0,1.0);

  private Variable<File> mRootFolderVariable =
      new Variable("RootFolder",
                 new File("C:\\temp\\"));


  private final BoundedVariable<Integer> mSwitchCameraControlPlaneIndex =
      new BoundedVariable<Integer>("Switch camera at control plane index", 4, 0, Integer.MAX_VALUE, 1);

  private Variable<Boolean> mStartWithCamera0 = new Variable<Boolean>("Stack starts with camera 0", false);


  public AdaptiveZInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Adaptation Z scheduler", pLightSheetMicroscope);
  }

  @Override public boolean initialize()
  {
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {

    int lNumberOfControlPlanes = ((InterpolatedAcquisitionState)getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState()).getNumberOfControlPlanes();
    int lControlPlane = (int)(pTimePoint % lNumberOfControlPlanes);

    int lDetectionArmIndex;
    if (lControlPlane < mSwitchCameraControlPlaneIndex.get())
    {// first half
      lDetectionArmIndex = mStartWithCamera0.get()?0:1;
    } else {
      lDetectionArmIndex = mStartWithCamera0.get()?1:0;
    }

    OffHeapPlanarStack stackL0 = acquireStack(lControlPlane, 0, lDetectionArmIndex);
    OffHeapPlanarStack stackL1 = acquireStack(lControlPlane, 1, lDetectionArmIndex);
    OffHeapPlanarStack stackL2 = acquireStack(lControlPlane, 2, lDetectionArmIndex);
    OffHeapPlanarStack stackL3 = acquireStack(lControlPlane, 3, lDetectionArmIndex);

    saveStack("t" + pTimePoint + "C" + lDetectionArmIndex + "L0", stackL0);
    saveStack("t" + pTimePoint + "C" + lDetectionArmIndex + "L1", stackL1);
    saveStack("t" + pTimePoint + "C" + lDetectionArmIndex + "L2", stackL2);
    saveStack("t" + pTimePoint + "C" + lDetectionArmIndex + "L3", stackL3);

    double[] qualityL0 = determineSliceWiseQuality(stackL0);
    logQuality("t" + pTimePoint + "C" + lDetectionArmIndex + "L0_Q", qualityL0);
    double[] qualityL1 = determineSliceWiseQuality(stackL1);
    logQuality("t" + pTimePoint + "C" + lDetectionArmIndex + "L1_Q", qualityL1);
    double[] qualityL2 = determineSliceWiseQuality(stackL2);
    logQuality("t" + pTimePoint + "C" + lDetectionArmIndex + "L2_Q", qualityL2);
    double[] qualityL3 = determineSliceWiseQuality(stackL3);
    logQuality("t" + pTimePoint + "C" + lDetectionArmIndex + "L3_Q", qualityL3);

    //OffHeapPlanarStack lCroppedStackL0 = cropStack(stackL0, 0, stackL0.getWidth() / 2, 0, stackL0.getHeight() / 2);

    double[] mae01 = getMAEOfPlaneToStack(stackL0, 5, stackL1);
    double[] mae10 = getMAEOfPlaneToStack(stackL1, 5, stackL0);
    double[] mae12 = getMAEOfPlaneToStack(stackL1, 5, stackL2);
    double[] mae21 = getMAEOfPlaneToStack(stackL2, 5, stackL1);
    double[] mae23 = getMAEOfPlaneToStack(stackL2, 5, stackL3);
    double[] mae32 = getMAEOfPlaneToStack(stackL3, 5, stackL2);
    double[] mae30 = getMAEOfPlaneToStack(stackL3, 5, stackL0);
    double[] mae03 = getMAEOfPlaneToStack(stackL0, 5, stackL3);


    logQuality("t" + pTimePoint + "C" + lDetectionArmIndex + "_mae01", mae01);
    logQuality("t" + pTimePoint + "C" + lDetectionArmIndex + "_mae10", mae10);
    logQuality("t" + pTimePoint + "C" + lDetectionArmIndex + "_mae12", mae12);
    logQuality("t" + pTimePoint + "C" + lDetectionArmIndex + "_mae21", mae21);
    logQuality("t" + pTimePoint + "C" + lDetectionArmIndex + "_mae23", mae23);
    logQuality("t" + pTimePoint + "C" + lDetectionArmIndex + "_mae32", mae32);
    logQuality("t" + pTimePoint + "C" + lDetectionArmIndex + "_mae30", mae30);
    logQuality("t" + pTimePoint + "C" + lDetectionArmIndex + "_mae03", mae03);

    return true;
  }

  @Override
  public AdaptiveZInstruction copy() {
    return new AdaptiveZInstruction(getLightSheetMicroscope());
  }

  private double[] getMAEOfPlaneToStack(OffHeapPlanarStack pStack1, int pPlaneIndex1, OffHeapPlanarStack pStack2) {
    ContiguousMemoryInterface pMemory1 = pStack1.getContiguousMemory(pPlaneIndex1);

    double[] result = new double[(int)pStack2.getDepth()];

    for (int z = 0; z < pStack2.getDepth(); z++)
    {
      ContiguousMemoryInterface pMemory2 = pStack2.getContiguousMemory(z);

      double sumAbsoluteDifference = 0;
      for (int p = 0; p < pStack1.getWidth() * pStack1.getHeight(); p++) {
        sumAbsoluteDifference += Math.abs(pMemory1.getShort(p) - pMemory2.getShort(p));
      }

      result[z] = sumAbsoluteDifference;

    }
    return result;
  }

  private void logQuality(String pDatasetName, double[] pQuality)
  {
    File lLogFile = new File(mRootFolderVariable.get() + "\\adaptiveZsch", pDatasetName + "_log.txt");
    lLogFile.getParentFile().mkdir();

    try
    {
      BufferedWriter lLogFileWriter = new BufferedWriter(new FileWriter(lLogFile));

      for (int i = 0; i < pQuality.length; i++)
      {
        info("Quality " + pDatasetName + " / " + i + " = " + pQuality[i]);
        lLogFileWriter.write("Quality[" + i
                             + "] "
                             + "\t"
                             + pQuality[i]
                             + "\n");
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }



  private double[] determineSliceWiseQuality(OffHeapPlanarStack pStack)
  {
    DiscreteConsinusTransformEntropyPerSliceEstimator
        lImageQualityEstimator =
        new DiscreteConsinusTransformEntropyPerSliceEstimator(pStack);
    double[] lQualityPerSliceMeasurementsArray =
        lImageQualityEstimator.getQualityArray();
    return lQualityPerSliceMeasurementsArray;
  }

  private boolean saveStack(String pStackName, OffHeapPlanarStack pStack)
  {

    RawFileStackSink lSink = new RawFileStackSink();
    lSink.setLocation(mRootFolderVariable.get(), "adaptiveZsch");
    lSink.appendStack(pStackName, pStack);
    try
    {
      lSink.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  private OffHeapPlanarStack acquireStack(int pControlPlaneIndex, int pLightSheetIndex, int pDetectionArmIndex) {
    info("programStep stack CPI " + pControlPlaneIndex + " L" + pLightSheetIndex + " D" + pDetectionArmIndex   );
    LightSheetMicroscopeQueue lQueue = getLightSheetMicroscope().requestQueue();


    InterpolatedAcquisitionState
        lAcquisitionState =
        (InterpolatedAcquisitionState)getLightSheetMicroscope().getAcquisitionStateManager()
                                                           .getCurrentState();


    double lStepSize = (mDeltaZVariable.get() * 2) / (mNumberOfSamplesVariable.get() - 1);

    double lCurrentDZ =
        lAcquisitionState.getControlPlaneZ(pControlPlaneIndex);//
        //lAcquisitionState.get(LightSheetDOF.DZ, pControlPlaneIndex, pDetectionArmIndex); // lQueue.getDZ(0);
    info(" lAcquisitionState.getControlPlaneZ(pControlPlaneIndex) : " +  lAcquisitionState.getControlPlaneZ(pControlPlaneIndex));
    //info(" lAcquisitionState.get(LightSheetDOF.DZ, pControlPlaneIndex, pDetectionArmIndex) : " +  lAcquisitionState.get(LightSheetDOF.DZ, pControlPlaneIndex, pDetectionArmIndex));

    double lStartZ = lCurrentDZ - mDeltaZVariable.get();

    lQueue.clearQueue();
    int lNumberOfExpectedImages = 0;

    // here we set IZ:
    lAcquisitionState.applyStateAtControlPlane(lQueue, pControlPlaneIndex);

    lQueue.setI(pLightSheetIndex);
    lQueue.setIZ(lAcquisitionState.getControlPlaneZ(pControlPlaneIndex));
    lQueue.setExp(mExposureInSecondsVariable.get());
    lQueue.setIP(pLightSheetIndex, mLaserPowerVariable.get());
    lQueue.setILO(false);
    lQueue.setC(false);
    lQueue.setDZ(lStartZ);
    lQueue.addCurrentStateToQueue();
    lQueue.addCurrentStateToQueue();

    lQueue.setILO(true);
    lQueue.setC(true);
    for (int i = 0; i < mNumberOfSamplesVariable.get(); i++)
    {
      double z = lStepSize * i;
      info("Imaging at " + (lStartZ + z));
      lQueue.setDZ(lStartZ + z);
      lQueue.addCurrentStateToQueue();
      lNumberOfExpectedImages ++;
    }

    lQueue.setILO(false);
    lQueue.setC(false);
    lQueue.setDZ(lCurrentDZ);
    lQueue.addCurrentStateToQueue();

    lQueue.setTransitionTime(0.5);
    lQueue.setFinalisationTime(0.001);

    lQueue.finalizeQueue();

    lQueue.addMetaDataEntry(MetaDataChannel.Channel, "NoDisplay");

    getLightSheetMicroscope().useRecycler("adaptation", 1, 4, 4);
    final Boolean lPlayQueueAndWait;
    try
    {
      lPlayQueueAndWait = getLightSheetMicroscope().playQueueAndWaitForStacks(lQueue,
                                                      100 + lQueue.getQueueLength(),
                                                                          TimeUnit.SECONDS);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
      return null;
    }
    catch (ExecutionException e)
    {
      e.printStackTrace();
      return null;
    }
    catch (TimeoutException e)
    {
      e.printStackTrace();
      return null;
    }

    if (!lPlayQueueAndWait)
    {
      return null;
    }

    OffHeapPlanarStack lResultingStack =
        (OffHeapPlanarStack) getLightSheetMicroscope().getCameraStackVariable(pDetectionArmIndex)
                                                  .get();

    if (lResultingStack.getDepth() != lNumberOfExpectedImages)
    {
      warning("Warning: number of resulting image does not match the expected number. The stack may be corrupted.");
    }

    info("imaging done...");
    return lResultingStack;
  }

  public BoundedVariable<Double> getDeltaZVariable()
  {
    return mDeltaZVariable;
  }

  public BoundedVariable<Double> getExposureInSecondsVariable()
  {
    return mExposureInSecondsVariable;
  }

  public BoundedVariable<Double> getLaserPowerVariable()
  {
    return mLaserPowerVariable;
  }

  public BoundedVariable<Integer> getNumberOfSamplesVariable()
  {
    return mNumberOfSamplesVariable;
  }

  public Variable<File> getRootFolderVariable()
  {
    return mRootFolderVariable;
  }

  public Variable<Boolean> getStartWithCamera0()
  {
    return mStartWithCamera0;
  }

  public BoundedVariable<Integer> getSwitchCameraControlPlaneIndex()
  {
    return mSwitchCameraControlPlaneIndex;
  }
}
