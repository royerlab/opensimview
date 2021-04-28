package gsao64;

import coremem.util.NativeLibResourceHandler;
import gsao64.exceptions.*;
import bindings.AO64_64b_Driver_CLibrary;
import bindings.GS_NOTIFY_OBJECT;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.NativeLongByReference;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayDeque;

/**
 * for continuous function, this is the order of operations:
 * 1) construct a gsao64 Sequencer
 * 2) construct a gsao64 Buffer
 * 3) write function to buffer using primarily "appendValue" and "appendEndofTP", "appendEndofFunction"
 * 4)
 */
public class GSSequencer {

    static {
        try {
            NativeLibResourceHandler lNativeLibResourceHandler = new NativeLibResourceHandler();

            File lTmpFile = lNativeLibResourceHandler.copyResourceFromJarToTempFile(GSSequencer.class,"/win32-x86-64/GS64ebApi.dll");
            File lFile = new File("GS64ebApi.dll");
            Files.copy(lTmpFile.toPath(), lFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.load(lFile.getAbsolutePath());

            lNativeLibResourceHandler.loadResourceFromJar(GSSequencer.class, "/win32-x86-64/AO64_64b_Driver_C.dll");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static AO64_64b_Driver_CLibrary INSTANCE;

    public static volatile int sequencerEmpty = 0;
    public static double currentSampleRate;

    private GS_NOTIFY_OBJECT Event = new GS_NOTIFY_OBJECT();
    private HANDLE myHandle = new HANDLE();
    private DWORD EventStatus = new DWORD();
    private NativeLongByReference BuffPtr = new NativeLongByReference();
    private int target_thresh_values;



    /**
     * IMPORTANT: Choosing numThresholdValues minimum is wise. As given: new GSSequencer(1, 40000)
     * The reason behind this suggestion is that GS card has a buffer threshold flag which goes low
     * when number of elements on its buffer goes below the numThresholdValues. Have tried to set that
     * flag high manually but was not successful. Hence suggested use requires minimum possible value
     * of numThresholdValues for the construction of GSSequencer.
     *
     * constructor establishes communication with board
     * Must execute in order:
     *  1) Find Boards
     *  2) Get Handle
     *  3) Initialize
     *  4) Autocalibrate only ONCE per day or computer restart.
     */
    public GSSequencer(int numThresholdValues) throws InvalidBoardParamsException, DriverBindingsException
    {
        this(numThresholdValues, 400000,false, true);
    }

    public GSSequencer(int numThresholdValues, int sample_rate) throws InvalidBoardParamsException, DriverBindingsException
    {
        this(numThresholdValues,sample_rate,false, true);
    }

    public GSSequencer(int numThresholdValues, int sample_rate, boolean runAutoCal) throws InvalidBoardParamsException, DriverBindingsException
    {
        this(numThresholdValues,sample_rate,runAutoCal, true);
    }

    public GSSequencer(int numThresholdValues, int sample_rate, boolean runAutoCal, boolean twosComplement) throws InvalidBoardParamsException, DriverBindingsException
    {
        target_thresh_values = numThresholdValues;
        if(target_thresh_values <= 0 || target_thresh_values > 256000 || sample_rate > 500000 || sample_rate < 1){
            throw new InvalidBoardParamsException(
                    "Threshold value out of range, or Sample rate out of range");
        }
        if(sample_rate > 450000){
            throw new InvalidBoardParamsException("Sample rate too high.  Unpredictable behavior close to 500ksps");
        }

        try {
            INSTANCE = AO64_64b_Driver_CLibrary.INSTANCE;
        } catch (NoClassDefFoundError cdef) {
            throw new DriverBindingsException("Could not initialize class bindings.AO64_64b_Driver_CLibrary");
        } catch (UnsatisfiedLinkError lnk) {
            throw new DriverBindingsException("Unsatisfied Link Error, driver not installed");
        }

        GSConstants.ulBdNum = new NativeLong(1);
        GSConstants.ulError = new NativeLongByReference();

        findBoards();
        getHandle();
        setBoardParams();

        InitializeBoard();
        if(runAutoCal) {
            AutoCalibration();
        }
        if(twosComplement){
            TwosComplement();
        }

        setBufferThreshold(target_thresh_values);
        currentSampleRate = setSampleRate(sample_rate);

    }

    /**
     * Sends head of ArrayDeque list to buffers until list is empty
     *      Event Handlers, Interrupts, Interrupt notification are used to monitor threshold event
     *      prefill buffer writes values to buffer before starting clock (might not be necessary)
     *
     * @param data ArrayDeque of GSBuffers.  Buffers constructed using CoreMem Contiguous Buffer
     * @return true when done playing
     */
    public boolean play(ArrayDeque<GSBuffer> data, int wait_for_trigger_milliseconds)
    {
        try{
            setEventHandlers();
            setEnableInterrupt(0, 0x04);
            setInterruptNotification();
        } catch (Exception ex) {System.out.println(ex);}

        connectOutputs();
        openDMAChannel(1);

        prefillBuffer(data);

        startClock();

        System.out.println("writing to outputs now");
        while(!data.isEmpty())
        {
            EventStatus.setValue(Kernel32.INSTANCE.WaitForSingleObject(myHandle, wait_for_trigger_milliseconds));
            System.out.println("buffer size before switch = "+ getLongRegister(GSConstants.BUFFER_SIZE_RA).toString());
            switch(EventStatus.intValue()) {
                case 0x00://wait_object_0, object is signaled;
                    System.out.println(" object signaled ... writing to outputs");
                    if( !checkDMAOverflow(data.peek()) ){
                        //check buffer sample rate.
                        if (data.peek().sampleRate != 0){
                            currentSampleRate = setSampleRate(data.peek().sampleRate);
                        }
                        sendDMABuffer(data.peek(), data.peek().getValsWritten());
                        data.remove();
                    }
                    break;
                case 0x80://wait abandoned;
                    System.out.println(" Error ... Wait abandoned");
                    break;
                case 0x102://wait timeout.  object stat is non signaled
                    System.out.println(" Error ... Wait timeout");
                    break;
                case 0xFFFFFFFF:// wait failed.  Function failed.  call GetLastError for extended info.
                    System.out.println(" Error ... Wait failed");
                    break;
            }
            sequencerEmpty += 1;
        }

        return true;
    }

    /**
     * Writes to DMA buffer a JNAPointer to Coremem buffer.
     *  Also specifies number of words to write from this buffer.
     * @param bufferElement A single GSBuffer block
     * @param words the number of values written to the block.  This is different from the size of the block.
     */
    private void sendDMABuffer(GSBuffer bufferElement, int words)
    {
        BuffPtr.setPointer(bufferElement.getMemory().getJNAPointer().share(0));
        INSTANCE.AO64_66_DMA_Transfer(GSConstants.ulBdNum, GSConstants.ulChannel, new NativeLong(words), BuffPtr, GSConstants.ulError);
    }

    /**
     * Queries the GSDAC board for target buffer threshold anc current buffer size
     *      Requires that Interrupt event, Buffer Threshold high-low is enabled.
     *      Used ONLY during buffer prefill initialization
     * @return False if below threshold, true if above.
     * @throws DMAOccupancyException thrown if DMA high-low thresh is not set
     */
    private boolean checkDMAThreshSatisfied() throws DMAOccupancyException
    {
        // This will break if we allow the user to define his/her own interrupt flag.
        if (GSConstants.InterruptValue.intValue() == 0x04)
        {
            //int targetTHRSHLD = getLongRegister(GSConstants.BUFFER_THRSHLD_RA).intValue();
            int targetTHRSHLD = this.target_thresh_values;
            int currentSize = getLongRegister(GSConstants.BUFFER_SIZE_RA).intValue();
            System.out.println("   targetThsld = "+targetTHRSHLD);
            System.out.println("   currentSize = "+currentSize);
            // True = Threshold satisfied, False = still under threshold
            return(currentSize > targetTHRSHLD);
        } else {
            throw new DMAOccupancyException(
                    "DMA threshold interruption not set");
        }
    }

    /**
     * check whether writing the next entry in the GSBuffer queue will fill the DMA to max
     * @param nextBufferEntry GSBuffer object received from ArrayDeque
     * @return false if DMA will overflow.  True if OK to write.
     */
    private boolean checkDMAOverflow(GSBuffer nextBufferEntry)
    {
        if(nextBufferEntry == null) {return false;}

        int nextBufferSize = nextBufferEntry.getValsWritten();
        int currentSize = getLongRegister(GSConstants.BUFFER_SIZE_RA).intValue();
        System.out.println("   nextBuff = "+nextBufferSize);
        System.out.println("   currentSize = "+currentSize);
        // true=WILL overflow, false=WILL NOT overflow
        return(currentSize + nextBufferSize > 256000);
    }

    /**
     * DMA initialization method to fill buffer before clock is started
     *      Continually writes next BufferArray entry if DMAbuffer is below threshold
     *      If DMABuffer will overflow, or if BufferArrray is empty, will exit loop and allow operation
     *      !!! should we manually flip the threshold trigger bit if there is overflow at prefill event??? !!!!
     * @param buffer Entire ArrayDeque of GSBuffers
     */
    private void prefillBuffer(ArrayDeque<GSBuffer> buffer)
    {
        try{
            do {
                System.out.println("\n");
                boolean checkThresh = checkDMAThreshSatisfied();
                boolean dmaOverflow = checkDMAOverflow(buffer.peek());
                System.out.println("start do loop checkThresh = "+checkThresh);
                System.out.println("DMA Overflow = "+checkDMAOverflow(buffer.peek()));
                System.out.println("buffer is empty = "+buffer.isEmpty());
                if (!checkThresh && !dmaOverflow && buffer.isEmpty()) {
                    // under thresh, will not overflow, no more values to write
                    System.out.println("WARNING: not enough values to trigger threshold");
                    break;
                } else if ( !checkThresh && !dmaOverflow ) {
                    // under thresh, will not overflow
                    sendDMABuffer(buffer.peek(), buffer.peek().getValsWritten());
                    buffer.remove();
                } else if (!checkThresh && dmaOverflow) {
                    // under thresh, will overflow
                    System.out.println("WARNING: next buffer value will overflow DMA");
                    break;
                } else {
                    continue;
                }
                checkThresh = checkDMAThreshSatisfied();
                System.out.println("end do loop checkThresh = "+checkThresh);
            } while (!checkDMAThreshSatisfied());

        } catch (Exception ex) {System.out.println(ex);}

    }
    
    /**
     * Set the desired sampling rate.
     * Board contains Rate-A and Rate-B.  Rate-B can be used for triggering, while Rate-A is used for sampling
     * calculated by fRate = Fclk / Nrate, with Fclk = 49.152 MHZ, Nrate = control register value
     * @param fRate desired sample rate
     * @return actual sample rate
     */
    private double setSampleRate(double fRate)
    {
        System.out.println("setting sample rate = "+fRate);
        return INSTANCE.AO64_66_Set_Sample_Rate(GSConstants.ulBdNum, fRate, GSConstants.ulError);
    }

    /**
     * Threshold number of values that triggers a buffer threshold flag interruption event
     * @param numValues Buffer takes maximum 256k values
     */
    private void setBufferThreshold(int numValues)
    {
        System.out.println("setting buffer threshold = "+numValues);
        NativeLong val = new NativeLong(numValues);
        setLongRegister(GSConstants.BUFFER_THRSHLD_RA, val );
    }

    /**
     * creates a GS_NOTIFY_OBJECT that receives Kernel32 event
     */
    private void setEventHandlers() throws HandleCreationException
    {
        myHandle = Kernel32.INSTANCE.CreateEvent(null, false, false, null);
        if( myHandle == null){
            throw new HandleCreationException("Insufficient Resources to create event handle");
            //System.exit(1);
        } else {
            Event.hEvent.setPointer(myHandle.getPointer().share(0, 16));
        }
    }

    /**
     * enable interrupt using established event handler(type)
     * @param DeviceType Either 0 = LOCAL, or 1 = DMA.  Always use LOCAL
     * @param InterruptValue Table 3.6-1 below:
     *
     * Interrupt    Interrupt Event
     * 0            Idle. Interrupt disabled unless initializing. Default state.
     * 1            Autocalibration completed
     * 2            Output buffer empty
     * 3            Buffer threshold flag Low-to-High transition
     * 4            Buffer threshold flag High-to-Low transition
     * 5            Burst Trigger Ready
     * 6            Load Ready (LOW-to-HIGH transition)
     * 7            End Load Ready (HIGH-to-LOW transition of Load Ready)
     *
     */
    private void setEnableInterrupt(int DeviceType, int InterruptValue) throws InterruptDeviceTypeException, InterruptValueException
    {
        if (DeviceType!=0 && DeviceType!=1){
            throw new InterruptDeviceTypeException("Invalid interrupt device type: must be int 0 or 1");
        }

        if (DeviceType==0 && (InterruptValue<1 || InterruptValue>7)){
            throw new InterruptValueException("Invalid interrupt event value: must be 1 through 7");
        } else if (DeviceType == 1 && (InterruptValue!=0 && InterruptValue!=1)){
            throw new InterruptValueException("Invalid interrupt event value: must be 0 or 1 for DMA interrupt");
        } else {
            //intentionally instantiate value and type here
            GSConstants.InterruptValue = new NativeLong(InterruptValue);
            GSConstants.InterruptType = new NativeLong(DeviceType);
            INSTANCE.AO64_66_EnableInterrupt(GSConstants.ulBdNum, GSConstants.InterruptValue, GSConstants.InterruptType, GSConstants.ulError);
        }
    }

    /**
     * disable most recently enabled interrupt (determined by ulValue)
     */
    private void setDisableInterrupt()
    {
        INSTANCE.AO64_66_DisableInterrupt(GSConstants.ulBdNum, GSConstants.InterruptValue, GSConstants.InterruptType, GSConstants.ulError);
    }

    /**
     * Uses GS_NOTIFY_OBJECT to receive interrupt notification event
     * EventStatus can be used to read the status of the interruption event
     */
    private void setInterruptNotification() throws EventHandlerException, InterruptDeviceTypeException
    {
        //CHECK THAT THIS IS THE RIGHT WAY TO SEE IF HANDLER IS SET
        if (Event == null) {
            throw new EventHandlerException("Event Handler not set");
        } else if (GSConstants.InterruptType == null) {
            throw new InterruptDeviceTypeException("Interrupt device type not created");
        } else{
            INSTANCE.AO64_66_Register_Interrupt_Notify(GSConstants.ulBdNum, Event, GSConstants.InterruptValue, GSConstants.InterruptType, GSConstants.ulError);
        }
    }

    private void stopInterruptNotification()
    {
        INSTANCE.AO64_66_Cancel_Interrupt_Notify(GSConstants.ulBdNum, Event, GSConstants.ulError);
    }

    private void startClock()
    {
        INSTANCE.AO64_66_Enable_Clock(GSConstants.ulBdNum, GSConstants.ulError);
    }

    private void stopClock()
    {
        INSTANCE.AO64_66_Disable_Clock(GSConstants.ulBdNum, GSConstants.ulError);
    }

    /**
     * Only two DMA channels.  We hard set this to 1
     * @param channel 1 or 0
     */
    private void openDMAChannel(int channel)
    {
        //intentionally instantiate channel here
        GSConstants.ulChannel = new NativeLong(channel);
        INSTANCE.AO64_66_Open_DMA_Channel(GSConstants.ulBdNum, GSConstants.ulChannel, GSConstants.ulError);
    }

    /**
     * Close active DMA channel.  Value set by GSConstants.ulChannel is closed.
     */
    private void closeDMAChannel()
    {
        INSTANCE.AO64_66_Close_DMA_Channel(GSConstants.ulBdNum, GSConstants.ulChannel, GSConstants.ulError);
    }


    /**
     * Return value never used but "FindBoards" must be called for board initialization
     * @return number of boards found.  We have only one.
     */
    private NativeLong findBoards()
    {
        Memory p = new Memory(69);
        ByteBuffer DeviceInfo = p.getByteBuffer(0, p.size()).order(ByteOrder.nativeOrder());
        NativeLong out = INSTANCE.AO64_66_FindBoards(DeviceInfo, GSConstants.ulError);
        byte[] bytes;
        if(DeviceInfo.hasArray()){
            bytes = DeviceInfo.array();
        } else{
            bytes = new byte[DeviceInfo.remaining()];
            DeviceInfo.get(bytes);
        }
        String buf = new String(bytes, Charset.forName("UTF-8"));
        System.out.println("device info from FindBoards = "+ buf);
        return out;
    }

    /**
     * Return value is never used but "get_handle" must be called for board initialization
     * @return board handle
     */
    private NativeLong getHandle()
    {
        return INSTANCE.AO64_66_Get_Handle(GSConstants.ulError, GSConstants.ulBdNum);
    }

    /**
     * Not explicitly required for board initialization,
     *      but sets values for GSConstants class that are critical for operation.
     */
    private void setBoardParams()
    {
        GSConstants.numChan = new NativeLong();
        GSConstants.id_off = new NativeLong();
        GSConstants.eog = new NativeLong();
        GSConstants.eof = new NativeLong();
        GSConstants.disconnect = new NativeLong();

        GSConstants.ValueRead = getLongRegister(GSConstants.FW_REV_RA);

        switch((GSConstants.ValueRead.intValue() >> 16) & 0x03){
            case 1:
            case 2: GSConstants.numChan.setValue(32); break;
            case 3: GSConstants.numChan.setValue(16); break;
            default: GSConstants.numChan.setValue(64);
        }
        if((GSConstants.ValueRead.intValue() & 0xFFFF) >= 0x400){
            GSConstants.id_off.setValue(24);
            GSConstants.eog.setValue(30);
            GSConstants.eof.setValue(31);
        } else{
            GSConstants.id_off.setValue(16);
            GSConstants.eog.setValue(22);
            GSConstants.eof.setValue(23);
        }

        if((GSConstants.ValueRead.intValue() & 0x1000000) == 0x00){
            GSConstants.disconnect.setValue(1);
        }

        // Example uses the below to reset outputs to midscale
        GSConstants.ReadValue = new NativeLong[16385];
        for(int i = 0; i< GSConstants.numChan.intValue(); i++){
            GSConstants.ReadValue[i] = new NativeLong( (i << GSConstants.id_off.intValue()) | (1 << GSConstants.eog.intValue()) | 0x8000 );
        }

        System.out.println("numChan : ... : " + GSConstants.numChan);
        System.out.println("id_off: ..... : " + GSConstants.id_off);
        System.out.println("eog : ....... : " + GSConstants.eog);
        System.out.println("eof : ....... : " + GSConstants.eof);
    }

    /**
     * Required for board Initialization.
     * Sets values in GSConstants class that are critical for operation
     */
    private void InitializeBoard()
    {
        System.out.println("Initializing Board");
        INSTANCE.AO64_66_Initialize(GSConstants.ulBdNum, GSConstants.ulError);
    }

    /**
     * Necessary for board Initialization
     *      Need only be run ONCE when computer is restarted or loads on outputs change substantially.
     */
    private void AutoCalibration()
    {
        System.out.println("Autocalibrating the board");
        if(INSTANCE.AO64_66_Autocal(GSConstants.ulBdNum, GSConstants.ulError).intValue() != 1)
        {
            System.out.println("Autocal Failed");
            System.exit(1);
        } else {
            System.out.println("Autocal Passed");
        }
    }

    /**
     * The default output data coding is "OffsetBinary".  We want to use "twos complement"
     *  this flag in the constructor will flip the control bit HIGH, enabling
     *
     * Analog Output Level                  Offset Binary       Two-s Complement
     * Positive Full Scale minus 1 LSB      XXXX FFFF           XXXX 7FFF
     * Zero plus 1 LSB                      XXXX 8001           XXXX 0001
     * Zero                                 XXXX 8000           XXXX 0000
     * Zero minus 1 LSB                     XXXX 7FFF           XXXX FFFF
     * Negative Full Scale plus 1 LSB       XXXX 0001           XXXX 8001
     * Negative Full Scale                  XXXX 0000           XXXX 8000
     */
    private void TwosComplement()
    {
        int bcrValue = INSTANCE.AO64_66_Read_Local32(GSConstants.ulBdNum, GSConstants.ulError, GSConstants.BCR_RA).intValue();
        if(((bcrValue>>4) & 1) == 1);
        {
            //flag is set high, must flip
            bcrValue &= ~0x10;
            NativeLong newValue = new NativeLong(bcrValue);
            INSTANCE.AO64_66_Write_Local32(GSConstants.ulBdNum, GSConstants.ulError, GSConstants.BCR_RA, newValue);
        } //else do nothing
    }

    /**
     * Flips "Disconnect outputs" bit to LOW in the BCR_RA
     */
    private static void connectOutputs()
    {
        if(GSConstants.disconnect.equals(0)){
            return;
        }
        NativeLong myData = getLongRegister(GSConstants.BCR_RA);
        myData.setValue(myData.intValue() & ~0x4);
        setLongRegister(GSConstants.BCR_RA, myData);
    }

    /**
     * used by external processes to reset all outputs.
     */
    public static void resetOutputsToZero()
    {
        for(int cntr = 0; cntr < GSConstants.numChan.intValue() ; cntr++){
            setLongRegister(GSConstants.OUTPUT_DATA_BUFFER_RA, GSConstants.ReadValue[cntr]);
        }
    }

    private void closeHandle()
    {
        INSTANCE.AO64_66_Close_Handle(GSConstants.ulBdNum, GSConstants.ulError);
    }

    private static NativeLong getLongRegister(NativeLong register) {
        return INSTANCE.AO64_66_Read_Local32(GSConstants.ulBdNum, GSConstants.ulError, register);
    }

    private static void setLongRegister(NativeLong register, NativeLong value) {
        INSTANCE.AO64_66_Write_Local32(GSConstants.ulBdNum, GSConstants.ulError, register, value);
    }

    /**
     *
     * @return number of times sequencer has emptied during play
     */
    public int getSequencerEmpty(){
        return sequencerEmpty;
    }


    /**
     * to be called at end of operation.
     */
    public void close() {
        resetOutputsToZero();
        stopInterruptNotification();
        setDisableInterrupt();
        stopClock();
        closeDMAChannel();
        closeHandle();
    }

}
