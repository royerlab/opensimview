package clearcontrol.stack.sourcesink;

import com.google.common.collect.Lists;

import java.util.ArrayList;

/**
 * Interface for stack sources and sinks.
 *
 * @author royer
 */
public interface StackSinkSourceInterface
{
  /**
   * Stacks sinks ans sources default channel
   */
  public static final String cDefaultChannel = "default";

  /**
   * Basename string for stacks written to files
   */
  public static final String cBasename = "%06d";

  /**
   * Filename suffix for stacks written to files
   */
  public static final String cDefaultFileExtension = ".raw";

  /**
   * Filename suffix for stacks written to files using blosc compression
   */
  public static final String cCompressedFileExtension = ".blc";

  /**
   * Format used to write the stacks filenames
   */
  public static final String cRawFormat = cBasename + cDefaultFileExtension;

  /**
   * Format used to write the stacks filenames
   */
  public static final String cCompressedFormat = cBasename + cCompressedFileExtension;

  /**
   * Returns the list of available channels for this source or sink
   *
   * @return list of channels
   */
  default ArrayList<String> getChannelList()
  {
    return Lists.newArrayList(cDefaultChannel);
  }

}
