package dcamj2.imgseq;

import coremem.recycling.RecyclerRequestInterface;
import dcamj2.DcamDevice;

/**
 * Dcam image sequence request. Describes a requested image sequence
 *
 * @author royer
 */
public class DcamImageSequenceRequest implements
                                      RecyclerRequestInterface
{
  private DcamDevice mDcamDevice;
  private final long mBytesPerPixel, mWidth, mHeight, mDepth;
  private final boolean mFragmented;

  /**
   * Instantiates a Dcam image sequence request
   * 
   * @param pDcamDevice
   *          parent Dcam device
   * @param pBytesPerPixel
   *          bytes per pixel
   * @param pWidth
   *          width
   * @param pHeight
   *          height
   * @param pDepth
   *          depth
   * @param pFragmented
   *          'is-fragmented' flag
   * @return request
   */
  public static DcamImageSequenceRequest build(DcamDevice pDcamDevice,
                                               long pBytesPerPixel,
                                               long pWidth,
                                               long pHeight,
                                               long pDepth,
                                               boolean pFragmented)
  {
    return new DcamImageSequenceRequest(pDcamDevice,
                                        pBytesPerPixel,
                                        pWidth,
                                        pHeight,
                                        pDepth,
                                        pFragmented);
  }

  /**
   * Instantiates a Dcam image sequence request
   * 
   * @param pDcamDevice
   *          parent Dcam device
   * @param pBytesPerPixel
   *          bytes per pixel
   * @param pWidth
   *          width
   * @param pHeight
   *          height
   * @param pDepth
   *          depth
   * @param pFragmented
   *          'is-fragmented' flag
   */
  public DcamImageSequenceRequest(DcamDevice pDcamDevice,
                                  long pBytesPerPixel,
                                  long pWidth,
                                  long pHeight,
                                  long pDepth,
                                  boolean pFragmented)
  {
    super();
    mDcamDevice = pDcamDevice;
    mBytesPerPixel = pBytesPerPixel;
    mWidth = pWidth;
    mHeight = pHeight;
    mDepth = pDepth;
    mFragmented = pFragmented;
  }

  /**
   * Returns whether the given image sequence is compatible with this request
   * 
   * @param pDcamImageSequence
   *          image sequence
   * @return true: compatible, false otherwise
   */
  public boolean isCompatible(DcamImageSequence pDcamImageSequence)
  {
    return mDcamDevice == pDcamImageSequence.getDcamDevice()
           && mBytesPerPixel == pDcamImageSequence.getBytesPerPixel()
           && mWidth == pDcamImageSequence.getWidth()
           && mHeight == pDcamImageSequence.getHeight()
           && mDepth == pDcamImageSequence.getDepth()
           && mFragmented == pDcamImageSequence.isFragmented();
  }

  /**
   * Instantiates a new image sequence based on this request
   * 
   * @return new compatible image sequence
   */
  public DcamImageSequence newImageSequence()
  {
    DcamImageSequence lSequence = new DcamImageSequence(mDcamDevice,
                                                        mBytesPerPixel,
                                                        mWidth,
                                                        mHeight,
                                                        mDepth,
                                                        mFragmented);
    return lSequence;
  }

  @Override
  public String toString()
  {
    return String.format("DcamImageSequenceRequest [mDcamDevice=%s, mBytesPerPixel=%s, mWidth=%s, mHeight=%s, mDepth=%s, mFragmented=%s]",
                         mDcamDevice,
                         mBytesPerPixel,
                         mWidth,
                         mHeight,
                         mDepth,
                         mFragmented);
  }

}
