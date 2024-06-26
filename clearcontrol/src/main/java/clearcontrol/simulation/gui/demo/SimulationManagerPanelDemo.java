package clearcontrol.simulation.gui.demo;

import clearcontrol.simulation.SimulationManager;
import clearcontrol.simulation.gui.SimulationManagerPanel;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Simulation manager demo
 *
 * @author royer
 */
public class SimulationManagerPanelDemo extends Application
{

  @Override
  public void start(Stage stage)
  {
    Group root = new Group();
    Scene scene = new Scene(root, 800, 600);
    stage.setScene(scene);
    stage.setTitle("Slider Sample");
    // scene.setFill(Color.BLACK);

    SimulationManager lSimulationManager = new SimulationManager(null);

    SimulationManagerPanel lSimulationManagerPanel = new SimulationManagerPanel(lSimulationManager);

    root.getChildren().add(lSimulationManagerPanel);

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
