package clearcl.ops.math;

import java.io.IOException;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLHostImageBuffer;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.ClearCLQueue;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.KernelAccessType;
import clearcl.interfaces.ClearCLImageInterface;
import clearcl.ocllib.OCLlib;
import clearcl.ops.OpsBase;
import coremem.buffers.ContiguousBuffer;
import coremem.enums.NativeTypeEnum;

/**
 * Computes the min max values of an image of buffer.
 *
 * @author royer
 */
public class MinMax extends OpsBase
{

  private ClearCLBuffer mScratchBuffer;
  private ClearCLHostImageBuffer mScratchHostBuffer;
  private ClearCLKernel mMinKernelBufferF, mMinKernelImage1F,
      mMinKernelImage2F, mMinKernelImage3F;
  private ClearCLKernel mMinKernelImage1UI, mMinKernelImage2UI,
      mMinKernelImage3UI;
  private ClearCLKernel mMinKernelImage1I, mMinKernelImage2I,
      mMinKernelImage3I;

  /**
   * Instanciates a MinMax object given a queue.
   * 
   * @param pClearCLQueue
   *          queue
   * @throws IOException
   *           thrown if kernels canot be read.
   */
  public MinMax(ClearCLQueue pClearCLQueue) throws IOException
  {
    super(pClearCLQueue);

    {
      ClearCLProgram lMinMaxFloatProgram =
                                         getContext().createProgram(OCLlib.class,
                                                                    "reduction/reductions.cl");
      lMinMaxFloatProgram.addBuildOptionAllMathOpt();
      lMinMaxFloatProgram.addDefine("FLOAT");
      lMinMaxFloatProgram.buildAndLog();

      mMinKernelBufferF =
                        lMinMaxFloatProgram.createKernel("reduce_min_buffer");

      mMinKernelImage1F =
                        lMinMaxFloatProgram.createKernel("reduce_min_image_1d");
      mMinKernelImage2F =
                        lMinMaxFloatProgram.createKernel("reduce_min_image_2d");
      mMinKernelImage3F =
                        lMinMaxFloatProgram.createKernel("reduce_min_image_3d");
    }

    {
      ClearCLProgram lMinMaxUintProgram =
                                        getContext().createProgram(OCLlib.class,
                                                                   "reduction/reductions.cl");
      lMinMaxUintProgram.addBuildOptionAllMathOpt();
      lMinMaxUintProgram.addDefine("UINT");
      lMinMaxUintProgram.buildAndLog();

      mMinKernelImage1UI =
                         lMinMaxUintProgram.createKernel("reduce_min_image_1d");
      mMinKernelImage2UI =
                         lMinMaxUintProgram.createKernel("reduce_min_image_2d");
      mMinKernelImage3UI =
                         lMinMaxUintProgram.createKernel("reduce_min_image_3d");

    }

    {
      ClearCLProgram lMinMaxIntProgram =
                                       getContext().createProgram(OCLlib.class,
                                                                  "reduction/reductions.cl");
      lMinMaxIntProgram.addBuildOptionAllMathOpt();
      lMinMaxIntProgram.addDefine("INT");
      lMinMaxIntProgram.buildAndLog();

      mMinKernelImage1I =
                        lMinMaxIntProgram.createKernel("reduce_min_image_1d");
      mMinKernelImage2I =
                        lMinMaxIntProgram.createKernel("reduce_min_image_2d");
      mMinKernelImage3I =
                        lMinMaxIntProgram.createKernel("reduce_min_image_3d");

    }

  }

  /**
   * Computes the min max of an image or buffer using a two step reduction
   * scheme.
   * 
   * @param pClearCLImage
   *          image
   * @param pReduction
   *          reduction factor
   * @return {min,max} float array
   */
  public float[] minmax(ClearCLImageInterface pClearCLImage,
                        int pReduction)
  {
    if (pClearCLImage instanceof ClearCLBuffer)
      return minmax((ClearCLBuffer) pClearCLImage, pReduction);
    else if (pClearCLImage instanceof ClearCLImage)
      return minmax((ClearCLImage) pClearCLImage, pReduction);
    return null;
  }

  /**
   * Computes the min max of an image using a two step reduction scheme.
   * 
   * @param pBuffer
   *          buffer
   * @param lReduction
   *          reduction factor
   * @return {min,max} float array
   */
  public float[] minmax(ClearCLBuffer pBuffer, int lReduction)
  {
    if (mScratchBuffer == null
        || mScratchBuffer.getLength() != 2 * lReduction)
    {
      mScratchBuffer =
                     getContext().createBuffer(HostAccessType.ReadOnly,
                                               KernelAccessType.WriteOnly,
                                               NativeTypeEnum.Float,
                                               2 * lReduction);
      mScratchHostBuffer =
                         ClearCLHostImageBuffer.allocateSameAs(mScratchBuffer);
    }

    mMinKernelBufferF.setArgument("buffer", pBuffer);
    mMinKernelBufferF.setArgument("result", mScratchBuffer);
    mMinKernelBufferF.setArgument("length",
                                  pBuffer.getLength()
                                            * pBuffer.getNumberOfChannels());
    mMinKernelBufferF.setGlobalSizes(Math.min(pBuffer.getLength()
                                              * pBuffer.getNumberOfChannels(),
                                              lReduction));

    mMinKernelBufferF.run();
    mScratchBuffer.copyTo(mScratchHostBuffer, true);

    ContiguousBuffer lContiguousBuffer =
                                       ContiguousBuffer.wrap(mScratchHostBuffer.getContiguousMemory());

    float lMin = Float.POSITIVE_INFINITY;
    float lMax = Float.NEGATIVE_INFINITY;
    lContiguousBuffer.rewind();
    while (lContiguousBuffer.hasRemainingFloat())
    {
      float lMinValue = lContiguousBuffer.readFloat();
      lMin = Math.min(lMin, lMinValue);
      float lMaxValue = lContiguousBuffer.readFloat();
      lMax = Math.max(lMax, lMaxValue);
    }

    return new float[]
    { lMin, lMax };
  }

  /**
   * Computes the min max of an image using a two step reduction scheme.
   * 
   * @param pImage
   *          image
   * @param lReduction
   *          reduction factor
   * @return {min,max} float array
   */
  public float[] minmax(ClearCLImage pImage, int lReduction)
  {

    ClearCLKernel lKernel = null;
    long[] lGlobalSizes = null;
    long lVolume = 1;

    ImageChannelDataType lDataType = pImage.getChannelDataType();

    if (pImage.getDimension() == 1)
    {
      if (lDataType.isNormalized() || lDataType.isFloat())
        lKernel = mMinKernelImage1F;
      else if (lDataType.isInteger() && lDataType.isUnSigned())
        lKernel = mMinKernelImage1UI;
      else if (lDataType.isInteger() && lDataType.isSigned())
        lKernel = mMinKernelImage1I;

      lGlobalSizes = new long[]
      { lReduction };
      lVolume = lReduction;
    }
    else if (pImage.getDimension() == 2)
    {
      if (lDataType.isNormalized() || lDataType.isFloat())
        lKernel = mMinKernelImage2F;
      else if (lDataType.isInteger() && lDataType.isUnSigned())
        lKernel = mMinKernelImage2UI;
      else if (lDataType.isInteger() && lDataType.isSigned())
        lKernel = mMinKernelImage2I;

      lGlobalSizes = new long[]
      { lReduction, lReduction };
      lVolume = lReduction * lReduction;
    }
    else if (pImage.getDimension() == 3)
    {
      if (lDataType.isNormalized() || lDataType.isFloat())
        lKernel = mMinKernelImage3F;
      else if (lDataType.isInteger() && lDataType.isUnSigned())
        lKernel = mMinKernelImage3UI;
      else if (lDataType.isInteger() && lDataType.isSigned())
        lKernel = mMinKernelImage3I;

      lGlobalSizes = new long[]
      { lReduction, lReduction, lReduction };
      lVolume = lReduction * lReduction * lReduction;
    }

    if (mScratchBuffer == null
        || mScratchBuffer.getLength() != 2 * lVolume)
    {
      mScratchBuffer =
                     getContext().createBuffer(HostAccessType.ReadWrite,
                                               KernelAccessType.WriteOnly,
                                               NativeTypeEnum.Float,
                                               2 * lVolume);
      mScratchHostBuffer =
                         ClearCLHostImageBuffer.allocateSameAs(mScratchBuffer);
    }

    lKernel.setArgument("image", pImage);
    lKernel.setArgument("result", mScratchBuffer);

    lKernel.setGlobalSizes(lGlobalSizes);

    lKernel.run();
    mScratchBuffer.copyTo(mScratchHostBuffer, true);

    ContiguousBuffer lContiguousBuffer =
                                       ContiguousBuffer.wrap(mScratchHostBuffer.getContiguousMemory());

    float lMin = Float.POSITIVE_INFINITY;
    float lMax = Float.NEGATIVE_INFINITY;
    lContiguousBuffer.rewind();
    while (lContiguousBuffer.hasRemainingFloat())
    {
      float lMinValue = lContiguousBuffer.readFloat();
      lMin = Math.min(lMin, lMinValue);
      float lMaxValue = lContiguousBuffer.readFloat();
      lMax = Math.max(lMax, lMaxValue);
      // if (lMin != 0 && lMax != 0)
      // System.out.format("reduced: min=%f, max=%f \n", lMin, lMax);
    }

    return new float[]
    { lMin, lMax };
  }
}
