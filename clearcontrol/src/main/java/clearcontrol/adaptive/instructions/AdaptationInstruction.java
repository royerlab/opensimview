package clearcontrol.adaptive.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.adaptive.AdaptiveEngine;
import clearcontrol.adaptive.modules.AdaptationModuleInterface;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.instructions.LightSheetMicroscopeInstructionBase;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) March
 * 2018
 */
public class AdaptationInstruction extends LightSheetMicroscopeInstructionBase implements InstructionInterface, LoggingFeature
{
  AdaptiveEngine mAdaptiveEngine;
  Class mTargetAdapationModuleClass;

  /**
   * INstanciates a virtual device with a given name
   */
  public AdaptationInstruction(String pName, Class pTargetAdapationModuleClass, LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pName, pLightSheetMicroscope);
    mTargetAdapationModuleClass = pTargetAdapationModuleClass;
  }

  @Override
  public boolean initialize()
  {
    mAdaptiveEngine = (AdaptiveEngine) getLightSheetMicroscope().getDevice(AdaptiveEngine.class, 0);
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    for (int i = 0; i < mAdaptiveEngine.getModuleList().size(); i++)
    {
      AdaptationModuleInterface<?> lAdaptationModel = (AdaptationModuleInterface<?>) mAdaptiveEngine.getModuleList().get(i);
      lAdaptationModel.getIsActiveVariable().set(lAdaptationModel.getClass() == mTargetAdapationModuleClass);
    }

    while (mAdaptiveEngine.step()) ;

    return true;
  }

  @Override
  public AdaptationInstruction copy()
  {
    return new AdaptationInstruction(getName(), mTargetAdapationModuleClass, getLightSheetMicroscope());
  }
}
