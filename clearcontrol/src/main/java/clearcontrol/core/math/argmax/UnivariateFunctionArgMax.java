package clearcontrol.core.math.argmax;

import clearcontrol.core.math.argmax.methods.ModeArgMaxFinder;
import gnu.trove.list.array.TDoubleArrayList;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.rank.Median;

/**
 * Univariate function argmax.
 * <p>
 * Utility class offering static methods to compute the mode-based argmax of
 * univariate functions.
 *
 * @author royer
 */
public class UnivariateFunctionArgMax
{

  private static ModeArgMaxFinder sModeArgMaxFinder = new ModeArgMaxFinder();

  /**
   * Returns the argmax for a given range of x value and a univariate function.
   *
   * @param pX                  List of x values, only the first and last are considered and used
   *                            to define a range [xmin,xmax].
   * @param pUnivariateFunction univariate function
   * @param pNumberOfSamples    Number of samples to compute within [xmin,xmax]
   * @return argmax
   */
  public static double argmax(double[] pX, UnivariateFunction pUnivariateFunction, int pNumberOfSamples)
  {
    double lMinX = pX[0];
    double lMaxX = pX[pX.length - 1];

    TDoubleArrayList lXList = new TDoubleArrayList();
    TDoubleArrayList lYList = new TDoubleArrayList();

    for (int i = 0; i < pNumberOfSamples; i++)
    {
      double lX = lMinX + (lMaxX - lMinX) * ((1.0 * i) / pNumberOfSamples);
      double lY = pUnivariateFunction.value(lX);

      lXList.add(lX);
      lYList.add(lY);
    }

    double lArgMax = sModeArgMaxFinder.argmax(lXList.toArray(), lYList.toArray());
    return lArgMax;
  }

  /**
   * Returns the median argmax for a given range of x value and a list of
   * univariate functions.
   *
   * @param pX                   List of x values, only the first and last are considered and used
   *                             to define a range [xmin,xmax].
   * @param pUnivariateFunctions list of univariate functions
   * @param pNumberOfSamples     Number of samples to compute within [xmin,xmax]
   * @return median of argmax values
   */
  public static double argmaxmedian(double[] pX, UnivariateFunction[] pUnivariateFunctions, int pNumberOfSamples)
  {
    Median lMedian = new Median();
    TDoubleArrayList lYValues = new TDoubleArrayList();

    double lMinX = pX[0];
    double lMaxX = pX[pX.length - 1];

    TDoubleArrayList lXList = new TDoubleArrayList();
    TDoubleArrayList lYList = new TDoubleArrayList();

    for (int i = 0; i < pNumberOfSamples; i++)
    {
      double lX = lMinX + (lMaxX - lMinX) * ((1.0 * i) / pNumberOfSamples);
      double lY = 0;

      lYValues.clear();
      for (UnivariateFunction lUnivariateFunction : pUnivariateFunctions)
        if (lUnivariateFunction != null) try
        {
          lYValues.add(lUnivariateFunction.value(lX));
        } catch (OutOfRangeException e)
        {

        }

      lY = lMedian.evaluate(lYValues.toArray());

      lXList.add(lX);
      lYList.add(lY);
    }

    Double lArgMax = sModeArgMaxFinder.argmax(lXList.toArray(), lYList.toArray());
    return lArgMax;
  }

  /**
   * Returns the mean argmax for a given range of x value and a list of
   * univariate functions.
   *
   * @param pX                   List of x values, only the first and last are considered and used
   *                             to define a range [xmin,xmax].
   * @param pUnivariateFunctions list of univariate functions
   * @param pNumberOfSamples     Number of samples to compute within [xmin,xmax]
   * @return mean of argmax values
   */
  public static double argmaxmean(double[] pX, UnivariateFunction[] pUnivariateFunctions, int pNumberOfSamples)
  {
    Mean lMean = new Mean();
    TDoubleArrayList lYValues = new TDoubleArrayList();

    double lMinX = pX[0];
    double lMaxX = pX[pX.length - 1];

    TDoubleArrayList lXList = new TDoubleArrayList();
    TDoubleArrayList lYList = new TDoubleArrayList();

    for (int i = 0; i < pNumberOfSamples; i++)
    {
      double lX = lMinX + (lMaxX - lMinX) * ((1.0 * i) / pNumberOfSamples);
      double lY = 0;

      lYValues.clear();
      for (UnivariateFunction lUnivariateFunction : pUnivariateFunctions)
        if (lUnivariateFunction != null) try
        {
          lYValues.add(lUnivariateFunction.value(lX));
        } catch (OutOfRangeException e)
        {

        }

      lY = lMean.evaluate(lYValues.toArray());

      lXList.add(lX);
      lYList.add(lY);
    }

    Double lArgMax = sModeArgMaxFinder.argmax(lXList.toArray(), lYList.toArray());
    return lArgMax;
  }

}
