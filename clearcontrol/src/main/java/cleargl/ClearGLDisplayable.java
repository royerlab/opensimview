package cleargl;

import com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.opengl.GLException;

import java.awt.*;
import java.io.PrintStream;

public interface ClearGLDisplayable extends GLCloseable
{

  @Override
  public void close() throws GLException;

  public void setWindowTitle(String pTitleString);

  public void setVisible(boolean pIsVisible);

  public void toggleFullScreen();

  // TODO remove
  public void setPerspectiveProjectionMatrix(float fov, float ratio, float nearP, float farP);

  // TODO remove
  public void setOrthoProjectionMatrix(float left, float right, float bottom, float top, float zNear, float zFar);

  // TODO remove
  public void lookAt(float pPosX, float pPosY, float pPosZ, float pLookAtX, float pLookAtY, float pLookAtZ, float pUpX, float pUpY, float pUpZ);

  // TODO remove
  public GLMatrix getProjectionMatrix();

  // TODO remove
  public GLMatrix getViewMatrix();

  public String getWindowTitle();

  public void disableClose();

  public boolean isFullscreen();

  public void setFullscreen(boolean pFullScreen);

  // TODO remove
  public void display();

  public WindowClosingMode setDefaultCloseOperation(WindowClosingMode pWindowClosingMode);

  public void setWindowPosition(int pX, int pY);

  public void setSize(int pWindowWidth, int pWindowHeight);

  public int getWindowX();

  public int getWindowY();

  public int getWindowHeight();

  public int getWindowWidth();

  public int getSurfaceHeight();

  public int getSurfaceWidth();

  public boolean isVisible();

  public void addMouseListener(MouseListener pMouseListener);

  public void addKeyListener(KeyListener pKeyListener);

  public void addWindowListener(WindowAdapter pWindowAdapter);

  public void setUpdateFPSFrames(int pFramesPerSecond, PrintStream pPrintStream);

  public float getLastFPS();

  // TODO remove
  public float getAspectRatio();

  public Component getComponent();

  public float[] getBounds();


}
