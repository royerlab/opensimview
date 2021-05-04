package clearcontrol.core.math.argmax.methods;

import clearcontrol.core.math.argmax.*;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

/**
 * Loess fit argmax filter.
 * <p>
 * Fits a Loess interpolator and returns the x position of the maxima as argmax.
 *
 * @author royer
 */
public class LoessFitArgMaxFinder extends Fitting1DBase implements ArgMaxFinder1DInterface, Fitting1DInterface
{
  private static final int cNumberOfSamples = 1024;

  private PolynomialSplineFunction mPolynomialSplineFunction;

  @Override
  public Double argmax(double[] pX, double[] pY)
  {
    if (mPolynomialSplineFunction == null) fit(pX, pY);

    double lArgMax = UnivariateFunctionArgMax.argmax(pX, mPolynomialSplineFunction, cNumberOfSamples);

    mPolynomialSplineFunction = null;
    return lArgMax;
  }

  @Override
  public double[] fit(double[] pX, double[] pY)
  {

    LoessInterpolator lLoessInterpolator = new LoessInterpolator(2.0 / pX.length + 0.1, LoessInterpolator.DEFAULT_ROBUSTNESS_ITERS);

    mPolynomialSplineFunction = lLoessInterpolator.interpolate(pX, pY);

    double[] lFittedY = new double[pY.length];

    for (int i = 0; i < pX.length; i++)
      lFittedY[i] = mPolynomialSplineFunction.value(pX[i]);

    mRMSD = ComputeFitError.rmsd(pY, lFittedY);

    return lFittedY;
  }

  @Override
  public String toString()
  {
    return String.format("LoessFitArgMaxFinder [mPolynomialSplineFunction=%s]", mPolynomialSplineFunction);
  }

}
