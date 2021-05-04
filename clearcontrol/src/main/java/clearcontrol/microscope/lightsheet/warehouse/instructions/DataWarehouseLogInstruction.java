package clearcontrol.microscope.lightsheet.warehouse.instructions;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;

/**
 * DataWarehouseLogInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class DataWarehouseLogInstruction extends DataWarehouseInstructionBase
{
  private DataWarehouse mDataWarehouse;
  private LightSheetTimelapse mTimelapse;
  private final LightSheetMicroscope mLightSheetMicroscope;

  public DataWarehouseLogInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("IO: Log content of the DataWarehouse", pLightSheetMicroscope.getDataWarehouse());
    mLightSheetMicroscope = pLightSheetMicroscope;
  }

  @Override
  public boolean initialize()
  {

    mDataWarehouse = mLightSheetMicroscope.getDataWarehouse();
    mTimelapse = mLightSheetMicroscope.getTimelapse();
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    return false;
  }

  @Override
  public DataWarehouseLogInstruction copy()
  {
    return new DataWarehouseLogInstruction(mLightSheetMicroscope);
  }
}
