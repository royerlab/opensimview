package clearcontrol.microscope.lightsheet.warehouse.containers;

/**
 * The TextContainer allows storing text in the data warehouse.
 * <p>
 * Author: @haesleinhuepf September 2018
 */
public class TextContainer extends DataContainerBase
{
  String text;

  protected TextContainer(long pTimepoint, String text)
  {
    super(pTimepoint);
    this.text = text;
  }

  public String getText()
  {
    return text;
  }

  @Override
  public boolean isDataComplete()
  {
    return true;
  }

  @Override
  public void dispose()
  {

  }
}
