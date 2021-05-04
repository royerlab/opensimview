package clearcontrol.timelapse.gui.demo;

import clearcontrol.core.concurrent.thread.ThreadSleep;
import clearcontrol.timelapse.TimelapseBase;
import clearcontrol.timelapse.TimelapseInterface;
import clearcontrol.timelapse.gui.TimelapseToolbar;
import clearcontrol.stack.sourcesink.sink.CompressedStackSink;
import clearcontrol.stack.sourcesink.sink.RawFileStackSink;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.concurrent.TimeUnit;

/**
 * Timelapse toolbar demo
 *
 * @author royer
 */
public class TimeLapseToolBarDemo extends Application
{

  @Override
  public void start(Stage stage)
  {
    HBox root = new HBox();
    root.setAlignment(Pos.CENTER);
    Scene scene = new Scene(root, 800, 600);
    stage.setScene(scene);
    stage.setTitle(this.getClass().getSimpleName());
    // scene.setFill(Color.BLACK);

    TimelapseInterface lTimelapse = new TimelapseBase(null)
    {

      @Override
      public boolean programStep()
      {
        System.out.println("programStep time point: " + getTimePointCounterVariable().get());
        ThreadSleep.sleep(10, TimeUnit.MILLISECONDS);
        return true;
      }

    };

    lTimelapse.addFileStackSinkType(RawFileStackSink.class);
    lTimelapse.addFileStackSinkType(CompressedStackSink.class);

    TimelapseToolbar lTimelapseToolbar = new TimelapseToolbar(lTimelapse);

    root.getChildren().add(lTimelapseToolbar);

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
