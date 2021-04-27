package clearcontrol.microscope.lightsheet.component.lightsheet.demo;

import clearcontrol.devices.signalgen.staves.IntervalStave;
import clearcontrol.devices.signalgen.staves.StaveInterface;
import clearcontrol.devices.signalgen.staves.SteppingFunction;
import clearcontrol.microscope.lightsheet.component.lightsheet.si.ClosureStructuredIlluminationPattern;
import clearcontrol.microscope.lightsheet.component.lightsheet.si.StructuredIlluminationPatternInterface;
import org.junit.Test;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) January
 * 2018
 */
public class StructuredIlluminationDemo
{

  @Test public void demoStructuredIlluminationPatternInterface()
  {
    StructuredIlluminationPatternInterface
        lStructuredIlluminationPatternInterface =
        new ClosureStructuredIlluminationPattern(new SteppingFunction()
        {
          @Override public float function(int pIndex)
          {
            return pIndex % 2;
          }
        }, 10);

    // new BinaryStructuredIlluminationPattern();

    StaveInterface stave = lStructuredIlluminationPatternInterface.getStave(0.1);

    for (float d = 0; d < 1; d += 0.05)
    {
      System.out.println("si[" + d + "] = " + stave.getValue(d));
    }
  }

  @Test public void demoStave()
  {
    StaveInterface stave = new IntervalStave("trigger.out", 0, 1, 1, 0);

    for (float d = 0; d < 1; d += 0.05)
    {
      System.out.println("si[" + d + "] = " + stave.getValue(d));
    }

  }
}
