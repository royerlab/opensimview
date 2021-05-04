package clearcontrol.microscope.lightsheet.processor.fusion;

import clearcl.util.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionProcessor;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.stacks.StackRecyclerManager;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import clearcontrol.stack.metadata.MetaDataOrdinals;
import coremem.recycling.RecyclerInterface;

import java.util.Arrays;

/**
 * This generalised fusion instructions executes the actual fast fusion operation.
 * Depending on how the images were acquired, they might have to be passed differently to
 * the FastFuse engine. Details are available in derived classes.
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public abstract class FusionInstruction extends LightSheetMicroscopeInstructionBase implements LoggingFeature
{
  private static Object mLock = new Object();
  private StackInterface mFusedStack = null;

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pDeviceName device name
   */
  public FusionInstruction(String pDeviceName, LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pDeviceName, pLightSheetMicroscope);
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  protected StackInterface fuseStacks(StackInterfaceContainer pContainer, String[] pImageKeys)
  {
    if (pContainer == null)
    {
      return null;
    }
    mFusedStack = null;

    final LightSheetFastFusionProcessor lProcessor = getLightSheetMicroscope().getDevice(LightSheetFastFusionProcessor.class, 0);

    resetEngine();

    StackRecyclerManager lStackRecyclerManager = getLightSheetMicroscope().getDevice(StackRecyclerManager.class, 0);
    RecyclerInterface<StackInterface, StackRequest> lRecycler = lStackRecyclerManager.getRecycler("warehouse", 1024, 1024);

    ElapsedTime.measure("Handle container (" + pContainer + ") and fuse", () ->
    {
      synchronized (mLock)
      {
        info("available keys in container: " + pContainer.keySet());
        info("needed keys in container: " + Arrays.toString(pImageKeys));
        for (String key : pImageKeys)
        {
          StackInterface lResultingStack = pContainer.get(key);
          StackInterface lStackInterface = lProcessor.process(lResultingStack, lRecycler);
          info("Got back: " + lStackInterface);
          if (lStackInterface != null)
          {
            mFusedStack = lStackInterface;
          }
        }
        if (mFusedStack == null)
        {
          lProcessor.getEngine().executeAllTasks();
          warning("Finished, but there are just " + lProcessor.getEngine().getAvailableImagesSlotKeys());
        }
      }
    });

    return mFusedStack;
  }

  protected void resetEngine()
  {
    resetEngine(getLightSheetMicroscope().getNumberOfLightSheets(), getLightSheetMicroscope().getNumberOfDetectionArms());
  }

  protected void resetEngine(int pNumberOfLightSheets, int pNumberOfDetectionArms)
  {
    final LightSheetFastFusionProcessor lProcessor = getLightSheetMicroscope().getDevice(LightSheetFastFusionProcessor.class, 0);

    lProcessor.getEngine().setup(pNumberOfLightSheets, pNumberOfDetectionArms);
  }

  protected void storeFusedContainer(StackInterface lFusedStack)
  {
    long lTimePoint = lFusedStack.getMetaData().getValue(MetaDataOrdinals.TimePoint);
    DataWarehouse lDataWarehouse = getLightSheetMicroscope().getDataWarehouse();
    FusedImageDataContainer lFusedContainer = new FusedImageDataContainer(lTimePoint);
    lFusedContainer.put("fused", lFusedStack);
    lDataWarehouse.put("fused_" + lTimePoint, lFusedContainer);
  }

  /**
   * Deprecated: consider accessing resulting fused stacks from the DataWarehouse
   *
   * @return
   */
  @Deprecated
  public StackInterface getFusedStack()
  {
    return mFusedStack;
  }
}
