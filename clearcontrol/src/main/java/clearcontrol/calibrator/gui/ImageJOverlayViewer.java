package clearcontrol.calibrator.gui;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.gui.video.video2d.Stack2DDisplay;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.imglib2.StackToImgConverter;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.RGBStackMerge;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) January
 * 2018
 */
public class ImageJOverlayViewer implements LoggingFeature
{
  List<Stack2DDisplay> mStack2DDisplays;

  public ImageJOverlayViewer(List<Stack2DDisplay> pStack2DDisplays)
  {
    mStack2DDisplays = pStack2DDisplays;
  }

  public void show()
  {
    new ImageJ();

    ArrayList<ImagePlus> lImagePlusList = new ArrayList<ImagePlus>();
    for (Stack2DDisplay lStack2DDisplay : mStack2DDisplays)
    {
      StackInterface lStack = lStack2DDisplay.getLastViewedStack();
      if (lStack != null)
      {
        RandomAccessibleInterval lRandomAccessibleInterval = new StackToImgConverter(lStack).getRandomAccessibleInterval();
        ImagePlus lImagePlus = ImageJFunctions.wrap(lRandomAccessibleInterval, "temp");
        lImagePlus = new Duplicator().run(lImagePlus);
        if (lStack2DDisplay.isFlipX())
        {
          IJ.run(lImagePlus, "Flip Horizontally", "");
        }
        lImagePlusList.add(lImagePlus);
      } else
      {
        warning("Stack was null.");
      }
    }

    ImagePlus[] lImagePlusArray = new ImagePlus[lImagePlusList.size()];
    lImagePlusList.toArray(lImagePlusArray);

    info("Showing " + lImagePlusList.size() + " images");
    ImagePlus fusedImage = RGBStackMerge.mergeChannels(lImagePlusArray, false);
    if (fusedImage != null)
    {
      fusedImage.show();
      for (int c = 0; c < fusedImage.getNChannels(); c++)
      {
        fusedImage.setC(c + 1);
        IJ.run(fusedImage, "Enhance Contrast", "saturated=0.35");
      }
    } else
    {
      warning("Multichannel fusion result was null.");
    }
  }
}
