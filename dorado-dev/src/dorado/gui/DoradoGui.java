package dorado.gui;

import clearcontrol.devices.optomech.filterwheels.instructions.FilterWheelInstruction;
import clearcontrol.microscope.gui.halcyon.MicroscopeNodeType;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.gui.LightSheetMicroscopeGUI;
import javafx.stage.Stage;
import dorado.adaptive.AdaptiveZInstruction;
import dorado.adaptive.gui.AdaptiveZSchedulerPanel;

/**
 * XWing microscope GUI
 *
 * @author royer
 */
public class DoradoGui extends LightSheetMicroscopeGUI
{

  /**
   * Instantiates XWing microscope GUI
   * 
   * @param pLightSheetMicroscope
   *          microscope
   * @param pPrimaryStage
   *          JFX primary stage
   * @param p2DDisplay
   *          2D display
   * @param p3DDisplay
   *          3D display
   */
  public DoradoGui(LightSheetMicroscope pLightSheetMicroscope,
                   Stage pPrimaryStage,
                   boolean p2DDisplay,
                   boolean p3DDisplay)
  {
    super(pLightSheetMicroscope,
          pPrimaryStage,
          p2DDisplay,
          p3DDisplay);

    addGroovyScripting("lsm");
    addJythonScripting("lsm");

    addPanelMappingEntry(AdaptiveZInstruction.class,
            AdaptiveZSchedulerPanel.class,
            MicroscopeNodeType.AdaptiveOptics);


  }

}
