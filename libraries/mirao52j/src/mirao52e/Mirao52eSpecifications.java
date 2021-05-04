package mirao52e;

import org.bridj.Pointer;

public class Mirao52eSpecifications
{
  public static final int cMirao52NumberOfActuators = 52;
  public static final int cMirao52FullMatrixMirrorShapeVectorLength = 64;

  public static void convertLayout(Pointer<Double> pSquareMirrorShapeVectorDoubleBuffer, Pointer<Double> pRawMirrorShapeVectorsDoubleBuffer)
  {
    for (int i = 0, j = 2; i < cMirao52NumberOfActuators; i++, j++)
    {
      pRawMirrorShapeVectorsDoubleBuffer.setDoubleAtIndex(i, pSquareMirrorShapeVectorDoubleBuffer.getDoubleAtIndex(j));

      if (i == 3)
      {
        j += 3;
      } else if (i == 9)
      {
        j += 1;
      } else if (i == 41)
      {
        j += 1;
      } else if (i == 47)
      {
        j += 3;
      }
    }
  }

  public static void convertLayout(double[] pSquareMirrorShapeVectorDoubleArray, double[] pRawMirrorShapeVectorsDoubleArray)
  {
    for (int i = 0, j = 2; i < cMirao52NumberOfActuators; i++, j++)
    {
      pRawMirrorShapeVectorsDoubleArray[i] = pSquareMirrorShapeVectorDoubleArray[j];

      if (i == 3)
      {
        j += 3;
      } else if (i == 9)
      {
        j += 1;
      } else if (i == 41)
      {
        j += 1;
      } else if (i == 47)
      {
        j += 3;
      }
    }
  }
}
