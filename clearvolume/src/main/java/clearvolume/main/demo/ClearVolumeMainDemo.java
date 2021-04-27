package clearvolume.main.demo;

import clearvolume.main.ClearVolumeMain;

import org.junit.Test;

public class ClearVolumeMainDemo
{

  @Test
  public void demoServer()
  {
    ClearVolumeMain.main(new String[]
    { "--demo-server" });
  }

  @Test
  public void demoClient() throws InterruptedException
  {
    ClearVolumeMain.main(new String[] {});
    Thread.sleep(10000000);
  }

}
