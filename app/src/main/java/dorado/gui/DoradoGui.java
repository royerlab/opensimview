package dorado.gui;

import clearcontrol.gui.halcyon.MicroscopeNodeType;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.gui.LightSheetMicroscopeGUI;
import dorado.adaptive.AdaptiveZInstruction;
import dorado.adaptive.gui.AdaptiveZSchedulerPanel;
import javafx.stage.Stage;

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
