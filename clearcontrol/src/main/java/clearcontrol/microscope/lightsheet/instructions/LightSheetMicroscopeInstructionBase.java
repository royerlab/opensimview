package clearcontrol.microscope.lightsheet.instructions;

import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;

/**
 * LightSheetMicroscopeInstructionBase
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public abstract class LightSheetMicroscopeInstructionBase extends InstructionBase
{
  private final LightSheetMicroscope mLightSheetMicroscope;

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pDeviceName device name
   */
  public LightSheetMicroscopeInstructionBase(String pDeviceName, LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pDeviceName);
    mLightSheetMicroscope = pLightSheetMicroscope;
  }

  public LightSheetMicroscope getLightSheetMicroscope()
  {
    return mLightSheetMicroscope;
  }
}
