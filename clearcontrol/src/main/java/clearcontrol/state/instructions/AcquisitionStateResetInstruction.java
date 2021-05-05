package clearcontrol.state.instructions;

import clearcontrol.LightSheetDOF;
import clearcontrol.LightSheetMicroscope;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.state.InterpolatedAcquisitionState;
import clearcontrol.state.tables.InterpolationTables;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) March
 * 2018
 */
public class AcquisitionStateResetInstruction extends LightSheetMicroscopeInstructionBase implements InstructionInterface, LoggingFeature
{
  /**
   * INstanciates a virtual device with a given name
   */
  public AcquisitionStateResetInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Adaptation: Reset acquisition state", pLightSheetMicroscope);
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    InterpolatedAcquisitionState lAcquisitionState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState();

    for (int lLightSheetIndex = 0; lLightSheetIndex < getLightSheetMicroscope().getNumberOfLightSheets(); lLightSheetIndex++)
    {
      for (int cpi = 0; cpi < lAcquisitionState.getNumberOfControlPlanes(); cpi++)
      {
        InterpolationTables it = lAcquisitionState.getInterpolationTables();
        it.set(LightSheetDOF.IZ, cpi, lLightSheetIndex, 0);
        it.set(LightSheetDOF.IY, cpi, lLightSheetIndex, 0);
        it.set(LightSheetDOF.IX, cpi, lLightSheetIndex, 0);
        it.set(LightSheetDOF.IA, cpi, lLightSheetIndex, 0);
        it.set(LightSheetDOF.IW, cpi, lLightSheetIndex, 0.45); // Todo: this
        // value is XWing
        // specific
        it.set(LightSheetDOF.IH, cpi, lLightSheetIndex, 500); // Todo: this
        // value is XWing
        // speciific
      }
    }

    return true;
  }

  @Override
  public AcquisitionStateResetInstruction copy()
  {
    return new AcquisitionStateResetInstruction(getLightSheetMicroscope());
  }
}
