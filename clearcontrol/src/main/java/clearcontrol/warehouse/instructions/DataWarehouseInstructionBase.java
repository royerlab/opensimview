package clearcontrol.warehouse.instructions;

import clearcontrol.instructions.InstructionBase;
import clearcontrol.warehouse.DataWarehouse;

/**
 * DataWarehouseInstructionBase
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public abstract class DataWarehouseInstructionBase extends InstructionBase
{

  private final DataWarehouse mDataWarehouse;

  public DataWarehouseInstructionBase(String pDeviceName, DataWarehouse pDataWarehouse)
  {
    super(pDeviceName);
    mDataWarehouse = pDataWarehouse;
  }

  public DataWarehouse getDataWarehouse()
  {
    return mDataWarehouse;
  }
}
