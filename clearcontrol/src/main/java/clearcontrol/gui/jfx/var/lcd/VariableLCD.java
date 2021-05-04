package clearcontrol.gui.jfx.var.lcd;

import clearcontrol.core.variable.Variable;
import eu.hansolo.enzo.lcd.Lcd;
import javafx.application.Platform;
import javafx.scene.layout.HBox;

/**
 * This LCD class syncs a LCD panel with a number variable
 *
 * @param <T> lcd number type
 * @author royer
 */
public class VariableLCD<T extends Number> extends HBox
{

  /**
   * Instantiates a variable synced LCD panel, given an existing LCD panel and
   * number variable
   *
   * @param pLCDDisplay     existing LCD panel
   * @param pNumberVariable number variable to sync with
   */
  public VariableLCD(Lcd pLCDDisplay, Variable<T> pNumberVariable)
  {
    pNumberVariable.addSetListener((o, n) ->
    {
      if (!n.equals(o) && n != null) Platform.runLater(() ->
      {
        double lValue = pNumberVariable.get().doubleValue();
        pLCDDisplay.valueProperty().set(lValue);
      });
    });

    getChildren().add(pLCDDisplay);
  }

}
