package clearcl.ops.render;

import clearcl.*;
import clearcl.ocllib.OCLlib;
import clearcl.ops.OpsBase;
import clearcl.ops.render.enums.Algorithm;
import clearcl.ops.render.enums.Parameter;
import clearcl.util.MatrixUtils;

import javax.vecmath.Matrix4f;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Headless modular volume rendering
 *
 * @author royer
 */
public class ImageRender extends OpsBase
{

  private ClearCLKernel mRenderKernel;

  private final ConcurrentHashMap<Parameter, Float> mFloatParameters = new ConcurrentHashMap<Parameter, Float>();
  private final ConcurrentHashMap<Parameter, Integer> mIntegerParameters = new ConcurrentHashMap<Parameter, Integer>();
  private final ConcurrentHashMap<Parameter, Matrix4f> mMatrixParameters = new ConcurrentHashMap<Parameter, Matrix4f>();
  private final ConcurrentHashMap<Parameter, ClearCLBuffer> mMatrixBufferParameters = new ConcurrentHashMap<Parameter, ClearCLBuffer>();

  /**
   * Instanciates a volume renderer given a queue
   *
   * @param pClearCLQueue          queue
   * @param pVolumeRenderAlgorithm type of volume rendering algorithm
   */
  public ImageRender(ClearCLQueue pClearCLQueue, Algorithm pVolumeRenderAlgorithm)
  {
    super(pClearCLQueue);

    setDefaultParameters();

    try
    {
      String lKernelPath = pVolumeRenderAlgorithm.getKernelPath();
      String lKernelName = pVolumeRenderAlgorithm.getKernelName();

      ClearCLProgram lNoiseProgram = getContext().createProgram(OCLlib.class, lKernelPath);
      // lNoiseProgram.addBuildOptionAllMathOpt();
      lNoiseProgram.buildAndLog();
      System.out.println(lNoiseProgram.getSourceCode());

      mRenderKernel = lNoiseProgram.createKernel(lKernelName);
    } catch (Throwable e)
    {
      throw new RuntimeException(e);
    }
  }

  private void setDefaultParameters()
  {
    setFloatParameter(Parameter.Min, 0);
    setFloatParameter(Parameter.Max, 1);
    setFloatParameter(Parameter.Gamma, 1);
    setFloatParameter(Parameter.Alpha, 0.2f);
    setIntegerParameter(Parameter.MaxSteps, 64);

    Matrix4f lIdentityMatrix = new Matrix4f();
    lIdentityMatrix.setIdentity();

    setMatrixParameter(Parameter.ProjectionMatrix, lIdentityMatrix);
    setMatrixParameter(Parameter.ModelViewMatrix, lIdentityMatrix);
  }

  @SuppressWarnings("unused")
  private void setArguments(ClearCLKernel pNoiseKernel, ClearCLBuffer pBuffer)
  {
    pNoiseKernel.setArgument("output", pBuffer);

  }

  /**
   * Sets float parameter.
   *
   * @param pParameter parameter
   * @param pValue     value
   */
  public void setFloatParameter(Parameter pParameter, float pValue)
  {
    mFloatParameters.put(pParameter, pValue);
  }

  /**
   * Sets integer parameter
   *
   * @param pParameter parameter
   * @param pValue     value
   */
  public void setIntegerParameter(Parameter pParameter, int pValue)
  {
    mIntegerParameters.put(pParameter, pValue);
  }

  /**
   * Sets matrix parameter
   *
   * @param pParameter parameter
   * @param pMatrix    matrix
   */
  public void setMatrixParameter(Parameter pParameter, Matrix4f pMatrix)
  {
    mMatrixParameters.put(pParameter, pMatrix);
  }

  private ClearCLBuffer getMatrixBuffer(Parameter pParameter)
  {
    Matrix4f lMatrix = mMatrixParameters.get(pParameter);
    if (lMatrix == null) return null;

    ClearCLBuffer lBuffer = mMatrixBufferParameters.get(pParameter);
    lBuffer = MatrixUtils.matrixToBuffer(getContext(), lBuffer, lMatrix);
    mMatrixBufferParameters.put(pParameter, lBuffer);

    return lBuffer;
  }

  /**
   * Renders
   *
   * @param p3DImage     3D image input
   * @param pRGBABuffer  RGBA buffer output
   * @param waitToFinish true -> wait fro computation to finish.
   */
  public void render(ClearCLImage p3DImage, ClearCLBuffer pRGBABuffer, boolean waitToFinish)
  {
    mRenderKernel.setArgument("image", p3DImage);
    mRenderKernel.setArgument("rgbabuffer", pRGBABuffer);

    for (Parameter lParameter : Parameter.values())
    {
      String lKernelArgumentName = lParameter.getKernelArgumentName();
      Float lFloat = mFloatParameters.get(lParameter);
      if (lFloat != null) mRenderKernel.setOptionalArgument(lKernelArgumentName, lFloat);

      Integer lInteger = mIntegerParameters.get(lParameter);
      if (lInteger != null) mRenderKernel.setOptionalArgument(lKernelArgumentName, lInteger);
      ClearCLBuffer lMatrixBuffer = getMatrixBuffer(lParameter);
      if (lMatrixBuffer != null) mRenderKernel.setOptionalArgument(lKernelArgumentName, lMatrixBuffer);
    }

    mRenderKernel.setGlobalSizes(pRGBABuffer);
    mRenderKernel.run(waitToFinish);

  }

}
