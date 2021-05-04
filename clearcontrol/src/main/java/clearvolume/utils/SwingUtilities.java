package clearvolume.utils;

import javax.swing.*;
import java.net.URL;

public class SwingUtilities
{
  public static final ImageIcon loadIcon(Class pRootClass, String strPath)
  {
    URL imgURL = pRootClass.getResource(strPath);
    if (imgURL != null) return new ImageIcon(imgURL);
    else return null;
  }
}
