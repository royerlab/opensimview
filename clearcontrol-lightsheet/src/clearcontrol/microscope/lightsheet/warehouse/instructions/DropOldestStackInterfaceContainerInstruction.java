package clearcontrol.microscope.lightsheet.warehouse.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * This instructions takes the oldest StackInterfaceContainer from the DataWarehouse and
 * recycles it. Its memory is freed then.
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class DropOldestStackInterfaceContainerInstruction extends
                                                          DataWarehouseInstructionBase implements
                                                                                       InstructionInterface,
                                                                                       LoggingFeature
{
  Class mContainerClassToDrop;

  /**
   * INstanciates a virtual device with a given name
   */
  public DropOldestStackInterfaceContainerInstruction(Class pContainerClassToDrop,
                                                      DataWarehouse pDataWarehouse)
  {
    super("Memory: Recycle container of type " + pContainerClassToDrop.getSimpleName(),
          pDataWarehouse);
    mContainerClassToDrop = pContainerClassToDrop;
  }

  @Override public boolean initialize()
  {
    return false;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    DataWarehouse lWarehouse = getDataWarehouse();
    StackInterfaceContainer
        lContainer =
        lWarehouse.getOldestContainer(mContainerClassToDrop);
    lWarehouse.disposeContainer(lContainer);
    return true;
  }

  @Override public DropOldestStackInterfaceContainerInstruction copy()
  {
    return new DropOldestStackInterfaceContainerInstruction(mContainerClassToDrop,
                                                            getDataWarehouse());
  }
}
