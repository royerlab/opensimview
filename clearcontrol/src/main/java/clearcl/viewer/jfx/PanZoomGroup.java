package clearcl.viewer.jfx;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * This subclass of Scene allows for panning and zooming.
 *
 * @author royer
 */
public class PanZoomGroup extends Group
{
  private Parent mRoot;
  private volatile double mPressedX, mPressedY;
  private Scale mScale;
  private Translate mTranslate;
  private Point2D mSceneCenterInRoot;

  /**
   * Instanciates a pan-zoom group.
   *
   * @param pRoot      root node
   * @param pWidth     width
   * @param pHeight    height
   * @param pFillPaint fill paint
   */
  public PanZoomGroup(Parent pRoot, double pWidth, double pHeight, Paint pFillPaint)
  {
    super(pRoot);
    mRoot = pRoot;

    mScale = new Scale();
    mTranslate = new Translate();
    mRoot.getTransforms().add(mScale);
    mRoot.getTransforms().add(mTranslate);

  }

  /**
   * Initializes this scene
   *
   * @param pScene scene
   */
  public void init(Scene pScene)
  {

    setOnMousePressed((event) ->
    {

      if (event.getButton() == MouseButton.PRIMARY)
      {
        double lMouseX = event.getX();
        double lMouseY = event.getY();
        double lSceneWidth = getScene().getWidth();
        double lSceneHeight = getScene().getHeight();

        if (lMouseX >= 10 && lMouseX <= lSceneWidth - 11 && lMouseY >= 10 && lMouseY <= lSceneHeight - 11)
        {
          Point2D lRootNodePoint = mRoot.sceneToLocal(event.getX(), event.getY());
          mPressedX = lRootNodePoint.getX();
          mPressedY = lRootNodePoint.getY();
          event.consume();
        }
      }
    });

    setOnMouseDragged((event) ->
    {
      if (event.getButton() == MouseButton.PRIMARY)
      {

        double lMouseX = event.getX();
        double lMouseY = event.getY();
        double lSceneWidth = getScene().getWidth();
        double lSceneHeight = getScene().getHeight();

        if (lMouseX >= 10 && lMouseX <= lSceneWidth - 11 && lMouseY >= 10 && lMouseY <= lSceneHeight - 11)
        {
          Point2D lRootNodePoint = mRoot.sceneToLocal(lMouseX, lMouseY);

          double lDeltaX = mTranslate.getX() + (lRootNodePoint.getX() - mPressedX);
          double lDeltaY = mTranslate.getY() + (lRootNodePoint.getY() - mPressedY);

          mTranslate.setX(lDeltaX);
          mTranslate.setY(lDeltaY);

          event.consume();
        }
      }
    });

    setOnScroll((event) ->
    {

      double lDelta = event.getDeltaY() * 0.001;
      double lDeltaFactor = Math.exp(lDelta);

      scaleScene(lDeltaFactor);

      if (mSceneCenterInRoot == null)
      {
        resetZoomPivot();
      }

      event.consume();
    });

    getScene().widthProperty().addListener((obs, o, n) ->
    {

      scaleScene(Math.sqrt(n.doubleValue() / o.doubleValue()));
      resetZoomPivot();
    });

    getScene().heightProperty().addListener((obs, o, n) ->
    {

      scaleScene(Math.sqrt(n.doubleValue() / o.doubleValue()));
      resetZoomPivot();
    });

  }

  private void scaleScene(double lDeltaFactor)
  {
    if (lDeltaFactor < 0.5) lDeltaFactor = 0.5;
    else if (lDeltaFactor > 2) lDeltaFactor = 2;

    mScale.setX(mScale.getX() * lDeltaFactor);
    mScale.setY(mScale.getY() * lDeltaFactor);
  }

  private void resetZoomPivot()
  {
    double lSceneWidth = getScene().getWidth();
    double lSceneHeight = getScene().getHeight();

    mScale.setPivotX(lSceneWidth / 2);
    mScale.setPivotY(lSceneHeight / 2);/**/
  }

}
