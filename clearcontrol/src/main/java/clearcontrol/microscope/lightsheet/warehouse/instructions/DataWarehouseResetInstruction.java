package clearcontrol.microscope.lightsheet.warehouse.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;

/**
 * This instructions recycles or disposes all DataContainers in the DataWarehouse.
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class DataWarehouseResetInstruction extends DataWarehouseInstructionBase implements InstructionInterface, LoggingFeature
{
  /**
   * INstanciates a virtual device with a given name
   */
  public DataWarehouseResetInstruction(DataWarehouse pDataWarehouse)
  {
    super("Memory: Reset memory", pDataWarehouse);
  }

  @Override
  public boolean initialize()
  {
    return false;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    getDataWarehouse().clear();
    return true;
  }

  @Override
  public DataWarehouseResetInstruction copy()
  {
    return new DataWarehouseResetInstruction(getDataWarehouse());
  }
}
