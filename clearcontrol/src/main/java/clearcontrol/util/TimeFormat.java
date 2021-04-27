package clearcontrol.util;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class TimeFormat
{
  public static String humanReadableTime(long pPauseTimeInMilliseconds)
  {
    String lPauseTimeHumanReadable = "";
    if (pPauseTimeInMilliseconds == 0)
    {
    }
    else if (pPauseTimeInMilliseconds < 1000)
    {
      lPauseTimeHumanReadable = "" + pPauseTimeInMilliseconds + " msec";
    }
    else if (pPauseTimeInMilliseconds < 60000)
    {
      lPauseTimeHumanReadable = "" + (pPauseTimeInMilliseconds / 1000) + " sec";
    }
    else if (pPauseTimeInMilliseconds < 3600000)
    {
      lPauseTimeHumanReadable = "" + (pPauseTimeInMilliseconds / 60000) + " min";
    }
    else
    {
      lPauseTimeHumanReadable = "" + (pPauseTimeInMilliseconds / 3600000) + " h";
    }
    return lPauseTimeHumanReadable;
  }
}
