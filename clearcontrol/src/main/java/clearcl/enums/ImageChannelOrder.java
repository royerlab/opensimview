package clearcl.enums;

/**
 * OpenCl image channel order.
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public enum ImageChannelOrder
{

  Intensity(1), Luminance(1), R(1), A(1), RG(2), RA(2), RGB(3), RGBA(4), ARGB(4), BGRA(4);

  private final int mNumberOfChannels;

  private ImageChannelOrder(int pNumberOfChannels)
  {
    mNumberOfChannels = pNumberOfChannels;
  }

  /**
   * Returns the number of channels
   *
   * @return number of channels
   */
  public int getNumberOfChannels()
  {
    return mNumberOfChannels;
  }

}
