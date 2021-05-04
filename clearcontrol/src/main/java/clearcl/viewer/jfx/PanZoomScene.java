package clearcl.viewer.jfx;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

/**
 * This subclass of Scene allows for panning and zooming.
 *
 * @author royer
 */
public class PanZoomScene extends Scene
{
  private Stage mStage;
  private Node mPanZoomNode;
  private volatile double mPressedX, mPressedY;
  private Affine mAffine = new Affine();
  private Scale mScale;
  private Translate mTranslate;
  private boolean mPivotInitialized;

  private volatile float mMouseX, mMouseY;

  /**
   * Constructs a PanZoomScene from a stage, root node, a node to pan and zoom,
   * window dimensions and a fill color.
   *
   * @param pStage         stage
   * @param pRoot          root node
   * @param pNodeToPanZoom node to pan and zoom
   * @param pWidth         window width
   * @param pHeight        window height
   * @param pFill          fill color
   */

  public PanZoomScene(Stage pStage, Parent pRoot, Node pNodeToPanZoom, double pWidth, double pHeight, Paint pFill)
  {
    super(pRoot, pWidth, pHeight, pFill);
    mStage = pStage;
    mPanZoomNode = pNodeToPanZoom;

    mScale = new Scale();
    mTranslate = new Translate();
    mAffine = new Affine();
    mPanZoomNode.getTransforms().add(mAffine);

    pStage.setFullScreenExitHint("Double click or ESC to exit fullscreen mode");

    setOnMouseMoved((e) ->
    {

      if (pStage.isFullScreen())
      {
        Point2D lRootNodePoint = mPanZoomNode.screenToLocal(e.getX(), e.getY());

        mMouseX = (float) (lRootNodePoint.getX() / mPanZoomNode.getBoundsInLocal().getWidth());
        mMouseY = (float) (lRootNodePoint.getY() / mPanZoomNode.getBoundsInLocal().getHeight());

      } else
      {
        Point2D lRootNodePoint = mPanZoomNode.sceneToLocal(e.getX(), e.getY());

        mMouseX = (float) (lRootNodePoint.getX() / mPanZoomNode.getBoundsInLocal().getWidth());
        mMouseY = (float) (lRootNodePoint.getY() / mPanZoomNode.getBoundsInLocal().getHeight());
      }
    });

    setOnMousePressed((event) ->
    {

      if (event.getButton() == MouseButton.PRIMARY)
      {

        if (event.getClickCount() == 2)
        {
          pStage.setFullScreen(!pStage.isFullScreen());

          Platform.runLater(() -> resetZoomPivot());

        } else
        {

          double lMouseX = event.getX();
          double lMouseY = event.getY();
          double lSceneWidth = getWidth();
          double lSceneHeight = getHeight();

          if (lMouseX >= 10 && lMouseX <= lSceneWidth - 11 && lMouseY >= 10 && lMouseY <= lSceneHeight - 11)
          {
            Point2D lRootNodePoint = mPanZoomNode.sceneToLocal(event.getX(), event.getY());
            mPressedX = lRootNodePoint.getX();
            mPressedY = lRootNodePoint.getY();
            event.consume();
          }
        }
      }
    });

    setOnMouseDragged((event) ->
    {
      if (event.getButton() == MouseButton.PRIMARY)
      {

        double lMouseX = event.getX();
        double lMouseY = event.getY();
        double lSceneWidth = getWidth();
        double lSceneHeight = getHeight();

        if (lMouseX >= 10 && lMouseX <= lSceneWidth - 11 && lMouseY >= 10 && lMouseY <= lSceneHeight - 11)
        {
          Point2D lRootNodePoint = mPanZoomNode.sceneToLocal(lMouseX, lMouseY);

          double lDeltaX = lRootNodePoint.getX() - mPressedX;
          double lDeltaY = lRootNodePoint.getY() - mPressedY;

          mTranslate.setX(lDeltaX);
          mTranslate.setY(lDeltaY);

          mAffine.append(mTranslate);

          event.consume();
        }
      }
    });

    setOnScroll((event) ->
    {

      double lDelta = event.getDeltaY() * 0.001;
      double lDeltaFactor = Math.exp(lDelta);

      scaleScene(lDeltaFactor, true);

      if (!mPivotInitialized)
      {
        resetZoomPivot();
        mPivotInitialized = true;
      }

      event.consume();
    });

    widthProperty().addListener((obs, o, n) ->
    {
      // resetZoomPivot();
      scaleScene(Math.sqrt(n.doubleValue() / o.doubleValue()), true);
    });

    heightProperty().addListener((obs, o, n) ->
    {
      // resetZoomPivot();
      scaleScene(Math.sqrt(n.doubleValue() / o.doubleValue()), true);
    });

  }

  /**
   * Scales scene by an isotropic factor
   *
   * @param pScaleFactor scale factor
   * @param pClamp       true -> limit to factor 2 up/down scaling
   */
  public void scaleScene(double pScaleFactor, boolean pClamp)
  {
    if (pClamp)
    {
      if (pScaleFactor < 0.5) pScaleFactor = 0.5;
      else if (pScaleFactor > 2) pScaleFactor = 2;
    }

    resetZoomPivot();

    mScale.setX(pScaleFactor);
    mScale.setY(pScaleFactor);

    mAffine.append(mScale);
  }

  /**
   * Resets the zoom pivo point, typically after a change in the window size or
   * shape.
   */
  public void resetZoomPivot()
  {
    double lSceneWidth = getWidth();
    double lSceneHeight = getHeight();

    if (mStage.isFullScreen())
    {

      Point2D lRootNodePoint = mPanZoomNode.screenToLocal(lSceneWidth / 2, lSceneHeight / 2);

      mScale.setPivotX(lRootNodePoint.getX());
      mScale.setPivotY(lRootNodePoint.getY());/**/

    } else
    {

      Point2D lRootNodePoint = mPanZoomNode.sceneToLocal(lSceneWidth / 2, lSceneHeight / 2);

      mScale.setPivotX(lRootNodePoint.getX());
      mScale.setPivotY(lRootNodePoint.getY());/**/
    }
  }

  /**
   * Returns mouse x position
   *
   * @return x position
   */
  public float getMouseX()
  {
    return mMouseX;
  }

  /**
   * Returns mouse y position
   *
   * @return y position
   */
  public float getMouseY()
  {
    return mMouseY;
  }

}
