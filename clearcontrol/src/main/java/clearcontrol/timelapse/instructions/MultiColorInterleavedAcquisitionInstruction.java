package clearcontrol.timelapse.instructions;

import clearcontrol.LightSheetDOF;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.LightSheetMicroscopeQueue;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.stack.metadata.*;
import clearcontrol.state.AcquisitionType;
import clearcontrol.state.InterpolatedAcquisitionState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This instructions acquires an image stack per camera where every slice is imaged
 * several times for each light sheet and for each color.
 * <p>
 * C0L0Z0 C0L1Z0 C0L2Z0 C0L3Z0 C0L0Z1 C0L1Z1 C0L2Z1 C0L3Z1 ...
 * <p>
 * Author: Loic Royer
 * 2018
 */
public class MultiColorInterleavedAcquisitionInstruction extends AbstractAcquistionInstruction implements InstructionInterface, PropertyIOableInstructionInterface, LoggingFeature
{

  private final Variable<String> mChannelNameVariable;
  private final BoundedVariable<Integer>[] mPeriodVariableArray;
  private final BoundedVariable<Integer>[] mOffsetVariableArray;
  private final BoundedVariable<Double>[] mLaserPowerAdjustmentVariableArray;


  /**
   * Instanciates a virtual device with a given name
   */
  public MultiColorInterleavedAcquisitionInstruction(LightSheetMicroscope pLightSheetMicroscope, String pChannelName)
  {
    super("Acquisition: MultiColorInterleaved", pLightSheetMicroscope);

    mChannelNameVariable = new Variable<>("Acquisition Channel", pChannelName);

    int lNumberOfLaserLines = getLightSheetMicroscope().getNumberOfLaserLines();
    mPeriodVariableArray = new BoundedVariable<>[lNumberOfLaserLines];
    mOffsetVariableArray = new BoundedVariable<>[lNumberOfLaserLines];
    mLaserPowerAdjustmentVariableArray = new BoundedVariable<>[lNumberOfLaserLines];


    for (int la = 0; la < getLightSheetMicroscope().getNumberOfLaserLines(); la++) {
      mPeriodVariableArray[la] = new BoundedVariable<>("Period for laser line" + la, 1, 0, 1000);
      mOffsetVariableArray[la] = new BoundedVariable<>("Offset for laser line" + la, 0, 0, 1000);
      mLaserPowerAdjustmentVariableArray[la] = new BoundedVariable<>("Laser Line " + la + " Power Adjustment", 1.0, 0.01, 10.0);
    }
  }

  @Override
  public boolean execute(long pTimePoint)
  {
    mCurrentState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState();

    int lImageWidth = mCurrentState.getImageWidthVariable().get().intValue();
    int lImageHeight = mCurrentState.getImageHeightVariable().get().intValue();
    double lExposureTimeInSeconds = mCurrentState.getExposureInSecondsVariable().get().doubleValue();

    int lNumberOfImagesToTake = mCurrentState.getNumberOfZPlanesVariable().get().intValue();

    int lNumberOfLaserLines = getLightSheetMicroscope().getNumberOfLaserLines();
    //mCurrentState.getLaserOnOffVariable();

    // build a queue
    LightSheetMicroscopeQueue lQueue = getLightSheetMicroscope().requestQueue();

    // initialize queue
    lQueue.clearQueue();
    lQueue.setCenteredROI(lImageWidth, lImageHeight);

    lQueue.setExp(lExposureTimeInSeconds);

    // initial position
    goToInitialPosition(getLightSheetMicroscope(), lQueue, mCurrentState.getStackZLowVariable().get().doubleValue(), mCurrentState.getStackZLowVariable().get().doubleValue());

    // --------------------------------------------------------------------
    // build a queue

    for (int lImageCounter = 0; lImageCounter < lNumberOfImagesToTake; lImageCounter++)
    {
      mCurrentState.applyAcquisitionStateAtStackPlane(lQueue, lImageCounter);


      // acquire an image per light sheet + one more
      for (int l = 0; l < getLightSheetMicroscope().getNumberOfLightSheets(); l++)
        if (isLightSheetOn(l))
        {

          // Light intensity:
          double lLightsheetPower = mCurrentState.get(LightSheetDOF.IP, lImageCounter, l);

          // configure light sheets:
          for (int k = 0; k < getLightSheetMicroscope().getNumberOfLightSheets(); k++)
            lQueue.setI(k, k == l);

          for (int la = 0; la < getLightSheetMicroscope().getNumberOfLaserLines(); la++)
            if (isLaserLineOn(pTimePoint, la))
            {

              // configure laser lines:
              for (int k = 0; k < getLightSheetMicroscope().getNumberOfLaserLines(); k++)
                lQueue.setILO(l, k, k == la);

              // Adjust laser power:
              double lLaserLinePower = lLightsheetPower * getLaserPowerAdjustmentVariable(la).get();
              lQueue.setIP(l, lLaserLinePower);

              lQueue.addCurrentStateToQueue();
            }
        }
    }

    // back to initial position
    goToInitialPosition(getLightSheetMicroscope(), lQueue, mCurrentState.getStackZLowVariable().get().doubleValue(), mCurrentState.getStackZLowVariable().get().doubleValue());

    lQueue.setTransitionTime(0.5);
    lQueue.setFinalisationTime(0.005);

    for (int c = 0; c < getLightSheetMicroscope().getNumberOfDetectionArms(); c++)
      if (isCameraOn(c))
      {
        StackMetaData lMetaData = lQueue.getCameraDeviceQueue(c).getMetaDataVariable().get();

        lMetaData.addEntry(MetaDataAcquisitionType.AcquisitionType, AcquisitionType.TimeLapseMultiColorInterleaved);
        lMetaData.addEntry(MetaDataView.Camera, c);

        StringBuilder lChannelName = new StringBuilder(mChannelNameVariable.get().toUpperCase().trim() + "-C" + c);
        for (int l = 0; l < getLightSheetMicroscope().getNumberOfLightSheets(); l++)
          if (isLightSheetOn(l))
          {
            lChannelName.append("I").append(l);

            for (int la = 0; la < getLightSheetMicroscope().getNumberOfLaserLines(); la++)
              if (isLaserLineOn(pTimePoint, la))
                lChannelName.append("L").append(la);
          }

        lMetaData.addEntry(MetaDataChannel.Channel, lChannelName.toString());
      }

    lQueue.addVoxelDimMetaData(getLightSheetMicroscope(), mCurrentState.getStackZStepVariable().get().doubleValue());
    lQueue.addMetaDataEntry(MetaDataOrdinals.TimePoint, pTimePoint);

    for (int c = 0; c < getLightSheetMicroscope().getNumberOfDetectionArms(); c++)
      if (isCameraOn(c))
        lQueue.addMetaDataEntry(MetaDataOther.getCameraExposure(c), getLightSheetMicroscope().getExposure(c));

    for (int la = 0; la < getLightSheetMicroscope().getNumberOfLaserLines(); la++)
      if (isLaserLineOn(pTimePoint, la))
        lQueue.addMetaDataEntry(MetaDataLaser.getLaserPower(la), getLightSheetMicroscope().getLP(la));

    lQueue.finalizeQueue();

    // programStep!
    boolean lPlayQueueAndWait = false;
    try
    {
      mTimeStampBeforeImaging = System.nanoTime();
      lPlayQueueAndWait = getLightSheetMicroscope().playQueueAndWait(lQueue, 100 + lQueue.getQueueLength(), TimeUnit.SECONDS);

    } catch (InterruptedException | ExecutionException | TimeoutException e)
    {
      e.printStackTrace();
    }

    if (!lPlayQueueAndWait)
    {
      System.out.print("Error while imaging");
      return false;
    }

    return true;
  }

  protected boolean isLaserLineOn(long pTimePointIndex, int pLaserLineIndex)
  {
    boolean ison = super.isLaserLineOn(pLaserLineIndex);
    int period = mPeriodVariableArray[pLaserLineIndex].get();
    int offset = mOffsetVariableArray[pLaserLineIndex].get();
    return ison && ((pTimePointIndex+ 2L *period-offset)%period)==0;
  }

  public Variable<String> getChannelNameVariable()
  {
    return mChannelNameVariable;
  }

  public BoundedVariable<Integer> getPeriodVariable(int pLaserLineIndex)
  {
    return mPeriodVariableArray[pLaserLineIndex];
  }

  public BoundedVariable<Integer> getOffsetVariable(int pLaserLineIndex)
  {
    return mOffsetVariableArray[pLaserLineIndex];
  }
  public BoundedVariable<Double> getLaserPowerAdjustmentVariable(int pLaserLineIndex)
  {
    return mLaserPowerAdjustmentVariableArray[pLaserLineIndex];
  }

  @Override
  public MultiColorInterleavedAcquisitionInstruction copy()
  {
    MultiColorInterleavedAcquisitionInstruction lInstruction = new MultiColorInterleavedAcquisitionInstruction(getLightSheetMicroscope(), getChannelNameVariable().get());

    for (int la = 0; la < getLightSheetMicroscope().getNumberOfLaserLines(); la++) {
      lInstruction.mPeriodVariableArray[la] = mPeriodVariableArray[la];
      lInstruction.mOffsetVariableArray[la] = mOffsetVariableArray[la];
      lInstruction.mLaserPowerAdjustmentVariableArray[la] = mLaserPowerAdjustmentVariableArray[la];
    }

    return lInstruction;
  }

  @Override
  public Variable[] getProperties()
  {

    ArrayList<Variable> lList = new ArrayList<>();
    lList.add(mChannelNameVariable);
    Collections.addAll(lList, mPeriodVariableArray);
    Collections.addAll(lList, mOffsetVariableArray);
    Collections.addAll(lList, mLaserPowerAdjustmentVariableArray);

    int length = mPeriodVariableArray.length
            + mOffsetVariableArray.length
            + mLaserPowerAdjustmentVariableArray.length ;

    return lList.toArray(new Variable[length]);
  }
}
