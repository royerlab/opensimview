package clearcontrol.gui;

import clearcontrol.LightSheetMicroscope;
import clearcontrol.adaptive.AdaptationStateEngine;
import clearcontrol.adaptive.AdaptiveEngine;
import clearcontrol.adaptive.gui.AdaptationStateEnginePanel;
import clearcontrol.adaptive.gui.LightSheetAdaptiveEnginePanel;
import clearcontrol.calibrator.CalibrationEngine;
import clearcontrol.calibrator.gui.CalibrationEnginePanel;
import clearcontrol.calibrator.gui.CalibrationEngineToolbar;
import clearcontrol.component.detection.DetectionArmInterface;
import clearcontrol.component.detection.gui.DetectionArmPanel;
import clearcontrol.component.lightsheet.LightSheetInterface;
import clearcontrol.component.lightsheet.gui.LightSheetPanel;
import clearcontrol.devices.signalgen.LightSheetSignalGeneratorDevice;
import clearcontrol.devices.signalgen.gui.jfx.LightSheetSignalGeneratorPanel;
import clearcontrol.gui.halcyon.MicroscopeNodeType;
import clearcontrol.gui.video.video2d.Stack2DDisplay;
import clearcontrol.interactive.InteractiveAcquisition;
import clearcontrol.interactive.gui.InteractiveAcquisitionToolbar;
import clearcontrol.processor.LightSheetFastFusionProcessor;
import clearcontrol.processor.gui.LightSheetFastFusionProcessorPanel;
import clearcontrol.state.AcquisitionStateManager;
import clearcontrol.state.gui.AcquisitionStateManagerPanel;
import clearcontrol.timelapse.LightSheetTimelapse;
import clearcontrol.timelapse.gui.LightSheetTimelapseToolbar;
import clearcontrol.timelapse.gui.TimelapsePanel;
import clearcontrol.timelapse.timer.TimelapseTimerInterface;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Lightsheet microscope Ggraphical User Interface (GUI)
 *
 * @author royer
 */
public class LightSheetMicroscopeGUI extends MicroscopeGUI
{

  /**
   * Instanciates a lightsheet microscope GUI given a lightsheet microscope and two flags
   * determining whether to setup 2D and 3D displays.
   *
   * @param pLightSheetMicroscope lightsheet microscope
   * @param pPrimaryStage         JFX primary stage
   * @param p2DDisplay            true -> setup 2D display
   * @param p3DDisplay            true -> setup 3D display
   */
  public LightSheetMicroscopeGUI(LightSheetMicroscope pLightSheetMicroscope, Stage pPrimaryStage, boolean p2DDisplay, boolean p3DDisplay)
  {
    super(pLightSheetMicroscope, LSMNodeType.values(), pPrimaryStage, p2DDisplay, p3DDisplay);

    for (int c = 0; c < pLightSheetMicroscope.getNumberOfDetectionArms(); c++)
      for (int i = 0; i < pLightSheetMicroscope.getNumberOfLightSheets(); i++)
      {
        String lChannel = "C" + c + "L" + i;
        add3DDisplayChannel(lChannel);
      }

    addPanelMappingEntry(LightSheetInterface.class, LightSheetPanel.class, LSMNodeType.LightSheet);

    addPanelMappingEntry(DetectionArmInterface.class, DetectionArmPanel.class, LSMNodeType.DetectionArm);

    addPanelMappingEntry(AcquisitionStateManager.class, AcquisitionStateManagerPanel.class, MicroscopeNodeType.Acquisition);

    addPanelMappingEntry(TimelapseTimerInterface.class, TimelapsePanel.class, MicroscopeNodeType.Acquisition);

    addPanelMappingEntry(LightSheetSignalGeneratorDevice.class, LightSheetSignalGeneratorPanel.class, MicroscopeNodeType.Other);

    addToolbarMappingEntry(InteractiveAcquisition.class, InteractiveAcquisitionToolbar.class);

    addToolbarMappingEntry(CalibrationEngine.class, CalibrationEngineToolbar.class);

    addPanelMappingEntry(CalibrationEngine.class, CalibrationEnginePanel.class, MicroscopeNodeType.Acquisition);

    addToolbarMappingEntry(LightSheetTimelapse.class, LightSheetTimelapseToolbar.class);

    addPanelMappingEntry(AdaptiveEngine.class, LightSheetAdaptiveEnginePanel.class, MicroscopeNodeType.Acquisition);

    addPanelMappingEntry(AdaptationStateEngine.class, AdaptationStateEnginePanel.class, MicroscopeNodeType.Acquisition);

    addPanelMappingEntry(LightSheetFastFusionProcessor.class, LightSheetFastFusionProcessorPanel.class, MicroscopeNodeType.Acquisition);

    addPanelMappingEntry(LightSheetFastFusionProcessor.class, LightSheetFastFusionProcessorPanel.class, MicroscopeNodeType.Other);

    ArrayList<Stack2DDisplay> lDisplayDeviceList = get2DDisplayDeviceList();
    for (Stack2DDisplay lDisplay : pLightSheetMicroscope.getDevices(Stack2DDisplay.class))
    {
      if (!lDisplayDeviceList.contains(lDisplay))
      {
        lDisplayDeviceList.add(lDisplay);
      }
    }
  }

  @Override
  public void setup()
  {
    super.setup();
  }

}
