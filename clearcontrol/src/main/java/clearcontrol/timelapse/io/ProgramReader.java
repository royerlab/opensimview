package clearcontrol.timelapse.io;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.LightSheetMicroscope;

import java.io.*;
import java.util.ArrayList;

/**
 * The ProgramReader reads an instruction list from disc and puts it in the given
 * ArrayList. This list might be the schedule from Timelapse.
 * <p>
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de) April
 * 2018
 */
public class ProgramReader implements LoggingFeature
{
  private final ArrayList<InstructionInterface> mSchedulerList;
  private final LightSheetMicroscope mLightSheetMicroscope;
  private final File mSourceFile;

  public ProgramReader(ArrayList<InstructionInterface> pSchedulerList, LightSheetMicroscope pLightSheetMicroscope, File pSourceFile)
  {
    mSchedulerList = pSchedulerList;
    mLightSheetMicroscope = pLightSheetMicroscope;
    mSourceFile = pSourceFile;
  }

  public boolean read()
  {
    StringBuilder sb = new StringBuilder();
    BufferedReader br = null;
    try
    {
      br = new BufferedReader(new FileReader(mSourceFile));
    } catch (FileNotFoundException e)
    {
      e.printStackTrace();
      return false;
    }
    try
    {
      String line = br.readLine();

      while (line != null)
      {
        sb.append(line);
        sb.append(System.lineSeparator());
        line = br.readLine();
      }
    } catch (IOException e)
    {
      e.printStackTrace();
      return false;
    } finally
    {
      try
      {
        br.close();
      } catch (IOException e)
      {
        e.printStackTrace();
        return false;
      }
    }

    String[] lInstructionNames = sb.toString().split("\n");
    for (String lInstructionName : lInstructionNames)
    {
      System.out.println(lInstructionName);
      String lSearchForName = lInstructionName.split(":")[0] + ":" + lInstructionName.split(":")[1].replace("\r", "");

      InstructionInterface lInstruction = mLightSheetMicroscope.getSchedulerDevice(lSearchForName);
      if (lInstruction != null)
      {
        lInstruction = lInstruction.copy();
        parseParameters(lInstruction, lInstructionName);
        mSchedulerList.add(lInstruction);
      } else
      {
        mSchedulerList.add(new InstructionBase("UNKNOWN INSTRUCTION: " + lInstructionName)
        {

          @Override
          public boolean initialize()
          {
            return false;
          }

          @Override
          public boolean enqueue(long pTimePoint)
          {
            return false;
          }

          @Override
          public InstructionInterface copy()
          {
            return null;
          }
        });
      }
    }

    return true;
  }

  private void parseParameters(InstructionInterface lInstruction, String lInstructionName)
  {
    if (lInstruction instanceof PropertyIOableInstructionInterface)
    {
      String[] temp = lInstructionName.split("::");
      if (temp.length > 1)
      {
        String[] properties = temp[1].split("] ");
        for (String property : properties)
        {
          temp = property.split("=");
          String name = temp[0];
          if (temp.length > 1)
          {
            String value = temp[1].replace("[", "");

            for (Variable variable : ((PropertyIOableInstructionInterface) lInstruction).getProperties())
            {
              if (ProgramWriter.variableNameToString(variable).compareTo(name) == 0)
              {
                Object object = variable.get();
                if (object instanceof String)
                {
                  variable.set(value);
                } else if (object instanceof Double)
                {
                  variable.set(Double.parseDouble(value));
                } else if (object instanceof Integer)
                {
                  variable.set(Integer.parseInt(value));
                } else if (object instanceof Boolean)
                {
                  variable.set(Boolean.parseBoolean(value));
                } else if (object instanceof File)
                {
                  variable.set(new File(value));
                } else
                {
                  warning("Couldn't read parameter " + name);
                }
                break;
              }
            }
          }
        }
      }
    }
  }
}
