package asdk;

import org.bridj.Pointer;

import static java.lang.Math.pow;

public class AlpaoDeformableMirrorsSpecifications
{
  public static int getFullMatrixHeightWidth(int pNumberOfActuators)
  {
    if (pNumberOfActuators == 97) return 11;
    return 0;
  }

  public static int getFullMatrixLength(int pNumberOfActuators)
  {
    return (int) pow(getFullMatrixHeightWidth(pNumberOfActuators), 2);
  }

  public static void convertLayout(int pNumberOfActuators, Pointer<Double> pSquareMirrorShapeVectorDoubleBuffer, Pointer<Double> pRawMirrorShapeVectorsDoubleBuffer)
  {
    if (pNumberOfActuators == 97)
      convertLayout97(pSquareMirrorShapeVectorDoubleBuffer, pRawMirrorShapeVectorsDoubleBuffer);
  }

  public static void convertLayout97(Pointer<Double> pSquareMirrorShapeVectorDoubleBuffer, Pointer<Double> pRawMirrorShapeVectorsDoubleBuffer)
  {
    for (int i = 0, j = 2; i < 97; i++, j++)
    {
      pRawMirrorShapeVectorsDoubleBuffer.setDoubleAtIndex(i, pSquareMirrorShapeVectorDoubleBuffer.getDoubleAtIndex(j));

      if (i == 4)
      {
        j += 5;
      } else if (i == 11)
      {
        j += 3;
      } else if (i == 20)
      {
        j += 1;
      } else if (i == 75)
      {
        j += 1;
      } else if (i == 84)
      {
        j += 3;
      } else if (i == 91)
      {
        j += 5;
      }

    }
  }
}
