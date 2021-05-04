package clearcontrol.configurationstate;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public enum ConfigurationState
{
  UNINITIALIZED("Uninitialized", "#878585"), SUCCEEDED("Succeeded", "#3ed860"), ACCEPTABLE("Acceptable", "#84c600"), FAILED("Failed", "#ff7c4c"), PERCENT0("Initialising", "#878585"), PERCENT25("25%", "#9cafbf"), PERCENT50("50%", "#acd1ef"), PERCENT75("75%", "#97c6ed"), PERCENT100("Finishing", "#49b0ff"), CANCELLED("Cancelled", "#eda807"), INTERRUPTED("Interrupted", "#eda807");

  private String mColor;
  private String mTitle;

  ConfigurationState(String pTitle, String pColor)
  {
    mColor = pColor;
    mTitle = pTitle;
  }

  public String getColor()
  {
    return mColor;
  }

  public String toString()
  {
    return mTitle;
  }

  public static ConfigurationState fromProgressValue(double value)
  {
    if (value < 0.25)
    {
      return ConfigurationState.PERCENT0;
    }
    if (value < 0.5)
    {
      return ConfigurationState.PERCENT25;
    }
    if (value < 0.75)
    {
      return ConfigurationState.PERCENT50;
    }
    if (value < 0.99)
    {
      return ConfigurationState.PERCENT75;
    }
    return ConfigurationState.PERCENT100;
  }
}
