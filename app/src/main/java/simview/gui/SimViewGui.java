package dorado.gui;

import clearcontrol.LightSheetMicroscope;
import clearcontrol.gui.LightSheetMicroscopeGUI;
import javafx.stage.Stage;

/**
 * SimView microscope GUI
 *
 * @author royer
 */
public class SimViewGui extends LightSheetMicroscopeGUI
{

  /**
   * Instantiates SimView microscope GUI
   *
   * @param pLightSheetMicroscope microscope
   * @param pPrimaryStage         JFX primary stage
   * @param p2DDisplay            2D display
   * @param p3DDisplay            3D display
   */
  public SimViewGui(LightSheetMicroscope pLightSheetMicroscope, Stage pPrimaryStage, boolean p2DDisplay, boolean p3DDisplay)
  {
    super(pLightSheetMicroscope, pPrimaryStage, p2DDisplay, p3DDisplay);

    addGroovyScripting("lsm");
    addJythonScripting("lsm");
  }

}
