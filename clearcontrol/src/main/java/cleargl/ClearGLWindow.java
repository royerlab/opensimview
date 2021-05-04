package cleargl;

import com.jogamp.nativewindow.CapabilitiesImmutable;
import com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode;
import com.jogamp.newt.Window;
import com.jogamp.newt.*;
import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.FPSAnimator;

import java.awt.*;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClearGLWindow implements ClearGLDisplayable
{

  private final GLWindow mGlWindow;
  private final Window mWindow;
  private final String mWindowTitle;
  private final int mWindowDefaultWidth;
  private final int mWindowDefaultHeight;

  private final GLMatrix mProjectionMatrix;
  private final GLMatrix mViewMatrix;
  private NewtCanvasAWT mNewtCanvasAWT;

  private FPSAnimator mAnimator;
  private int mFramesPerSecond = 60;

  static
  {
    System.setProperty("sun.awt.noerasebackground", "true");
  }

  static class MultisampleChooser extends DefaultGLCapabilitiesChooser
  {
    public int chooseCapabilities(final GLCapabilities desired, final List<? extends CapabilitiesImmutable> available, final int windowSystemRecommendedChoice)
    {
      boolean anyHaveSampleBuffers = false;
      for (int i = 0; i < available.size(); i++)
      {
        final GLCapabilitiesImmutable caps = (GLCapabilitiesImmutable) available.get(i);
        if (caps != null && caps.getSampleBuffers())
        {
          anyHaveSampleBuffers = true;
          break;
        }
      }
      final int selection = super.chooseCapabilities(desired, available, windowSystemRecommendedChoice);
      if (!anyHaveSampleBuffers)
      {
        System.err.println("WARNING: antialiasing will be disabled because none of the available pixel formats had it to offer");
      } else if (selection >= 0)
      {
        final GLCapabilitiesImmutable caps = (GLCapabilitiesImmutable) available.get(selection);
        if (!caps.getSampleBuffers())
        {
          System.err.println("WARNING: antialiasing will be disabled because the DefaultGLCapabilitiesChooser didn't supply it");
        }
      }
      return selection;
    }
  }

  public static final void setWindowIconsDefault()
  {
    setWindowIcons("cleargl/icon/ClearGLIcon16.png", "cleargl/icon/ClearGLIcon32.png");
  }

  public static final void setWindowIcons(final String... pIconsLowToHighRessourcePaths)
  {
    try
    {

      final StringBuilder lStringBuilder = new StringBuilder();

      for (final String lIconRessourcePath : pIconsLowToHighRessourcePaths)
      {
        lStringBuilder.append(lIconRessourcePath);
        lStringBuilder.append(' ');
      }

      System.setProperty("newt.window.icons", lStringBuilder.toString());
    } catch (final Throwable e)
    {
      e.printStackTrace();
    }
  }

  public ClearGLWindow(final String pWindowTitle, final int pDefaultWidth, final int pDefaultHeight, final ClearGLEventListener pClearGLWindowEventListener)
  {
    this(pWindowTitle, pDefaultWidth, pDefaultHeight, "GL4", 1, pClearGLWindowEventListener);
  }

  public ClearGLWindow(final String pWindowTitle, final int pDefaultWidth, final int pDefaultHeight, final String pGLVersion, final int pNumberOfSamples, final ClearGLEventListener pClearGLWindowEventListener)
  {
    mWindowTitle = pWindowTitle;
    mWindowDefaultWidth = pDefaultWidth;
    mWindowDefaultHeight = pDefaultHeight;

    mProjectionMatrix = new GLMatrix();
    mViewMatrix = new GLMatrix();

    final GLProfile lProfile = GLProfile.get(pGLVersion);
    System.out.println(this.getClass().getSimpleName() + ": " + lProfile);
    final GLCapabilities lCapabilities = new GLCapabilities(lProfile);

    lCapabilities.setSampleBuffers(pNumberOfSamples > 1);
    lCapabilities.setNumSamples(pNumberOfSamples);
    lCapabilities.setDepthBits(24);
    lCapabilities.setHardwareAccelerated(true);

    final GLCapabilitiesChooser lMultisampleChooser = new MultisampleChooser();

    mWindow = NewtFactory.createWindow(lCapabilities);
    mGlWindow = GLWindow.create(mWindow);

    runOnEDT(true, () ->
    {
      mGlWindow.setCapabilitiesChooser(lMultisampleChooser);
      mGlWindow.setTitle(pWindowTitle);

      if (pClearGLWindowEventListener != null)
      {
        pClearGLWindowEventListener.setClearGLWindow(this);
        mGlWindow.addGLEventListener(pClearGLWindowEventListener);
      }
      mGlWindow.setSurfaceSize(pDefaultWidth, pDefaultHeight);
      mGlWindow.setAutoSwapBufferMode(true);
    });

    // if(mGlWindow.getContext() instanceof GLContextImpl) {
    // }
    // GLContextShareSet.registerSharing(, mGlWindow.getContext());

    // lAnimator.add(mClearGLWindow.getGLAutoDrawable());
  }

  public void setResizable(final boolean resizable)
  {
    mGlWindow.setResizable(resizable);
  }

  public void setFPS(final int pFramesPerSecond)
  {
    mFramesPerSecond = pFramesPerSecond;

    if (mAnimator != null)
    {
      // mAnimator.setRunAsFastAsPossible(true);
    }
  }

  public void start()
  {
    mAnimator = new FPSAnimator(this.getGLAutoDrawable(), mFramesPerSecond);
    mAnimator.setUpdateFPSFrames(60, null);

    mAnimator.start();
    while (!mAnimator.isAnimating()) Thread.yield();
  }

  public void pause()
  {
    mAnimator.pause();
  }

  public void resume()
  {
    mAnimator.resume();
  }

  public void stop()
  {
    mAnimator.setIgnoreExceptions(true);
    mAnimator.pause();
    mAnimator.stop();
    while (mAnimator.isAnimating()) Thread.yield();
  }

  /*
   * (non-Javadoc)
   *
   * @see cleargl.ClearGLDisplayable#close()
   */
  @Override
  public void close() throws GLException
  {
    try
    {

      try
      {
        runOnEDT(false, () ->
        {
          mAnimator.stop();
        });

      } catch (final Throwable e)
      {
        System.err.println(e.getLocalizedMessage());
      }

      try
      {
        setVisible(false);
      } catch (final Throwable e)
      {
        System.err.println(e.getLocalizedMessage());
      }

      if (mGlWindow.isRealized()) runOnEDT(false, () ->
      {
        try
        {
          mGlWindow.destroy();
        } catch (Throwable e)
        {
        }
      });

    } catch (final Throwable e)
    {
      System.err.println(e.getLocalizedMessage());
    }
  }

  @Override
  public void setWindowTitle(final String pTitleString)
  {
    runOnEDT(false, () -> mGlWindow.setTitle(pTitleString));
  }

  @Override
  public void setVisible(final boolean pIsVisible)
  {
    CountDownLatch lLatch = new CountDownLatch(1);
    runOnEDT(false, () ->
    {
      try
      {
        mGlWindow.setVisible(pIsVisible);
      } catch (java.lang.InternalError e)
      {
      }
      lLatch.countDown();
    });
    try
    {
      lLatch.await(100, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e)
    {
    }
  }

  @Override
  public void setWindowPosition(int pX, int pY)
  {
    runOnEDT(false, () -> mGlWindow.setPosition(pX, pY));
  }

  @Override
  public void setSize(final int pWidth, final int pHeight)
  {
    runOnEDT(false, () -> mGlWindow.setSize(pWidth, pHeight));
  }

  public void requestFocus()
  {
    runOnEDT(false, () -> mGlWindow.requestFocus());
  }

  @Override
  public void toggleFullScreen()
  {
    runOnEDT(false, () ->
    {
      try
      {
        if (mGlWindow.isFullscreen())
        {
          mGlWindow.setFullscreen(false);
        } else
        {
          mGlWindow.setSize(mWindowDefaultWidth, mWindowDefaultHeight);
          mGlWindow.setFullscreen(true);
        }
        mGlWindow.display();
      } catch (final Exception e)
      {
        e.printStackTrace();
      }
    });

  }

  /*
   * (non-Javadoc)
   *
   * @see cleargl.ClearGLDisplayable#setPerspectiveProjectionMatrix(float,
   * float, float, float)
   */
  @Override
  public void setPerspectiveProjectionMatrix(final float fov, final float ratio, final float nearP, final float farP)
  {
    if (mProjectionMatrix != null) mProjectionMatrix.setPerspectiveProjectionMatrix(fov, ratio, nearP, farP);
  }

  public void setPerspectiveAnaglyphProjectionMatrix(final float fov, final float convergenceDist, final float aspectRatio, final float eyeSeparation, final float near, final float far)
  {
    mProjectionMatrix.setPerspectiveAnaglyphProjectionMatrix(fov, convergenceDist, aspectRatio, eyeSeparation, near, far);
  }

  /*
   * (non-Javadoc)
   *
   * @see cleargl.ClearGLDisplayable#setOrthoProjectionMatrix(float, float,
   * float, float, float, float)
   */
  @Override
  public void setOrthoProjectionMatrix(final float left, final float right, final float bottom, final float top, final float zNear, final float zFar)
  {
    if (mProjectionMatrix != null) mProjectionMatrix.setOrthoProjectionMatrix(left, right, bottom, top, zNear, zFar);
  }

  /*
   * (non-Javadoc)
   *
   * @see cleargl.ClearGLDisplayable#lookAt(float, float, float, float, float,
   * float, float, float, float)
   */
  @Override
  public void lookAt(final float pPosX, final float pPosY, final float pPosZ, final float pLookAtX, final float pLookAtY, final float pLookAtZ, final float pUpX, final float pUpY, final float pUpZ)
  {
    mViewMatrix.setCamera(pPosX, pPosY, pPosZ, pLookAtX, pLookAtY, pLookAtZ, pUpX, pUpY, pUpZ);
  }

  /*
   * (non-Javadoc)
   *
   * @see cleargl.ClearGLDisplayable#getProjectionMatrix()
   */
  @Override
  public GLMatrix getProjectionMatrix()
  {
    return mProjectionMatrix;
  }

  /*
   * (non-Javadoc)
   *
   * @see cleargl.ClearGLDisplayable#getViewMatrix()
   */
  @Override
  public GLMatrix getViewMatrix()
  {
    return mViewMatrix;
  }

  /*
   * (non-Javadoc)
   *
   * @see cleargl.ClearGLDisplayable#getWindowTitle()
   */
  @Override
  public String getWindowTitle()
  {
    return mWindowTitle;
  }

  /*
   * (non-Javadoc)
   *
   * @see cleargl.ClearGLDisplayable#disableClose()
   */
  @Override
  public void disableClose()
  {
    runOnEDT(false, () -> mGlWindow.setDefaultCloseOperation(WindowClosingMode.DO_NOTHING_ON_CLOSE));
  }

  @Override
  public String toString()
  {
    return "ClearGLWindow [mGlWindow=" + mGlWindow + ", mWindow=" + mWindow + ", mWindowDefaultWidth=" + mWindowDefaultWidth + ", mWindowDefaultHeight=" + mWindowDefaultHeight + "]";
  }

  /*
   * (non-Javadoc)
   *
   * @see cleargl.ClearGLDisplayable#isFullscreen()
   */
  @Override
  public boolean isFullscreen()
  {
    return mGlWindow.isFullscreen();
  }

  /*
   * (non-Javadoc)
   *
   * @see cleargl.ClearGLDisplayable#setFullscreen(boolean)
   */
  @Override
  public void setFullscreen(final boolean pFullScreen)
  {

    runOnEDT(false, () ->
    {
      if (pFullScreen)
      {
        final Display display = NewtFactory.createDisplay(null); // local
        // display
        final Screen screen = NewtFactory.createScreen(display, 0); // screen
        // 0
        final ArrayList<MonitorDevice> monitors = new ArrayList<MonitorDevice>();
        int lFullscreen;

        int index = 0;
        for (final MonitorDevice m : screen.getMonitorDevices())
        {
          System.out.println(index + ": " + m.toString());
          index++;
        }

        try
        {
          lFullscreen = Integer.parseInt(System.getProperty("ClearGL.FullscreenDevice"));
          System.out.println("Fullscreen ID set to " + lFullscreen + " by property.");
        } catch (final java.lang.NumberFormatException e)
        {
          lFullscreen = 0;
        }

        System.out.println(screen.getMonitorDevices().get(lFullscreen).toString());
        System.out.println(screen.getMonitorDevices().get(lFullscreen).getScreen().getFQName());

        screen.addReference(); // trigger creation

        if (pFullScreen)
        {
          monitors.add(screen.getMonitorDevices().get(lFullscreen)); // Q1
        } else
        {
          // monitor array stays empty
        }
        mGlWindow.setSurfaceSize(screen.getMonitorDevices().get(lFullscreen).getCurrentMode().getSurfaceSize().getResolution().getWidth(), screen.getMonitorDevices().get(lFullscreen).getCurrentMode().getSurfaceSize().getResolution().getHeight());
        mGlWindow.setFullscreen(monitors);
      } else
      {
        mGlWindow.setFullscreen(false);
      }
    });
  }

  @Override
  public void display()
  {
    mGlWindow.display();
  }

  public static boolean isRetina(final GL pGL)
  {
    final int[] trialSizes = new int[2];

    trialSizes[0] = 512;
    trialSizes[1] = 512;

    pGL.getContext().getGLDrawable().getNativeSurface().convertToPixelUnits(trialSizes);

    if (trialSizes[0] == 512 && trialSizes[1] == 512)
    {
      return false;
    } else
    {
      return true;
    }
  }

  @Override
  public WindowClosingMode setDefaultCloseOperation(final WindowClosingMode pWindowClosingMode)
  {
    return mGlWindow.setDefaultCloseOperation(pWindowClosingMode);
  }

  @Override
  public int getWindowHeight()
  {
    return mGlWindow.getHeight();
  }

  @Override
  public int getWindowWidth()
  {
    return mGlWindow.getWidth();
  }

  @Override
  public int getSurfaceWidth()
  {
    return mGlWindow.getSurfaceHeight();
  }

  @Override
  public int getSurfaceHeight()
  {
    return mGlWindow.getSurfaceHeight();
  }

  @Override
  public int getWindowX()
  {
    return mGlWindow.getX();
  }

  @Override
  public int getWindowY()
  {
    return mGlWindow.getY();
  }

  @Override
  public boolean isVisible()
  {
    return mGlWindow.isVisible();
  }

  @Override
  public void addMouseListener(final MouseListener pMouseListener)
  {
    mGlWindow.addMouseListener(pMouseListener);
  }

  @Override
  public void addKeyListener(final KeyListener pKeyListener)
  {
    mGlWindow.addKeyListener(pKeyListener);
  }

  @Override
  public void addWindowListener(final WindowAdapter pWindowAdapter)
  {
    mGlWindow.addWindowListener(pWindowAdapter);
  }

  @Override
  public void setUpdateFPSFrames(final int pFramesPerSecond, final PrintStream pPrintStream)
  {
    mGlWindow.setUpdateFPSFrames(pFramesPerSecond, pPrintStream);
  }

  @Override
  public float getLastFPS()
  {
    return mGlWindow.getLastFPS();
  }

  @Override
  public float getAspectRatio()
  {
    return mGlWindow.getSurfaceWidth() / mGlWindow.getSurfaceHeight();
  }

  @Override
  public Component getComponent()
  {
    return getNewtCanvasAWT();
  }

  public NewtCanvasAWT getNewtCanvasAWT()
  {
    if (mNewtCanvasAWT == null)
    {
      mNewtCanvasAWT = new NewtCanvasAWT(mGlWindow);
      mNewtCanvasAWT.setShallUseOffscreenLayer(false);
    }

    return mNewtCanvasAWT;
  }

  public GLAutoDrawable getGLAutoDrawable()
  {
    return mGlWindow;
  }

  public GL getGL()
  {
    return mGlWindow.getGL();
  }

  public void runOnEDT(final boolean pWait, final Runnable pRunnable)
  {
    mGlWindow.runOnEDTIfAvail(pWait, pRunnable);
  }

  @Override
  public float[] getBounds()
  {
    final com.jogamp.nativewindow.util.Rectangle bounds = mGlWindow.getBounds();
    return new float[]{bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()};
  }

}
