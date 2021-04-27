package clearcl.ops.noise;

import java.io.IOException;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.ClearCLQueue;
import clearcl.ocllib.OCLlib;
import clearcl.ops.OpsBase;

/**
 * Fractional Brownian Noise generation.
 *
 * @author royer
 */
public class FractionalBrownianNoise extends OpsBase
{

  private ClearCLKernel mFBM2D, mFBM3D;

  private float sx = 1, sy = 1, sz = 1;
  private float ox = 0, oy = 0, oz = 0;
  private int mOctaves = 6;

  private int mSeed = 0;

  /**
   * Instanciates a Noise generation object given a queue
   * 
   * @param pClearCLQueue
   *          queue
   * @throws IOException
   *           thrown if kernels cannot be read
   */
  public FractionalBrownianNoise(ClearCLQueue pClearCLQueue) throws IOException
  {
    super(pClearCLQueue);

    ClearCLProgram lNoiseProgram =
                                 getContext().createProgram(OCLlib.class,
                                                            "noise/noisetexture.cl");
    // lNoiseProgram.addBuildOptionAllMathOpt();
    lNoiseProgram.buildAndLog();
    System.out.println(lNoiseProgram.getSourceCode());

    mFBM2D = lNoiseProgram.createKernel("fbmrender2");
    mFBM3D = lNoiseProgram.createKernel("fbmrender3");
  }

  /**
   * Sets seed for random number generation
   * 
   * @param pSeed
   *          seed
   */
  public void setSeed(int pSeed)
  {
    mSeed = pSeed;
  }

  private void setArguments(ClearCLKernel pNoiseKernel,
                            ClearCLBuffer pBuffer)
  {
    pNoiseKernel.setArgument("output", pBuffer);
    pNoiseKernel.setArgument("sx", sx);
    pNoiseKernel.setArgument("sy", sy);
    pNoiseKernel.setOptionalArgument("sz", sz);
    pNoiseKernel.setArgument("ox", ox);
    pNoiseKernel.setArgument("oy", oy);
    pNoiseKernel.setOptionalArgument("oz", oz);
    pNoiseKernel.setOptionalArgument("seed", mSeed);
    pNoiseKernel.setArgument("octaves", mOctaves);
  }

  /**
   * Generates 2D Fractional Brownian noise.
   * 
   * @param pBuffer
   *          buffer to store noise data
   * @param pBlockingRun
   *          true -> blocking call
   */
  public void fbm2D(ClearCLBuffer pBuffer, boolean pBlockingRun)
  {
    long lWidth = pBuffer.getWidth();
    long lHeight = pBuffer.getHeight();

    setArguments(mFBM2D, pBuffer);

    mFBM2D.setGlobalSizes(lWidth, lHeight);
    mFBM2D.run(pBlockingRun);

    pBuffer.notifyListenersOfChange(getQueue());
  }

  /**
   * Generates 3D Fractional Brownian noise.
   * 
   * @param pBuffer
   *          buffer to store noise data
   * @param pBlockingRun
   *          true -> blocking call
   */
  public void fbm3D(ClearCLBuffer pBuffer, boolean pBlockingRun)
  {
    long lWidth = pBuffer.getWidth();
    long lHeight = pBuffer.getHeight();
    long lDepth = pBuffer.getDepth();

    setArguments(mFBM3D, pBuffer);

    mFBM3D.setGlobalSizes(lWidth, lHeight, lDepth);
    mFBM3D.run(pBlockingRun);

    pBuffer.notifyListenersOfChange(getQueue());
  }

}
