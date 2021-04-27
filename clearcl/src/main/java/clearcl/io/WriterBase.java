package clearcl.io;

import coremem.enums.NativeTypeEnum;

/**
 * This base class provides common fields and methods needed by implementations
 * of image writers.
 *
 * @author royer
 */
public abstract class WriterBase implements WriterInterface
{
  protected float mScaling, mOffset;
  private boolean mOverwrite;
  private NativeTypeEnum mNativeTypeEnum;

  /**
   * Instanciates an Image raw writer. The image values are scaled according to
   * y = a*x+b and the data is saved using the provided data type.
   * 
   * @param pNativeTypeEnum
   *          native type to save to.
   * @param pScaling
   *          value scaling a
   * @param pOffset
   *          value offset b
   */
  public WriterBase(NativeTypeEnum pNativeTypeEnum,
                    float pScaling,
                    float pOffset)
  {
    super();
    setScaling(pScaling);
    setOffset(pOffset);
    mNativeTypeEnum = pNativeTypeEnum;
  }

  /**
   * Instanciates a Phantom raw writer - without any scaling or offset and a
   * datatype of float.
   */
  public WriterBase()
  {
    setScaling(1);
    setOffset(0);
    setDataType(NativeTypeEnum.Float);
  }

  /**
   * Sets the native data set to use to store the data in the file
   * 
   * @param pNativeTypeEnum
   *          native data type.
   */
  public void setDataType(NativeTypeEnum pNativeTypeEnum)
  {
    mNativeTypeEnum = pNativeTypeEnum;
  }

  /**
   * Returns the native data type used to store the data in the file.
   * 
   * @return native data type
   */
  public NativeTypeEnum getDataType()
  {
    return mNativeTypeEnum;
  }

  /**
   * Sets whether to overwrite existing files
   * 
   * @param pOverwrite
   *          true -> overwrites, false othewise
   */
  public void setOverwrite(boolean pOverwrite)
  {
    mOverwrite = pOverwrite;
  }

  /**
   * Returns the state of the overwrite flag
   * 
   * @return true -> overwrite, fale otherwise
   */
  public boolean getOverwrite()
  {
    return mOverwrite;
  }

  /**
   * Returns the scaling parameter.
   * 
   * @return scaling parameter
   */
  public float getScaling()
  {
    return mScaling;
  }

  /**
   * Sets the scaling parameter
   * 
   * @param pScaling
   *          scaling parameter
   */
  public void setScaling(float pScaling)
  {
    mScaling = pScaling;
  }

  /**
   * Returns the offset parameter
   * 
   * @return offset parameter
   */
  public float getOffset()
  {
    return mOffset;
  }

  /**
   * Sets the offset parameter
   * 
   * @param pOffset
   *          offset parameter
   */
  public void setOffset(float pOffset)
  {
    mOffset = pOffset;
  }

}
