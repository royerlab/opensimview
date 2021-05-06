package clearcontrol.devices.signalgen.staves.staves;

import clearcontrol.component.lightsheet.LightSheet;
import clearcontrol.component.lightsheet.LightSheetQueue;
import clearcontrol.component.lightsheet.si.StructuredIlluminationPatternInterface;
import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.math.functions.UnivariateAffineFunction;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.signalgen.measure.Measure;
import clearcontrol.devices.signalgen.staves.*;

import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;

/**
 * @author royer
 */
@SuppressWarnings("javadoc")
public class LightSheetStaves implements LoggingFeature
{
  private final static MachineConfiguration cCurrentMachineConfiguration = MachineConfiguration.get();

  private LightSheetQueue mLightSheetQueue;

  private final BoundedVariable<Double> mLineExposureInMicrosecondsVariable = new BoundedVariable<Double>("LineExposureInMicroseconds", 9.744);

  private BezierStave mBeforeExposureYStave, mBeforeExposureZStave;

  private RampSteppingStave mExposureYStave, mExposureZStave;

  private RampContinuousStave mFinalYStave;

  private ConstantStave mBeforeExposureXStave, mExposureXStave, mBeforeExposureBStave, mExposureBStave, mBeforeExposureWStave, mExposureWStave, mBeforeExposureLAStave, mExposureLAStave;
  private IntervalStave[] mNonSIIluminationLaserTriggerStaveArray;

  private EdgeStave mBeforeExposureTStave, mExposureTStave, mFinalTStave;

  private int mStaveXIndex, mStaveYIndex, mStaveZIndex, mStaveBIndex, mStaveWIndex, mStaveLAIndex, mStaveTIndex;

  public LightSheetStaves(LightSheetQueue pLightSheetQueue)
  {
    super();
    mLightSheetQueue = pLightSheetQueue;

    mBeforeExposureLAStave = new ConstantStave("laser.beforeexp.am", 0);
    mExposureLAStave = new ConstantStave("laser.exposure.am", 0);

    mBeforeExposureXStave = new ConstantStave("lightsheet.x.be", 0);
    mBeforeExposureYStave = new BezierStave("lightsheet.y.be", 0);
    mBeforeExposureZStave = new BezierStave("lightsheet.z.be", 0);
    mBeforeExposureBStave = new ConstantStave("lightsheet.b.be", 0);
    mBeforeExposureWStave = new ConstantStave("lightsheet.r.be", 0);
    mBeforeExposureTStave = new EdgeStave("trigger.out.be", 1, 1, 0);

    mExposureXStave = new ConstantStave("lightsheet.x.e", 0);
    mExposureYStave = new RampSteppingStave("lightsheet.y.e");
    mExposureZStave = new RampSteppingStave("lightsheet.z.e");
    mExposureBStave = new ConstantStave("lightsheet.b.e", 0);
    mExposureWStave = new ConstantStave("lightsheet.r.e", 0);
    mExposureTStave = new EdgeStave("trigger.out.e", 1, 0, 0);

    mFinalTStave = new EdgeStave("trigger.out.f", 0.5f, 1, 0);
    mFinalYStave = new RampContinuousStave("lightsheet.y.f");

    mNonSIIluminationLaserTriggerStaveArray = new IntervalStave[mLightSheetQueue.getNumberOfLaserDigitalControls()];
    for (int i = 0; i < mLightSheetQueue.getNumberOfLaserDigitalControls(); i++)
      mNonSIIluminationLaserTriggerStaveArray[i] = new IntervalStave("laser.digital" + i + ".trigger.out", 0, 1, 1, 0);


    String lPrefix = getDevicePrefixInConfigFile();
    mStaveXIndex = cCurrentMachineConfiguration.getIntegerProperty(lPrefix + ".x.index", 2);

    mStaveYIndex = cCurrentMachineConfiguration.getIntegerProperty(lPrefix + ".y.index", 3);

    mStaveZIndex = cCurrentMachineConfiguration.getIntegerProperty(lPrefix + ".z.index", 4);

    mStaveBIndex = cCurrentMachineConfiguration.getIntegerProperty(lPrefix + ".b.index", 5);

    mStaveWIndex = cCurrentMachineConfiguration.getIntegerProperty(lPrefix + ".w.index", 6);

    mStaveLAIndex = cCurrentMachineConfiguration.getIntegerProperty(lPrefix + ".la.index", 7);

    mStaveTIndex = cCurrentMachineConfiguration.getIntegerProperty(lPrefix + ".t.index", 8 + 7);

  }

  protected String getDevicePrefixInConfigFile()
  {
    return "device.lsm.lightsheet." + getLightSheet().getName();
  }

  private LightSheet getLightSheet()
  {
    return mLightSheetQueue.getLightSheet();
  }

  public void addStavesToMeasures(Measure pBeforeExposureMeasure, Measure pExposureMeasure, Measure pFinalMeasure)
  {
    ensureStavesAddedToBeforeExposureMeasure(pBeforeExposureMeasure);
    ensureStavesAddedToExposureMeasure(pExposureMeasure);
    ensureStavesAddedToFinalMeasure(pFinalMeasure);
  }

  public void ensureStavesAddedToBeforeExposureMeasure(Measure pBeforeExposureMeasure)
  {

    // Analog outputs before exposure:
    mBeforeExposureXStave = pBeforeExposureMeasure.ensureSetStave(mStaveXIndex, mBeforeExposureXStave);

    mBeforeExposureYStave = pBeforeExposureMeasure.ensureSetStave(mStaveYIndex, mBeforeExposureYStave);

    mBeforeExposureZStave = pBeforeExposureMeasure.ensureSetStave(mStaveZIndex, mBeforeExposureZStave);

    mBeforeExposureBStave = pBeforeExposureMeasure.ensureSetStave(mStaveBIndex, mBeforeExposureBStave);

    mBeforeExposureWStave = pBeforeExposureMeasure.ensureSetStave(mStaveWIndex, mBeforeExposureWStave);

    mBeforeExposureLAStave = pBeforeExposureMeasure.ensureSetStave(mStaveLAIndex, mBeforeExposureLAStave);

    mBeforeExposureTStave = pBeforeExposureMeasure.ensureSetStave(mStaveTIndex, mBeforeExposureTStave);

  }

  public void ensureStavesAddedToExposureMeasure(Measure pExposureMeasure)
  {

    // Analog outputs at exposure:

    mExposureXStave = pExposureMeasure.ensureSetStave(mStaveXIndex, mExposureXStave);

    mExposureYStave = pExposureMeasure.ensureSetStave(mStaveYIndex, mExposureYStave);

    mExposureZStave = pExposureMeasure.ensureSetStave(mStaveZIndex, mExposureZStave);

    mExposureBStave = pExposureMeasure.ensureSetStave(mStaveBIndex, mExposureBStave);

    mExposureWStave = pExposureMeasure.ensureSetStave(mStaveWIndex, mExposureWStave);

    mExposureLAStave = pExposureMeasure.ensureSetStave(mStaveLAIndex, mExposureLAStave);

    mExposureTStave = pExposureMeasure.ensureSetStave(mStaveTIndex, mExposureTStave);

    for (int i = 0; i < mLightSheetQueue.getNumberOfLaserDigitalControls(); i++)
      setLaserDigitalTriggerStave(pExposureMeasure, i, mNonSIIluminationLaserTriggerStaveArray[i]);/**/

  }

  public void ensureStavesAddedToFinalMeasure(Measure pFinalMeasure)
  {

    pFinalMeasure.ensureSetStave(mStaveXIndex, mBeforeExposureXStave);

    mFinalYStave = pFinalMeasure.ensureSetStave(mStaveYIndex, mFinalYStave);

    pFinalMeasure.ensureSetStave(mStaveZIndex, mBeforeExposureZStave);

    pFinalMeasure.ensureSetStave(mStaveBIndex, mBeforeExposureBStave);

    pFinalMeasure.ensureSetStave(mStaveWIndex, mBeforeExposureWStave);

    pFinalMeasure.ensureSetStave(mStaveLAIndex, mBeforeExposureLAStave);

    mFinalTStave = pFinalMeasure.ensureSetStave(mStaveTIndex, mFinalTStave);

  }

  public void update(Measure pBeforeExposureMeasure, Measure pExposureMeasure, Measure pFinalMeasure)
  {
    synchronized (this)
    {

      // info("Updating: " + getLightSheet().getName());

      final double lReadoutTimeInMicroseconds = getBeforeExposureMeasureDuration(TimeUnit.MICROSECONDS);
      final double lExposureMeasureTimeInMicroseconds = getExposureMeasureDuration(TimeUnit.MICROSECONDS);

      final double lFinalMeasureTimeInMicroseconds = getFinalMeasureDuration(TimeUnit.MICROSECONDS);

      pBeforeExposureMeasure.setDuration(round(lReadoutTimeInMicroseconds), TimeUnit.MICROSECONDS);

      pExposureMeasure.setDuration(round(lExposureMeasureTimeInMicroseconds), TimeUnit.MICROSECONDS);

      pFinalMeasure.setDuration(round(lFinalMeasureTimeInMicroseconds), TimeUnit.MICROSECONDS);

      final double lLineExposureTimeInMicroseconds = lReadoutTimeInMicroseconds + lExposureMeasureTimeInMicroseconds;
      mLineExposureInMicrosecondsVariable.set(lLineExposureTimeInMicroseconds);

      UnivariateAffineFunction lYFunction = getLightSheet().getYFunction().get();

      UnivariateAffineFunction lZFunction = getLightSheet().getZFunction().get();

      UnivariateAffineFunction lHeightFunction = getLightSheet().getHeightFunction().get();

      final double lYBF = mLightSheetQueue.getYVariable().get().doubleValue();

      final double lZBF = mLightSheetQueue.getZVariable().get().doubleValue();
      final double lZminBF = mLightSheetQueue.getZVariable().getMin().doubleValue();
      final double lZmaxBF = mLightSheetQueue.getZVariable().getMax().doubleValue();

      final double lZCenter = (lZmaxBF - lZminBF) / 2;

      final double lGalvoYOffsetBeforeRotation = lYBF;
      final double lGalvoZOffsetBeforeRotation = lZBF;

      final double lHeightBF = mLightSheetQueue.getHeightVariable().get().doubleValue();

      final double lLightSheetHeight = lHeightFunction.value(lHeightBF);

      final double lGalvoYOffset = galvoRotateY(lGalvoYOffsetBeforeRotation, lGalvoZOffsetBeforeRotation - lZCenter);
      final double lGalvoZOffset = galvoRotateZ(lGalvoYOffsetBeforeRotation, lGalvoZOffsetBeforeRotation);

      final double lGalvoAmplitudeY = galvoRotateY(lLightSheetHeight, 0);
      final double lGalvoAmplitudeZ = galvoRotateZ(lLightSheetHeight, 0);

      final double lGalvoYLowValue = lYFunction.value(lGalvoYOffset - lGalvoAmplitudeY);
      final double lGalvoYHighValue = lYFunction.value(lGalvoYOffset + lGalvoAmplitudeY);

      final double lGalvoZLowValue = lZFunction.value(lGalvoZOffset - lGalvoAmplitudeZ);
      final double lGalvoZHighValue = lZFunction.value(lGalvoZOffset + lGalvoAmplitudeZ);

      mBeforeExposureYStave.setStartValue((float) lGalvoYHighValue);
      mBeforeExposureYStave.setStopValue((float) lGalvoYLowValue);
      mBeforeExposureYStave.setStartSlope((float) (lGalvoYHighValue - lGalvoYLowValue));
      mBeforeExposureYStave.setStopSlope((float) (lGalvoYHighValue - lGalvoYLowValue));
      mBeforeExposureYStave.setSmoothness(0.50f);
      mBeforeExposureYStave.setMargin(0.1f);

      mBeforeExposureZStave.setStartValue((float) lGalvoZHighValue);
      mBeforeExposureZStave.setStopValue((float) lGalvoZLowValue);
      mBeforeExposureZStave.setStartSlope((float) (lGalvoZHighValue - lGalvoZLowValue));
      mBeforeExposureZStave.setStopSlope((float) (lGalvoZHighValue - lGalvoZLowValue));
      mBeforeExposureZStave.setSmoothness(0.50f);
      mBeforeExposureZStave.setMargin(0.1f);

      mExposureYStave.setSyncStart(0);
      mExposureYStave.setSyncStop(1);
      mExposureYStave.setStartValue((float) lGalvoYLowValue);
      mExposureYStave.setStopValue((float) lGalvoYHighValue);
      mExposureYStave.setOutsideValue((float) lGalvoYHighValue);
      mExposureYStave.setNoJump(true);

      mExposureZStave.setSyncStart(0);
      mExposureZStave.setSyncStop(1);
      mExposureZStave.setStartValue((float) lGalvoZLowValue);
      mExposureZStave.setStopValue((float) lGalvoZHighValue);
      mExposureZStave.setOutsideValue((float) lGalvoZHighValue);
      mExposureZStave.setNoJump(true);

      mFinalYStave.setSyncStart(0);
      mFinalYStave.setSyncStop(1);
      mFinalYStave.setStartValue((float) lGalvoYHighValue);
      mFinalYStave.setStopValue((float) lGalvoYHighValue);
      mFinalYStave.setOutsideValue((float) lGalvoYHighValue);
      mFinalYStave.setNoJump(true);

      mBeforeExposureXStave.setValue((float) getLightSheet().getXFunction().get().value(mLightSheetQueue.getXVariable().get().doubleValue()));
      mExposureXStave.setValue((float) getLightSheet().getXFunction().get().value(mLightSheetQueue.getXVariable().get().doubleValue()));

      mBeforeExposureBStave.setValue((float) getLightSheet().getBetaFunction().get().value(mLightSheetQueue.getBetaInDegreesVariable().get().doubleValue()));
      mExposureBStave.setValue((float) getLightSheet().getBetaFunction().get().value(mLightSheetQueue.getBetaInDegreesVariable().get().doubleValue()));

      /*final double lFocalLength = mFocalLengthInMicronsVariable.get();
      final double lLambdaInMicrons = mLambdaInMicronsVariable.get();
      final double lLightSheetRangeInMicrons = mWidthVariable.getValue();
      
      final double lIrisDiameterInMm = GaussianBeamGeometry.getBeamIrisDiameter(lFocalLength,
                                                                                lLambdaInMicrons,
                                                                                lLightSheetRangeInMicrons);/**/
      double lWidthValue = getLightSheet().getWidthFunction().get().value(mLightSheetQueue.getWidthVariable().get().doubleValue());

      mBeforeExposureWStave.setValue((float) lWidthValue);
      mExposureWStave.setValue((float) lWidthValue);

      final double lOverscan = mLightSheetQueue.getOverScanVariable().get().doubleValue();
      double lMarginTimeInMicroseconds = (lOverscan - 1) / (2 * lOverscan) * lExposureMeasureTimeInMicroseconds;
      final double lMarginTimeRelativeUnits = microsecondsToRelative(lExposureMeasureTimeInMicroseconds, lMarginTimeInMicroseconds);

      boolean lIsStepping = true;
      for (int i = 0; i < mLightSheetQueue.getNumberOfLaserDigitalControls(); i++)
        lIsStepping &= mLightSheetQueue.getSIPatternOnOffVariable(i).get();

      mExposureYStave.setStepping(lIsStepping);
      mExposureZStave.setStepping(lIsStepping);

      for (int i = 0; i < mLightSheetQueue.getNumberOfLaserDigitalControls(); i++)
      {
        final Variable<Boolean> lLaserBooleanVariable = mLightSheetQueue.getLaserOnOffArrayVariable(i);

        if (mLightSheetQueue.getSIPatternOnOffVariable(i).get())
        {

          final StructuredIlluminationPatternInterface lStructuredIlluminationPatternInterface = mLightSheetQueue.getSIPatternVariable(i).get();
          final StaveInterface lSIIlluminationLaserTriggerStave = lStructuredIlluminationPatternInterface.getStave(lMarginTimeRelativeUnits);
          lSIIlluminationLaserTriggerStave.setEnabled(lLaserBooleanVariable.get());

          setLaserDigitalTriggerStave(pExposureMeasure, i, lSIIlluminationLaserTriggerStave);
        } else
        {

          mNonSIIluminationLaserTriggerStaveArray[i].setEnabled(lLaserBooleanVariable.get());
          mNonSIIluminationLaserTriggerStaveArray[i].setStart((float) lMarginTimeRelativeUnits);
          mNonSIIluminationLaserTriggerStaveArray[i].setStop((float) (1.0f - lMarginTimeRelativeUnits));

          setLaserDigitalTriggerStave(pExposureMeasure, i, mNonSIIluminationLaserTriggerStaveArray[i]);

        }

      }

      double lPowerValue = getLightSheet().getPowerFunction().get().value(mLightSheetQueue.getPowerVariable().get().doubleValue());

      if (mLightSheetQueue.getAdaptPowerToWidthHeightVariable().get())
      {
        double lWidthPowerFactor = getLightSheet().getWidthPowerFunction().get().value(lWidthValue);

        double lHeightPowerFactor = getLightSheet().getHeightPowerFunction().get().value(lLightSheetHeight / lOverscan);/**/

        lPowerValue *= lWidthPowerFactor * lHeightPowerFactor;
      }

      mBeforeExposureLAStave.setValue((float) lPowerValue);
      mExposureLAStave.setValue((float) lPowerValue);

    }

  }

  private <O extends StaveInterface> void setLaserDigitalTriggerStave(Measure pExposureMeasure, int pLaserLineIndex, O pStave)
  {

    final int lLaserDigitalLineIndex = MachineConfiguration.get().getIntegerProperty("device.lsm.lightsheet." + getLightSheet().getName() + ".ld" + pLaserLineIndex + ".index", 8 + pLaserLineIndex);
    pExposureMeasure.setStave(lLaserDigitalLineIndex, pStave);
  }

  public long getBeforeExposureMeasureDuration(TimeUnit pTimeUnit)
  {
    return pTimeUnit.convert((long) (mLightSheetQueue.getReadoutTimeInMicrosecondsPerLineVariable().get().doubleValue() * mLightSheetQueue.getImageHeightVariable().get().doubleValue() / 2), TimeUnit.MICROSECONDS);
  }

  public long getExposureMeasureDuration(TimeUnit pTimeUnit)
  {
    return pTimeUnit.convert((long) (mLightSheetQueue.getEffectiveExposureInSecondsVariable().get().doubleValue() * 1e6), TimeUnit.MICROSECONDS);
  }

  public long getFinalMeasureDuration(TimeUnit pTimeUnit)
  {
    return pTimeUnit.convert((long) (mLightSheetQueue.getFinalisationTimeInSecondsVariable().get().doubleValue() * 1e6), TimeUnit.MICROSECONDS);
  }

  private double galvoRotateY(double pY, double pZ)
  {
    UnivariateAffineFunction lAlphaFunction = getLightSheet().getAlphaFunction().get();
    double lAlphaDegrees = mLightSheetQueue.getAlphaInDegreesVariable().get().doubleValue();
    final double lAlpha = Math.toRadians(lAlphaFunction.value(lAlphaDegrees));
    return pY * cos(lAlpha) - pZ * sin(lAlpha);
  }

  private double galvoRotateZ(double pY, double pZ)
  {
    UnivariateAffineFunction lAlphaFunction = getLightSheet().getAlphaFunction().get();

    double lAlphaDegrees = mLightSheetQueue.getAlphaInDegreesVariable().get().doubleValue();

    final double lAlpha = Math.toRadians(lAlphaFunction.value(lAlphaDegrees));
    return pY * sin(lAlpha) + pZ * cos(lAlpha);
  }

  private static double microsecondsToRelative(final double pTotalTime, final double pSubTime)
  {
    return pSubTime / pTotalTime;
  }

  public BezierStave getGalvoScannerStaveBeforeExposureZ()
  {
    return mBeforeExposureZStave;
  }

  public BezierStave getGalvoScannerStaveBeforeExposureY()
  {
    return mBeforeExposureYStave;
  }

  public ConstantStave getIllumPifocStaveBeforeExposureX()
  {
    return mBeforeExposureXStave;
  }

  public RampSteppingStave getGalvoScannerStaveExposureZ()
  {
    return mExposureZStave;
  }

  public RampSteppingStave getGalvoScannerStaveExposureY()
  {
    return mExposureYStave;
  }

  public ConstantStave getIllumPifocStaveExposureX()
  {
    return mExposureXStave;
  }

  public EdgeStave getTriggerOutStaveBeforeExposure()
  {
    return mBeforeExposureTStave;
  }

  public EdgeStave getTriggerOutStaveExposure()
  {
    return mExposureTStave;
  }

  public ConstantStave getLaserAnalogModulationBeforeExposure()
  {
    return mBeforeExposureLAStave;
  }

  public ConstantStave getLaserAnalogModulationExposure()
  {
    return mExposureLAStave;
  }

}
