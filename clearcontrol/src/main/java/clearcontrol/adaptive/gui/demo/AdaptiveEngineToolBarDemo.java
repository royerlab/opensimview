package clearcontrol.adaptive.gui.demo;

import clearcontrol.adaptive.AdaptiveEngine;
import clearcontrol.adaptive.gui.AdaptiveEngineToolbar;
import clearcontrol.adaptive.test.AdaptationTestModule;
import clearcontrol.adaptive.test.TestState;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Simulation manager demo
 *
 * @author royer
 */
public class AdaptiveEngineToolBarDemo extends Application
{

  @Override
  public void start(Stage stage)
  {
    Group root = new Group();
    Scene scene = new Scene(root, 800, 600);
    stage.setScene(scene);
    stage.setTitle(AdaptiveEngineToolBarDemo.class.getSimpleName());
    // scene.setFill(Color.BLACK);

    AdaptiveEngine<TestState> lAdaptator = new AdaptiveEngine<TestState>(null, new TestState("initial state"));

    lAdaptator.add(new AdaptationTestModule("A", 2, 2, 2));
    lAdaptator.add(new AdaptationTestModule("B", 3));
    // lAdaptator.add(new AdaptationTestModule("C", 2, 3));

    AdaptiveEngineToolbar lAdaptorToolBar = new AdaptiveEngineToolbar(lAdaptator);

    root.getChildren().add(lAdaptorToolBar);

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
