package cleargl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GLMatrixtest
{

  @Test
  public void testSetMultGet()
  {
    final GLMatrix lGLMatrix = new GLMatrix();
    lGLMatrix.set(0, 1, 1);
    assertEquals(1, lGLMatrix.get(0, 1), 0);
    lGLMatrix.mult(0, 1, 2);
    assertEquals(2, lGLMatrix.get(0, 1), 0);
    lGLMatrix.mult(1, 0, 0.5f);
    assertEquals(2, lGLMatrix.get(0, 1), 0);
  }

}
