package simview.icon;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SplashScreen extends ImageView
{

  public SplashScreen()
  {
    super(new Image(SplashScreen.class.getResourceAsStream("SplashScreen.png")));
  }
}
