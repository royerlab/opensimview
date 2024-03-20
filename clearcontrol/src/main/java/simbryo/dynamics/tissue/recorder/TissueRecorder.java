package simbryo.dynamics.tissue.recorder;

import simbryo.dynamics.tissue.TissueDynamics;

import java.io.*;

public class TissueRecorder implements Closeable
{

    private final TissueDynamics mTissueDynamics;

    private FileWriter mCellPositionsFileWriter = null, mCellEventsFileWriter = null;
    private PrintWriter mCellPositionsPrintWriter = null, mCellEventsPrintWriter = null;


    public TissueRecorder(TissueDynamics pTissueDynamics, String pRecordingFolderPath)
    {
        // store tissue dynamics
        mTissueDynamics = pTissueDynamics;

        // Create the recording folder
        File recordingFolder = new File(pRecordingFolderPath);
        recordingFolder.mkdirs();

        // Create the cell positions file
        File mCellPositionsFile = new File(recordingFolder, "cell_positions.csv");

        // Create the cell event file
        File mCellEventsFile = new File(recordingFolder, "cell_events.csv");

        // Create file writer and print writer for both files, and write headers:

        try
        {
            // Create file writer and print writer for both files:
            mCellPositionsFileWriter = new FileWriter(mCellPositionsFile);
            mCellPositionsPrintWriter = new PrintWriter(mCellPositionsFileWriter);

            mCellEventsFileWriter = new FileWriter(mCellEventsFile);
            mCellEventsPrintWriter = new PrintWriter(mCellEventsFileWriter);


            // Write the header for the cell positions file:
            String[] cCellPositionsHeaders = {"time (ns)", "cell id", "x", "y", "z", "r"};
            for (int i = 0; i < cCellPositionsHeaders.length; i++) {
                mCellPositionsPrintWriter.print(cCellPositionsHeaders[i]);
                if (i < cCellPositionsHeaders.length - 1) {
                    mCellPositionsPrintWriter.print(", ");
                }
            }
            mCellPositionsPrintWriter.println();

            // Write the header for the cell events file:
            String[] cCellEventsHeaders = {"time (ns)", "event", "cell id 1", "cell id 2"};
            for (int i = 0; i < cCellEventsHeaders.length; i++) {
                mCellEventsPrintWriter.print(cCellEventsHeaders[i]);
                if (i < cCellEventsHeaders.length - 1) {
                    mCellEventsPrintWriter.print(", ");
                }
            }
            mCellEventsPrintWriter.println();

        }
        catch (IOException e)
        {
            System.out.println("An error occurred while writing the CSV file for tissue recording!");
            e.printStackTrace();
        }
    }


    @Override
    public void close() throws IOException
    {
        // flush both writers:
        mCellPositionsPrintWriter.flush();
        mCellEventsPrintWriter.flush();

        // Close both writers:
        mCellPositionsFileWriter.close();
        mCellEventsFileWriter.close();
    }

    public void recordCellDivisionEvent(int pExistingCellId, int pDaughterCellId)
    {
        // get system time in nanoseconds:
        long lTimeInNanoseconds = System.nanoTime();

        // Ensure that cell is not dead:
        if (mTissueDynamics.isCellDead(pExistingCellId))
            return;

        // write in the CellEvent file the time, the event type, the existing cell id, and the new cell id:
        mCellEventsPrintWriter.println(lTimeInNanoseconds + ", " + "CellDivision" + ", " + pExistingCellId + ", " + pDaughterCellId);

        // flush the print writer:
        mCellEventsPrintWriter.flush();
    }

    public void recordCellDeathEvent(int pDyingCellId)
    {
        // get system time in nanoseconds:
        long lTimeInNanoseconds = System.nanoTime();

        // write in the CellEvent file the time, the event type, the existing cell id, and the new cell id:
        mCellEventsPrintWriter.println(lTimeInNanoseconds + ", " + "CellDeath" + ", " + pDyingCellId+ ", " + -1 );

        // flush the print writer:
        mCellEventsPrintWriter.flush();
    }

    public void recordPositions()
    {
        // get system time in nanoseconds:
        long lTimeInNanoseconds = System.nanoTime();

        // write in the CellPositions file, for each cell/particle in mTissueDynamics:  the time, and the cell position, and radius:
        for (int i = 0; i < mTissueDynamics.getNumberOfParticles(); i++)
        if (!mTissueDynamics.isCellDead(i))
            {
                mCellPositionsPrintWriter.println(lTimeInNanoseconds + ", " + i + ", " + mTissueDynamics.getPositions().getCurrentArray()[i*3] + ", " + mTissueDynamics.getPositions().getCurrentArray()[i*3+1] + ", " + mTissueDynamics.getPositions().getCurrentArray()[i*3+2] + ", " + mTissueDynamics.getRadii().getCurrentArray()[i]);
            }

        // flush the print writer:
        mCellPositionsPrintWriter.flush();
    }
}

