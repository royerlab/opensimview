package clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.gui.video.video3d.Stack3DDisplay;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * ViewStack3DInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public class ViewStack3DInstruction<T extends StackInterfaceContainer> extends
                                                                       ViewStackInstructionBase<T> implements
                                                                                                   LoggingFeature
{

  /**
   * Instanciates a virtual device with a given name
   */
  public ViewStack3DInstruction(Class<T> pContainerStackInterfaceClass,
                                LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Visualisation: View stack '"
          + pContainerStackInterfaceClass.getSimpleName()
          + "' in 3D", pContainerStackInterfaceClass, pLightSheetMicroscope);
  }

  @Override public boolean initialize()
  {
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    DataWarehouse lDataWarehouse = getLightSheetMicroscope().getDataWarehouse();
    T lContainer = lDataWarehouse.getOldestContainer(getStackInterfaceContainerClass());
    if (lContainer == null || !lContainer.isDataComplete())
    {
      return false;
    }

    Stack3DDisplay
        lDisplay =
        (Stack3DDisplay) getLightSheetMicroscope().getDevice(Stack3DDisplay.class, 0);
    if (lDisplay == null)
    {
      return false;
    }

    lDisplay.getInputStackVariable().set(getImageFromContainer(lContainer));

    return true;
  }

  @Override public ViewStack3DInstruction copy()
  {
    return new ViewStack3DInstruction(getStackInterfaceContainerClass(),
                                      getLightSheetMicroscope());
  }
}
