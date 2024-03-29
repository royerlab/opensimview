package clearcontrol.core.math.argmax.methods;

import clearcontrol.core.math.argmax.ArgMaxFinder1DInterface;
import gnu.trove.list.array.TDoubleArrayList;

/**
 * Top-5 argmax finder.
 * <p>
 * keeps only the top 5 Y values and sends the corresponding (X,Y) pairs to a
 * delegated argmax finder
 *
 * @author royer
 */
public class Top5ArgMaxFinder implements ArgMaxFinder1DInterface
{

  private ArgMaxFinder1DInterface mArgMaxFinder1DInterface;

  /**
   * Instantiates a top-5 argmax finder given a delegated argmax finder.
   *
   * @param pArgMaxFinder1D delegated argmax finder
   */
  public Top5ArgMaxFinder(ArgMaxFinder1DInterface pArgMaxFinder1D)
  {
    super();
    mArgMaxFinder1DInterface = pArgMaxFinder1D;
  }

  @Override
  public Double argmax(double[] pX, double[] pY)
  {
    int lIndexMax = 0;
    double lMaxY = Double.NEGATIVE_INFINITY;

    final int lLength = pY.length;
    for (int i = 0; i < lLength; i++)
    {
      final double lY = pY[i];
      if (lY > lMaxY)
      {
        lIndexMax = i;
        lMaxY = lY;
      }
    }

    TDoubleArrayList lTop5X = new TDoubleArrayList();
    TDoubleArrayList lTop5Y = new TDoubleArrayList();

    lTop5X.add(pX[lIndexMax]);
    lTop5Y.add(pY[lIndexMax]);

    int lLeftToAdd = 4;

    int i = 1;
    while (lLeftToAdd > 0)
    {
      if (0 <= lIndexMax + i && lIndexMax + i < lLength - 1)
      {
        lTop5X.add(pX[lIndexMax + i]);
        lTop5Y.add(pY[lIndexMax + i]);
        lLeftToAdd--;
      }

      if (0 <= lIndexMax - i && lIndexMax - i < lLength - 1)
      {
        lTop5X.insert(0, pX[lIndexMax - i]);
        lTop5Y.insert(0, pY[lIndexMax - i]);
        lLeftToAdd--;
      }
    }

    double[] lTop5XArray = lTop5X.toArray();
    double[] lTop5YArray = lTop5Y.toArray();

    Double lArgmax = mArgMaxFinder1DInterface.argmax(lTop5XArray, lTop5YArray);

    return lArgmax;
  }

  @Override
  public String toString()
  {
    return String.format("Top5ArgMaxFinder [mArgMaxFinder1DInterface=%s]", mArgMaxFinder1DInterface);
  }

}
