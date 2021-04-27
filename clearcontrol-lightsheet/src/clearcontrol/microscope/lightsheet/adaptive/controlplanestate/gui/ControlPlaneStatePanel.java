package clearcontrol.microscope.lightsheet.adaptive.controlplanestate.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.adaptive.controlplanestate.ControlPlaneStateListener;
import clearcontrol.microscope.lightsheet.adaptive.controlplanestate.HasControlPlaneState;
import clearcontrol.microscope.lightsheet.configurationstate.ConfigurationState;
import clearcontrol.microscope.lightsheet.gui.VariableLabel;
import javafx.scene.control.Label;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public class ControlPlaneStatePanel extends CustomGridPane
{

  public ControlPlaneStatePanel(HasControlPlaneState pHasControlPlaneState,
                                int pNumberOfLightSheets,
                                int pNumberOfControlPlanes)
  {

    int posX = 0;
    for (int x = 0; x < pNumberOfControlPlanes; x++)
    {
      Label lLabel = new Label("" + x);
      add(lLabel, posX + 1, 0);
      posX++;
    }
    for (int y = 0; y < pNumberOfLightSheets; y++)
    {
      Label lLabel = new Label("L" + y);
      add(lLabel, 0, y + 1);
    }

    posX = 0;
    for (int x = 0; x < pNumberOfControlPlanes; x++)
    {
      for (int y = 0; y < pNumberOfLightSheets; y++)
      {
        final int lLightSheetIndex = y;
        final int lControlPlaneIndex = x;
        VariableLabel lVariableLabel = new VariableLabel("", "");

        pHasControlPlaneState.addControlPlaneStateChangeListener(new ControlPlaneStateListener()
        {
          @Override public void controlPlaneStateChanged(int pLightSheetIndex,
                                                         int pControlPlaneIndex)
          {
            if (pLightSheetIndex == lLightSheetIndex
                && pControlPlaneIndex == lControlPlaneIndex)
            {
              ConfigurationState
                  lConfigurationState =
                  pHasControlPlaneState.getControlPlaneState(pLightSheetIndex,
                                                             pControlPlaneIndex);

              String lConfigurationStateDescription = lConfigurationState.toString();

              lConfigurationStateDescription +=
                  "\n" + pHasControlPlaneState.getControlPlaneStateDescription(
                      pLightSheetIndex,
                      pControlPlaneIndex);

              lVariableLabel.getStringVariable().set("" + lConfigurationStateDescription);
              lVariableLabel.setStyle(

                  " -fx-padding: 2 2 2 2; -fx-border-color:white; -fx-text-fill:white; -fx-background-color: "
                  + lConfigurationState.getColor().toLowerCase()
                  + ";");
            }
          }
        });
        add(lVariableLabel, x + 1, y + 1);

      }
    }

  }
}