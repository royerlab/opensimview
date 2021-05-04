package cleargl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLException;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Hashtable;

/**
 * ClearGeometryObject -
 * <p>
 * Created by Ulrik Guenter on 05/02/15.
 */
public class ClearGeometryObject implements GLCloseable, GLInterface
{

  private GLProgram mGLProgram;

  private GLMatrix mModelMatrix;
  private GLMatrix mViewMatrix;
  private GLMatrix mModelViewMatrix;
  private GLMatrix mProjectionMatrix;

  private final Hashtable<String, Integer> additionalBufferIds = new Hashtable<>();

  private final int[] mVertexArrayObject = new int[1];
  private final int[] mVertexBuffers = new int[3];
  private final int[] mIndexBuffer = new int[1];

  private boolean mIsDynamic = false;

  private final int mGeometryType;
  // length of vectors and texcoords
  private int mGeometrySize = 3;
  private int mTextureCoordSize = 2;

  private int mStoredIndexCount = 0;
  private int mStoredPrimitiveCount = 0;

  private final int mId;
  private static int counter = 0;

  public ClearGeometryObject(GLProgram pGLProgram, int pVectorSize, int pGeometryType)
  {
    mGLProgram = pGLProgram;
    mGeometrySize = pVectorSize;
    mTextureCoordSize = mGeometrySize - 1;
    mGeometryType = pGeometryType;

    mId = counter;
    counter++;

    // generate VAO for attachment of VBO and indices
    getGL().getGL3().glGenVertexArrays(1, mVertexArrayObject, 0);

    // generate three VBOs for coords, normals, texcoords
    getGL().glGenBuffers(3, mVertexBuffers, 0);
    getGL().glGenBuffers(1, mIndexBuffer, 0);
  }

  private static void printBuffer(FloatBuffer buf)
  {
    buf.rewind();
    System.err.print(buf.toString() + ": ");
    for (int i = 0; i < buf.remaining(); i++)
    {
      System.err.print(buf.get(i) + " ");
    }

    System.err.println(" ");

    buf.rewind();
  }

  private static void printBuffer(IntBuffer buf)
  {
    buf.rewind();
    System.err.print(buf.toString() + ": ");
    for (int i = 0; i < buf.remaining(); i++)
    {
      System.err.print(buf.get(i) + " ");
    }

    System.err.println(" ");

    buf.rewind();
  }

  public void setProgram(GLProgram program)
  {
    mGLProgram = program;
  }

  public void setVerticesAndCreateBuffer(FloatBuffer pVertexBuffer)
  {
    mStoredPrimitiveCount = pVertexBuffer.remaining() / mGeometrySize;

    getGL().getGL3().glBindVertexArray(mVertexArrayObject[0]);
    getGL().glBindBuffer(GL.GL_ARRAY_BUFFER, mVertexBuffers[0]);

    getGL().getGL3().glEnableVertexAttribArray(0);
    getGL().glBufferData(GL.GL_ARRAY_BUFFER, pVertexBuffer.limit() * (Float.SIZE / Byte.SIZE), pVertexBuffer, isDynamic() ? GL.GL_DYNAMIC_DRAW : GL.GL_STATIC_DRAW);

    getGL().getGL3().glVertexAttribPointer(0, mGeometrySize, GL.GL_FLOAT, false, 0, 0);

    getGL().getGL3().glBindVertexArray(0);
    getGL().glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
  }

  public void setArbitraryAndCreateBuffer(String name, FloatBuffer pBuffer, int pBufferGeometrySize)
  {
    // create additional buffers
    if (!additionalBufferIds.containsKey(name))
    {
      getGL().glGenBuffers(1, mVertexBuffers, mVertexBuffers.length - 1);
      additionalBufferIds.put(name, mVertexBuffers[mVertexBuffers.length - 1]);
    }

    mStoredPrimitiveCount = pBuffer.remaining() / mGeometrySize;

    getGL().getGL3().glBindVertexArray(mVertexArrayObject[0]);
    getGL().glBindBuffer(GL.GL_ARRAY_BUFFER, mVertexBuffers[mVertexBuffers.length - 1]);

    getGL().getGL3().glEnableVertexAttribArray(0);
    getGL().glBufferData(GL.GL_ARRAY_BUFFER, pBuffer.limit() * (Float.SIZE / Byte.SIZE), pBuffer, isDynamic() ? GL.GL_DYNAMIC_DRAW : GL.GL_STATIC_DRAW);

    getGL().getGL3().glVertexAttribPointer(mVertexBuffers.length - 1, pBufferGeometrySize, GL.GL_FLOAT, false, 0, 0);

    getGL().getGL3().glBindVertexArray(0);
    getGL().glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
  }

  public GLProgram getProgram()
  {
    return mGLProgram;
  }

  public void updateVertices(FloatBuffer pVertexBuffer)
  {
    mStoredPrimitiveCount = pVertexBuffer.remaining() / mGeometrySize;

    if (!isDynamic()) throw new UnsupportedOperationException("Cannot update non dynamic buffers!");

    getGL().getGL3().glBindVertexArray(mVertexArrayObject[0]);
    getGL().glBindBuffer(GL.GL_ARRAY_BUFFER, mVertexBuffers[0]);

    getGL().getGL3().glEnableVertexAttribArray(0);
    getGL().glBufferData(GL.GL_ARRAY_BUFFER, pVertexBuffer.limit() * (Float.SIZE / Byte.SIZE), pVertexBuffer, isDynamic() ? GL.GL_DYNAMIC_DRAW : GL.GL_STATIC_DRAW);

    getGL().getGL3().glVertexAttribPointer(0, mGeometrySize, GL.GL_FLOAT, false, 0, 0);

    getGL().getGL3().glBindVertexArray(0);
    getGL().glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
  }

  public void setNormalsAndCreateBuffer(FloatBuffer pNormalBuffer)
  {
    getGL().getGL3().glBindVertexArray(mVertexArrayObject[0]);
    getGL().glBindBuffer(GL.GL_ARRAY_BUFFER, mVertexBuffers[1]);

    getGL().getGL3().glEnableVertexAttribArray(1);
    getGL().glBufferData(GL.GL_ARRAY_BUFFER, pNormalBuffer.limit() * (Float.SIZE / Byte.SIZE), pNormalBuffer, isDynamic() ? GL.GL_DYNAMIC_DRAW : GL.GL_STATIC_DRAW);

    getGL().getGL3().glVertexAttribPointer(1, mGeometrySize, GL.GL_FLOAT, false, 0, 0);

    getGL().getGL3().glBindVertexArray(0);
    getGL().glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
  }

  public void updateNormals(FloatBuffer pNormalBuffer)
  {
    if (!isDynamic()) throw new UnsupportedOperationException("Cannot update non dynamic buffers!");

    getGL().getGL3().glBindVertexArray(mVertexArrayObject[0]);
    getGL().glBindBuffer(GL.GL_ARRAY_BUFFER, mVertexBuffers[1]);

    getGL().getGL3().glEnableVertexAttribArray(1);
    getGL().glBufferSubData(GL.GL_ARRAY_BUFFER, 0, pNormalBuffer.limit() * (Float.SIZE / Byte.SIZE), pNormalBuffer);

    getGL().getGL3().glVertexAttribPointer(1, mGeometrySize, GL.GL_FLOAT, false, 0, 0);

    getGL().getGL3().glBindVertexArray(0);
    getGL().glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
  }

  public void setTextureCoordsAndCreateBuffer(FloatBuffer pTextureCoordsBuffer)
  {
    getGL().getGL3().glBindVertexArray(mVertexArrayObject[0]);
    getGL().glBindBuffer(GL.GL_ARRAY_BUFFER, mVertexBuffers[2]);

    getGL().getGL3().glEnableVertexAttribArray(2);
    getGL().glBufferData(GL.GL_ARRAY_BUFFER, pTextureCoordsBuffer.limit() * (Float.SIZE / Byte.SIZE), pTextureCoordsBuffer, isDynamic() ? GL.GL_DYNAMIC_DRAW : GL.GL_STATIC_DRAW);

    getGL().getGL3().glVertexAttribPointer(2, mTextureCoordSize, GL.GL_FLOAT, false, 0, 0);

    getGL().getGL3().glBindVertexArray(0);
    getGL().glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
  }

  public void updateTextureCoords(FloatBuffer pTextureCoordsBuffer)
  {
    if (!isDynamic()) throw new UnsupportedOperationException("Cannot update non dynamic buffers!");

    getGL().getGL3().glBindVertexArray(mVertexArrayObject[0]);
    GLError.printGLErrors(getGL(), "1");

    getGL().getGL3().glBindBuffer(GL.GL_ARRAY_BUFFER, mVertexBuffers[2]);
    GLError.printGLErrors(getGL(), "2");

    getGL().getGL3().glEnableVertexAttribArray(2);
    GLError.printGLErrors(getGL(), "3");

    getGL().glBufferSubData(GL.GL_ARRAY_BUFFER, 0, pTextureCoordsBuffer.limit() * (Float.SIZE / Byte.SIZE), pTextureCoordsBuffer);
    GLError.printGLErrors(getGL(), "4");

    getGL().getGL3().glVertexAttribPointer(2, mTextureCoordSize, GL.GL_FLOAT, false, 0, 0);
    GLError.printGLErrors(getGL(), "5");

    getGL().getGL3().glBindVertexArray(0);
    GLError.printGLErrors(getGL(), "6");

    getGL().glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
    GLError.printGLErrors(getGL(), "7");

  }

  public void setIndicesAndCreateBuffer(IntBuffer pIndexBuffer)
  {

    mStoredIndexCount = pIndexBuffer.remaining();

    getGL().getGL3().glBindVertexArray(mVertexArrayObject[0]);
    getGL().glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, mIndexBuffer[0]);

    getGL().glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, pIndexBuffer.limit() * (Integer.SIZE / Byte.SIZE), pIndexBuffer, isDynamic() ? GL.GL_DYNAMIC_DRAW : GL.GL_STATIC_DRAW);

    getGL().getGL3().glBindVertexArray(0);
    getGL().glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
  }

  public void updateIndices(IntBuffer pIndexBuffer)
  {
    if (!isDynamic()) throw new UnsupportedOperationException("Cannot update non dynamic buffers!");

    mStoredIndexCount = pIndexBuffer.remaining();

    getGL().getGL3().glBindVertexArray(mVertexArrayObject[0]);
    getGL().glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, mIndexBuffer[0]);

    getGL().glBufferSubData(GL.GL_ELEMENT_ARRAY_BUFFER, 0, pIndexBuffer.limit() * (Integer.SIZE / Byte.SIZE), pIndexBuffer);

    getGL().getGL3().glBindVertexArray(0);
    getGL().glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
  }

  public void setMVP(GLMatrix m, GLMatrix v, GLMatrix p)
  {
    mModelMatrix = m;
    mViewMatrix = v;
    mProjectionMatrix = p;
  }

  public void setModelView(GLMatrix mv)
  {
    mModelViewMatrix = mv;
  }

  public void setProjection(GLMatrix p)
  {
    mProjectionMatrix = p;
  }

  public void draw()
  {
    if (mStoredIndexCount > 0)
    {
      draw(0, mStoredIndexCount);
    } else
    {
      draw(0, mStoredPrimitiveCount);
    }
  }

  public void draw(int pOffset, int pCount)
  {
    mGLProgram.use(getGL());

    if (mModelViewMatrix != null)
      mGLProgram.getUniform("modelview").setFloatMatrix(mModelViewMatrix.getFloatArray(), false);

    if (mProjectionMatrix != null)
      mGLProgram.getUniform("projection").setFloatMatrix(mProjectionMatrix.getFloatArray(), false);

    getGL().getGL3().glBindVertexArray(mVertexArrayObject[0]);

    if (mStoredIndexCount > 0)
    {
      getGL().glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, mIndexBuffer[0]);
      getGL().glDrawElements(mGeometryType, pCount, GL.GL_UNSIGNED_INT, pOffset);

      getGL().glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
    } else
    {
      getGL().glDrawArrays(mGeometryType, pOffset, pCount);
    }

    getGL().getGL3().glUseProgram(0);
  }

  @Override
  public void close() throws GLException
  {
    getGL().getGL3().glDeleteVertexArrays(mVertexArrayObject.length, mVertexArrayObject, 0);

    getGL().glDeleteBuffers(mVertexBuffers.length, mVertexBuffers, 0);
    getGL().glDeleteBuffers(mIndexBuffer.length, mIndexBuffer, 0);
  }

  @Override
  public GL getGL()
  {
    if (mGLProgram == null) return null;
    return mGLProgram.getGL();
  }

  @Override
  public int getId()
  {
    return mId;
  }

  public boolean isDynamic()
  {
    return mIsDynamic;
  }

  public void setDynamic(boolean pIsDynamic)
  {
    mIsDynamic = pIsDynamic;
  }

}
