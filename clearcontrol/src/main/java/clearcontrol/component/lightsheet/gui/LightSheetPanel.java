package clearcontrol.component.lightsheet.gui;

import clearcontrol.component.lightsheet.LightSheetInterface;
import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.var.customvarpanel.CustomVariablePane;
import clearcontrol.gui.jfx.var.onoffarray.OnOffArrayPane;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import clearcontrol.core.math.functions.UnivariateAffineFunction;
import javafx.scene.control.Control;

/**
 * Light sheet panel
 *
 * @author royer
 */
public class LightSheetPanel extends CustomVariablePane
{

  /**
   * Instanciates a light sheet panel
   *
   * @param pLightSheetInterface light sheet
   */
  public LightSheetPanel(LightSheetInterface pLightSheetInterface)
  {
    super();


    {
      addTab("DOFs");

      addSliderForVariable("X :", pLightSheetInterface.getXVariable(), null).setUpdateIfChanging(true);

      addSliderForVariable("Y :", pLightSheetInterface.getYVariable(), null).setUpdateIfChanging(true);

      addSliderForVariable("Z :", pLightSheetInterface.getZVariable(), null).setUpdateIfChanging(true);

      addSliderForVariable("Alpha :", pLightSheetInterface.getAlphaInDegreesVariable(), null).setUpdateIfChanging(true);

      addSliderForVariable("Beta :", pLightSheetInterface.getBetaInDegreesVariable(), null).setUpdateIfChanging(true);

      addSliderForVariable("Width :", pLightSheetInterface.getWidthVariable(), null).setUpdateIfChanging(true);

      addSliderForVariable("Height :", pLightSheetInterface.getHeightVariable(), null).setUpdateIfChanging(true);

      addSliderForVariable("Power :", pLightSheetInterface.getPowerVariable(), null).setUpdateIfChanging(true);

      OnOffArrayPane lLaserOnOffArray = addOnOffArray("Laser :");

      int lNumberOfLaserDigitalControls = pLightSheetInterface.getNumberOfLaserDigitalControls();
      for (int l = 0; l < lNumberOfLaserDigitalControls; l++)
      {
        lLaserOnOffArray.addSwitch("L" + l, pLightSheetInterface.getLaserOnOffArrayVariable(l));
      }

      OnOffArrayPane lStructuredIlluminationPane = addOnOffArray("Structured illumination");
      for (int l = 0; l < lNumberOfLaserDigitalControls; l++)
      {
        lStructuredIlluminationPane.addSwitch("L", pLightSheetInterface.getSIPatternOnOffVariable(l));
      }
    }

    {
      addTab("Functions");

      addFunctionPane("X function", pLightSheetInterface.getXFunction());
      addFunctionPane("Y function", pLightSheetInterface.getYFunction());
      addFunctionPane("Z function", pLightSheetInterface.getZFunction());

      addFunctionPane("Alpha function", pLightSheetInterface.getAlphaFunction());
      addFunctionPane("Beta function", pLightSheetInterface.getBetaFunction());

      addFunctionPane("Width function", pLightSheetInterface.getWidthFunction());
      addFunctionPane("Height function", pLightSheetInterface.getHeightFunction());

      addFunctionPane("Power function", pLightSheetInterface.getPowerFunction());
    }

    {
      addTab("Bounds");

      addBoundedVariable("X :", pLightSheetInterface.getXVariable());

      addBoundedVariable("Y :", pLightSheetInterface.getYVariable());

      addBoundedVariable("Z :", pLightSheetInterface.getZVariable());

      addBoundedVariable("Alpha :", pLightSheetInterface.getAlphaInDegreesVariable());

      addBoundedVariable("Beta :", pLightSheetInterface.getBetaInDegreesVariable());

      addBoundedVariable("Width :", pLightSheetInterface.getWidthVariable());

      addBoundedVariable("Height :", pLightSheetInterface.getHeightVariable());

      addBoundedVariable("Power :", pLightSheetInterface.getPowerVariable());
    }

    {
      addTab("Advanced");

      addSliderForVariable("EffectiveExposure :", pLightSheetInterface.getEffectiveExposureInSecondsVariable(), 1e-6, 1, 0.001, 0.1).setUpdateIfChanging(true);

      /*addSliderForVariable("LineExposure :",
                           pLightSheetInterface.getLineExposureInMicrosecondsVariable(),
                           1.0,
                           1000000.0,
                           1.0,
                           500000.0).setUpdateIfChanging(true);/**/

      addSliderForVariable("Overscan :", pLightSheetInterface.getOverScanVariable(), 0.0, 2.0, 0.01, 0.1).setUpdateIfChanging(true);

      addSliderForVariable("Readout Time :", pLightSheetInterface.getReadoutTimeInMicrosecondsPerLineVariable(), 0.0, 10.0, 0.0, 1.0).setUpdateIfChanging(true);
    }

    {
      addTab("Focus Fine-Tuning");

      // Get the min and max values for the Z variable:
      double lMinZValue = pLightSheetInterface.getZVariable().getMin().doubleValue();
      double lMaxZValue = pLightSheetInterface.getZVariable().getMax().doubleValue();

      // Create the variables for the sliders:
      BoundedVariable<Double> lAdjustLowZVariable = new BoundedVariable<Double>("Low Z position", 0.0, lMinZValue, lMaxZValue);
      BoundedVariable<Double> lAdjustHighZVariable = new BoundedVariable<Double>("High Z position", 0.0, lMinZValue, lMaxZValue);

      // Add sliders for the variables:
      addSliderForVariable("Low Z position:", lAdjustLowZVariable, null).setUpdateIfChanging(true);
      addSliderForVariable("High Z position:", lAdjustHighZVariable, null).setUpdateIfChanging(true);

      // Create a variable for the Z focus step size:
      BoundedVariable<Double> lZFocusStepVariable = new BoundedVariable<Double>("Z Focus step size", 0.0, 0.0, 32.0);

      // Add a slider for the Z focus step size:
      addSliderForVariable("Z focus step size: ", lZFocusStepVariable, null).setUpdateIfChanging(true);

      // Create a button for changing the low Z focus up:
      Button lLowZPosUpButton = new Button("Low Z position Up");
      lLowZPosUpButton.setAlignment(Pos.CENTER);
      lLowZPosUpButton.setOnAction((e) ->
      {
        double z_low = lAdjustLowZVariable.get();
        double z_high = lAdjustHighZVariable.get();
        double step = lZFocusStepVariable.get();
        updateFunction(pLightSheetInterface.getZFunction().get(), z_low, z_high, +step, 0);
      });

      // Create a button for changing the low Z focus down:
      Button lLowZPosDownButton = new Button("Low Z position Down");
      lLowZPosDownButton.setAlignment(Pos.CENTER);
      lLowZPosDownButton.setOnAction((e) ->
      {
        double z_low = lAdjustLowZVariable.get();
        double z_high = lAdjustHighZVariable.get();
        double step = lZFocusStepVariable.get();
        updateFunction(pLightSheetInterface.getZFunction().get(), z_low, z_high, -step, 0);
      });

      // Create a button for changing the high Z focus up:
      Button lHighZPosUpButton = new Button("High Z position Up");
      lHighZPosUpButton.setAlignment(Pos.CENTER);
      lHighZPosUpButton.setOnAction((e) ->
      {
        double z_low = lAdjustLowZVariable.get();
        double z_high = lAdjustHighZVariable.get();
        double step = lZFocusStepVariable.get();
        updateFunction(pLightSheetInterface.getZFunction().get(), z_low, z_high, 0, +step);
      });

      // Create a button for changing the high Z focus down:
      Button lHighZPosDownButton = new Button("Low Z position Down");
      lHighZPosDownButton.setAlignment(Pos.CENTER);
      lHighZPosDownButton.setOnAction((e) ->
      {
        double z_low = lAdjustLowZVariable.get();
        double z_high = lAdjustHighZVariable.get();
        double step = lZFocusStepVariable.get();
        updateFunction(pLightSheetInterface.getZFunction().get(), z_low, z_high, 0, -step);
      });

      // Add the buttons to the panel:
      addControl(lLowZPosUpButton, 0,0, 1);
      addControl(lLowZPosDownButton, 1,1, 1);
      addControl(lHighZPosUpButton, 0, 0, 1);
      addControl(lHighZPosDownButton, 1,1, 1);

    }

  }


  // Function that updates the function for a given delta low and delta high value:
  public void updateFunction(UnivariateAffineFunction function, double z_low, double z_high, double delta_l, double delta_h)
  {
    double a = function.getSlope();
    double b = function.getConstant();
    double a_prime = a + (delta_h - delta_l) / (z_high - z_low);
    double b_prime = b + delta_l - ((delta_h - delta_l) / (z_high - z_low)) * z_low;
    function.setSlope(a_prime);
    function.setConstant(b_prime);
  }


}
