package gsao64;

import com.sun.jna.NativeLong;
import coremem.ContiguousMemoryInterface;
import coremem.buffers.ContiguousBuffer;
import gsao64.exceptions.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Build a buffer to pass to gsao64-DAQ card via JNA interface
 * Structure is:
 * Buffer
 * Timepoint1
 * Channel1, Value1
 * Channel2, Value2
 * ...
 * ChannelX, ValueY, EOG Flag
 * Timepoint2
 * Channel1, Value1
 * ...
 * ChannelX, ValueY, EOG Flag, EOF Flag
 * End of Buffer
 * Rules:
 * 1) can write exactly one value to exactly one channel per timepoint
 * 2) channels must be written in incremental order per timepoint
 * 3) eog = "end of group" = "end of timepoint"
 * 4) eof = "end of function" = "end of buffer"
 * 5) eog must be written before "end of function" flag is placed
 */
public class GSBuffer
{

  static
  {
    if (GSConstants.id_off == null) GSConstants.id_off = new NativeLong(24);
    if (GSConstants.eog == null) GSConstants.eog = new NativeLong(30);
    if (GSConstants.eof == null) GSConstants.eof = new NativeLong(31);
  }

  private ContiguousBuffer buffer;

  private SortedSet<Integer> activeChans;
  private Integer[] chanValues;
  private HashMap<Integer, Integer> TPtoPosMap;
  private int tpsWritten;
  private int valsWritten;
  private int maxSizeInBytes;
  private boolean newValueAddedInCurrentTP;
  public double sampleRate = 0;

  /**
   * Constructor creates buffer by simple maxTP*maxChan calculation
   * Buffer has a maximum capacity of 256k VALUES, values = 32 bit each
   *
   * @param maxTP number of timepoints addressed, maximum allowed is 3000 per buffer
   *              as all buffers support 64 bit by default
   */
  public GSBuffer(int maxTP) throws BufferTooLargeException, BoardInitializeException
  {

    if (GSConstants.id_off == null || GSConstants.eog == null || GSConstants.eof == null)
    {
      throw new BoardInitializeException("gsao64 DAC Board constants not Initialized.  Must construct a GSSequencer first");
    }

    if (maxTP >= 3000)
    {
      throw new BufferTooLargeException("Requested buffer too large: threshold set to < 3/4 max capacity of 256k values");
    } else
    {
      maxSizeInBytes = maxTP * 64 * 4;
      buffer = ContiguousBuffer.allocate(maxSizeInBytes);
    }
    buffer.pushPosition();

    // initialize trackers
    tpsWritten = 0;
    valsWritten = 0;
    activeChans = new TreeSet<>();
    //maps timepoint to absolute memory position
    TPtoPosMap = new HashMap<>();
    TPtoPosMap.put(0, 0);
    //maps channel to most recent value
    chanValues = new Integer[65];
    newValueAddedInCurrentTP = false;
  }

  /**
   * Method to convert voltage into 16bit value for DAC card.
   * Scale limits are based on 2's complement
   *
   * @param voltage float ranging from -1 to 1
   * @return scaled voltage recast as short.
   * @throws VoltageRangeException value is less than 1 or greater than 1
   */
  private static int voltageToInt(double voltage) throws VoltageRangeException
  {
    int scaledVoltage;
    if (voltage < -1 || voltage > 1)
    {
      throw new VoltageRangeException("float voltage is out of range");
    } else if (voltage < 0)
    {
      scaledVoltage = (short) (voltage * (Math.pow(2, 15)));
      // most significant bit is padded when recasting negative short to int.  Must flip all those bits
      scaledVoltage = scaledVoltage ^ (65535 << 16);
    } else if (voltage > 0)
    {
      scaledVoltage = (short) (voltage * (Math.pow(2, 15) - 1));
    } else
    {
      scaledVoltage = (short) voltage;
    }
    return scaledVoltage;
  }

  /**
   * Add a value/chan to the end of the memory.  MUST add only to end.  MUST increment channel.
   *
   * @param voltage double from -1 to 1
   * @param chan    integer
   * @throws ActiveChanException must follow channel writing rules
   */
  public boolean appendValue(double voltage, int chan) throws ActiveChanException, VoltageRangeException
  {
    //optimization: if channel already has this value written, do not write
    if (chanValues[chan] == (Integer) this.voltageToInt(voltage))
    {
      System.out.println("chan contains key = " + chanValues[chan]);
      return false;
    }

    if (!activeChans.isEmpty() && (activeChans.last() >= chan))
    {
      throw new ActiveChanException("Higher channel exists.  Must write in increasing channel order");
    } else if (chan < 0 || chan >= 64)
    {
      throw new ActiveChanException("Channel must be 0 to 63");
    } else
    {
      int value;
      try
      {
        value = this.voltageToInt(voltage);
      } catch (VoltageRangeException ex)
      {
        throw ex;
      }

      int writevalue = (chan << GSConstants.id_off.intValue() | value);
      buffer.writeInt(writevalue);
      System.out.println(Integer.toString(writevalue));
      // push endpoint to stack
      buffer.pushPosition();
      valsWritten += 1;
      activeChans.add(chan);
      chanValues[chan] = value;
      newValueAddedInCurrentTP = true;
      return true;
    }
  }

  /**
   * adds an "end of group" flag to the most recent value written
   */
  public void appendEndofTP()
  {
    if (!newValueAddedInCurrentTP) return;

    // move to beginning of last value
    buffer.popPosition();
    buffer.popPosition();
    buffer.pushPosition();

    int value = buffer.readInt();
    int writeValue = ((1 << GSConstants.eog.intValue()) | value);

    // do not use 'appendValue'
    buffer.popPosition();
    buffer.pushPosition();
    buffer.writeInt(writeValue);
    buffer.pushPosition();
    System.out.println("appending end of timepoint with = " + writeValue);

    // marks end of TP.  Register next TP in hashmap
    tpsWritten += 1;
    TPtoPosMap.put(tpsWritten, 4 * valsWritten);
    activeChans.clear();
    activeChans.add(-1);
    newValueAddedInCurrentTP = false;
  }

  /**
   * adds an "end of function" flag to the most recent value written
   */
  public void appendEndofFunction() throws FlagException
  {
    // move to beginning of last value
    buffer.popPosition();
    buffer.popPosition();
    buffer.pushPosition();

    int value = buffer.readInt();

    if ((value >>> GSConstants.eof.intValue()) == 1)
    {
      throw new FlagException("end of function flag already exists!");
    } else if (value >>> GSConstants.eog.intValue() != 1)
    {
      throw new FlagException("must tag end of TP before end of buffer");
    }
    int newValue = (1 << GSConstants.eof.intValue() | value);

    // do not use 'appendValue'
    buffer.popPosition();
    buffer.pushPosition();
    buffer.writeInt(newValue);
    buffer.pushPosition();
  }

  /**
   * Retrieve only the short value (16bit) that was most recently written
   *
   * @return short value
   */
  public short getLastValue()
  {
    buffer.popPosition();
    buffer.popPosition();
    buffer.pushPosition();
    int value = buffer.readInt();
    buffer.pushPosition();
    return (short) (value);
  }

  /**
   * Retrieve the entire 32 bit value most recently written
   *
   * @return int (32bit)
   */
  public int getLastBlock()
  {
    buffer.popPosition();
    buffer.popPosition();
    buffer.pushPosition();
    int value = buffer.readInt();
    buffer.pushPosition();
    return value;
  }

  /**
   * Get all channels and their most recently written value
   *
   * @return hashmap of <channel, last value>
   */
  public Integer[] getAllChannels()
  {
    return chanValues;
  }

  /**
   * Get all channels written in this time point
   *
   * @return set of channels
   */
  public TreeSet<Integer> getActiveChannels()
  {
    HashSet<Integer> newset = new HashSet<>(activeChans);
    newset.remove(-1);
    return new TreeSet<>(newset);
  }

  /**
   * get total timepoints written
   *
   * @return integer tps written
   */
  public int getNumTPWritten()
  {
    return tpsWritten;
  }

  /**
   * get total timepoints possible
   *
   * @return integer tps written
   */
  public int getNumTP()
  {
    return maxSizeInBytes / (64 * 4);
  }

  /**
   * get total number of values written
   *
   * @return integer number of values
   */
  public int getValsWritten()
  {
    return valsWritten;
  }

  /**
   * get allocated size of this buffer
   * does not represent the # values written
   *
   * @return total size in bytes
   */
  public int getMaxSizeInBytes()
  {
    return maxSizeInBytes;
  }


  /**
   * returns hashmap of ALL channels/value pairs at given timepoint
   * <p>
   * push to set endpoint on stack
   * retrieve TPtoPosMap(tp) and (tp+1) values
   * difference between returned values/4 represents # values written
   * go to tp memory position
   * loop for number of values and return all channel/value pairs (readint)
   * pop to return to endpoint on stack
   *
   * @param timepoint integer timepoint location to query
   * @return hashmap with (key, value) = (channel, short(16bit)value)
   */
  public HashMap<Integer, Short> getTPValues(int timepoint)
  {
    HashMap<Integer, Short> ChanValPair = new HashMap<>();
    int channel, value, eof_flag, eog_flag;
    buffer.pushPosition();
    int tpOffset1 = TPtoPosMap.get(timepoint);
    int tpOffset2 = TPtoPosMap.get(timepoint + 1);
    int numVals = (tpOffset2 - tpOffset1) / 4;
    buffer.setPosition(tpOffset1);
    for (int i = 0; i < numVals; i++)
    {
      value = buffer.readInt();

      channel = (value >>> GSConstants.id_off.intValue());
      eof_flag = (value >>> GSConstants.eof.intValue());
      eog_flag = (value >>> GSConstants.eog.intValue());

      // remove EOF, EOG and channel bits from int value
      if (eof_flag == 1)
      {
        channel &= ~192;    // turn eof AND eog flag off
        value &= ~(192 << GSConstants.id_off.intValue());
        value &= ~(channel << GSConstants.id_off.intValue());
      } else if (eog_flag == 1)
      {
        channel &= ~64;     // turn eog flag off
        value &= ~(64 << GSConstants.id_off.intValue());
        value &= ~(channel << GSConstants.id_off.intValue());
      } else
      {
        value &= ~(channel << GSConstants.id_off.intValue());
      }
      //System.out.println("channel = "+channel);
      //System.out.println("value = "+(short)value);
      ChanValPair.put(channel, (short) value);
    }
    buffer.popPosition();
    return ChanValPair;
  }

  public void setBufferSampleRate(double rate)
  {
    this.sampleRate = rate;
  }

  /**
   * reset stack to beginning and clear it
   * reset all channel trackers
   * overwrite memory with zeros
   */
  public void clearALL()
  {
    buffer.rewind();
    buffer.clearStack();
    chanValues = new Integer[65];
    valsWritten = 0;
    tpsWritten = 0;
    activeChans = new TreeSet<>();
    TPtoPosMap = new HashMap<>();
    buffer.fillBytes((byte) 0);
  }

  /**
   * retrieve memory interface of this block
   *
   * @return memory interface
   */
  public ContiguousMemoryInterface getMemory()
  {
    return buffer.getContiguousMemory();
  }

}
