package clearvolume.main.test;

import clearvolume.exceptions.UnsupportedArchitectureException;
import clearvolume.main.CheckRequirements;

import org.junit.Test;

public class CheckRequirementsTests
{

  @Test
  public void test() throws UnsupportedArchitectureException
  {
    CheckRequirements.check();
  }

}
