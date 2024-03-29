package clearcontrol.processor.gui.demo;

import clearcontrol.LightSheetMicroscope;
import clearcontrol.component.detection.DetectionArm;
import clearcontrol.component.lightsheet.LightSheet;
import clearcontrol.core.concurrent.executors.AsynchronousExecutorFeature;
import clearcontrol.processor.LightSheetFastFusionProcessor;
import clearcontrol.processor.gui.LightSheetFastFusionProcessorPanel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Acquisition state manager demo
 *
 * @author royer
 */
public class LightSheetFastFusionProcessorPanelDemo extends Application implements AsynchronousExecutorFeature
{

  @Override
  public void start(Stage stage)
  {

    LightSheetMicroscope lLightSheetMicroscope = new LightSheetMicroscope("Dummy", null, 1, 1);
    DetectionArm lDetectionArm0 = new DetectionArm("D0");
    DetectionArm lDetectionArm1 = new DetectionArm("D1");
    lLightSheetMicroscope.addDevice(0, lDetectionArm0);
    lLightSheetMicroscope.addDevice(1, lDetectionArm1);

    LightSheet lLightSheet0 = new LightSheet("L0", 1, 2);
    LightSheet lLightSheet1 = new LightSheet("L1", 1, 2);
    lLightSheetMicroscope.addDevice(0, lLightSheet0);
    lLightSheetMicroscope.addDevice(1, lLightSheet1);

    LightSheetFastFusionProcessor lProcessor = new LightSheetFastFusionProcessor("processor", lLightSheetMicroscope, null);

    LightSheetFastFusionProcessorPanel lPanel = new LightSheetFastFusionProcessorPanel(lProcessor);

    Scene scene = new Scene(lPanel, 500, 500);
    stage.setScene(scene);
    stage.setTitle("Interactive2DAcquisitionPanel Demo");

    stage.show();

  }

  /**
   * Main
   *
   * @param args NA
   */
  public static void main(String[] args)
  {

    launch(args);
  }
}
