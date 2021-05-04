package clearcontrol.microscope.lightsheet.adaptive.gui;

import clearcontrol.core.device.name.ReadOnlyNameableInterface;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.adaptive.AdaptiveEngine;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.adaptive.AdaptationStateEngine;
import clearcontrol.microscope.lightsheet.adaptive.controlplanestate.HasControlPlaneState;
import clearcontrol.microscope.lightsheet.adaptive.controlplanestate.gui.ControlPlaneStatePanel;
import clearcontrol.microscope.lightsheet.calibrator.CalibrationEngine;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheetInterface;
import clearcontrol.microscope.lightsheet.configurationstate.CanBeActive;
import clearcontrol.microscope.lightsheet.configurationstate.gui.ConfigurationStatePanel;
import clearcontrol.microscope.lightsheet.gui.VariableLabel;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

/**
 * Todo: this class may be too XWing specific and should move to its repository
 * eventually.
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) November
 * 2017
 */
public class AdaptationStateEnginePanel extends CustomGridPane
{
  AdaptationStateEngine pAdaptationStateEngine;
  AdaptiveEngine mAdaptiveEngine;
  LightSheetMicroscope mLightSheetMicroscope;
  InterpolatedAcquisitionState mInterpolatedAcquisitionState;

  /**
   * Instantiates a panel given an adaptive engine
   *
   * @param pAdaptationStateEngine adaptor
   */
  public AdaptationStateEnginePanel(AdaptationStateEngine pAdaptationStateEngine)
  {
    super();

    int lRow = 0;

    mAdaptiveEngine = pAdaptationStateEngine.getAdaptiveEngine();
    mLightSheetMicroscope = pAdaptationStateEngine.getLightSheetMicroscope();
    mInterpolatedAcquisitionState = pAdaptationStateEngine.getInterpolatedAcquisitionState();

    TabPane lTabPane = new TabPane();
    lTabPane.getTabs().add(buildCalibrationTab());
    lTabPane.getTabs().add(buildLastStateTab());

    this.add(lTabPane, 0, 0);

  }

  private Tab buildLastStateTab()
  {
    int lRow = 0;

    CustomGridPane lGridPane = new CustomGridPane();

    // The following blocks are XWing specific; they should move to the
    // corresponding repository
    if (mLightSheetMicroscope.getNumberOfLightSheets() > 0)
    {
      Node lLightSheetNode = buildLightSheetCurrentStatePane(0);
      lLightSheetNode.setRotate(15);
      lGridPane.add(lLightSheetNode, 2, 1);
    }
    if (mLightSheetMicroscope.getNumberOfLightSheets() > 1)
    {
      Node lLightSheetNode = buildLightSheetCurrentStatePane(1);
      lLightSheetNode.setRotate(-15);
      lGridPane.add(lLightSheetNode, 2, 0);
    }
    if (mLightSheetMicroscope.getNumberOfLightSheets() > 2)
    {
      Node lLightSheetNode = buildLightSheetCurrentStatePane(2);
      lLightSheetNode.setRotate(15);
      lGridPane.add(lLightSheetNode, 0, 0);
    }
    if (mLightSheetMicroscope.getNumberOfLightSheets() > 3)
    {
      Node lLightSheetNode = buildLightSheetCurrentStatePane(3);
      lLightSheetNode.setRotate(-15);
      lGridPane.add(lLightSheetNode, 0, 1);
    }

    if (mLightSheetMicroscope.getNumberOfDetectionArms() > 0)
    {
      Node lLightSheetNode = buildDetectionArmCurrentStatePane(0);
      lGridPane.add(lLightSheetNode, 1, 1);
    }
    if (mLightSheetMicroscope.getNumberOfDetectionArms() > 1)
    {
      Node lLightSheetNode = buildDetectionArmCurrentStatePane(1);
      lGridPane.add(lLightSheetNode, 1, 0);
    }

    lGridPane.setGap(50);

    // lGridPane.add(placeHolder(300, 75), 1, 1);

    Tab lMostRecentStateTab = new Tab("Most recent state");
    lMostRecentStateTab.setContent(lGridPane);

    return lMostRecentStateTab;
  }

  private Node placeHolder(int lWidth, int lHeight)
  {
    Label lPlaceHolder1 = new Label();
    lPlaceHolder1.setMinWidth(lWidth);
    lPlaceHolder1.setMinHeight(lHeight);
    return lPlaceHolder1;
  }

  private Node buildDetectionArmCurrentStatePane(final int pDetectionArmIndex)
  {
    // Variable<LightSheetMicroscopeQueue> lQueueVariable = new
    // Variable<LightSheetMicroscopeQueue>("D" + pDetectionArmIndex +
    // "lastQueue", mLightSheetMicroscope.getLastQueueVariable().get());
    // mLightSheetMicroscope.getLastQueueVariable().sendUpdatesTo(lQueueVariable);

    CustomGridPane lGridPane = new CustomGridPane();

    final Label lZLabel = new Label("Z");
    lGridPane.add(lZLabel, 0, 0);

    final Label lQueueLengthLabel = new Label("Queue length");
    lGridPane.add(lQueueLengthLabel, 0, 1);

    /*
    lQueueVariable.addSetListener(new VariableSetListener<LightSheetMicroscopeQueue>()
    {
      @Override public void setEvent(LightSheetMicroscopeQueue pCurrentValue,
                                     LightSheetMicroscopeQueue pNewValue)
      {
        Platform.runLater(new Runnable()
        {
          @Override public void run()
          {
            lQueueLengthLabel.setText("Queue length: " + pNewValue.getDetectionArmDeviceQueue(pDetectionArmIndex).getQueueLength());
    
          }
        });
    
      }
    });
    */
    Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), (ae) ->
    {
      lZLabel.setText("Z: " + formatNumber(mLightSheetMicroscope.getDetectionArm(pDetectionArmIndex).getZVariable().get().doubleValue()));

    }));
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();

    TitledPane lTitledPane = new TitledPane("Detection arm " + pDetectionArmIndex + " state", lGridPane);
    lTitledPane.setAnimated(false);
    lTitledPane.setExpanded(true);
    lTitledPane.setMinWidth(200);
    lTitledPane.setMinHeight(200);
    lTitledPane.setMaxWidth(200);
    lTitledPane.setMaxHeight(200);
    lTitledPane.setAlignment(Pos.CENTER);
    return lTitledPane;
  }

  private Node buildLightSheetCurrentStatePane(final int pLightSheetIndex)
  {
    // Variable<LightSheetMicroscopeQueue> lQueueVariable = new
    // Variable<LightSheetMicroscopeQueue>("L" + pLightSheetIndex + "lastQueue",
    // mLightSheetMicroscope.getLastQueueVariable().get());
    // mLightSheetMicroscope.getLastQueueVariable().sendUpdatesTo(lQueueVariable);

    int maxColumns = 0;
    int presign = 1;
    if (pLightSheetIndex > 1)
    {
      maxColumns = 3;
      presign = -1;
    }

    CustomGridPane lGridPane = new CustomGridPane();
    lGridPane.setMinWidth(300);
    lGridPane.setMinHeight(100);

    final Label lLaserOnLabel = new Label("  \n\n\n\n");
    lGridPane.add(lLaserOnLabel, maxColumns + presign * 0, 0, 1, 3);

    final Label lXLabel = new Label("X");
    lGridPane.add(lXLabel, maxColumns + presign * 1, 0);
    final Label lYLabel = new Label("Y");
    lGridPane.add(lYLabel, maxColumns + presign * 1, 1);
    final Label lZLabel = new Label("Z");
    lGridPane.add(lZLabel, maxColumns + presign * 1, 2);

    final Label lALabel = new Label("A");
    lGridPane.add(lALabel, maxColumns + presign * 2, 0);
    final Label lBLabel = new Label("B");
    lGridPane.add(lBLabel, maxColumns + presign * 2, 1);

    final Label lHLabel = new Label("W");
    lGridPane.add(lHLabel, maxColumns + presign * 3, 0);
    final Label lWLabel = new Label("H");
    lGridPane.add(lWLabel, maxColumns + presign * 3, 1);

    /*
    lQueueVariable.addSetListener(new VariableSetListener<LightSheetMicroscopeQueue>()
    {
      @Override public void setEvent(LightSheetMicroscopeQueue pCurrentValue,
                                     LightSheetMicroscopeQueue pNewValue)
      {
        Platform.runLater(new Runnable()
                          {
                            @Override public void run()
                            {
    
    
    
                            }
                          });
    
      }
    });*/

    Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), (ae) ->
    {

      if (mLightSheetMicroscope.getPlayedQueueVariable().get() != null)
      {
        LightSheetMicroscopeQueue pNewValue = mLightSheetMicroscope.getPlayedQueueVariable().get();

        lXLabel.setText("X: " + formatNumber(pNewValue.getIX(pLightSheetIndex)));
        lYLabel.setText("Y: " + formatNumber(pNewValue.getIY(pLightSheetIndex)));
        lALabel.setText("A: " + formatNumber(pNewValue.getIA(pLightSheetIndex)));
        lBLabel.setText("B: " + formatNumber(pNewValue.getIB(pLightSheetIndex)));
        lWLabel.setText("W: " + formatNumber(pNewValue.getIW(pLightSheetIndex)));
        lHLabel.setText("H: " + formatNumber(pNewValue.getIH(pLightSheetIndex)));

        if (pNewValue.getI(pLightSheetIndex))
        {
          lLaserOnLabel.setStyle("-fx-background-color: blue;");
        } else
        {
          lLaserOnLabel.setStyle("");
        }

                                                    /*LightSheetMicroscopeQueue lQueue = lQueueVariable.get();
                                                    if (lQueue != null) {
                                                      lZLabel.setText("Z: " + formatNumber(lQueue.getIZ(pLightSheetIndex)));
                                                      //lZLabel.setText("Z: " + lQueue.getDZ(pDetectionArmIndex));
                                                    }*/

        lZLabel.setText("Z: " + formatNumber(
                // mLightSheetMicroscope.getLightSheet(pLightSheetIndex).getZVariable().get().doubleValue()
                mLightSheetMicroscope.getPlayedQueueVariable().get().getIZ(pLightSheetIndex)));
      }
    }));
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();
    //

    TitledPane lTitledPane = new TitledPane("Light sheet " + pLightSheetIndex + " state", lGridPane);
    lTitledPane.setAnimated(false);
    lTitledPane.setExpanded(true);
    return lTitledPane;
  }

  private Tab buildCalibrationTab()
  {
    int lRow = 0;

    Tab lCalibrationTab = new Tab("Calibration");

    CustomGridPane lMainGridPane = new CustomGridPane();

    CustomGridPane lCustomGridPane = new CustomGridPane();

    CalibrationEngine lCalibrationEngine = mLightSheetMicroscope.getDevice(CalibrationEngine.class, 0);

    ConfigurationStatePanel lConfigurationStatePanel = new ConfigurationStatePanel(lCalibrationEngine.getModuleList(), mLightSheetMicroscope.getNumberOfLightSheets());

    TitledPane lTitledPane = new TitledPane("Calibration state", lConfigurationStatePanel);
    lTitledPane.setAnimated(false);
    lTitledPane.setExpanded(true);
    // GridPane.setColumnSpan(lTitledPane, 3);
    lCustomGridPane.add(lTitledPane, 1, lRow);
    lRow++;

    for (Object pAdaptationModuleInterface : mAdaptiveEngine.getModuleList())
    {
      if (pAdaptationModuleInterface instanceof HasControlPlaneState)
      {
        TitledPane lModuleStatePanel = buildModuleStatePanel((HasControlPlaneState) pAdaptationModuleInterface);
        lCustomGridPane.add(lModuleStatePanel, 1, lRow);
        lRow++;
      }
    }
    GridPane.setRowSpan(lCustomGridPane, 2);
    lMainGridPane.add(lCustomGridPane, 1, 0);

    buildLightSheetPanel(mLightSheetMicroscope.getLightSheet(0), 2, 1, lMainGridPane);

    buildLightSheetPanel(mLightSheetMicroscope.getLightSheet(1), 2, 0, lMainGridPane);

    buildLightSheetPanel(mLightSheetMicroscope.getLightSheet(2), 0, 0, lMainGridPane);

    buildLightSheetPanel(mLightSheetMicroscope.getLightSheet(3), 0, 1, lMainGridPane);

    lCalibrationTab.setContent(lMainGridPane);
    return lCalibrationTab;
  }

  private void buildLightSheetPanel(LightSheetInterface pLightSheetInterface, int pColumnIndex, int pRowIndex, GridPane pGridPane)
  {
    if (pLightSheetInterface == null)
    {
      return;
    }

    CustomGridPane lCustomGridPane = new CustomGridPane();

    int lRow = 0;
    lCustomGridPane.add(new Label("X function"), 0, lRow++);
    lCustomGridPane.add(buildVariableLabel("", pLightSheetInterface.getXFunction()), 0, lRow++);
    lCustomGridPane.add(new Label("Y function"), 0, lRow++);
    lCustomGridPane.add(buildVariableLabel("", pLightSheetInterface.getYFunction()), 0, lRow++);
    lCustomGridPane.add(new Label("Z function"), 0, lRow++);
    lCustomGridPane.add(buildVariableLabel("", pLightSheetInterface.getZFunction()), 0, lRow++);
    lCustomGridPane.add(new Label("Alpha function"), 0, lRow++);
    lCustomGridPane.add(buildVariableLabel("", pLightSheetInterface.getAlphaFunction()), 0, lRow++);
    lCustomGridPane.add(new Label("Beta function"), 0, lRow++);
    lCustomGridPane.add(buildVariableLabel("", pLightSheetInterface.getBetaFunction()), 0, lRow++);
    lCustomGridPane.add(new Label("Width function"), 0, lRow++);
    lCustomGridPane.add(buildVariableLabel("", pLightSheetInterface.getWidthFunction()), 0, lRow++);
    lCustomGridPane.add(new Label("Height function"), 0, lRow++);
    lCustomGridPane.add(buildVariableLabel("", pLightSheetInterface.getHeightFunction()), 0, lRow++);
    lCustomGridPane.add(new Label("Power function"), 0, lRow++);
    lCustomGridPane.add(buildVariableLabel("", pLightSheetInterface.getPowerFunction()), 0, lRow++);

    TitledPane lTitledPane = new TitledPane("Light sheet " + pLightSheetInterface.getName() + " calibration state", lCustomGridPane);
    lTitledPane.setAnimated(false);
    lTitledPane.setExpanded(true);
    // GridPane.setColumnSpan(lTitledPane, 3);
    pGridPane.add(lTitledPane, pColumnIndex, pRowIndex);
  }

  private VariableLabel buildVariableLabel(String name, Variable variable)
  {
    VariableLabel lVariableLabel = new VariableLabel(name, variable.get().toString());

    variable.addSetListener(new VariableSetListener()
    {
      @Override
      public void setEvent(Object pCurrentValue, Object pNewValue)
      {
        Platform.runLater(new Runnable()
        {
          @Override
          public void run()
          {
            lVariableLabel.setText(pNewValue.toString());
            lVariableLabel.setStyle("-fx-border-color:red;");
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(2500), (ae) ->
            {
              lVariableLabel.setStyle("");
            }));
            timeline.play();
          }
        });

      }
    });
    return lVariableLabel;
  }

  private TitledPane buildModuleStatePanel(HasControlPlaneState pHasControlPlaneState)
  {

    int lNumberOfLightSheets = mLightSheetMicroscope.getNumberOfLightSheets();
    int lNumberOfControlPlanes = mInterpolatedAcquisitionState.getNumberOfControlPlanes();

    ControlPlaneStatePanel lControlPlaneStatePanel = new ControlPlaneStatePanel(pHasControlPlaneState, lNumberOfLightSheets, lNumberOfControlPlanes);

    String lName = pHasControlPlaneState.toString();
    if (pHasControlPlaneState instanceof ReadOnlyNameableInterface)
    {
      lName = ((ReadOnlyNameableInterface) pHasControlPlaneState).getName();
    }

    TitledPane lTitledPane = new TitledPane("Adaptation " + lName + " state", lControlPlaneStatePanel);
    lTitledPane.setAnimated(false);
    lTitledPane.setExpanded(true);

    if (pHasControlPlaneState instanceof CanBeActive && !((CanBeActive) pHasControlPlaneState).isActive())
    {
      lTitledPane.setExpanded(false);
    }
    // GridPane.setColumnSpan(lTitledPane, 3);
    return lTitledPane;
  }

  private String formatNumber(double pNumber)
  {
    return String.format("%.3f", pNumber);
  }
}