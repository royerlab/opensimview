package clearcontrol.component.lightsheet.gui;

import clearcontrol.component.lightsheet.LightSheetInterface;
import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.var.customvarpanel.CustomVariablePane;
import clearcontrol.gui.jfx.var.onoffarray.OnOffArrayPane;

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

      BoundedVariable<Double> lAdjustLowZFocusVariable = new BoundedVariable<Double>("Adjust Low Z Focus", 0.0, -32.0, 32.0);
      BoundedVariable<Double> lAdjustHighZFocusVariable = new BoundedVariable<Double>("Adjust Low Z Focus", 0.0, -32.0, 32.0);

      // Set listeners for the sliders:
      lAdjustLowZVariable.addSetListener((o, n) ->
      {
        // nothing needed here, we just use that variable value when the focus is tuned.
      });

      lAdjustHighZVariable.addSetListener((o, n) ->
      {
        // nothing needed here, we just use that variable value when the focus is tuned.
      });

      lAdjustLowZFocusVariable.addSetListener((o, n) ->
      {
        // adjust the low Z position by modifying a and b in the Z function: pLightSheetInterface.getZFunction()
        double a = pLightSheetInterface.getZFunction().get().getSlope();
        double b = pLightSheetInterface.getZFunction().get().getConstant();
      });

      lAdjustHighZFocusVariable.addSetListener((o, n) ->
      {
        // adjust the HIGH Z position by modifying a and b in the Z function: pLightSheetInterface.getZFunction()
        double a = pLightSheetInterface.getZFunction().get().getSlope();
        double b = pLightSheetInterface.getZFunction().get().getConstant();
      });

      addSliderForVariable("Low Z position:", lAdjustLowZVariable, null).setUpdateIfChanging(true);
      addSliderForVariable("High Z position:", lAdjustHighZVariable, null).setUpdateIfChanging(true);
      addSliderForVariable("Low Z focus: ", lAdjustLowZFocusVariable, null).setUpdateIfChanging(true);
      addSliderForVariable("High Z focus: ", lAdjustHighZFocusVariable, null).setUpdateIfChanging(true);

    }

  }

}
