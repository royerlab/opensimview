package clearcontrol.microscope.lightsheet.postprocessing.measurements;

import clearcontrol.ip.iqm.DCTS2D;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.StackInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) October
 * 2017
 */
public class DiscreteConsinusTransformEntropyPerSliceEstimator
{
  StackInterface mStack;
  double[] mEntropyArray = null;

  public DiscreteConsinusTransformEntropyPerSliceEstimator(StackInterface stack)
  {
    mStack = stack;
  }

  public double[] getQualityArray()
  {
    if (mEntropyArray == null)
    {
      calculate();
    }
    return mEntropyArray;
  }

  private synchronized void calculate()
  {
    DCTS2D lDCTS2D = new DCTS2D();

    mEntropyArray = lDCTS2D.computeImageQualityMetric((OffHeapPlanarStack) mStack);
  }

}
