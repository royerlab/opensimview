package dorado.adaptive.gui;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.checkbox.VariableCheckBox;
import clearcontrol.gui.jfx.var.file.VariableFileChooser;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import dorado.adaptive.AdaptiveZInstruction;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class AdaptiveZSchedulerPanel extends CustomGridPane
    implements LoggingFeature
{
  public AdaptiveZSchedulerPanel(AdaptiveZInstruction pAdaptiveZInstruction) {


    int lRow = 0;
    {
      this.add(new Label("This will ..."), 0, lRow);

      lRow++;
    }

    {
      VariableFileChooser lRootFolderChooser =
          new VariableFileChooser("Folder:",
                                  pAdaptiveZInstruction.getRootFolderVariable(),
                                  true);
      GridPane.setColumnSpan(lRootFolderChooser.getLabel(),
                             Integer.valueOf(1));
      GridPane.setColumnSpan(lRootFolderChooser.getTextField(),
                             Integer.valueOf(2));
      GridPane.setColumnSpan(lRootFolderChooser.getButton(),
                             Integer.valueOf(1));
      this.add(lRootFolderChooser.getLabel(), 0, lRow);
      this.add(lRootFolderChooser.getTextField(), 1, lRow);
      this.add(lRootFolderChooser.getButton(), 3, lRow);

      lRow++;
    }

    {
      BoundedVariable<Integer>
          lNumberOfSamplesVariable = pAdaptiveZInstruction.getNumberOfSamplesVariable();
      NumberVariableTextField<Integer> lField =
          new NumberVariableTextField<Integer>(lNumberOfSamplesVariable.getName(),
                                               lNumberOfSamplesVariable,
                                               lNumberOfSamplesVariable.getMin(),
                                               lNumberOfSamplesVariable.getMax(),
                                               lNumberOfSamplesVariable.getGranularity());
      this.add(lField.getLabel(), 0, lRow);
      this.add(lField.getTextField(), 1, lRow);
      lRow++;
    }

    {
      BoundedVariable<Integer>
          lSwitchCameraAtControlPlaneVariable = pAdaptiveZInstruction.getSwitchCameraControlPlaneIndex();
      NumberVariableTextField<Integer> lField =
          new NumberVariableTextField<Integer>(lSwitchCameraAtControlPlaneVariable.getName(),
                                               lSwitchCameraAtControlPlaneVariable,
                                               lSwitchCameraAtControlPlaneVariable.getMin(),
                                               lSwitchCameraAtControlPlaneVariable.getMax(),
                                               lSwitchCameraAtControlPlaneVariable.getGranularity());
      this.add(lField.getLabel(), 0, lRow);
      this.add(lField.getTextField(), 1, lRow);
      lRow++;
    }

    {
      BoundedVariable<Double>
          lDeltaZVariable = pAdaptiveZInstruction.getDeltaZVariable();
      NumberVariableTextField<Double> lField =
          new NumberVariableTextField<Double>(lDeltaZVariable.getName(),
                                               lDeltaZVariable,
                                               lDeltaZVariable.getMin(),
                                               lDeltaZVariable.getMax(),
                                               lDeltaZVariable.getGranularity());
      this.add(lField.getLabel(), 0, lRow);
      this.add(lField.getTextField(), 1, lRow);
      lRow++;
    }



    {
      BoundedVariable<Double>
          lLaserPowerVariable = pAdaptiveZInstruction.getLaserPowerVariable();
      NumberVariableTextField<Double> lField =
          new NumberVariableTextField<Double>(lLaserPowerVariable.getName(),
                                               lLaserPowerVariable,
                                               lLaserPowerVariable.getMin(),
                                               lLaserPowerVariable.getMax(),
                                               lLaserPowerVariable.getGranularity());
      this.add(lField.getLabel(), 0, lRow);
      this.add(lField.getTextField(), 1, lRow);
      lRow++;
    }



    {
      BoundedVariable<Double>
          lExposureTimeVariable = pAdaptiveZInstruction.getExposureInSecondsVariable();
      NumberVariableTextField<Double> lField =
          new NumberVariableTextField<Double>(lExposureTimeVariable.getName(),
                                              lExposureTimeVariable,
                                              lExposureTimeVariable.getMin(),
                                              lExposureTimeVariable.getMax(),
                                              lExposureTimeVariable.getGranularity());
      this.add(lField.getLabel(), 0, lRow);
      this.add(lField.getTextField(), 1, lRow);
      lRow++;
    }

    {
      Variable<Boolean> lStackStartsWithCamera0 = pAdaptiveZInstruction.getStartWithCamera0();
      VariableCheckBox lCheckBox = new VariableCheckBox(lStackStartsWithCamera0.getName(), lStackStartsWithCamera0);

      this.add(lCheckBox.getLabel(), 0, lRow);
      this.add(lCheckBox.getCheckBox(), 1, lRow);
      lRow++;
    }

  }
}
