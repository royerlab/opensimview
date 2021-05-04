package clearcontrol.warehouse.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.warehouse.DataWarehouse;
import clearcontrol.warehouse.containers.DataContainerInterface;

/**
 * DropAllContainersOfTypeInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class DropAllContainersOfTypeInstruction extends DataWarehouseInstructionBase implements InstructionInterface, LoggingFeature
{
  Class mContainerClassToDrop;

  /**
   * INstanciates a virtual device with a given name
   */
  public DropAllContainersOfTypeInstruction(Class pContainerClassToDrop, DataWarehouse pDataWarehouse)
  {
    super("Memory: Recycle all containers of type " + pContainerClassToDrop.getSimpleName(), pDataWarehouse);
    mContainerClassToDrop = pContainerClassToDrop;
  }

  @Override
  public boolean initialize()
  {
    return false;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    DataWarehouse lWarehouse = getDataWarehouse();
    while (true)
    {
      DataContainerInterface lContainer = lWarehouse.getOldestContainer(mContainerClassToDrop);
      if (lContainer == null)
      {
        break;
      }
      lWarehouse.disposeContainer(lContainer);
    }
    return true;
  }

  @Override
  public DropAllContainersOfTypeInstruction copy()
  {
    return new DropAllContainersOfTypeInstruction(mContainerClassToDrop, getDataWarehouse());
  }
}
