package clearcontrol.microscope.lightsheet.state.io;

import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

/**
 * This class allows reading acquisition states from disc
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class InterpolatedAcquisitionStateReader
{
  private final File mSourceFile;
  private final InterpolatedAcquisitionState mTargetState;

  public InterpolatedAcquisitionStateReader(File pSourceFile, InterpolatedAcquisitionState pTargetState)
  {
    mSourceFile = pSourceFile;
    mTargetState = pTargetState;
  }

  public boolean read()
  {
    InterpolatedAcquisitionStateData lData;

    ObjectMapper lObjectMapper = new ObjectMapper();
    lObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    lObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try
    {
      lData = lObjectMapper.readValue(mSourceFile, InterpolatedAcquisitionStateData.class);
    } catch (IOException e)
    {
      e.printStackTrace();
      return false;
    }

    lData.copyTo(mTargetState);
    return true;
  }
}
