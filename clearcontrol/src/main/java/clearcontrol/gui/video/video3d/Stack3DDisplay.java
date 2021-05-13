package clearcontrol.gui.video.video3d;

import clearcontrol.core.concurrent.asyncprocs.AsynchronousProcessorBase;
import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.variable.Variable;
import clearcontrol.gui.video.StackDisplayInterface;
import clearcontrol.gui.video.util.MinMaxControlDialog;
import clearcontrol.gui.video.util.WindowControl;
import clearcontrol.stack.EmptyStack;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.metadata.MetaDataOrdinals;
import cleargl.ClearGLWindow;
import clearvolume.renderer.ClearVolumeRendererInterface;
import clearvolume.renderer.cleargl.ClearGLVolumeRenderer;
import clearvolume.renderer.factory.ClearVolumeRendererFactory;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import coremem.ContiguousMemoryInterface;
import coremem.enums.NativeTypeEnum;
import coremem.offheap.OffHeapMemory;
import coremem.util.Size;

import java.util.concurrent.TimeUnit;

public class Stack3DDisplay extends VirtualDevice implements StackDisplayInterface
{
  private static final int cDefaultDisplayQueueLength = 2;
  protected static final long cTimeOutForBufferCopy = 5;

  private ClearVolumeRendererInterface mClearVolumeRenderer;

  private final Variable<StackInterface> mInputStackVariable;
  private Variable<StackInterface> mOutputStackVariable;

  private AsynchronousProcessorBase<StackInterface, Object> mAsynchronousDisplayUpdater;

  private volatile Variable<Boolean> mVisibleVariable;
  private volatile Variable<Boolean> mWaitForLastChannel;
  private String mChannelFilter;

  public Stack3DDisplay(final String pWindowName)
  {
    this(pWindowName, "", 512, 512, 1, cDefaultDisplayQueueLength);
  }

  public Stack3DDisplay(final String pWindowName, final String pChannelFilter, final int pWindowWidth, final int pWindowHeight, final int pNumberOfLayers, final int pUpdaterQueueLength)
  {
    super(pWindowName);
    mChannelFilter = pChannelFilter;

    NativeTypeEnum lNativeTypeEnum = NativeTypeEnum.UnsignedShort;

    mClearVolumeRenderer = ClearVolumeRendererFactory.newBestRenderer(pWindowName, pWindowWidth, pWindowHeight, lNativeTypeEnum, 2048, 2048, pNumberOfLayers, false);

    mVisibleVariable = new Variable<Boolean>("Visible", false);

    mVisibleVariable.addSetListener((o, n) ->
    {
      mClearVolumeRenderer.setVisible(n);
    });

    setVisible(false);

    mClearVolumeRenderer.setAdaptiveLODActive(true);
    mClearVolumeRenderer.disableClose();

    if (mClearVolumeRenderer instanceof ClearGLVolumeRenderer)
    {
      ClearGLVolumeRenderer lClearGLVolumeRenderer = (ClearGLVolumeRenderer) mClearVolumeRenderer;
      ClearGLWindow lClearGLWindow = lClearGLVolumeRenderer.getClearGLWindow();
      lClearGLWindow.addWindowListener(new WindowControl(lClearGLWindow));

      KeyListener lKeyAdapter = new KeyAdapter()
      {
        @Override
        public void keyPressed(KeyEvent pE)
        {
          switch (pE.getKeyCode())
          {
            case KeyEvent.VK_V:
              MinMaxControlDialog.showDialog(mClearVolumeRenderer);
              break;
          }
        }
      };

      lClearGLWindow.addKeyListener(lKeyAdapter);

    }

    class Aynchronous3DStackDisplayUpdater extends AsynchronousProcessorBase<StackInterface, Object>
    {
      public Aynchronous3DStackDisplayUpdater(String pName, int pMaxQueueSize)
      {
        super(pName, pMaxQueueSize);
      }

      @Override
      public Object process(final StackInterface pStack)
      {
        if (pStack instanceof EmptyStack) return null;

        if (mClearVolumeRenderer.isShowing() && pStack.getDepth() > 1)
        {
          // info("received stack: " + pStack);

          if (!pStack.getMetaData().getValue(MetaDataChannel.Channel).contains(mChannelFilter))
          {
            forwardStack(pStack);
            return null;
          }

          if (pStack.getMetaData().hasValue("NoDisplay"))
          {
            info("Received stack with NoDisplay metadata value");
            forwardStack(pStack);
            return null;
          }

          final long lSizeInBytes = pStack.getSizeInBytes();
          long lWidth = pStack.getWidth();
          long lHeight = pStack.getHeight();
          long lDepth = pStack.getDepth();

          final NativeTypeEnum lNativeTypeEnum = mClearVolumeRenderer.getNativeType();
          final long lBytesPerVoxel = Size.of(lNativeTypeEnum);

          int lChannel = 0;
          Long lChannelObj = pStack.getMetaData().getValue(MetaDataOrdinals.DisplayChannel);
          if (lChannelObj != null) lChannel = lChannelObj.intValue() % pNumberOfLayers;

          if (lWidth * lHeight * lDepth * lBytesPerVoxel != lSizeInBytes)
          {
            System.err.println(Stack3DDisplay.class.getSimpleName() + ": receiving wrong pointer size!");
            return null;
          }

          ContiguousMemoryInterface lContiguousMemory = pStack.getContiguousMemory();

          if (lContiguousMemory.isFree())
          {
            System.err.println(Stack3DDisplay.class.getSimpleName() + ": buffer released!");
            return null;
          }

          Double lVoxelWidth = pStack.getMetaData().getVoxelDimX();
          Double lVoxelHeight = pStack.getMetaData().getVoxelDimY();
          Double lVoxelDepth = pStack.getMetaData().getVoxelDimZ();

          if (lVoxelWidth == null)
          {
            lVoxelWidth = 1.0 / lWidth;
            warning("No voxel width provided, using 1.0 instead.");

          }

          if (lVoxelHeight == null)
          {
            lVoxelHeight = 1.0 / lHeight;
            warning("No voxel height provided, using 1.0 instead.");

          }

          if (lVoxelDepth == null)
          {
            lVoxelDepth = 1.0 / lDepth;
            warning("No voxel depth provided, using 1.0 instead.");

          }

          if (lContiguousMemory.getSizeInBytes() > 2147483000)
          {
            // we need to downscale the image! ideally along x and y!
            lContiguousMemory = downscale(lContiguousMemory, lWidth, lHeight, lDepth);
            lWidth /= 2;
            lHeight /= 2;
            lVoxelWidth *= 2;
            lVoxelHeight *= 2;
          }

          mClearVolumeRenderer.setVolumeDataBuffer(lChannel, lContiguousMemory, lWidth, lHeight, lDepth, lVoxelWidth, lVoxelHeight, lVoxelDepth);

          if (mWaitForLastChannel.get() && ((lChannel + 1) % mClearVolumeRenderer.getNumberOfRenderLayers()) == 0)
          {
            mClearVolumeRenderer.waitToFinishAllDataBufferCopy(cTimeOutForBufferCopy, TimeUnit.SECONDS);/**/
          } else mClearVolumeRenderer.waitToFinishDataBufferCopy(lChannel, cTimeOutForBufferCopy, TimeUnit.SECONDS);/**/


        }

        forwardStack(pStack);

        return null;
      }
    }

    mAsynchronousDisplayUpdater = new Aynchronous3DStackDisplayUpdater("AsynchronousDisplayUpdater-" + pWindowName, pUpdaterQueueLength);

    mInputStackVariable = new Variable<StackInterface>("3DStackInput")
    {

      @Override
      public StackInterface setEventHook(final StackInterface pOldStack, final StackInterface pNewStack)
      {
        if (!mAsynchronousDisplayUpdater.passOrFail(pNewStack))
        {
          forwardStack(pNewStack);
        }

        return super.setEventHook(pOldStack, pNewStack);
      }

    };

    mWaitForLastChannel = new Variable<Boolean>("WaitForLastChannel", false);

  }

  public String getChannelFilter()
  {
    return mChannelFilter;
  }


  private void forwardStack(final StackInterface pNewStack)
  {
    if (mOutputStackVariable != null) mOutputStackVariable.setAsync(pNewStack);
    else if (!pNewStack.isReleased()) pNewStack.release();
  }

  @Override
  public Variable<StackInterface> getOutputStackVariable()
  {
    return mOutputStackVariable;
  }

  @Override
  public void setOutputStackVariable(Variable<StackInterface> pOutputStackVariable)
  {
    mOutputStackVariable = pOutputStackVariable;
  }

  public Variable<Boolean> getVisibleVariable()
  {
    return mVisibleVariable;
  }

  @Override
  public Variable<StackInterface> getInputStackVariable()
  {
    return mInputStackVariable;
  }

  public void setVisible(final boolean pIsVisible)
  {
    mVisibleVariable.set(pIsVisible);
  }

  @Override
  public boolean open()
  {
    mAsynchronousDisplayUpdater.start();
    return false;
  }

  @Override
  public boolean close()
  {
    try
    {
      mAsynchronousDisplayUpdater.stop();
      mAsynchronousDisplayUpdater.waitToFinish(1, TimeUnit.SECONDS);
      mClearVolumeRenderer.waitToFinishAllDataBufferCopy(1, TimeUnit.SECONDS);
      if (mClearVolumeRenderer != null) mClearVolumeRenderer.close();
      return true;
    } catch (final Throwable e)
    {
      e.printStackTrace();
      return false;
    }
  }

  public boolean isVisible()
  {
    return mClearVolumeRenderer.isShowing();
  }

  public void requestFocus()
  {
    if (mClearVolumeRenderer instanceof ClearGLVolumeRenderer)
    {
      ClearGLVolumeRenderer lClearGLVolumeRenderer = (ClearGLVolumeRenderer) mClearVolumeRenderer;
      ClearGLWindow lClearGLWindow = lClearGLVolumeRenderer.getClearGLWindow();
      lClearGLWindow.requestFocus();
    }
  }

  public void disableClose()
  {
    mClearVolumeRenderer.disableClose();
  }

  public Variable<Boolean> getWaitForLastChannel()
  {
    return mWaitForLastChannel;
  }

  public void setWaitForLastChannel(Variable<Boolean> pWaitForLastChannel)
  {
    mWaitForLastChannel = pWaitForLastChannel;
  }

  /**
   * Gets GL window for handling size and position.
   *
   * @return the GL window
   */
  public ClearGLWindow getGLWindow()
  {
    if (mClearVolumeRenderer instanceof ClearGLVolumeRenderer)
    {
      ClearGLVolumeRenderer lClearGLVolumeRenderer = (ClearGLVolumeRenderer) mClearVolumeRenderer;
      return lClearGLVolumeRenderer.getClearGLWindow();
    } else return null;
  }


  private ContiguousMemoryInterface downscale(ContiguousMemoryInterface pSource, long swidth, long sheight, long sdepth)
  {
    long dwidth = swidth / 2;
    long dheight = sheight / 2;
    long ddepth = sdepth;

    // allocate new array
    ContiguousMemoryInterface lDest = OffHeapMemory.allocateBytes(pSource.getSizeInBytes() / 4);

    long lDestIndex = 0;
    for (int dz = 0; dz < ddepth; dz++)
    {
      final int sz = dz;
      for (int dy = 0; dy < dheight; dy++)
      {
        final int sy = dy * 2;
        for (int dx = 0; dx < dwidth; dx++)
        {
          int sx = dx * 2;

          long lSrcIndex = sx + swidth * sy + swidth * sheight * sz;

          final int lValue1 = 0xFFFF & pSource.getCharAligned(lSrcIndex);
          final int lValue2 = 0xFFFF & pSource.getCharAligned(lSrcIndex + 1);
          final int lValue3 = 0xFFFF & pSource.getCharAligned(lSrcIndex + swidth);
          final int lValue4 = 0xFFFF & pSource.getCharAligned(lSrcIndex + 1 + swidth);

          final char lValue = (char) ((lValue1 + lValue2 + lValue3 + lValue4) / 4);

          lDest.setCharAligned(lDestIndex, lValue);
          lDestIndex++;
        }
      }
    }

    return lDest;

  }


}
