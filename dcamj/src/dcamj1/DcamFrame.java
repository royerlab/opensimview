package dcamj1;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.bridj.Pointer;

public class DcamFrame
{

  private static final int cPageAlignment = 4096;

  public static BlockingQueue<DcamFrame> mAvailableFramesQueue =
                                                               new ArrayBlockingQueue<DcamFrame>(1000);

  public static DcamFrame requestFrame(final long pBytesPerPixel,
                                       final long pWidth,
                                       final long pHeight,
                                       final long pDepth)
  {
    DcamFrame lDcamFrame;
    do
    {
      lDcamFrame = mAvailableFramesQueue.poll();
    }
    while (lDcamFrame != null
           && (lDcamFrame.getPixelSizeInBytes() != pBytesPerPixel
               || lDcamFrame.getWidth() != pWidth
               || lDcamFrame.getHeight() != pHeight
               || lDcamFrame.getDepth() != pDepth));

    if (lDcamFrame == null)
    {
      lDcamFrame = new DcamFrame(pBytesPerPixel,
                                 pWidth,
                                 pHeight,
                                 pDepth);
    }

    return lDcamFrame;
  }

  public static void releaseFrame(final DcamFrame pDcamFrame)
  {
    mAvailableFramesQueue.offer(pDcamFrame);
  }

  public static void clearFrames()
  {
    mAvailableFramesQueue.clear();
  }

  public static void preallocateFrames(final long pNumberOfFramesToAllocate,
                                       final long pBytesPerPixel,
                                       final long pWidth,
                                       final long pHeight,
                                       final long pDepth)
  {
    clearFrames();
    for (int i = 0; i < pNumberOfFramesToAllocate; i++)
    {
      final DcamFrame lRequestedFrame = requestFrame(pBytesPerPixel,
                                                     pWidth,
                                                     pHeight,
                                                     pDepth);
      lRequestedFrame.release();
    }
  }

  /****************************************************/

  private final Pointer<Byte>[] mPointerArray;
  private final DcamFrame[] mSinglePlaneDcamFrameArray;

  private final long mBytesPerPixel, mWidth, mHeight, mDepth;
  private long mIndex, mTimeStampInNs;

  @SuppressWarnings("unchecked")
  public DcamFrame(final long pBytesPerPixel,
                   final long pWidth,
                   final long pHeight,
                   final long pDepth)
  {
    mBytesPerPixel = pBytesPerPixel;
    mWidth = pWidth;
    mHeight = pHeight;
    mDepth = pDepth;
    mPointerArray = new Pointer[(int) pDepth];
    mSinglePlaneDcamFrameArray = new DcamFrame[(int) pDepth];

    for (int i = 0; i < pDepth; i++)
    {
      mPointerArray[i] = Pointer.allocateAlignedArray(byte.class,
                                                      pBytesPerPixel
                                                                  * pWidth
                                                                  * pHeight,
                                                      cPageAlignment);
      /*mByteBufferArray[i] = ByteBuffer.allocateDirect((int) (pBytesPerPixel * pWidth * pHeight))
      																.order(ByteOrder.nativeOrder());/**/
      mSinglePlaneDcamFrameArray[i] =
                                    new DcamFrame(getPointerForSinglePlane(i),
                                                  getPixelSizeInBytes(),
                                                  getWidth(),
                                                  getHeight());
    }
  }

  @SuppressWarnings("unchecked")
  public DcamFrame(final Pointer<Byte> pSinglePlanePointer,
                   final long pBytesPerPixel,
                   final long pWidth,
                   final long pHeight)
  {
    mBytesPerPixel = pBytesPerPixel;
    mWidth = pWidth;
    mHeight = pHeight;
    mDepth = 1;
    mPointerArray = new Pointer[1];
    mPointerArray[0] = pSinglePlanePointer;
    mSinglePlaneDcamFrameArray = null;
  }

  public final long getWidth()
  {
    return mWidth;
  }

  public final long getHeight()
  {
    return mHeight;
  }

  public final long getDepth()
  {
    return mDepth;
  }

  public final long getIndex()
  {
    return mIndex;
  }

  public void setIndex(final long pIndex)
  {
    mIndex = pIndex;
  }

  public void setTimeStampInNs(final long pTimeStampInNs)
  {
    mTimeStampInNs = pTimeStampInNs;
  }

  public final long getFrameTimeStampInNs()
  {
    return mTimeStampInNs;
  }

  public final long getPixelSizeInBytes()
  {
    return mBytesPerPixel;
  }

  public Pointer<Byte> getPointerForSinglePlane(final int pIndex)
  {
    return mPointerArray[pIndex];
  }

  public long getTotalSizeInBytesForAllPlanes()
  {
    long size = 0;
    for (final Pointer<Byte> lPointer : mPointerArray)
    {
      size += lPointer.getValidBytes();
    }
    return size;
  }

  public boolean copyAllPlanesToSinglePointer(final Pointer<Byte> pDestinationPointer)
  {
    return copyAllPlanesToSinglePointer(pDestinationPointer,
                                        mPointerArray.length);
  }

  public boolean copyAllPlanesToSinglePointer(final Pointer<Byte> pDestinationPointer,
                                              final long pMaxNumberOfPlanes)
  {
    final long lTotalSizeInBytes = getTotalSizeInBytesForAllPlanes();
    if (pDestinationPointer.getValidBytes() != lTotalSizeInBytes)
      return false;

    Pointer<Byte> lDestinationPointer = pDestinationPointer;

    for (int i = 0; i < Math.min(mPointerArray.length,
                                 pMaxNumberOfPlanes); i++)
    {
      final Pointer<Byte> lPlanePointer = mPointerArray[i];
      lPlanePointer.copyTo(lDestinationPointer);
      lDestinationPointer =
                          lDestinationPointer.next(lPlanePointer.getValidElements());
    }

    return true;
  }

  public DcamFrame getSinglePlaneDcamFrame(final long pIndex)
  {
    if (mSinglePlaneDcamFrameArray == null)
      return this;
    else
      return mSinglePlaneDcamFrameArray[Math.toIntExact(pIndex)];
  }

  public final long getLengthInBytesForSinplePlane(final int pIndex)
  {
    return getPixelSizeInBytes() * getWidth() * getHeight();
  }

  public final void release()
  {
    releaseFrame(this);
  }

  public final void destroy()
  {
    try
    {
      for (int i = 0; i < mPointerArray.length; i++)
      {
        final Pointer<Byte> lPointer = mPointerArray[i];
        if (lPointer != null)
        {
          lPointer.release();
          mPointerArray[i] = null;
        }
      }
    }
    catch (final Throwable e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public String toString()
  {
    return String.format("DcamFrame [mBytesPerPixel=%s, mWidth=%s, mHeight=%s, mDepth=%s, mIndex=%s, mTimeStampInNs=%s]",
                         mBytesPerPixel,
                         mWidth,
                         mHeight,
                         mDepth,
                         mIndex,
                         mTimeStampInNs);
  }
}
