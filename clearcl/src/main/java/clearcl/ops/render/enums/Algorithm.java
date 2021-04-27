package clearcl.ops.render.enums;

/**
 * Different volume rendering algorithms.
 *
 * @author royer
 */
public enum Algorithm implements AlgorithmInterface
{
 /**
  * Maximum projection
  */
 MaximumProjection("render/volume/maxproj.cl", "image_render_maxproj_3d");

  private final String mKernelPath, mKernelName;

  Algorithm(String pKernelPath, String pKernelName)
  {
    mKernelPath = pKernelPath;
    mKernelName = pKernelName;
  }

  @Override
  public String getKernelPath()
  {
    return mKernelPath;
  }

  @Override
  public String getKernelName()
  {
    return mKernelName;
  }
}
