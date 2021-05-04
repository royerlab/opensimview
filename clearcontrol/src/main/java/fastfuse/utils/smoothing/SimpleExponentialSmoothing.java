package fastfuse.utils.smoothing;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class SimpleExponentialSmoothing implements OnlineSmoothingFilter<double[]>
{
  private final int mSize;
  private double mAlpha;
  private RealVector mCurrentValue;
  private int mCount;

  public SimpleExponentialSmoothing(int pSize, double pAlpha)
  {
    mSize = pSize;
    setAlpha(pAlpha);
    reset();
  }

  public int getSize()
  {
    return mSize;
  }

  public double getAlpha()
  {
    return mAlpha;
  }

  public void setAlpha(double pAlpha)
  {
    // assert 0 <= pAlpha && pAlpha <= 1;
    mAlpha = Math.min(Math.max(0, pAlpha), 1);
  }

  @Override
  public void reset()
  {
    mCount = 0;
    mCurrentValue = null;
  }

  @Override
  public int getCount()
  {
    return mCount;
  }

  @Override
  public double[] getCurrent()
  {
    return mCurrentValue == null ? null : mCurrentValue.toArray();
  }

  @Override
  public double[] update(double[] pValue)
  {
    assert pValue.length == mSize;
    mCount++;
    ArrayRealVector lNewValue = new ArrayRealVector(pValue);
    if (mCount == 1)
    {
      mCurrentValue = lNewValue;
    } else
    {
      mCurrentValue.combineToSelf(1 - mAlpha, mAlpha, lNewValue);
    }
    return getCurrent();
  }

}
