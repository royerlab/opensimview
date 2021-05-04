package clearcontrol.processor.gui;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsolePanel;
import clearcontrol.gui.jfx.var.customvarpanel.CustomVariablePane;
import clearcontrol.processor.LightSheetFastFusionProcessor;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * Lightsheet fast fusion processor panel
 *
 * @author royer
 */
public class LightSheetFastFusionProcessorPanel extends TabPane
{

  private final VisualConsolePanel mVisualConsolePanel;

  /**
   * Instantiates a lightsheet fast fusion processor panel
   *
   * @param pLightSheetFastFusionProcessor lightsheet fast fusion processor
   */
  public LightSheetFastFusionProcessorPanel(LightSheetFastFusionProcessor pLightSheetFastFusionProcessor)
  {
    super();

    Tab lParametersTab = new Tab("Parameters");

    lParametersTab.setContent(getParametersPanel(pLightSheetFastFusionProcessor));

    mVisualConsolePanel = new VisualConsolePanel((VisualConsoleInterface) pLightSheetFastFusionProcessor);

    Tab lVisualLogTab = new Tab("Visual Log");
    lVisualLogTab.setContent(mVisualConsolePanel);

    getTabs().addAll(lParametersTab, lVisualLogTab);
  }

  private Node getParametersPanel(LightSheetFastFusionProcessor pLightSheetFastFusionProcessor)
  {
    Variable<Integer> lNumberOfRestartsVariable = pLightSheetFastFusionProcessor.getNumberOfRestartsVariable();

    Variable<Integer> lMaxNumberOfEvaluationsVariable = pLightSheetFastFusionProcessor.getMaxNumberOfEvaluationsVariable();

    BoundedVariable<Double> lTranslationSearchRadiusVariable = pLightSheetFastFusionProcessor.getTranslationSearchRadiusVariable();

    BoundedVariable<Double> lRotationSearchRadiusVariable = pLightSheetFastFusionProcessor.getRotationSearchRadiusVariable();

    BoundedVariable<Double> lSmoothingConstantVariable = pLightSheetFastFusionProcessor.getSmoothingConstantVariable();

    Variable<Boolean> lTransformLockSwitchVariable = pLightSheetFastFusionProcessor.getTransformLockSwitchVariable();

    Variable<Integer> lTransformLockThresholdVariable = pLightSheetFastFusionProcessor.getTransformLockThresholdVariable();

    Variable<Boolean> lBackgroundSubtractionSwitchVariable = pLightSheetFastFusionProcessor.getBackgroundSubtractionSwitchVariable();

    CustomVariablePane lCustomVariablePane = new CustomVariablePane();

    lCustomVariablePane.addTab("");

    lCustomVariablePane.addNumberTextFieldForVariable("Number of restarts", lNumberOfRestartsVariable, 0, Integer.MAX_VALUE, 1);

    lCustomVariablePane.addNumberTextFieldForVariable("Maximum number of evaluations", lMaxNumberOfEvaluationsVariable, 0, Integer.MAX_VALUE, 1);

    lCustomVariablePane.addNumberTextFieldForVariable("Translation search radius", lTranslationSearchRadiusVariable, 0d, 1000d, 1d);

    lCustomVariablePane.addNumberTextFieldForVariable("Rotation search radius", lRotationSearchRadiusVariable, 0d, 1000d, 1d);

    lCustomVariablePane.addNumberTextFieldForVariable("Smoothing constant", lSmoothingConstantVariable, 0d, 1d, 0.00001d);

    lCustomVariablePane.addCheckBoxForVariable("Transform lock switch", lTransformLockSwitchVariable);

    lCustomVariablePane.addNumberTextFieldForVariable("Transform lock timepoint threshold", lTransformLockThresholdVariable, 0, Integer.MAX_VALUE, 1);

    lCustomVariablePane.addCheckBoxForVariable("Do background subtraction", lBackgroundSubtractionSwitchVariable);

    return lCustomVariablePane;
  }

}
