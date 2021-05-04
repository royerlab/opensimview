package fastfuse.registration;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

/**
 * Helper functions for affine matrix construction
 *
 * @author uschmidt, royer
 */

public class AffineMatrix
{

  /**
   * Returns an identity matrix
   *
   * @return identity matrix
   */
  public static Matrix4f identity()
  {
    Matrix4f lMatrix = new Matrix4f();
    lMatrix.setIdentity();
    return lMatrix;
  }

  /**
   * Returns a translation matrix
   *
   * @param pTranslationVector translation matrix
   * @return transation matrix
   */
  public static Matrix4f translation(float... pTranslationVector)
  {
    assert pTranslationVector.length == 3;
    Matrix4f lMatrix = identity();
    lMatrix.setTranslation(new Vector3f(pTranslationVector));
    return lMatrix;
  }

  /**
   * Returns a scaling matrix given the diagonal
   *
   * @param pScalingVector scaling vector (diagonal)
   * @return scaling matrix
   */
  public static Matrix4f scaling(float... pScalingVector)
  {
    assert pScalingVector.length == 3;
    Matrix4f lMatrix = identity();
    lMatrix.setElement(0, 0, pScalingVector[0]);
    lMatrix.setElement(1, 1, pScalingVector[1]);
    lMatrix.setElement(2, 2, pScalingVector[2]);
    return lMatrix;
  }

  /**
   * Returns a rotation matrix given the rotation angles in degrees around the
   * X, Y and Z axis.
   *
   * @param pXYZRotationAnglesInDegrees rotation angles in degrees along the X, Y and Z axis.
   * @return rotation matrix
   */
  public static Matrix4f rotation(float... pXYZRotationAnglesInDegrees)
  {
    Matrix4f Rx = new Matrix4f();
    Rx.rotX((float) Math.toRadians(pXYZRotationAnglesInDegrees[0]));
    Matrix4f Ry = new Matrix4f();
    Ry.rotY((float) Math.toRadians(pXYZRotationAnglesInDegrees[1]));
    Matrix4f Rz = new Matrix4f();
    Rz.rotZ((float) Math.toRadians(pXYZRotationAnglesInDegrees[2]));
    return multiply(Rz, Ry, Rx);
  }

  /**
   * Multiples the given matrices
   *
   * @param pMatrices matrices
   * @return product matrix
   */
  public static Matrix4f multiply(Matrix4f... pMatrices)
  {
    Matrix4f lProductMatrix = identity();
    for (Matrix4f lMatrix : pMatrices)
      lProductMatrix.mul(lMatrix);

    return lProductMatrix;
  }

}
