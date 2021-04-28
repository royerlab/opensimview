package gsao64;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;


/**
 * constants used by the example.GSConstants program supplied by General Standards
 */
public class GSConstants
{
    // board parameters
    public static NativeLong ulBdNum, numChan, id_off, eog, eof, disconnect;
    public static NativeLongByReference ulError;

    // board register addresses - RA stands for register address
    public static final NativeLong BCR_RA = new NativeLong(0x00);
    public static final NativeLong Reserved_RA = new NativeLong(0x04);
    public static final NativeLong Reserved1_RA = new NativeLong(0x08);
    public static final NativeLong BUFFER_OPS_RA = new NativeLong(0x0C);
    public static final NativeLong FW_REV_RA = new NativeLong(0x10);
    public static final NativeLong AUTO_CAL_RA = new NativeLong(0x14);
    public static final NativeLong OUTPUT_DATA_BUFFER_RA = new NativeLong(0x18);
    public static final NativeLong BUFFER_SIZE_RA = new NativeLong(0x1C);
    public static final NativeLong BUFFER_THRSHLD_RA = new NativeLong(0x20);
    public static final NativeLong RATE_A_RA = new NativeLong(0x24);
    public static final NativeLong RATE_B_RA = new NativeLong(0x28);

    // input/output values from board
    public static NativeLong ValueRead;
    public static NativeLong[] ReadValue;
    public static NativeLongByReference BuffPtr;

    // for DMA memory handling
    public static NativeLong InterruptType;
    public static NativeLong InterruptValue;
    public static NativeLong ulChannel;
    public static NativeLong ulWords;
}
