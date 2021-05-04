package clearcontrol.devices.optomech.filterwheels.devices.ludl.demo;

import clearcontrol.core.variable.Variable;
import clearcontrol.devices.optomech.filterwheels.devices.ludl.LudlFilterWheelDevice;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;


/**
 * FLI filter wheel demo
 *
 * @author royer
 */
public class LudlFilterWheelDemo
{

  /**
   * Demo
   *
   * @throws InterruptedException NA
   */
  @Test
  public void test() throws InterruptedException
  {
    final LudlFilterWheelDevice lLudlFilterWheelDevice = new LudlFilterWheelDevice("COM31");

    assertTrue(lLudlFilterWheelDevice.open());

    final Variable<Integer> lPositionVariable = lLudlFilterWheelDevice.getPositionVariable();
    final Variable<Integer> lSpeedVariable = lLudlFilterWheelDevice.getSpeedVariable();

    for (int j = 0; j < 1; j++)
      for (int i = 0; i < 17; i++)
      {
        int lTargetPosition = (2 * i) % 6;
        lPositionVariable.set(lTargetPosition);
        lSpeedVariable.set((i / 30));
        Thread.sleep(30);
        int lCurrentPosition = lPositionVariable.get();
        System.out.format("i=%d, tp=%d, cp=%d\n", i, lTargetPosition, lCurrentPosition);
        assert lCurrentPosition == lTargetPosition;
      }

    lPositionVariable.set(0);

    assertTrue(lLudlFilterWheelDevice.close());

  }

}
