package clearcontrol.gui;

import clearcontrol.MicroscopeInterface;
import clearcontrol.adaptive.AdaptiveEngine;
import clearcontrol.adaptive.gui.AdaptiveEnginePanel;
import clearcontrol.adaptive.gui.AdaptiveEngineToolbar;
import clearcontrol.core.concurrent.executors.AsynchronousExecutorFeature;
import clearcontrol.core.concurrent.thread.ThreadSleep;
import clearcontrol.core.concurrent.timing.WaitingInterface;
import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.variable.Variable;
import clearcontrol.devices.cameras.StackCameraDeviceInterface;
import clearcontrol.devices.cameras.gui.CameraDevicePanel;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.devices.lasers.gui.LaserDevicePanel;
import clearcontrol.devices.optomech.filterwheels.FilterWheelDeviceInterface;
import clearcontrol.devices.optomech.filterwheels.gui.jfx.FilterWheelDevicePanel;
import clearcontrol.devices.optomech.opticalswitch.OpticalSwitchDeviceInterface;
import clearcontrol.devices.optomech.opticalswitch.gui.OpticalSwitchDevicePanel;
import clearcontrol.devices.signalamp.ScalingAmplifierDeviceInterface;
import clearcontrol.devices.signalamp.gui.ScalingAmplifierPanel;
import clearcontrol.devices.stages.StageDeviceInterface;
import clearcontrol.devices.stages.gui.StageDevicePanel;
import clearcontrol.gui.halcyon.HalcyonGUIGenerator;
import clearcontrol.gui.halcyon.MicroscopeNodeType;
import clearcontrol.gui.video.video2d.Stack2DDisplay;
import clearcontrol.gui.video.video3d.Stack3DDisplay;
import clearcontrol.scripting.engine.ScriptingEngine;
import clearcontrol.scripting.lang.ScriptingLanguageInterface;
import clearcontrol.scripting.lang.groovy.GroovyScripting;
import clearcontrol.scripting.lang.jython.JythonScripting;
import clearcontrol.simulation.SimulationManager;
import clearcontrol.simulation.gui.SimulationManagerPanel;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRecyclerManager;
import clearcontrol.stack.gui.StackRecyclerManagerPanel;
import halcyon.HalcyonFrame;
import halcyon.model.node.HalcyonNodeType;
import javafx.stage.Stage;
import org.dockfx.DockNode;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Microscope GUI.
 *
 * @author royer
 */
public class MicroscopeGUI extends VirtualDevice implements AsynchronousExecutorFeature, WaitingInterface
{

  private static final int cDefaultWindowWidth = 512;
  private static final int cDefaultWindowHeight = 512;

  private final MicroscopeInterface<?> mMicroscope;

  private final ArrayList<ScriptingEngine> mScriptingEngineList = new ArrayList<>();

  private ArrayList<Variable<StackInterface>> mStack2DTo3DDispatchVariableList = new ArrayList<>();
  private ArrayList<Stack2DDisplay> mStack2DDisplayList = new ArrayList<>();
  private ArrayList<Stack3DDisplay> mStack3DDisplayList = new ArrayList<>();

  private final boolean m2DDisplay, m3DDisplay;
  private final ArrayList<String> m3DDisplayChannels;
  private HalcyonGUIGenerator mHalcyonGUIGenerator;
  private Stage mPrimaryStage;
  private HalcyonFrame mHalcyonFrame;

  /**
   * Instanciates a microscope GUI given a microscope, an array of halcyon node
   * types, and two flags that decide whether to set up 2D and 3D displays.
   *
   * @param pMicroscope           microscope
   * @param pHalcyonNodeTypeArray halcyon node type array
   * @param pPrimaryStage         JFX primary stage
   * @param p2DDisplay            2D display
   * @param p3DDisplay            3D display
   */
  public MicroscopeGUI(MicroscopeInterface<?> pMicroscope, HalcyonNodeType[] pHalcyonNodeTypeArray, Stage pPrimaryStage, boolean p2DDisplay, boolean p3DDisplay)
  {
    super(pMicroscope.getName() + "GUI");
    mMicroscope = pMicroscope;
    mPrimaryStage = pPrimaryStage;
    m2DDisplay = p2DDisplay;
    m3DDisplay = p3DDisplay;
    m3DDisplayChannels = new ArrayList<>();

    ArrayList<HalcyonNodeType> lNodeTypeList = new ArrayList<>();
    for (HalcyonNodeType lNode : MicroscopeNodeType.values())
      lNodeTypeList.add(lNode);
    for (HalcyonNodeType lNode : pHalcyonNodeTypeArray)
      lNodeTypeList.add(lNode);

    mHalcyonGUIGenerator = new HalcyonGUIGenerator(pMicroscope, this, lNodeTypeList, pPrimaryStage);

    addPanelMappingEntry(LaserDeviceInterface.class, LaserDevicePanel.class, MicroscopeNodeType.Laser);

    addPanelMappingEntry(StackCameraDeviceInterface.class, CameraDevicePanel.class, MicroscopeNodeType.Camera);/**/

    addPanelMappingEntry(FilterWheelDeviceInterface.class, FilterWheelDevicePanel.class, MicroscopeNodeType.FilterWheel);

    addPanelMappingEntry(OpticalSwitchDeviceInterface.class, OpticalSwitchDevicePanel.class, MicroscopeNodeType.OpticalSwitch);

    addPanelMappingEntry(ScalingAmplifierDeviceInterface.class, ScalingAmplifierPanel.class, MicroscopeNodeType.ScalingAmplifier);

    addPanelMappingEntry(StageDeviceInterface.class, StageDevicePanel.class, MicroscopeNodeType.Stage);

    addPanelMappingEntry(StackRecyclerManager.class, StackRecyclerManagerPanel.class, MicroscopeNodeType.Other);

    addPanelMappingEntry(SimulationManager.class, SimulationManagerPanel.class, MicroscopeNodeType.Other);

    addToolbarMappingEntry(AdaptiveEngine.class, AdaptiveEngineToolbar.class);

    addPanelMappingEntry(AdaptiveEngine.class, AdaptiveEnginePanel.class, MicroscopeNodeType.Acquisition);/**/

    SimulationManager lSimulationManager = new SimulationManager(pMicroscope);
    mMicroscope.addDevice(0, lSimulationManager);

    initializeConcurentExecutor();
  }

  public void add3DDisplayChannel(String pChannel)
  {
    m3DDisplayChannels.add(pChannel);
  }

  /**
   * Adds a toolbar to this GUI
   *
   * @param pDockNode toolbar's dockable node.
   */
  public void addToolbar(DockNode pDockNode)
  {
    getHalcyonFrame().addToolbar(pDockNode);
  }

  /**
   * Adds a mapping between a device class, panel class and node type.
   *
   * @param pDeviceClass device class
   * @param pPanelClass  panel class
   * @param pNodeType    node type
   */
  public <U, V> void addPanelMappingEntry(Class<U> pDeviceClass, Class<V> pPanelClass, HalcyonNodeType pNodeType)
  {
    mHalcyonGUIGenerator.addPanelMappingEntry(pDeviceClass, pPanelClass, pNodeType);
  }

  /**
   * Adds a toolbar mapping entry
   *
   * @param pDeviceClass  device class
   * @param pToolbarClass toolbar class
   */
  public <U, V> void addToolbarMappingEntry(Class<U> pDeviceClass, Class<V> pToolbarClass)
  {
    mHalcyonGUIGenerator.addToolbarMappingEntry(pDeviceClass, pToolbarClass);
  }

  /**
   * Adds a scripting engine.
   *
   * @param pMicroscopeObjectName       name of the microscope object within the scripting environment
   * @param pScriptingLanguageInterface scripting language interface
   */
  public void addScripting(String pMicroscopeObjectName, ScriptingLanguageInterface pScriptingLanguageInterface)
  {
    final ScriptingEngine lScriptingEngine = new ScriptingEngine(pScriptingLanguageInterface, null);
    lScriptingEngine.set(pMicroscopeObjectName, mMicroscope);
    mScriptingEngineList.add(lScriptingEngine);
  }

  /**
   * Adds Groovy scripting.
   *
   * @param pMicroscopeObjectName name of the microscope object within the scripting environment
   */
  public void addGroovyScripting(String pMicroscopeObjectName)
  {
    GroovyScripting lGroovyScripting = new GroovyScripting();
    addScripting(pMicroscopeObjectName, lGroovyScripting);
  }

  /**
   * Adds Jython scripting.
   *
   * @param pMicroscopeObjectName name of the microscope object within the scripting environment
   */
  public void addJythonScripting(String pMicroscopeObjectName)
  {
    JythonScripting lJythonScripting = new JythonScripting();
    addScripting(pMicroscopeObjectName, lJythonScripting);
  }

  /**
   * Sets up the GUI - i.e. main Halcyon window, toolbars, panels for devices,
   * and the 2D and 3D displays.
   */
  public void setup()
  {
    setup2Dand3DDisplays();
    setupHalcyonWindow();
  }

  /**
   * Returns the microscope for this GUI
   *
   * @return microscope that this GUI serves
   */
  public MicroscopeInterface<?> getMicroscope()
  {
    return mMicroscope;
  }

  /**
   * Returns the list of scripting Engines
   *
   * @return scripting engines list
   */
  public ArrayList<ScriptingEngine> getScriptingEnginesList()
  {
    return mScriptingEngineList;
  }

  /**
   * Returns the list of 2D displays.
   *
   * @return list of 2D displays
   */
  public ArrayList<Stack2DDisplay> get2DDisplayDeviceList()
  {
    return mStack2DDisplayList;
  }

  /**
   * Returns the list of 3D displays
   *
   * @return list of 3D displays
   */
  public ArrayList<Stack3DDisplay> get3DDisplayDeviceList()
  {
    return mStack3DDisplayList;
  }

  /**
   * Returns Halcyon frame
   *
   * @return Halcyon frame
   */
  public HalcyonFrame getHalcyonFrame()
  {
    return mHalcyonFrame;
  }

  /**
   * Sets up 2D and 3D displays.
   */
  public void setup2Dand3DDisplays()
  {
    int lNumberOfCameras = getMicroscope().getNumberOfDevices(StackCameraDeviceInterface.class);

    for (int c = 0; c < lNumberOfCameras; c++)
    {
      if (m2DDisplay)
      {
        final Stack2DDisplay lStack2DDisplay = new Stack2DDisplay(String.format("Video 2D for camera %d", c), cDefaultWindowWidth, cDefaultWindowHeight, c % 2 == 1);
        lStack2DDisplay.setVisible(false);
        mStack2DDisplayList.add(lStack2DDisplay);
        getMicroscope().addDevice(c, lStack2DDisplay);

        mStack2DTo3DDispatchVariableList.add(new Variable<StackInterface>("DispatchFrom2d_Camera" + c));
      }
    }

    if (m3DDisplay)
    {
      int i = 0;
      for (String lChannel : m3DDisplayChannels)
      {
        final Stack3DDisplay lStack3DDisplay = new Stack3DDisplay(String.format("Video 3D for channel %s", lChannel), lChannel, cDefaultWindowWidth, cDefaultWindowHeight, 1, 8);
        lStack3DDisplay.getVisibleVariable().set(false);
        mStack3DDisplayList.add(lStack3DDisplay);
        getMicroscope().addDevice(i++, lStack3DDisplay);
      }
    }

  }

  private void setupHalcyonWindow()
  {
    mHalcyonGUIGenerator.setupDeviceGUIs();

    mHalcyonFrame = mHalcyonGUIGenerator.getHalcyonFrame();
  }

  /* (non-Javadoc)
   * @see clearcontrol.device.openclose.OpenCloseDeviceAdapter#open()
   */
  @Override
  public boolean open()
  {

    if (m2DDisplay) executeAsynchronously(() ->
    {
      for (final Stack2DDisplay lStack2DDisplay : mStack2DDisplayList)
      {
        lStack2DDisplay.open();
      }
    });

    if (m3DDisplay) executeAsynchronously(() ->
    {
      for (final Stack3DDisplay lStack3dDisplay : mStack3DDisplayList)
      {
        lStack3dDisplay.open();
      }
    });

    try
    {
      mHalcyonFrame.start(mPrimaryStage);
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    return super.open();
  }

  @Override
  public boolean close()
  {
    if (m2DDisplay) executeAsynchronously(() ->
    {
      for (final Stack2DDisplay lStack2DDisplayDevice : mStack2DDisplayList)
      {
        lStack2DDisplayDevice.close();
      }
    });

    if (m3DDisplay) executeAsynchronously(() ->
    {
      for (final Stack3DDisplay mStack3DDisplayDevice : mStack3DDisplayList)
      {
        mStack3DDisplayDevice.close();
      }
    });

    executeAsynchronously(() ->
    {
      try
      {
        mHalcyonFrame.externalStop();
      } catch (Exception e)
      {
        e.printStackTrace();
      }
    });

    return super.close();
  }


  /**
   * Connects Stack cameras of given index to 2D and 3D display of given index.
   */
  public void connectCamerasTo2D3D(boolean m2DDisplay, boolean pConnect3D)
  {
    if (m2DDisplay)
    {
      int lNumberOfCameras = getMicroscope().getNumberOfDevices(StackCameraDeviceInterface.class);

      for (int c = 0; c < lNumberOfCameras; c++)
      {
        Stack2DDisplay lStack2dDisplay = mStack2DDisplayList.get(c);
        mMicroscope.getCameraStackVariable(c).sendUpdatesTo(lStack2dDisplay.getInputStackVariable());

        if (pConnect3D) lStack2dDisplay.setOutputStackVariable(mStack2DTo3DDispatchVariableList.get(c));
        else lStack2dDisplay.setOutputStackVariable(mMicroscope.getTerminatorStackVariable(c));
      }

      if (pConnect3D) for (Stack3DDisplay lStack3DDisplay : mStack3DDisplayList)
      {
        for (Variable<StackInterface> lVariable : mStack2DTo3DDispatchVariableList)
        {
          lVariable.sendUpdatesTo(lStack3DDisplay.getInputStackVariable());
        }

        for (int c = 0; c < lNumberOfCameras; c++)
        {
          String lChannelFilter = lStack3DDisplay.getChannelFilter();
          if (lChannelFilter.contains("C" + c))
            lStack3DDisplay.setOutputStackVariable(mMicroscope.getTerminatorStackVariable(c));
        }

      }
    }
  }

  /**
   * Disconnects variable of given index.
   *
   * @param pCameraIndex    camera index
   * @param p2DDisplayIndex 2D display index
   */
  public void disconnectCamera(int pCameraIndex, int p2DDisplayIndex)
  {
    Stack2DDisplay lStack2dDisplay = mStack2DDisplayList.get(p2DDisplayIndex);
    mMicroscope.getCameraStackVariable(pCameraIndex).doNotSendUpdatesTo(lStack2dDisplay.getInputStackVariable());
  }

  /**
   * Disconnects all cameras from all displays
   */
  public void disconnectAllCameras()
  {
    int lNumberOfCameras = getMicroscope().getNumberOfDevices(StackCameraDeviceInterface.class);

    for (int c = 0; c < lNumberOfCameras; c++)
    {
      for (Stack2DDisplay lStack2DDisplay : mStack2DDisplayList)
        mMicroscope.getCameraStackVariable(c).doNotSendUpdatesTo(lStack2DDisplay.getInputStackVariable());

      for (Stack3DDisplay lStack3DDisplay : mStack3DDisplayList)
        mMicroscope.getCameraStackVariable(c).doNotSendUpdatesTo(lStack3DDisplay.getInputStackVariable());
    }

  }


  /**
   * Disconnects 2D output variables
   */
  public void disconnectAll2DOutputs()
  {
    for (Stack2DDisplay lStack2DDisplay : mStack2DDisplayList)
    {
      lStack2DDisplay.setOutputStackVariable(null);
    }
  }

  /**
   * Disconnects 3D output variables
   */
  public void disconnectAll3DOutputs()
  {
    for (Stack3DDisplay lStack3DDisplay : mStack3DDisplayList)
    {
      lStack3DDisplay.setOutputStackVariable(null);
    }
  }

  /**
   * Connects GUI to microscope variables
   */
  public void connectGUI()
  {
    connectCamerasTo2D3D(m2DDisplay, m3DDisplay);
  }

  /**
   * Disconnects GUI from microscope variables
   */
  public void disconnectGUI()
  {
    disconnectAllCameras();
    disconnectAll2DOutputs();
    disconnectAll3DOutputs();
  }

  /**
   * Retruns whether the GUI elements are visible.
   *
   * @return true if GUI elements (windows) are visible
   */
  public boolean isVisible()
  {
    return mHalcyonFrame.isVisible();
  }

  /**
   * Waits until the GUI main window is either visible or not visible.
   *
   * @param pVisible  main window state to wait for
   * @param pTimeOut  time out
   * @param pTimeUnit time out unit
   * @return whether the main window is visible or not.
   */
  public boolean waitForVisible(boolean pVisible, Long pTimeOut, TimeUnit pTimeUnit)
  {
    MicroscopeGUI lMicroscopeGUI = this;
    return waitFor(pTimeOut, pTimeUnit, () ->
    {
      ThreadSleep.sleep(100, TimeUnit.MILLISECONDS);
      return lMicroscopeGUI.isVisible() == pVisible;
    });
  }
}
