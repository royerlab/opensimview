package clearcontrol.microscope.lightsheet.warehouse.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DataWarehouseResetInstruction;
import javafx.scene.control.Button;

/**
 * DataWarehouseResetInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class DataWarehouseResetInstructionPanel extends CustomGridPane
{

  public DataWarehouseResetInstructionPanel(DataWarehouseResetInstruction pInstruction)
  {
    Button lResetButton = new Button("Reset");
    lResetButton.setOnAction((e) ->
    {
      pInstruction.enqueue(-1);
    });
    add(lResetButton, 0, 0);
  }
}
