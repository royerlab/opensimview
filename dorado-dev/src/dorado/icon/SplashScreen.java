package dorado.icon;

import dorado.DoradoMicroscope;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

public class SplashScreen extends ImageView
{

    public SplashScreen ()
    {
        super(new Image(SplashScreen.class.getResourceAsStream("SplashScreen.png")));



    }
}
