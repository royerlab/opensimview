package fastfuse.registration;

import javax.vecmath.Matrix4f;

/**
 * Class holding the registration parameters
 * 
 * @author uschmidt, royer
 */
public class RegistrationParameters
{

  // must be a power of 2; number of voxels must be evenly divisible by this
  // suggested: 128 or 256; max value limited by the GPU (e.g., 1024)
  // ideally: number of voxels even divisible by this more than once
  private static final int mGroupSize = 128;

  // number of additional optimization trials with random restarts
  private int mNumberOfRestarts = 4;
  // stop each optimization run after this many function evaluations
  private int mMaxNumberOfEvaluations = 200;
  // voxel scale in z direction (relative to scale 1 for both x and y)
  private float mScaleZ = 4;

  private Matrix4f mZeroTransformMatrix = AffineMatrix.identity();

  // initial transformation (transX, transY, transZ, rotX, rotY, rotZ)
  // rotation angles in degrees around center of volume
  private double[] mInitTransform = new double[]
  { 0, 0, 0, 0, 0, 0 };

  private double mTranslationSearchRadius = 20;

  private double mRotationSearchRadius = 10;

  private boolean mWaitToFinish = true;

  public void setMaxNumberOfEvaluations(int pMaxNumberOfEvaluations)
  {
    assert pMaxNumberOfEvaluations > 0;
    mMaxNumberOfEvaluations = pMaxNumberOfEvaluations;
  }

  public int getMaxNumberOfEvaluations()
  {
    return mMaxNumberOfEvaluations;
  }

  public float getScaleZ()
  {
    return mScaleZ;
  }

  public void setScaleZ(float pScaleZ)
  {
    assert pScaleZ > 0;
    mScaleZ = pScaleZ;
  }

  public void setTranslationSearchRadius(double pTranslationSearchRadius)
  {
    mTranslationSearchRadius = pTranslationSearchRadius;
  }

  public void setRotationSearchRadius(double pRotationSearchRadius)
  {
    mRotationSearchRadius = pRotationSearchRadius;
  }

  public double getTranslationSearchRadius()
  {
    return mTranslationSearchRadius;
  }

  public double getRotationSearchRadius()
  {
    return mRotationSearchRadius;
  }

  public double[] getUpperBounds()
  {
    return new double[]
    { +mTranslationSearchRadius,
      +mTranslationSearchRadius,
      +mTranslationSearchRadius,
      +mRotationSearchRadius,
      +mRotationSearchRadius,
      +mRotationSearchRadius };
  }

  public double[] getLowerBounds()
  {
    return new double[]
    { -mTranslationSearchRadius,
      -mTranslationSearchRadius,
      -mTranslationSearchRadius,
      -mRotationSearchRadius,
      -mRotationSearchRadius,
      -mRotationSearchRadius };
  }

  public Matrix4f getZeroTransformMatrix()
  {
    return mZeroTransformMatrix;
  }

  public void setZeroTransformMatrix(Matrix4f pZeroTransformMatrix)
  {
    mZeroTransformMatrix = new Matrix4f(pZeroTransformMatrix);
  }

  public double[] getInitialTransformation()
  {
    return mInitTransform;
  }

  public void setInitialTransformation(double... theta)
  {
    assert theta.length == 6;
    mInitTransform = theta;
  }

  public int getNumberOfRestarts()
  {
    return mNumberOfRestarts;
  }

  public void setNumberOfRestarts(int pRestarts)
  {
    assert pRestarts >= 0 && pRestarts < 50;
    mNumberOfRestarts = pRestarts;
  }

  public int getOpenCLGroupSize()
  {
    return mGroupSize;
  }

  public int getOpenCLReductionThreshold()
  {
    return getOpenCLGroupSize();
  }

  public boolean getWaitToFinish()
  {
    return mWaitToFinish;
  }

  public void setWaitToFinish(boolean pWaitToFinish)
  {
    mWaitToFinish = pWaitToFinish;
  }

}
