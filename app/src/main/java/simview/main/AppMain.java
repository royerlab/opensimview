package simview.main;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackends;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.core.concurrent.thread.ThreadSleep;
import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.simulation.LightSheetMicroscopeSimulationDevice;
import clearcontrol.simulation.SimulationUtils;
import simview.icon.SplashScreen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import simview.SimViewMicroscope;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * MultivView LightSheet Microscope main class
 *
 * @author royer
 */
public class AppMain extends Application implements LoggingFeature
{
  static simview.main.AppMain instance = null;
  private boolean headless = false;

  public ClearCL getClearCL()
  {
    return mClearCL;
  }

  private ClearCL mClearCL;

  public static simview.main.AppMain getInstance()
  {
    if (instance == null)
    {
      launch();
    }
    return instance;
  }

  public AppMain()
  {
    super();
  }

  public AppMain(boolean headless)
  {
    headless = true;
  }

  private LightSheetMicroscope mLightSheetMicroscope;

  public LightSheetMicroscope getLightSheetMicroscope()
  {
    return mLightSheetMicroscope;
  }

  private static Alert sAlert;
  private static Optional<ButtonType> sResult;

  static final MachineConfiguration sMachineConfiguration = MachineConfiguration.get();

  public static void main(String[] args)
  {
    launch(args);
  }

  @Override
  public void start(Stage pPrimaryStage)
  {
    instance = this;
    if (headless)
    {
      return;
    }

    // retreive configuration information about 2D and 3D displays:
    boolean l2DDisplay = MachineConfiguration.get().getBooleanProperty("display.2d", true);
    boolean l3DDisplay = MachineConfiguration.get().getBooleanProperty("display.3d", false);

    BorderPane lPane = new BorderPane();

    SplashScreen lSplashScreen = new SplashScreen();

    lSplashScreen.fitWidthProperty().bind(pPrimaryStage.widthProperty());
    lSplashScreen.fitHeightProperty().bind(pPrimaryStage.heightProperty());

    lPane.setCenter(lSplashScreen);

    Scene lScene = new Scene(lPane, 1539, 770, Color.WHITE);
    lScene.setFill(Color.TRANSPARENT);
    Stage lSplashStage = new Stage();
    lSplashStage.setScene(lScene);
    lSplashStage.centerOnScreen();
    lSplashStage.setTitle("Dorado");
    lSplashStage.initStyle(StageStyle.TRANSPARENT);
    lSplashStage.show();

    ButtonType lButtonReal = new ButtonType("Real");
    ButtonType lButtonSimulation = new ButtonType("Simulation");
    ButtonType lButtonCancel = new ButtonType("Cancel");

    sAlert = new Alert(AlertType.CONFIRMATION);

    sAlert.setTitle("Dialog");
    sAlert.setHeaderText("Simulation or Real ?");
    sAlert.setContentText("Choose whether you want to start in real or simulated hatrdware mode");

    sAlert.getButtonTypes().setAll(lButtonReal, lButtonSimulation, lButtonCancel);

    Platform.runLater(() ->
    {
      sResult = sAlert.showAndWait();

      pPrimaryStage.show();
      lSplashStage.hide();

      Runnable lRunnable = () ->
      {
        if (sResult.get() == lButtonSimulation)
        {
          startDorado(true, pPrimaryStage, l2DDisplay, l3DDisplay, false);
        } else if (sResult.get() == lButtonReal)
        {
          startDorado(false, pPrimaryStage, l2DDisplay, l3DDisplay, true);
        } else if (sResult.get() == lButtonCancel)
        {
          Platform.runLater(() -> pPrimaryStage.hide());
        }
      };

      Thread lThread = new Thread(lRunnable, "StartSimView");
      lThread.setDaemon(true);
      lThread.start();
    });

  }

  /**
   * Starts the microscope
   *
   * @param pSimulation   true
   * @param pPrimaryStage JFX primary stage
   * @param p2DDisplay    true: use 2D displays
   * @param p3DDisplay    true: use 3D displays
   */
  public SimViewMicroscope startDorado(boolean pSimulation, Stage pPrimaryStage, boolean p2DDisplay, boolean p3DDisplay, boolean pUseStages)
  {
    int pNumberOfDetectionArms = 2;
    int pNumberOfLightSheets = 2;

    int lMaxStackProcessingQueueLength = 32;
    int lThreadPoolSize = 1;
    int lNumberOfControlPlanes = 8;

    try (ClearCL lClearCL = new ClearCL(ClearCLBackends.getBestBackend()))
    {
      for (ClearCLDevice lClearCLDevice : lClearCL.getAllDevices())
        info("OpenCl devices available: %s \n", lClearCLDevice.getName());

      ClearCLContext lStackFusionContext = lClearCL.getDeviceByName(sMachineConfiguration.getStringProperty("clearcl.device.fusion", "")).createContext();

      info("Using device %s for stack fusion \n", lStackFusionContext.getDevice());

      SimViewMicroscope lSimViewMicroscope = new SimViewMicroscope(lStackFusionContext, lMaxStackProcessingQueueLength, lThreadPoolSize);
      mLightSheetMicroscope = lSimViewMicroscope;
      if (pSimulation)
      {
        ClearCLContext lSimulationContext = lClearCL.getDeviceByName(sMachineConfiguration.getStringProperty("clearcl.device.simulation", "")).createContext();

        info("Using device %s for simulation (Simbryo) \n", lSimulationContext.getDevice());

        LightSheetMicroscopeSimulationDevice lSimulatorDevice = SimulationUtils.getSimulatorDevice(lSimulationContext, pNumberOfDetectionArms, pNumberOfLightSheets, 2048, 11, 512, 512, 512, false);

        lSimViewMicroscope.addSimulatedDevices(false, false, true, lSimulatorDevice);
      } else
      {
        lSimViewMicroscope.addRealHardwareDevices(pNumberOfDetectionArms, pNumberOfLightSheets, pUseStages);
      }
      lSimViewMicroscope.addStandardDevices(lNumberOfControlPlanes);

      //EDFImagingEngine
      //    lDepthOfFocusImagingEngine =
      //    new EDFImagingEngine(lStackFusionContext, lDoradoMicroscope);
      //lDoradoMicroscope.addDevice(0, lDepthOfFocusImagingEngine);


      info("Opening microscope devices...");
      if (lSimViewMicroscope.open())
      {
        info("Starting microscope devices...");
        if (lSimViewMicroscope.start())
        {
          if (pPrimaryStage != null)
          {
            dorado.gui.SimViewGui lSimViewGui;

            info("Setting up Dorado GUI...");
            lSimViewGui = new dorado.gui.SimViewGui(lSimViewMicroscope, pPrimaryStage, p2DDisplay, p3DDisplay);
            lSimViewGui.setup();
            info("Opening Dorado GUI...");
            lSimViewGui.open();

            lSimViewGui.waitForVisible(true, 1L, TimeUnit.MINUTES);

            lSimViewGui.connectGUI();
            lSimViewGui.waitForVisible(false, null, null);

            lSimViewGui.disconnectGUI();
            info("Closing Dorado GUI...");
            lSimViewGui.close();

            info("Stopping microscope devices...");
            lSimViewMicroscope.stop();
            info("Closing microscope devices...");
            lSimViewMicroscope.close();
          } else
          {
            mClearCL = lClearCL;
            return lSimViewMicroscope;
          }
        } else severe("Not all microscope devices started!");
      } else severe("Not all microscope devices opened!");

      ThreadSleep.sleep(100, TimeUnit.MILLISECONDS);
    }

    if (pPrimaryStage != null)
    {
      System.exit(0);
    }
    return null;
  }

}
