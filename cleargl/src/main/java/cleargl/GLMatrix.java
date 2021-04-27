package cleargl;

import static java.lang.Math.*;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.math.VectorUtil;

public class GLMatrix implements Serializable {

	private final float[] mMatrix;
	private final float[] scratch = new float[16];

	public GLMatrix() {
		mMatrix = new float[16];
	}

	public GLMatrix(final float[] matrix) {
		if (matrix.length == 16) {
			mMatrix = matrix;
		} else {
			System.err.println("Incompatible matrix dimensions while converting from float!");
			mMatrix = new float[16];
		}
		// TODO: error handling! Throw exception?
	}

	public float get(final int pRow, final int pColumn) {
		return mMatrix[4 * pRow + pColumn];
	}

	public void set(final int pRow, final int pColumn, final float pValue) {
		mMatrix[4 * pRow + pColumn] = pValue;
	}

	public void mult(final int pRow, final int pColumn, final float pValue) {
		mMatrix[4 * pRow + pColumn] *= pValue;
	}

	public void setIdentity() {
		FloatUtil.makeIdentity(mMatrix);
	}

	public static GLMatrix getIdentity() {
		final GLMatrix lGLMatrix = new GLMatrix();
		lGLMatrix.setIdentity();
		return lGLMatrix;
	}

	public static GLMatrix getTranslation(final float x, final float y, final float z) {
		final GLMatrix m = getIdentity();

		m.set(0, 3, x);
		m.set(1, 3, y);
		m.set(2, 3, z);

		return m;
	}

	public static GLMatrix getScaling(final float sx, final float sy, final float sz) {
		final GLMatrix m = getIdentity();
		final float[] scaling = {sx, sy, sz, 1.0f};

		m.mult(scaling);

		return m;
	}

	public static GLMatrix getTranslation(final GLVector v) {
		final GLMatrix m = getIdentity();

		m.set(0, 3, v.x());
		m.set(1, 3, v.y());
		m.set(2, 3, v.z());

		return m;
	}

	public static GLMatrix getScaling(final GLVector v) {
		final GLMatrix m = getIdentity();
		final float[] scaling = {v.x(), v.y(), v.z(), 1.0f};

		m.mult(scaling);

		return m;
	}

	public void mult(final GLMatrix pGLMatrix) {
		FloatUtil.multMatrix(mMatrix, pGLMatrix.mMatrix);
	}

	public void multinv(final GLMatrix pGLMatrix) {
		pGLMatrix.invert();
		mult(pGLMatrix);
	}

	public GLMatrix setFrustumMatrix(final float left, final float right, final float bottom, final float top,
			final float near, final float far) {
		FloatUtil.makeFrustum(mMatrix, 0, true, left, right, bottom, top, near, far);

		return this;
	}

	public GLMatrix setPerspectiveProjectionMatrix(final float pFOV,
			final float pAspectRatio,
			final float pNearPlane,
			final float pFarPlane) {
		FloatUtil.makePerspective(mMatrix,
				0,
				true,
				pFOV,
				pAspectRatio,
				pNearPlane,
				pFarPlane);

		return this;
	}

	public void setPerspectiveAnaglyphProjectionMatrix(final float fov,
			final float convergenceDist,
			final float aspectRatio,
			final float eyeSeparation,
			final float near,
			final float far) {

		float top, bottom, left, right;

		top = near * (float) Math.tan(fov / 2);
		bottom = -top;

		final float a = aspectRatio * (float) Math.tan(fov / 2) * convergenceDist;
		final float b = a - eyeSeparation / 2.0f;
		final float c = a + eyeSeparation / 2.0f;

		left = -b * near / convergenceDist;
		right = c * near / convergenceDist;

		FloatUtil.makeFrustum(mMatrix, 0, true,
				left, right, bottom, top, near, far);
	}

	public GLMatrix setGeneralizedPerspectiveProjectionMatrix(final GLVector lowerLeft, final GLVector lowerRight,
			final GLVector upperLeft,
			final GLVector eye,
			final float near, final float far) {

		GLVector pa = lowerLeft;
		GLVector pb = lowerRight;
		GLVector pc = upperLeft;

		GLVector vr = lowerRight.minus(lowerLeft);
		GLVector vu = upperLeft.minus(lowerLeft);

		GLVector va = lowerLeft.minus(eye);
		GLVector vb = lowerRight.minus(eye);
		GLVector vc = upperLeft.minus(eye);

		vr.normalize();
		vu.normalize();
		GLVector vn = vr.cross(vu).normalize();

		float distance = -1.0f * va.times(vn);
		GLVector ep = new GLVector(eye.x(), eye.y(), eye.z(), 1.0f);

		final GLMatrix mt = new GLMatrix(new float[]{
				vr.x(), vr.y(), vr.z(), 0.0f,
				vu.x(), vu.y(), vu.z(), 0.0f,
				vn.x(), vn.y(), vn.z(), 0.0f,
				lowerLeft.x(), lowerLeft.y(), lowerLeft.z(), 1.0f});
		mt.invert();

		GLVector res = mt.mult(ep);
		float nd = near / distance;
		float left = -res.x();
		float right = Math.abs(lowerLeft.minus(lowerRight).x()) - res.x();
		float bottom = -res.y();
		float top = Math.abs(lowerLeft.minus(upperLeft).y()) - res.y();
		float n = -res.z();

		float s = 0.1f / n;

		System.err.println(eye + ", " + (right + res.x()) + "/" + (top + res.y()) + " => " + distance + "/" + n + " -> "
				+ left + "/" + right + "/" + bottom + "/" + top + ", s=" + s);
		FloatUtil.makeFrustum(mMatrix, 0, true, left * s, right * s, bottom * s, top * s, n * s, far);

		final GLMatrix flip = new GLMatrix(new float[]{
				1.0f, 0.0f, 0.0f, 0.0f,
				0.0f, 1.0f, 0.0f, 0.0f,
				0.0f, 0.0f, -1.0f, 0.0f,
				0.0f, 0.0f, 1.0f, 1.0f});

		// this.mult(flip);
		return this;
	}

	public GLMatrix setFrustumProjectionMatrix(final float top, final float bottom,
			final float left, final float right, final float near, final float far) {

		FloatUtil.makeFrustum(mMatrix, 0, true,
				left, right, bottom, top, near, far);

		return this;
	}

	public void setOrthoProjectionMatrix(final float pLeft,
			final float pRight,
			final float pBottom,
			final float pTop,
			final float pZNear,
			final float pZFar) {
		FloatUtil.makeOrtho(mMatrix,
				0,
				true,
				pLeft,
				pRight,
				pBottom,
				pTop,
				pZNear,
				pZFar);
	}

	public static GLMatrix getOrthoProjectionMatrix(final float pLeft,
			final float pRight,
			final float pBottom,
			final float pTop,
			final float pZNear,
			final float pZFar) {
		final GLMatrix lGLMatrix = new GLMatrix();
		lGLMatrix.setOrthoProjectionMatrix(pLeft,
				pRight,
				pBottom,
				pTop,
				pZNear,
				pZFar);
		return lGLMatrix;
	}

	public GLMatrix setCamera(final float pPosX,
			final float pPosY,
			final float pPosZ,
			final float pLookAtX,
			final float pLookAtY,
			final float pLookAtZ,
			final float pUpX,
			final float pUpY,
			final float pUpZ) {
		final float[] lPosition = new float[]{pPosX, pPosY, pPosZ};
		final float[] lLookAt = new float[]{pLookAtX, pLookAtY, pLookAtZ};
		final float[] lUp = new float[]{pUpX, pUpY, pUpZ};

		FloatUtil.makeLookAt(mMatrix,
				0,
				lPosition,
				0,
				lLookAt,
				0,
				lUp,
				0,
				new float[16]);

		return this;
	}

	public GLMatrix setCamera(final GLVector position, final GLVector target, final GLVector up) {
		setCamera(
				position.x(), position.y(), position.z(),
				target.x(), target.y(), target.z(),
				up.x(), up.y(), up.z());

		return this;
	}

	public void euler(final double bankX,
			final double headingY,
			final double attitudeZ) {
		FloatUtil.makeRotationEuler(mMatrix,
				0,
				(float) bankX,
				(float) headingY,
				(float) attitudeZ);

	}

	public GLMatrix rotEuler(final double bankX,
			final double headingY,
			final double attitudeZ) {
		final float[] lRotMatrix = FloatUtil.makeRotationEuler(scratch,
				0,
				(float) bankX,
				(float) headingY,
				(float) attitudeZ);
		FloatUtil.multMatrix(mMatrix, lRotMatrix);

		return this;
	}

	public GLMatrix translate(final float pDeltaX, final float pDeltaY, final float pDeltaZ) {
		final float[] lTranslationMatrix = FloatUtil.makeTranslation(scratch,
				true,
				pDeltaX,
				pDeltaY,
				pDeltaZ);

		FloatUtil.multMatrix(mMatrix, lTranslationMatrix);
		return this;
	}

	public GLMatrix translate(final GLVector v) {
		return this.translate(v.x(), v.y(), v.z());
	}

	public GLMatrix scale(final float pScaleX, final float pScaleY, final float pScaleZ) {

		Arrays.fill(scratch, 0.0f);
		final float[] lScaleMatrix = FloatUtil.makeScale(scratch,
				false,
				pScaleX,
				pScaleY,
				pScaleZ);

		FloatUtil.multMatrix(mMatrix, lScaleMatrix);
		return this;
	}

	public void invscale(final float pScaleX, final float pScaleY, final float pScaleZ) {

		final float[] lScaleMatrix = FloatUtil.makeScale(scratch,
				true,
				1.0f / pScaleX,
				1.0f / pScaleY,
				1.0f / pScaleZ);

		FloatUtil.multMatrix(mMatrix, lScaleMatrix);
	}

	public GLMatrix mult(final Quaternion pQuaternion) {
		final float[] lQuaternionMatrix = pQuaternion.toMatrix(scratch,
				0);
		FloatUtil.multMatrix(mMatrix, lQuaternionMatrix);

		return this;
	}

	public float[] mult(final float[] pVector) {
		final float[] lResultVector = new float[4];
		mulColMat4Vec4(lResultVector, mMatrix, pVector);
		return lResultVector;
	}

	public GLVector mult(final GLVector vec) {
		return new GLVector(mult(vec.toFloatArray()));
	}

	private static float[] mulColMat4Vec4(final float[] result,
			final float[] colMatrix,
			final float[] vec) {
		result[0] = vec[0] * colMatrix[0]
				+ vec[1]
						* colMatrix[4]
				+ vec[2]
						* colMatrix[8]
				+ vec[3]
						* colMatrix[12];
		result[1] = vec[0] * colMatrix[1]
				+ vec[1]
						* colMatrix[5]
				+ vec[2]
						* colMatrix[9]
				+ vec[3]
						* colMatrix[13];
		result[2] = vec[0] * colMatrix[2]
				+ vec[1]
						* colMatrix[6]
				+ vec[2]
						* colMatrix[10]
				+ vec[3]
						* colMatrix[14];
		result[3] = vec[0] * colMatrix[3]
				+ vec[1]
						* colMatrix[7]
				+ vec[2]
						* colMatrix[11]
				+ vec[3]
						* colMatrix[15];

		return result;
	}

	private static float[] mulRowMat4Vec4(final float[] result,
			final float[] rowMatrix,
			final float[] vec) {

		result[0] = vec[0] * rowMatrix[0]
				+ vec[1]
						* rowMatrix[1]
				+ vec[2]
						* rowMatrix[2]
				+ vec[3]
						* rowMatrix[3];
		result[1] = vec[0] * rowMatrix[4]
				+ vec[1]
						* rowMatrix[5]
				+ vec[2]
						* rowMatrix[6]
				+ vec[3]
						* rowMatrix[7];
		result[2] = vec[0] * rowMatrix[8]
				+ vec[1]
						* rowMatrix[9]
				+ vec[2]
						* rowMatrix[10]
				+ vec[3]
						* rowMatrix[11];
		result[3] = vec[0] * rowMatrix[12]
				+ vec[1]
						* rowMatrix[13]
				+ vec[2]
						* rowMatrix[14]
				+ vec[3]
						* rowMatrix[15];

		return result;
	}

	@Override
	public GLMatrix clone() {
		final GLMatrix lGLMatrix = new GLMatrix();
		lGLMatrix.copyFrom(this);
		return lGLMatrix;
	}

	public void copyFrom(final GLMatrix rhs) {
		System.arraycopy(rhs.getFloatArray(),
				0,
				mMatrix,
				0,
				mMatrix.length);
	}

	public float[] getFloatArray() {
		return mMatrix;
	}

	public float[] getTransposedFloatArray() {
		final GLMatrix lGLMatrix = new GLMatrix();
		System.arraycopy(mMatrix,
				0,
				lGLMatrix.getFloatArray(),
				0,
				mMatrix.length);
		lGLMatrix.transpose();
		return lGLMatrix.getFloatArray();
	}

	public GLMatrix invert() {
		System.arraycopy(mMatrix, 0, scratch, 0, mMatrix.length);

		FloatUtil.invertMatrix(scratch, mMatrix);
		return this;
	}

	public GLMatrix getInverse() {
		final GLMatrix inverse;
		System.arraycopy(mMatrix, 0, scratch, 0, mMatrix.length);

		FloatUtil.invertMatrix(mMatrix, scratch);
		inverse = new GLMatrix(scratch);

		return inverse;
	}

	public GLMatrix transpose() {
		System.arraycopy(mMatrix, 0, scratch, 0, mMatrix.length);

		FloatUtil.transposeMatrix(scratch, mMatrix);

		return this;
	}

	@Override
	public String toString() {
		final StringBuilder lStringBuilder = new StringBuilder();
		FloatUtil.matrixToString(lStringBuilder,
				"",
				"%10.5f",
				mMatrix,
				0,
				4,
				4,
				true);
		return "GLMatrix:\n" + lStringBuilder.toString();
	}

	public static GLMatrix fromQuaternion(final Quaternion q) {
		final float[] rotationMatrix = new float[16];
		q.toMatrix(rotationMatrix, 0);

		return new GLMatrix(rotationMatrix);
	}

	public static void mult(final float[] pVector, final float pValue) {
		for (int i = 0; i < pVector.length; i++)
			pVector[i] *= pValue;
	}

	public static void add(final float[] pVector, final float pValue) {
		for (int i = 0; i < pVector.length; i++)
			pVector[i] += pValue;
	}

	public static void add(final float[] pA, final float[] pB) {
		for (int i = 0; i < pA.length; i++)
			pA[i] += pB[i];
	}

	public static void sub(final float[] pA, final float[] pB) {
		final int lLength = min(pA.length, pB.length);
		for (int i = 0; i < lLength; i++)
			pA[i] = pA[i] - pB[i];
	}

	public static void normalize(final float[] pVector) {
		double lSumOfSquares = 0;
		for (int i = 0; i < pVector.length; i++)
			lSumOfSquares += pVector[i] * pVector[i];

		final double lNorm = sqrt(lSumOfSquares);

		if (abs(lNorm) == Double.MIN_VALUE)
			for (int i = 0; i < pVector.length; i++)
				pVector[i] = 0;

		for (int i = 0; i < pVector.length; i++)
			pVector[i] /= lNorm;
	}

	public static float dot(final float[] pA, final float[] pB) {
		float lDot = 0;
		for (int i = 0; i < pA.length; i++)
			lDot += pA[i] * pB[i];

		return lDot;
	}

	public static float[] clone(final float[] pVector) {
		final float[] lClone = new float[pVector.length];
		System.arraycopy(pVector, 0, lClone, 0, pVector.length);
		return lClone;
	}

	public static float norm(final float[] pVector) {
		float lNorm = 0;
		for (int i = 0; i < pVector.length; i++)
			lNorm += pVector[i] * pVector[i];

		lNorm = (float) sqrt(lNorm);

		return lNorm;
	}

	public static void cross(final float[] pResult, final float[] pA, final float[] pB) {
		VectorUtil.crossVec3(pResult, pA, pB);
	}

	public static void zero(final float[] pVector) {
		for (int i = 0; i < pVector.length; i++)
			pVector[i] = 0;
	}

	public static boolean compare(final GLMatrix left, final GLMatrix right, final boolean explainDiff) {
		final float EPSILON = 0.00001f;
		final float[] l = left.getFloatArray();
		final float[] r = right.getFloatArray();

		for (int i = 0; i < l.length; i++) {
			if (Math.abs(l[i] - r[i]) > EPSILON) {
				if (explainDiff) {
					System.err.println(
							"Matrices differ at least in component " + i + ", |delta|=" + Math.abs(l[i] - r[i]));
					System.err.println("LHS: " + left);
					System.err.println("RHS: " + right);
				}
				return false;
			}
		}

		return true;
	}

	public ByteBuffer push(ByteBuffer buffer) {
		final int pos = buffer.position();
		buffer.asFloatBuffer().put(mMatrix);
		buffer.position(pos);
		return buffer;
	}

	public ByteBuffer put(ByteBuffer buffer) {
		int position = buffer.position();
		buffer.asFloatBuffer().put(mMatrix);
		buffer.position(position + mMatrix.length * 4);
		return buffer;
	}

	public ByteBuffer toBuffer() {
		ByteBuffer b = ByteBuffer.allocateDirect(16 * 4);
		b.asFloatBuffer().put(mMatrix);
		b.position(0);
		b.limit(16 * 4);

		return b;
	}

	@Override
	public int hashCode() {
		long result = 1;
		int i = 0;
		for (float element : mMatrix) {
			long bits = Float.floatToIntBits(element);
			result = 31 * result + bits ^ ((++i + bits) >>> 32);
		}

		return (int) result;
	}

}
