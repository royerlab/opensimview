package clearcontrol.microscope.lightsheet.configurationstate.gui;

import clearcontrol.core.device.name.ReadOnlyNameableInterface;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.configurationstate.*;
import clearcontrol.microscope.lightsheet.gui.VariableLabel;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public class ConfigurationStatePanel extends CustomGridPane
{

  public ConfigurationStatePanel(ArrayList lObjectList, int pNumberOfLightSheets)
  {

    int posX = 0;
    for (int x = 0; x < lObjectList.size(); x++)
    {
      Object lObject = lObjectList.get(x);
      if (lObject instanceof HasConfigurationState && lObject instanceof ReadOnlyNameableInterface)
      {
        Label lLabel = new Label(((ReadOnlyNameableInterface) lObject).getName());
        add(lLabel, posX + 1, 0);
        posX++;
      }
    }
    for (int y = 0; y < pNumberOfLightSheets; y++)
    {
      Label lLabel = new Label("L" + y);
      add(lLabel, 0, y + 1);
    }

    posX = 0;
    for (int x = 0; x < lObjectList.size(); x++)
    {
      Object lObject = lObjectList.get(x);
      if (lObject instanceof HasConfigurationState && lObject instanceof ReadOnlyNameableInterface)
      {
        HasConfigurationState lHasConfigurationState = (HasConfigurationState) lObject;
        if (lHasConfigurationState instanceof HasConfigurationStatePerLightSheet)
        {
          HasConfigurationStatePerLightSheet lHasConfigurationStatePerLightSheet = (HasConfigurationStatePerLightSheet) lHasConfigurationState;

          for (int y = 0; y < pNumberOfLightSheets; y++)
          {
            final int lLightSheetIndex = y;
            VariableLabel lVariableLabel = new VariableLabel("", "");
            lHasConfigurationStatePerLightSheet.addConfigurationStateChangeListener(new ConfigurationStatePerLightSheetChangeListener()
            {
              @Override
              public void configurationStateChanged(HasConfigurationState pHasConfigurationState)
              {
              }

              @Override
              public void configurationStateOfLightSheetChanged(HasConfigurationStatePerLightSheet pHasConfigurationStatePerLightSheet, int pLightSheetIndex)
              {
                if (pLightSheetIndex == lLightSheetIndex)
                {
                  ConfigurationState lConfigurationState = pHasConfigurationStatePerLightSheet.getConfigurationState(pLightSheetIndex);

                  String lConfigurationStateDescription = lConfigurationState.toString();

                  if (pHasConfigurationStatePerLightSheet instanceof HasStateDescriptionPerLightSheet)
                  {
                    lConfigurationStateDescription += "\n" + ((HasStateDescriptionPerLightSheet) pHasConfigurationStatePerLightSheet).getStateDescription(pLightSheetIndex);
                  } else if (pHasConfigurationStatePerLightSheet instanceof HasStateDescription)
                  {
                    lConfigurationStateDescription += "\n" + ((HasStateDescription) pHasConfigurationStatePerLightSheet).getStateDescription();
                  }

                  lVariableLabel.getStringVariable().set("" + lConfigurationStateDescription);
                  lVariableLabel.setStyle(

                          " -fx-padding: 2 2 2 2; -fx-border-color:white; -fx-text-fill:white; -fx-background-color: " + lConfigurationState.getColor().toLowerCase() + ";");
                }
              }
            });
            add(lVariableLabel, posX + 1, y + 1);
          }
        } else
        {
          VariableLabel lVariableLabel = new VariableLabel("", "");

          lHasConfigurationState.addConfigurationStateChangeListener(new ConfigurationStateChangeListener()
          {
            @Override
            public void configurationStateChanged(HasConfigurationState pHasConfigurationState)
            {
              ConfigurationState lConfigurationState = pHasConfigurationState.getConfigurationState();

              String lConfigurationStateDescription = lConfigurationState.toString();

              if (pHasConfigurationState instanceof HasStateDescription)
              {
                lConfigurationStateDescription += "\n" + ((HasStateDescription) pHasConfigurationState).getStateDescription();
              }

              lVariableLabel.getStringVariable().set("" + lConfigurationStateDescription);
              lVariableLabel.setStyle(" -fx-padding: 2 2 2 2; -fx-border-color:white; -fx-text-fill:white; -fx-background-color: " + lConfigurationState.getColor().toLowerCase() + ";");
            }
          });
          GridPane.setFillHeight(lVariableLabel, true);
          GridPane.setRowSpan(lVariableLabel, pNumberOfLightSheets);
          add(lVariableLabel, posX + 1, 1);
        }
        posX++;
      }
    }
  }

}
