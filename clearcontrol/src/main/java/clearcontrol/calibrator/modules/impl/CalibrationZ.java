package clearcontrol.calibrator.modules.impl;

import clearcl.util.ElapsedTime;
import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.math.argmax.ArgMaxFinder1DInterface;
import clearcontrol.core.math.argmax.methods.ModeArgMaxFinder;
import clearcontrol.core.math.functions.UnivariateAffineFunction;
import clearcontrol.core.math.regression.linear.TheilSenEstimator;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface.ChartType;
import clearcontrol.ip.iqm.DCTS2D;
import clearcontrol.LightSheetMicroscopeQueue;
import clearcontrol.calibrator.CalibrationEngine;
import clearcontrol.calibrator.modules.CalibrationModuleInterface;
import clearcontrol.calibrator.modules.CalibrationPerLightSheetBase;
import clearcontrol.calibrator.utils.ImageAnalysisUtils;
import clearcontrol.component.detection.DetectionArmInterface;
import clearcontrol.component.lightsheet.LightSheetInterface;
import clearcontrol.configurationstate.ConfigurationState;
import clearcontrol.configurationstate.HasStateDescriptionPerLightSheet;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.sourcesink.sink.RawFileStackSink;
import gnu.trove.list.array.TDoubleArrayList;
import org.apache.commons.collections4.map.MultiKeyMap;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Math.*;

/**
 * Calibration module for the Z position of lightsheets and detection arms
 *
 * @author royer
 */
public class CalibrationZ extends CalibrationPerLightSheetBase implements CalibrationModuleInterface, HasStateDescriptionPerLightSheet
{

  private ArgMaxFinder1DInterface mArgMaxFinder;
  private MultiKeyMap<Integer, UnivariateAffineFunction> mModels;
  private int mNumberOfDetectionArmDevices;

  private boolean mUseDCTS = false;
  private DCTS2D mDCTS2D;
  private double[] mMetricArray;

  private BoundedVariable<Integer> mNumberOfISamples = new BoundedVariable<Integer>("Number of illumination samples", 13, 0, Integer.MAX_VALUE);
  private BoundedVariable<Integer> mNumberOfDSamples = new BoundedVariable<Integer>("Number of detection samples", 13, 0, Integer.MAX_VALUE);
  private BoundedVariable<Integer> mMaxIterationsVariable = new BoundedVariable<Integer>("Maximum number of iterations", 7, 0, Integer.MAX_VALUE);
  private BoundedVariable<Double> mMaxDeltaZ = new BoundedVariable<Double>("Maximum DeltaZ", 30.0, 0.0, Double.MAX_VALUE, 0.1);

  private BoundedVariable<Double> mExposureTimeInSecondsVariable = new BoundedVariable<Double>("Exposure time in seconds", 0.1, 0.0, Double.MAX_VALUE, 0.001);

  private BoundedVariable<Double> mLaserPowerVariable = new BoundedVariable<Double>("Laser Power", 0.1, 0.0, 1.0, 0.001);

  private Variable<Boolean> mFullRangeVariable = new Variable<Boolean>("Full range", false);

  private Variable<String> mDebugPath = new Variable<String>("Debug path", "");

  private BoundedVariable<Double> mStoppingConditionErrorThreshold = new BoundedVariable<Double>("Stopping condition error threshold", 0.02, 0.0, Double.MAX_VALUE, 0.001);

  /**
   * Instantiates a Z calibrator module given calibrator
   *
   * @param pCalibrator calibrator
   */
  public CalibrationZ(CalibrationEngine pCalibrator)
  {
    super("Z", pCalibrator);

    mNumberOfDetectionArmDevices = getLightSheetMicroscope().getDeviceLists().getNumberOfDevices(DetectionArmInterface.class);

    mModels = new MultiKeyMap<>();
  }

  /**
   * Calibrates the lightsheet and detection arm Z positions.
   *
   * @return true when succeeded
   */
  public double calibrateZ(int pLightSheetIndex)
  {
    int lIteration = 0;
    double lError = Double.POSITIVE_INFINITY;
    do
    {
      setConfigurationState(pLightSheetIndex, ConfigurationState.fromProgressValue((double) lIteration / mMaxIterationsVariable.get()));
      double lSearchAmplitude = 1.0 / (pow(2, 1 + lIteration));
      lError = calibrateZ(pLightSheetIndex, !mFullRangeVariable.get().booleanValue(), lSearchAmplitude, pLightSheetIndex == 0);
      info("############################################## Error = " + lError);

      if (getCalibrationEngine().isStopRequested())
      {
        setConfigurationState(pLightSheetIndex, ConfigurationState.CANCELLED);
        return Double.NaN;
      }
    } while (lError >= mStoppingConditionErrorThreshold.get() && lIteration++ < mMaxIterationsVariable.get());
    info("############################################## Done ");

    if (Double.isNaN(lError))
    {
      setConfigurationState(pLightSheetIndex, ConfigurationState.FAILED);
    } else if (lError < mStoppingConditionErrorThreshold.get())
    {
      setConfigurationState(pLightSheetIndex, ConfigurationState.SUCCEEDED);
    } else
    {
      setConfigurationState(pLightSheetIndex, ConfigurationState.ACCEPTABLE);
    }

    return lError;
  }

  /**
   * Calibrates the lightsheet and detection arms Z positions.
   *
   * @param pLightSheetIndex  lightsheet index
   * @param pRestrictedSearch true-> restrict search, false -> not
   * @param pSearchAmplitude  search amplitude (within [0,1])
   * @param pAdjustDetectionZ true -> adjust detection Z
   * @return true when succeeded
   */
  private double calibrateZ(int pLightSheetIndex, boolean pRestrictedSearch, double pSearchAmplitude, boolean pAdjustDetectionZ)
  {
    int lNumberOfISamples = mNumberOfISamples.get();
    int lNumberOfDSamples = mNumberOfDSamples.get();
    info("Starting to calibrate Z for lightsheet %d, with %d D samples, %d I samples, and a search amplitude of %g ", pLightSheetIndex, lNumberOfDSamples, lNumberOfISamples, pSearchAmplitude);

    mArgMaxFinder = new ModeArgMaxFinder();

    incrementIteration();

    final TheilSenEstimator[] lTheilSenEstimators = new TheilSenEstimator[mNumberOfDetectionArmDevices];

    for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
      lTheilSenEstimators[d] = new TheilSenEstimator();

    LightSheetInterface lLightSheetDevice = getLightSheetMicroscope().getDeviceLists().getDevice(LightSheetInterface.class, pLightSheetIndex);

    BoundedVariable<Number> lZVariable = lLightSheetDevice.getZVariable();
    double lMinIZ = lZVariable.getMin().doubleValue();
    double lMaxIZ = lZVariable.getMax().doubleValue();

    double lStepIZ = (lMaxIZ - lMinIZ) / (lNumberOfISamples - 1);

    double lMinDZ = Double.NEGATIVE_INFINITY;
    double lMaxDZ = Double.POSITIVE_INFINITY;

    double lDZSearchRadius = 0.5 * pSearchAmplitude * (lMaxIZ - lMinIZ);

    info("Range for Iz values: [%g,%g] with a step size of %g, Dz search radius is %g \n", lMinIZ, lMaxIZ, lStepIZ, lDZSearchRadius);

    for (double iz = lMinIZ; iz <= lMaxIZ; iz += lStepIZ)
    {

      final double lPerturbedIZ = iz + 0.1 * lStepIZ * (2 * Math.random() - 1);

      // TODO: this does not work when the calibration is really off:
      if (pRestrictedSearch)
      {
        lMinDZ = lPerturbedIZ - lDZSearchRadius;
        lMaxDZ = lPerturbedIZ + lDZSearchRadius;
      }

      final double[] dz = focusZ(pLightSheetIndex, lNumberOfDSamples, lMinDZ, lMaxDZ, lPerturbedIZ);

      if (dz == null)
      {
        setConfigurationState(pLightSheetIndex, ConfigurationState.FAILED);
        return Double.NaN;
      }

      String lChartName = this.getClass().getSimpleName() + " DZ v. IZ";

      String lSeriesName = "measured";

      getCalibrationEngine().configureChart(lChartName, lSeriesName, "DZ", "IZ", ChartType.Line);

      for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
      {
        if (!Double.isNaN(dz[d]))
        {
          if (dz[d] > 0.001 && Math.abs(dz[d] - lPerturbedIZ) < mMaxDeltaZ.get()) // this is a
          // workaround.
          // Too many
          // lightsheet
          // positions
          // resulted
          // in dz[d]
          // = 0 so
          // that the
          // fitting
          // doesn't
          // work
          // anymore
          {
            System.out.println("D" + d + " enter " + dz[d] + " / " + lPerturbedIZ);
            lTheilSenEstimators[d].enter(dz[d], lPerturbedIZ);

            getCalibrationEngine().addPoint(lChartName, lSeriesName, iz == lMinIZ,

                    dz[d], lPerturbedIZ);
          } else
          {
            System.out.println("D" + d + " ignore " + dz[d] + " / " + lPerturbedIZ);
          }
        }
      }

      if (getCalibrationEngine().isStopRequested())
      {
        setConfigurationState(pLightSheetIndex, ConfigurationState.CANCELLED);
        return Double.NaN;
      }

    }

    for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
    {
      final UnivariateAffineFunction lModel = lTheilSenEstimators[d].getModel();

      System.out.println("D" + d + " lModel=" + lModel);

      mModels.put(pLightSheetIndex, d, lTheilSenEstimators[d].getModel());

      BoundedVariable<Number> lDetectionFocusZVariable = getLightSheetMicroscope().getDeviceLists().getDevice(DetectionArmInterface.class, d).getZVariable();

      lMinDZ = lDetectionFocusZVariable.getMin().doubleValue();
      lMaxDZ = lDetectionFocusZVariable.getMax().doubleValue();
      double lStepDZ = (lMaxDZ - lMinDZ) / 1000;

      String lChartName = this.getClass().getSimpleName() + " DZ v. IZ";

      String lSeriesName = "fit";

      getCalibrationEngine().configureChart(lChartName, lSeriesName, "DZ", "IZ", ChartType.Line);

      for (double z = lMinDZ; z <= lMaxDZ; z += lStepDZ)
      {
        getCalibrationEngine().addPoint(lChartName, lSeriesName, z == lMinDZ, z, mModels.get(pLightSheetIndex, d).value(z));

      }

    }

    return apply(pLightSheetIndex, pAdjustDetectionZ);
  }

  private double[] focusZ(int pLightSheetIndex, int pNumberOfDSamples, double pMinDZ, double pMaxDZ, double pIZ)
  {

    try
    {

      double lMinDZ = pMinDZ;
      double lMaxDZ = pMaxDZ;

      for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
      {
        BoundedVariable<Number> lDetectionFocusZVariable = getLightSheetMicroscope().getDeviceLists().getDevice(DetectionArmInterface.class, d).getZVariable();

        lMinDZ = max(lMinDZ, lDetectionFocusZVariable.getMin().doubleValue());
        lMaxDZ = min(lMaxDZ, lDetectionFocusZVariable.getMax().doubleValue());
      }

      info("Focussing for lightsheet %d at %g, with %d D samples, with Dz values within [%g,%g] \n", pLightSheetIndex, pIZ, pNumberOfDSamples, lMinDZ, lMaxDZ);

      double lStep = (lMaxDZ - lMinDZ) / (pNumberOfDSamples - 1);

      // info("Begin building queue");
      LightSheetMicroscopeQueue lQueue = getLightSheetMicroscope().requestQueue();
      lQueue.clearQueue();
      // lQueue.zero();

      lQueue.setFullROI();
      lQueue.setExp(mExposureTimeInSecondsVariable.get());

      lQueue.setI(pLightSheetIndex);
      lQueue.setIX(pLightSheetIndex, 0);
      lQueue.setIY(pLightSheetIndex, 0);
      lQueue.setIZ(pLightSheetIndex, lMinDZ);
      lQueue.setIH(pLightSheetIndex, 0);
      lQueue.setIP(pLightSheetIndex, mLaserPowerVariable.get());

      final double[] dz = new double[mNumberOfDetectionArmDevices];

      final TDoubleArrayList lDZList = new TDoubleArrayList();

      for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
      {
        lQueue.setIZ(pLightSheetIndex, lMinDZ);
        lQueue.setDZ(d, lMinDZ);
        lQueue.setC(d, false);
      }
      lQueue.addCurrentStateToQueue();

      for (double z = lMinDZ; z <= lMaxDZ; z += lStep)
      {
        lDZList.add(z);

        for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
        {
          lQueue.setDZ(d, z);
          lQueue.setC(d, true);
        }

        lQueue.setIZ(pLightSheetIndex, pIZ);

        lQueue.addCurrentStateToQueue();
      }

      lQueue.addVoxelDimMetaData(getLightSheetMicroscope(), 10);

      for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
      {
        lQueue.setDZ(d, lMinDZ);
        lQueue.setC(d, false);
      }
      lQueue.addCurrentStateToQueue();

      lQueue.setTransitionTime(0.1);

      lQueue.finalizeQueue();
      // info("End building queue");

      /* ScoreVisualizerJFrame.visualize("queuedscore",
      																mLightSheetMicroscope.getDeviceLists()
      																											.getDevice(NIRIOSignalGenerator.class, 0)
      																											.get());/**/

      // info("Begin play queue");
      getLightSheetMicroscope().useRecycler("adaptation", 1, 4, 4);
      final Boolean lPlayQueueAndWait = getLightSheetMicroscope().playQueueAndWaitForStacks(lQueue, 100 + lQueue.getQueueLength(), TimeUnit.SECONDS);
      // info("End play queue");

      if (lPlayQueueAndWait)
      {
        for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
        {
          final OffHeapPlanarStack lStack = (OffHeapPlanarStack) getLightSheetMicroscope().getCameraStackVariable(d).get();

          if (lStack == null) continue;

          if (mDebugPath.get().length() > 0)
          {
            String timepoint = "" + System.currentTimeMillis();
            RawFileStackSink lRawFileStackSink = new RawFileStackSink();
            lRawFileStackSink.setLocation(new File(mDebugPath.get()), "tempZ" + timepoint);
            lRawFileStackSink.appendStack(lStack);
            lRawFileStackSink.close();
            info("Saved as " + timepoint);
          }

          // info("Begin compute metric");
          ElapsedTime.measureForceOutput("compute metric", () ->
          {
            if (mUseDCTS)
            {
              if (mDCTS2D == null) mDCTS2D = new DCTS2D();

              mMetricArray = mDCTS2D.computeImageQualityMetric(lStack);
            } else mMetricArray = ImageAnalysisUtils.smoothAndComputeSumPercentileMaxMultiplicationPerStack(lStack);
            //ImageAnalysisUtils.computeAverageSquareVariationPerPlane(lStack);/**/
          });
          // info("Begin compute metric");

          Double lArgMax = null;

          if (lDZList.size() != mMetricArray.length)
          {
            severe("Z position list and metric list have different lengths!");
          } else
          {
            // System.out.format("metric array: \n");

            String lChartName = String.format("D=%d, I=%d", d, pLightSheetIndex);

            String lSeriesName = String.format("iteration=%d", getIteration());

            getCalibrationEngine().configureChart(lChartName, lSeriesName, "ΔZ", "focus metric", ChartType.Line);

            for (int j = 0; j < lDZList.size(); j++)
            {
              getCalibrationEngine().addPoint(lChartName, lSeriesName, j == 0, lDZList.get(j), mMetricArray[j]);

              /*System.out.format("z=%s m=%s \n",
                              lDZList.get(j),
                              mMetricArray[j]);/**/

            }

            // info("Begin argmax");
            lArgMax = mArgMaxFinder.argmax(lDZList.toArray(), mMetricArray);
            // info("End argmax");
          }
          if (lArgMax != null)
          {
            TDoubleArrayList lDCTSList = new TDoubleArrayList(mMetricArray);

            double lAmplitudeRatio = (lDCTSList.max() - lDCTSList.min()) / lDCTSList.max();

            /*System.out.format("argmax=%s amplratio=%s \n",
                              lArgMax.toString(),
                              lAmplitudeRatio);/**/

            if (lAmplitudeRatio > 0.001)
            {
              if (lArgMax < lDZList.get(0)) dz[d] = lDZList.get(0);
              else if (lArgMax > lDZList.get(lDZList.size() - 1)) dz[d] = lDZList.get(lDZList.size() - 1);
              else dz[d] = lArgMax;
            } else dz[d] = Double.NaN;

            /*if (mArgMaxFinder instanceof Fitting1D)
            {
              Fitting1D lFitting1D = (Fitting1D) mArgMaxFinder;
            
              double[] lFit =
                            lFitting1D.fit(lDZList.toArray(),
                                           new double[lDZList.size()]);
            
              for (int j = 0; j < lDZList.size(); j++)
              {
                lPlot.setScatterPlot("fit");
                lPlot.addPoint("fit", lDZList.get(j), lFit[j]);
              }
            }/**/

          } else
          {
            dz[d] = Double.NaN;
            severe("Argmax is NULL!");
          }
        }
      }
      return dz;

    } catch (final InterruptedException e)
    {
      e.printStackTrace();
    } catch (final ExecutionException e)
    {
      e.printStackTrace();
    } catch (final TimeoutException e)
    {
      e.printStackTrace();
    } catch (IOException e)
    {
      e.printStackTrace();
    }

    return null;

  }

  /**
   * Applies correction for a given lightsheet index.
   *
   * @param pLightSheetIndex  lightsheet index
   * @param pAdjustDetectionZ this flag determines whther the coreection should be applied
   *                          to the detection arms too
   * @return calibration error
   */
  public double apply(int pLightSheetIndex, boolean pAdjustDetectionZ)
  {
    if (getCalibrationEngine().isStopRequested())
    {
      setConfigurationState(pLightSheetIndex, ConfigurationState.CANCELLED);
      return Double.NaN;
    }

    double lSlope = 0, lOffset = 0;

    for (int d = 0; d < mNumberOfDetectionArmDevices; d++)
    {
      lSlope += mModels.get(pLightSheetIndex, 0).getSlope();
      lOffset += mModels.get(pLightSheetIndex, 0).getConstant();
    }

    lSlope /= mNumberOfDetectionArmDevices;
    lOffset /= mNumberOfDetectionArmDevices;

    final LightSheetInterface lLightSheetDevice = getLightSheetMicroscope().getDeviceLists().getDevice(LightSheetInterface.class, pLightSheetIndex);

    /*System.out.println("before: getZFunction()="
                       + lLightSheetDevice.getZFunction());/**/

    if (abs(lSlope) > 0.00001 && !Double.isNaN(lSlope) && !Double.isNaN(lOffset))
    {
      lLightSheetDevice.getZFunction().get().composeWith(new UnivariateAffineFunction(lSlope, lOffset));
      lLightSheetDevice.getZFunction().setCurrent();
    } else
    {
      if (abs(lSlope) <= 0.00001) warning("slope too low: " + abs(lSlope));
      else warning("invalid slope or offset: (y= %g x + %g)", lSlope, lOffset);

    }

    /*
    System.out.println("after: getZFunction()="
                       + lLightSheetDevice.getZFunction());
    
    System.out.println("before: getYFunction()="
                       + lLightSheetDevice.getYFunction());/**/

    adjustYFunctionScale(lLightSheetDevice);

    if (mNumberOfDetectionArmDevices == 2 && pAdjustDetectionZ) applyDetectionZ(pLightSheetIndex);

    double lError = abs(1 - lSlope) + abs(lOffset);

    info("Error=" + lError);

    return lError;

  }

  protected void adjustYFunctionScale(final LightSheetInterface lLightSheetDevice)
  {
    MachineConfiguration lMachineConfiguration = MachineConfiguration.get();

    Double lXYRatio = lMachineConfiguration.getDoubleProperty("device.lsm.lighsheet.yzratio", 1.0);

    lLightSheetDevice.getYFunction().set(UnivariateAffineFunction.axplusb(lLightSheetDevice.getZFunction().get().getSlope() * lXYRatio, 0));
  }

  protected void applyDetectionZ(int pLightSheetIndex)
  {
    double a0 = mModels.get(pLightSheetIndex, 0).getSlope();
    double b0 = mModels.get(pLightSheetIndex, 0).getConstant();
    double a1 = mModels.get(pLightSheetIndex, 1).getSlope();
    double b1 = mModels.get(pLightSheetIndex, 1).getConstant();
    System.out.println("a0=" + a0);
    System.out.println("b0=" + b0);
    System.out.println("a1=" + a1);
    System.out.println("b1=" + b1);

    double lDZIntercept0 = -b0 / a0;
    double lDZIntercept1 = -b1 / a1;

    System.out.println("lDZIntercept0=" + lDZIntercept0);
    System.out.println("lDZIntercept1=" + lDZIntercept1);

    double lDesiredIntercept = 0.5 * (lDZIntercept0 + lDZIntercept1);

    System.out.println("lDesiredIntercept=" + lDesiredIntercept);

    double lInterceptCorrection0 = -(lDesiredIntercept - lDZIntercept0);
    double lInterceptCorrection1 = -(lDesiredIntercept - lDZIntercept1);

    System.out.println("lInterceptCorrection0=" + lInterceptCorrection0);
    System.out.println("lInterceptCorrection1=" + lInterceptCorrection1);

    final DetectionArmInterface lDetectionArmDevice0 = getLightSheetMicroscope().getDeviceLists().getDevice(DetectionArmInterface.class, 0);
    final DetectionArmInterface lDetectionArmDevice1 = getLightSheetMicroscope().getDeviceLists().getDevice(DetectionArmInterface.class, 1);

    System.out.println("Before: lDetectionArmDevice0.getDetectionFocusZFunction()=" + lDetectionArmDevice0.getZFunction());
    System.out.println("Before: lDetectionArmDevice1.getDetectionFocusZFunction()=" + lDetectionArmDevice1.getZFunction());

    UnivariateAffineFunction lFunction0 = lDetectionArmDevice0.getZFunction().get();
    UnivariateAffineFunction lFunction1 = lDetectionArmDevice1.getZFunction().get();

    lFunction0.composeWith(UnivariateAffineFunction.axplusb(1, lInterceptCorrection0));
    lFunction1.composeWith(UnivariateAffineFunction.axplusb(1, lInterceptCorrection1));

    lDetectionArmDevice0.getZFunction().setCurrent();
    lDetectionArmDevice1.getZFunction().setCurrent();

    System.out.println("After: lDetectionArmDevice0.getDetectionFocusZFunction()=" + lDetectionArmDevice0.getZFunction());
    System.out.println("After: lDetectionArmDevice1.getDetectionFocusZFunction()=" + lDetectionArmDevice1.getZFunction());
  }

  @Override
  public void reset()
  {
    super.reset();

    for (int lLightSheetIndex = 0; lLightSheetIndex < this.getLightSheetMicroscope().getNumberOfLightSheets(); lLightSheetIndex++)
    {
      setConfigurationState(lLightSheetIndex, ConfigurationState.UNINITIALIZED);
    }
  }

  public BoundedVariable<Double> getExposureTimeInSecondsVariable()
  {
    return mExposureTimeInSecondsVariable;
  }

  public BoundedVariable<Double> getLaserPowerVariable()
  {
    return mLaserPowerVariable;
  }

  public Variable<Boolean> getFullRangeVariable()
  {
    return mFullRangeVariable;
  }

  public BoundedVariable<Integer> getNumberOfISamples()
  {
    return mNumberOfISamples;
  }

  public BoundedVariable<Integer> getNumberOfDSamples()
  {
    return mNumberOfDSamples;
  }

  public BoundedVariable<Integer> getMaxIterationsVariable()
  {
    return mMaxIterationsVariable;
  }

  public BoundedVariable<Double> getStoppingConditionErrorThreshold()
  {
    return mStoppingConditionErrorThreshold;
  }

  @Override
  public String getStateDescription()
  {
    return "";
  }

  @Override
  public String getStateDescription(int pLightSheetIndex)
  {
    final LightSheetInterface lLightSheetDevice = getLightSheetMicroscope().getDeviceLists().getDevice(LightSheetInterface.class, pLightSheetIndex);

    UnivariateAffineFunction lUnivariateAffineFunction = lLightSheetDevice.getZFunction().get();

    return String.format("y = %.3f * x + %.3f", lUnivariateAffineFunction.getSlope(), lUnivariateAffineFunction.getConstant());
  }

  public BoundedVariable<Double> getMaxDeltaZ()
  {
    return mMaxDeltaZ;
  }

  public Variable<String> getDebugPath()
  {
    return mDebugPath;
  }
}
