package clearcontrol.state.io;

import clearcontrol.state.InterpolatedAcquisitionState;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

/**
 * This class allows writing acquisition states to disc
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class InterpolatedAcquisitionStateWriter
{
  private final File mTargetFile;
  private final InterpolatedAcquisitionState mSourceState;

  public InterpolatedAcquisitionStateWriter(File pSourceFile, InterpolatedAcquisitionState pSourceState)
  {
    mTargetFile = pSourceFile;
    mSourceState = pSourceState;
  }

  public boolean write()
  {
    InterpolatedAcquisitionStateData data = new InterpolatedAcquisitionStateData(mSourceState);

    ObjectMapper lObjectMapper = new ObjectMapper();
    lObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    lObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try
    {
      lObjectMapper.writeValue(mTargetFile, data);
    } catch (IOException e)
    {
      e.printStackTrace();
      return false;
    }
    return true;
  }
}
