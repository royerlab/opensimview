package clearcontrol.state;

import static java.lang.Math.PI;
import static java.lang.Math.cos;

/**
 * Control plane layouts
 *
 * @author royer
 */
public enum ControlPlaneLayout
{
  /**
   * Linear layout: control planes are laid out with equal spacing
   */
  Linear,

  /**
   * Linear layout, but the control plane in the center exists twice
   */
  LinearWithDoubledCenter,

  /**
   * Circular layout: control planes are laid with equal spacing on the unit circle and
   * then projected on the x axis.
   */
  Circular;

  /**
   * Returns the position of the control plane of given index for a given number of
   * control planes
   *
   * @param pNumberOfControlPlanes number of control planes
   * @param pControlPlaneIndex     control plane index
   * @return normalized position of the control plane within [0,1]
   */
  public double layout(int pNumberOfControlPlanes, int pControlPlaneIndex)
  {
    switch (this)
    {

      case Linear:
        return linear(pNumberOfControlPlanes, pControlPlaneIndex);

      case LinearWithDoubledCenter:
        return linearWithDoubledCenter(pNumberOfControlPlanes, pControlPlaneIndex);

      case Circular:
        return circular(pNumberOfControlPlanes, pControlPlaneIndex);

    }

    return linear(pNumberOfControlPlanes, pControlPlaneIndex);
  }

  private double linear(int pNumberOfControlPlanes, int pControlPlaneIndex)
  {
    return ((double) pControlPlaneIndex) / (pNumberOfControlPlanes - 1);
  }

  private double circular(int pNumberOfControlPlanes, int pControlPlaneIndex)
  {
    double z = linear(pNumberOfControlPlanes, pControlPlaneIndex);

    double zc = 0.5 * (1 + cos(PI * (1 - z)));

    return zc;
  }

  private double linearWithDoubledCenter(int pNumberOfControlPlanes, int pControlPlaneIndex)
  {
    int lFirstHalfNumberOfControlPlanes = pNumberOfControlPlanes / 2;
    int lSecondHalfNumberOfControlPlanes = pNumberOfControlPlanes - lFirstHalfNumberOfControlPlanes;

    if (pControlPlaneIndex < lFirstHalfNumberOfControlPlanes)
    {

      return ((double) pControlPlaneIndex) / (lFirstHalfNumberOfControlPlanes - 1) / 2.0;
    } else
    {
      return 0.5 + ((double) (pControlPlaneIndex - lFirstHalfNumberOfControlPlanes)) / (lSecondHalfNumberOfControlPlanes - 1) / 2.0 + (pControlPlaneIndex == lFirstHalfNumberOfControlPlanes ? 0.01 : 0.0);
    }
  }

  public static void main(String... args)
  {
    for (int i = 0; i < 7; i++)
    {
      System.out.println("" + i + ": " + LinearWithDoubledCenter.layout(7, i));
    }
  }

}
