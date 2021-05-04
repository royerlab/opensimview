package clearcontrol.adaptive.instructions;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.state.InterpolatedAcquisitionState;

/**
 * ChangeImageSizeInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 09 2018
 */
public class ChangeImageSizeInstruction extends LightSheetMicroscopeInstructionBase implements PropertyIOableInstructionInterface
{

  private BoundedVariable<Integer> imageWidth = new BoundedVariable<Integer>("Width in pixels", 1024, 1, Integer.MAX_VALUE);
  private BoundedVariable<Integer> imageHeight = new BoundedVariable<Integer>("Height in pixels", 2048, 1, Integer.MAX_VALUE);

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pLightSheetMicroscope
   */
  public ChangeImageSizeInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Adaptation: Change image size", pLightSheetMicroscope);
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    InterpolatedAcquisitionState state = (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState();
    state.getImageWidthVariable().set(imageWidth.get());
    state.getImageHeightVariable().set(imageHeight.get());
    return true;
  }

  @Override
  public ChangeImageSizeInstruction copy()
  {
    ChangeImageSizeInstruction copied = new ChangeImageSizeInstruction(getLightSheetMicroscope());
    copied.imageWidth.set(imageWidth.get());
    copied.imageHeight.set(imageHeight.get());
    return copied;
  }

  public BoundedVariable<Integer> getImageWidth()
  {
    return imageWidth;
  }

  public BoundedVariable<Integer> getImageHeight()
  {
    return imageHeight;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]{getImageWidth(), getImageHeight()};
  }
}
