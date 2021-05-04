package clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions;

import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;

/**
 * ViewStackInstructionBase
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public abstract class ViewStackInstructionBase<T extends StackInterfaceContainer> extends LightSheetMicroscopeInstructionBase implements PropertyIOableInstructionInterface
{

  private Class mStackInterfaceContainerClass;

  /**
   * INstanciates a virtual device with a given name
   */
  public ViewStackInstructionBase(String pName, Class<T> pContainerStackInterfaceClass, LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pName, pLightSheetMicroscope);
    mStackInterfaceContainerClass = pContainerStackInterfaceClass;
  }

  private Variable<String> mImageContainerKeyMustContain = new Variable<String>("Image key", "");

  protected StackInterface getImageFromContainer(StackInterfaceContainer lContainer)
  {
    String lImageContainerKey = lContainer.getKeyContainingString(mImageContainerKeyMustContain.get().toLowerCase());
    if (lImageContainerKey == null)
    {
      lImageContainerKey = lContainer.keySet().iterator().next();
    }
    return lContainer.get(lImageContainerKey);
  }

  protected Class<T> getStackInterfaceContainerClass()
  {
    return mStackInterfaceContainerClass;
  }

  public Variable<String> getImageKeyToShowVariable()
  {
    return mImageContainerKeyMustContain;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]{getImageKeyToShowVariable()};
  }

}
