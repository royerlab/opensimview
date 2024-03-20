package clearcontrol.devices.optomech.filterwheels.devices.sutter.demo;

import clearcontrol.core.variable.Variable;
import clearcontrol.devices.optomech.filterwheels.devices.sutter.SutterFilterWheelDevice;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;


/**
 * Sutter filter wheel demo
 *
 * @author royer
 */
public class SutterFilterWheelDemo
{

  /**
   * Demo
   *
   * @throws InterruptedException NA
   */
  @Test
  public void test() throws InterruptedException
  {
    final SutterFilterWheelDevice lSutterFilterWheelDevice = new SutterFilterWheelDevice("COM8");

    assertTrue(lSutterFilterWheelDevice.open());

    final Variable<Integer> lPositionVariable = lSutterFilterWheelDevice.getPositionVariable();
    final Variable<Integer> lSpeedVariable = lSutterFilterWheelDevice.getSpeedVariable();

    lSpeedVariable.set(5);
    lPositionVariable.set(0);
    Thread.sleep(5000);
    lPositionVariable.set(1);
    Thread.sleep(5000);

    for (int i = 0; i < 25; i++)
    {
      int lTargetPosition = (i*5) % 9;
      lPositionVariable.set(lTargetPosition);

      Thread.sleep(1000);
    }

    lPositionVariable.set(0);

    assertTrue(lSutterFilterWheelDevice.close());

  }

}
