package gsao64.test;

import com.sun.jna.NativeLong;
import gsao64.GSConstants;
import gsao64.GSSequencer;
import gsao64.GSSplitterBuffer;
import gsao64.exceptions.DriverBindingsException;
import gsao64.exceptions.InvalidBoardParamsException;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;

import static org.junit.jupiter.api.Assertions.*;

public class GSSplitterBufferTests
{
    private GSSplitterBuffer bufferTest1;
    private GSSplitterBuffer bufferTest2;

    private GSSequencer sequencerTest;

    //initialize constant values without initializing board
    private GSConstants lGSConstants = new GSConstants();

    private ArrayDeque<GSSplitterBuffer> arrayData;

    public GSSplitterBufferTests() throws InvalidBoardParamsException, DriverBindingsException {}

    @Test
    void GSSplitterBuffer_testConstructor()
    {
        try {
            bufferTest2 = new GSSplitterBuffer(4097);
            assertEquals(2, bufferTest2.mData.size());
        } catch (Exception ex) {
            fail(ex);
        }

    }

    @Test
    void GSSplitterBuffer_testBasicFunctionality()
    {
        try {
            sequencerTest = new GSSequencer(65536*2, 4096 * 2);
            bufferTest1 = new GSSplitterBuffer(4096 * 20);
        } catch (DriverBindingsException lnk) {
            System.out.println("test skipped: "+lnk.getMessage());
            return;
        } catch (Exception ex) {
            fail(ex);
        }

        continuousSineFunction(bufferTest1, 4096 * 20);

        if (sequencerTest.play(bufferTest1.getData(), 1)) {
            System.out.println("Successful write even with above problematic buffer size");
        } else {
            fail("failure to handle the bigger buffer with GSSplitterBuffer");
        }
    }

    /**
     *
     * @param data memory allocated from GSSplitterBuffer
     */
    private void continuousSineFunction(GSSplitterBuffer data, int timepoints)
    {
        int numTP = timepoints;
        try {
            for (int loop = 0; loop < numTP; loop++) {
                for (int i = 0; i < 16; i++) {
//                    float value = (float) Math.sin(0.02*loop+0.01*i);
                    float value = (float) Math.sin(Math.PI * 2 * ((float)loop/(4096 * 2)));
                    data.appendValue(value, i);
                }
                data.appendEndofTP();
            }
            data.appendEndofFunction();
        } catch (Exception ex) {
            fail(ex);
        }

    }
}
