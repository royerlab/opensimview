package clearcl.util;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.enums.HostAccessType;
import clearcl.enums.KernelAccessType;
import clearcl.enums.MemAllocMode;
import coremem.enums.NativeTypeEnum;
import coremem.offheap.OffHeapMemory;

import javax.vecmath.Matrix4f;

/**
 * @author royer
 */
public class MatrixUtils
{

  /**
   * Converts Matrix4f into an array that can be sent to an OpenCL kernel as a
   * float16.
   *
   * @param pMatrix matrix to convert
   * @return array of 16 floats representing 4x4 matrix
   */
  public static final float[] matrixToArray(Matrix4f pMatrix)
  {
    float[] lArray = new float[16];
    int k = 0;
    for (int i = 0; i < 4; i++)
      for (int j = 0; j < 4; j++)
        lArray[k++] = pMatrix.getElement(i, j);

    return lArray;
  }

  /**
   * Allocates an ClearCL buffer and copies the contents of a Matrix4f to it.
   * the buffer is newly allocated if it is not the case already or if it is of
   * the wrong size.
   *
   * @param pContext       clearcl context
   * @param pClearCLBuffer buffer
   * @param pMatrix        matrix
   * @return buffer
   */
  public static ClearCLBuffer matrixToBuffer(ClearCLContext pContext, ClearCLBuffer pClearCLBuffer, Matrix4f pMatrix)
  {
    if (pClearCLBuffer == null || pClearCLBuffer.getSizeInBytes() != 16)
    {
      if (pClearCLBuffer != null) pClearCLBuffer.close();

      pClearCLBuffer = pContext.createBuffer(MemAllocMode.Best, HostAccessType.WriteOnly, KernelAccessType.ReadOnly, 1, NativeTypeEnum.Float, 16);
    }
    float[] lMatrixToArray = MatrixUtils.matrixToArray(pMatrix);

    OffHeapMemory lBuffer = OffHeapMemory.allocateFloats(16);
    lBuffer.copyFrom(lMatrixToArray);

    pClearCLBuffer.readFrom(lBuffer, false);

    return pClearCLBuffer;
  }
}
