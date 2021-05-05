package clearcontrol.state.instructions;

import clearcontrol.LightSheetMicroscope;
import clearcontrol.MicroscopeBase;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.state.AcquisitionStateInterface;

import java.util.ArrayList;

/**
 * This instructions allows saving a copy of the current acquisition state to a list or
 * restoring the latest acquisition state if the list is not empty.
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) March
 * 2018
 */
public class AcquisitionStateBackupRestoreInstruction extends LightSheetMicroscopeInstructionBase implements InstructionInterface, LoggingFeature
{
  boolean mBackup;
  private static ArrayList<AcquisitionStateInterface> mAcquisitionStateList = new ArrayList<>();

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pBackup: If true, the instructions puts a new entry in the LIFO list, if false
   *                 it will restore the last entry.
   */
  public AcquisitionStateBackupRestoreInstruction(boolean pBackup, LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Adaptation: " + (pBackup ? ("Backup") : ("Restore")) + " acquisition state", pLightSheetMicroscope);
    mBackup = pBackup;
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    if (mBackup)
    {
      AcquisitionStateInterface lState = getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState().duplicate("backup " + System.currentTimeMillis());
      mAcquisitionStateList.add(lState);
    } else
    {
      if (mAcquisitionStateList.size() > 0)
      {
        AcquisitionStateInterface lState = mAcquisitionStateList.get(mAcquisitionStateList.size() - 1);
        mAcquisitionStateList.remove(mAcquisitionStateList.size() - 1);
        MicroscopeBase mMicroscope = getLightSheetMicroscope();
        mMicroscope.getAcquisitionStateManager().setCurrentState(lState);
      } else
      {
        warning("Error: Cannot restore state, list is empty.");
      }
    }
    return true;
  }

  public boolean isBackup()
  {
    return mBackup;
  }

  @Override
  public AcquisitionStateBackupRestoreInstruction copy()
  {
    return new AcquisitionStateBackupRestoreInstruction(mBackup, getLightSheetMicroscope());
  }
}
