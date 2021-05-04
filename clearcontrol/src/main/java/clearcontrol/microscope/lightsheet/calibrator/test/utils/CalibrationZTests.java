package clearcontrol.microscope.lightsheet.calibrator.test.utils;

import clearcontrol.microscope.lightsheet.calibrator.utils.ImageAnalysisUtils;
import clearcontrol.stack.OffHeapPlanarStack;
import coremem.ContiguousMemoryInterface;
import coremem.buffers.ContiguousBuffer;
import coremem.fragmented.FragmentedMemoryInterface;
import coremem.offheap.OffHeapMemory;
import org.junit.Test;

import java.util.stream.IntStream;

import static junit.framework.Assert.assertEquals;


public class CalibrationZTests
{
  @Test
  public void TestNewCalibrationScoringWithGeneratedStacks()
  {
    // Create a metric array to store score results and get the generated data
    double[] mMetricArray;
    final OffHeapPlanarStack lStack = GenerateData();

    // Function to test/demonstrate
    mMetricArray = ImageAnalysisUtils.smoothAndComputeSumPercentileMaxMultiplicationPerStack(lStack);

    // Run over metric array to find the index of maximum scoring frame
    int index = 0;
    double lCurrentMax = -1.0;

    System.out.println("mMetricArray: ");
    for (int i = 0; i < mMetricArray.length; i++)
    {
      System.out.println(mMetricArray[i]);
      if (mMetricArray[i] > lCurrentMax)
      {
        lCurrentMax = mMetricArray[i];
        index = i;
      }
    }

    assertEquals((lStack.getDepth() - 1) / 2, index); // frame in the middle must be the well focused one
  }

  private OffHeapPlanarStack GenerateData()
  {
    ContiguousMemoryInterface lContiguousMemory = OffHeapMemory.allocateFloats(2048 * 2048 * 7);
    OffHeapPlanarStack lStack = OffHeapPlanarStack.createStack(lContiguousMemory, 2048, 2048, 7);
    FragmentedMemoryInterface lFragmentedMemory = lStack.getFragmentedMemory();

    IntStream.range(0, (int) lStack.getDepth()).forEach((p) ->
    {
      ContiguousMemoryInterface lContiguousMemoryInterface = lFragmentedMemory.get(p);
      ContiguousBuffer lBuffer = ContiguousBuffer.wrap(lContiguousMemoryInterface);

      for (int y = 0; y < lStack.getHeight(); y++)
      {
        // Formula below is only valid for stacks with shape (121, 2048, 2048)
        double exp = Math.exp((y - 1024) * (y - 1024) / (-10000 * (p - 3.1) * (p - 3.1)));
        float value = (float) (exp / Math.abs(p - 3.1));
        for (int x = 0; x < lStack.getWidth(); x++)
          if (lBuffer.hasRemainingFloat())
          {
            float noise = (float) (Math.sin(x) * 10000 + Math.cos(y) * 15243);
            value += (noise - Math.floor(noise)) / 10.0f;
            lBuffer.writeFloat(value);
          } else System.out.println(x + " " + y);
      }


    });

    return lStack;
  }
}
