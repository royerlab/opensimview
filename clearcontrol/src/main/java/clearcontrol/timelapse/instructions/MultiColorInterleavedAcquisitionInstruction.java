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

  private BoundedVariable<Double>[] mLaserPowerAdjustmentVariableArray;

  /**
   * Instanciates a virtual device with a given name
   */
  public MultiColorInterleavedAcquisitionInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Acquisition: MultiColorInterleaved", pLightSheetMicroscope);
    mLaserPowerAdjustmentVariableArray = new BoundedVariable[getLightSheetMicroscope().getNumberOfLaserLines()];

    for (int la = 0; la < getLightSheetMicroscope().getNumberOfLaserLines(); la++)
      mLaserPowerAdjustmentVariableArray[la] = new BoundedVariable<Double>("Laser Line "+la+" Power Adjustment", 1.0, 0.01, 10.0);
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
            if (isLaserLineOn(la))
            {

              // configure laser lines:
              for (int k = 0; k < getLightSheetMicroscope().getNumberOfLaserLines(); k++)
                lQueue.setILO(l, la, k == la);

              // Adjust laser power:
              double lLaserLinePower = lLightsheetPower* getLaserPowerAdjustmentVariable(la).get();
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

        String lChannelName = "C"+c;
        for (int l = 0; l < getLightSheetMicroscope().getNumberOfLightSheets(); l++)
          if (isLightSheetOn(l))
          {
            lChannelName += "I"+l;

            for (int la = 0; la < getLightSheetMicroscope().getNumberOfLaserLines(); la++)
              if (isLaserLineOn(la))
                lChannelName += "L"+la;
          }

        lMetaData.addEntry(MetaDataChannel.Channel, lChannelName);
      }

    lQueue.addVoxelDimMetaData(getLightSheetMicroscope(), mCurrentState.getStackZStepVariable().get().doubleValue());
    lQueue.addMetaDataEntry(MetaDataOrdinals.TimePoint, pTimePoint);

    for (int c = 0; c < getLightSheetMicroscope().getNumberOfDetectionArms(); c++)
      if (isCameraOn(c))
        lQueue.addMetaDataEntry(MetaDataOther.getCameraExposure(c), getLightSheetMicroscope().getExposure(c));

    for (int la = 0; la < getLightSheetMicroscope().getNumberOfLaserLines(); la++)
      if (isLaserLineOn(la))
        lQueue.addMetaDataEntry(MetaDataLaser.getLaserPower(la), getLightSheetMicroscope().getLP(la));

    lQueue.finalizeQueue();

    // programStep!
    boolean lPlayQueueAndWait = false;
    try
    {
      mTimeStampBeforeImaging = System.nanoTime();
      lPlayQueueAndWait = getLightSheetMicroscope().playQueueAndWait(lQueue, 100 + lQueue.getQueueLength(), TimeUnit.SECONDS);

    } catch (InterruptedException e)
    {
      e.printStackTrace();
    } catch (ExecutionException e)
    {
      e.printStackTrace();
    } catch (TimeoutException e)
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

  public BoundedVariable<Double> getLaserPowerAdjustmentVariable(int pLaserLineIndex)
  {
    return mLaserPowerAdjustmentVariableArray[pLaserLineIndex];
  }



  @Override
  public MultiColorInterleavedAcquisitionInstruction copy()
  {
    return new MultiColorInterleavedAcquisitionInstruction(getLightSheetMicroscope());
  }

  @Override
  public Variable[] getProperties()
  {
    Variable[] lVariables = mLaserPowerAdjustmentVariableArray;
    return lVariables;
  }
}
