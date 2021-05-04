package clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.video.video2d.Stack2DDisplay;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * ViewStack2DInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 06 2018
 */
public class ViewStack2DInstruction<T extends StackInterfaceContainer> extends ViewStackInstructionBase<T> implements PropertyIOableInstructionInterface
{

  private BoundedVariable<Integer> mViewerIndexVariable = new BoundedVariable<Integer>("Viewer index", 0, 0, Integer.MAX_VALUE);

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pLightSheetMicroscope
   */
  public ViewStack2DInstruction(int pViewerIndex, Class<T> pTargetStackInterfaceContainerClass, LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Visualisation: View stack '" + pTargetStackInterfaceContainerClass.getSimpleName() + "' in 2D viewer", pTargetStackInterfaceContainerClass, pLightSheetMicroscope);

    mViewerIndexVariable.set(pViewerIndex);
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    DataWarehouse lDataWarehouse = getLightSheetMicroscope().getDataWarehouse();
    StackInterfaceContainer lContainer = lDataWarehouse.getOldestContainer(getStackInterfaceContainerClass());
    if (lContainer == null)
    {
      return false;
    }

    Stack2DDisplay lDisplay = (Stack2DDisplay) getLightSheetMicroscope().getDevice(Stack2DDisplay.class, mViewerIndexVariable.get());

    if (lDisplay == null)
    {
      return false;
    }

    lDisplay.getInputStackVariable().set(getImageFromContainer(lContainer));

    return true;
  }

  @Override
  public InstructionInterface copy()
  {
    return new ViewStack2DInstruction(mViewerIndexVariable.get(), getStackInterfaceContainerClass(), getLightSheetMicroscope());
  }

  public BoundedVariable<Integer> getViewerIndexVariable()
  {
    return mViewerIndexVariable;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]{getViewerIndexVariable(), getImageKeyToShowVariable()};
  }
}
