package gsao64.test;

import gsao64.GSBuffer;
import gsao64.GSConstants;
import gsao64.exceptions.ActiveChanException;
import gsao64.exceptions.BufferTooLargeException;
import gsao64.exceptions.VoltageRangeException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;


public class GSBufferTests
{

  private GSBuffer buffertest;

  //initialize constant values without initializing board
  private GSConstants lGSConstants = new GSConstants();

  /**
   * test gsao64 buffer constructor for too much data and > 3/4 full.
   */
  @Test
  void GSBuffer_BufferMaxSize()
  {
    assertThrows(BufferTooLargeException.class, () -> new GSBuffer(4001));

    assertThrows(BufferTooLargeException.class, () -> new GSBuffer(4000));

    assertThrows(BufferTooLargeException.class, () -> new GSBuffer(3000));
  }

  /**
   * test that voltage converter retains range of short
   */
  @Test
  void GSBuffer_VoltageToIntConversion_negativeFullScale()
  {
    try
    {
      buffertest = new GSBuffer(2000);
      buffertest.appendValue(-1.0, 1);
    } catch (Exception ex)
    {
      fail(ex);
    }

    assertEquals("8000", Integer.toHexString(buffertest.getLastValue() & 0xffff));
  }

  /**
   * test that voltage converter retains range of short
   */
  @Test
  void GSBuffer_VoltageToIntConversion_negativeFullScalePlus1LSB()
  {
    try
    {
      buffertest = new GSBuffer(2000);
      buffertest.appendValue(-0.999970, 1);
    } catch (Exception ex)
    {
      fail(ex);
    }

    assertEquals("8001", Integer.toHexString(buffertest.getLastValue() & 0xffff));
  }

  /**
   * test that voltage converter retains range of short
   */
  @Test
  void GSBuffer_VoltageToIntConversion_positiveFullScaleMinus1LSB()
  {
    try
    {
      buffertest = new GSBuffer(2000);
      buffertest.appendValue(1.0, 1);
    } catch (Exception ex)
    {
      fail(ex);
    }

    assertEquals("7fff", Integer.toHexString(buffertest.getLastValue() & 0xffff));
  }

  /**
   * test that zero voltage is handled
   */
  @Test
  void GSBuffer_VoltageToIntConversion_zeroPlus1LSB()
  {
    try
    {
      buffertest = new GSBuffer(2000);
      buffertest.appendValue(0.00006, 1);
    } catch (Exception ex)
    {
      fail(ex);
    }

    assertEquals("1", Integer.toHexString(buffertest.getLastValue() & 0xffff));
  }

  /**
   * test that zero voltage is handled
   */
  @Test
  void GSBuffer_VoltageToIntConversion_zero()
  {
    try
    {
      buffertest = new GSBuffer(2000);
      buffertest.appendValue(0, 1);
    } catch (Exception ex)
    {
      fail(ex);
    }

    assertEquals("0", Integer.toHexString(buffertest.getLastValue() & 0xffff));
  }

  /**
   * test that zero voltage is handled
   */
  @Test
  void GSBuffer_VoltageToIntConversion_zeroMinus1LSB()
  {
    try
    {
      buffertest = new GSBuffer(2000);
      buffertest.appendValue(-0.00006, 1);
    } catch (Exception ex)
    {
      fail(ex);
    }

    assertEquals("ffff", Integer.toHexString(buffertest.getLastValue() & 0xffff));
  }

  /**
   * test negative bounds exception
   */
  @Test
  void GSBuffer_VoltageToIntConversion_limit_low()
  {
    try
    {
      buffertest = new GSBuffer(2000);
    } catch (Exception ex)
    {
      fail(ex);
    }

    assertThrows(VoltageRangeException.class, () -> buffertest.appendValue(-10, 1));
    System.out.println("voltage limit low pass");
  }

  /**
   * test positive bounds exception
   */
  @Test
  void GSBuffer_VoltageToIntConversion_limit_high()
  {
    try
    {
      buffertest = new GSBuffer(2000);
    } catch (Exception ex)
    {
      fail(ex);
    }

    assertThrows(VoltageRangeException.class, () -> buffertest.appendValue(10, 1));
    System.out.println("voltage limit high pass");
  }

  /**
   * test positive write values
   */
  @Test
  void GSBuffer_VoltageToIntConversion_PostiveValues()
  {
    try
    {
      buffertest = new GSBuffer(2000);
    } catch (Exception ex)
    {
      fail(ex);
    }

    try
    {
      buffertest.appendValue(0.1, 1);
      assertEquals(3276, buffertest.getLastValue());
      buffertest.appendValue(0.2, 2);
      assertEquals(6553, buffertest.getLastValue());
      buffertest.appendValue(0.3, 3);
      assertEquals(9830, buffertest.getLastValue());
      buffertest.appendValue(0.4, 4);
      assertEquals(13106, buffertest.getLastValue());
      buffertest.appendValue(0.5, 5);
      assertEquals(16383, buffertest.getLastValue());
    } catch (Exception ex)
    {
      fail(ex);
    }

  }

  /**
   * test several negative write values
   */
  @Test
  void GSBuffer_VoltageToIntConversion_NegativeValues()
  {
    try
    {
      buffertest = new GSBuffer(2000);
    } catch (Exception ex)
    {
      fail(ex);
    }

    try
    {
      buffertest.appendValue(-0.1, 1);
      assertEquals(-3276, buffertest.getLastValue());
      buffertest.appendValue(-0.2, 2);
      assertEquals(-6553, buffertest.getLastValue());
      buffertest.appendValue(-0.3, 3);
      assertEquals(-9830, buffertest.getLastValue());
      buffertest.appendValue(-0.4, 4);
      assertEquals(-13107, buffertest.getLastValue());
      buffertest.appendValue(-0.5, 5);
      assertEquals(-16384, buffertest.getLastValue());
    } catch (Exception ex)
    {
      fail(ex);
    }

  }

  /**
   * EOGFlag: test handling of double TP flag write
   * // is this necessary?  double write does nothing anyway...
   */
  @Test
  void GSBuffer_EOG_2xWrite()
  {
    try
    {
      buffertest = new GSBuffer(2000);
      buffertest.appendValue(0.5, 1);
      buffertest.appendEndofTP();
    } catch (Exception ex)
    {
      fail(ex);
    }

    assert true;
    System.out.println("EOG 2x write pass");
  }


  /**
   * EOG flag: test correct placement of TP flag
   * check both positive and negative values with EOG and EOF flags
   */
  @Test
  void GSBuffer_EOG_correctFlag()
  {
    try
    {
      buffertest = new GSBuffer(2000);
      buffertest.appendValue(1, 1);
      buffertest.appendEndofTP();
      assertEquals(1, buffertest.getLastBlock() >>> lGSConstants.eog.intValue());
      buffertest.clearALL();

      buffertest = new GSBuffer(2000);
      buffertest.appendValue(-1, 1);
      buffertest.appendEndofTP();
      assertEquals(1, buffertest.getLastBlock() >>> lGSConstants.eog.intValue());
      buffertest.clearALL();

      buffertest = new GSBuffer(2000);
      buffertest.appendValue(1, 1);
      buffertest.appendEndofTP();
      buffertest.appendEndofFunction();
      //unsigned right shift necessary because EOF is most significant bit.
      assertEquals(3, buffertest.getLastBlock() >>> lGSConstants.eog.intValue());
      buffertest.clearALL();

      buffertest = new GSBuffer(2000);
      buffertest.appendValue(-1, 1);
      buffertest.appendEndofTP();
      buffertest.appendEndofFunction();
      //unsigned right shift necessary because EOF is most significant bit.
      assertEquals(3, buffertest.getLastBlock() >>> lGSConstants.eog.intValue());
      buffertest.clearALL();

    } catch (Exception ex)
    {
      fail(ex);
    }

  }

  /**
   * EOF flag: test correct placement of end of buffer flag
   * 4 byte int can't hold a positive number with this tag
   * it will ALWAYS return 2's complement
   */
  @Test
  void GSBuffer_EOF_correctFlag()
  {
    try
    {
      buffertest = new GSBuffer(2000);
      buffertest.appendValue(1, 1);
      buffertest.appendEndofTP();
      buffertest.appendEndofFunction();
    } catch (Exception ex)
    {
      fail(ex);
    }

    assertEquals(1, (buffertest.getLastBlock() >>> lGSConstants.eof.intValue()));

    try
    {
      buffertest = new GSBuffer(2000);
      buffertest.appendValue(-1, 1);
      buffertest.appendEndofTP();
      buffertest.appendEndofFunction();
    } catch (Exception ex)
    {
      fail(ex);
    }

    assertEquals(1, (buffertest.getLastBlock() >>> lGSConstants.eof.intValue()));

  }

  /**
   * attempt to write same active channel twice
   */
  @Test
  void GSBuffer_WriteChannel_2x()
  {
    try
    {
      buffertest = new GSBuffer(2000);
      buffertest.appendValue(1, 1);
    } catch (Exception ex)
    {
      fail(ex);
    }

    assertThrows(Exception.class, () -> buffertest.appendValue(0, 1));
    System.out.println("same active channel pass");
  }

  /**
   * attempt to write same active channel twice
   */
  @Test
  void GSBuffer_WriteChannel_OverFillBuffer()
  {
    try
    {
      buffertest = new GSBuffer(100);
    } catch (Exception ex)
    {
      fail(ex);
    }


    assertThrows(Exception.class, () ->
    {
      for (int i = 0; i < 1000; i++)
      {
        buffertest.appendValue(1, 0);
      }
    });
    System.out.println("OverFill Buffer pass");

  }

  /**
   * attempt to write beyond channel range
   */
  @Test
  void GSBuffer_WriteChannel_range()
  {
    try
    {
      buffertest = new GSBuffer(2000);
    } catch (Exception ex)
    {
      fail(ex);
    }

    assertThrows(Exception.class, () -> buffertest.appendValue(1, 65));
    System.out.println("active channel positive range pass");

    assertThrows(Exception.class, () -> buffertest.appendValue(1, -10));
    System.out.println("active channel negative range pass");
  }

  /**
   * attempt to write not in increasing order
   */
  @Test
  void GSBuffer_WriteChannel_order()
  {
    try
    {
      buffertest = new GSBuffer(2000);
      buffertest.appendValue(1, 1);
      buffertest.appendValue(1, 3);
    } catch (Exception ex)
    {
      fail(ex);
    }

    assertThrows(ActiveChanException.class, () -> buffertest.appendValue(1, 2));
    System.out.println("active channel out of order pass");
  }

  /**
   * Retrieve active channels for current timepoint
   */
  @Test
  void GSBuffer_getActiveChannels()
  {
    try
    {
      buffertest = new GSBuffer(2000);
      buffertest.appendValue(1, 1);
      buffertest.appendValue(1, 2);
      buffertest.appendValue(1, 3);
    } catch (Exception ex)
    {
      fail(ex);
    }

    TreeSet<Integer> returnset = new TreeSet<>(buffertest.getActiveChannels());

    assertEquals(3, returnset.size());

  }

  @Test
  void GSBuffer_consecutiveIdenticalTimePoints()
  {
    try
    {
      buffertest = new GSBuffer(2000);
    } catch (Exception ex)
    {
      fail(ex);
    }

    try
    {
      for (int loop = 0; loop < 100; loop++)
      {
        try
        {
          for (int i = 0; i < 16; i++)
          {
            buffertest.appendValue(0.0f, i);
          }
          buffertest.appendEndofTP();
        } catch (Exception ex)
        {
          fail(ex);
        }
      }
      buffertest.appendEndofFunction();
    } catch (Exception ex)
    {
      fail(ex);
    }
  }

  /**
   * loop to write several channels to several time points.
   * check the written values using "getTPValues"
   */
  @Test
  void GSBuffer_MultipleTimePoints()
  {
    try
    {
      buffertest = new GSBuffer(2000);
    } catch (Exception ex)
    {
      fail(ex);
    }

    try
    {
      for (int loop = 0; loop < 100; loop++)
      {
        if (loop % 2 == 0)
        {
          try
          {
            for (int i = 0; i < 10; i++)
            {
              buffertest.appendValue(0.1 * i, i);
            }
            buffertest.appendEndofTP();
          } catch (Exception ex)
          {
            fail(ex);
          }
        } else
        {
          try
          {
            for (int i = 0; i < 10; i++)
            {
              buffertest.appendValue(-0.1 * i, i);
            }
            buffertest.appendEndofTP();
          } catch (Exception ex)
          {
            fail(ex);
          }
        }
      }
      buffertest.appendEndofFunction();

    } catch (Exception ex)
    {
      fail(ex);
    }

    // check positives, EOG flag
    HashMap<Integer, Short> tpMap2 = buffertest.getTPValues(0);
    assertEquals((short) (0.1 * 0 * (Math.pow(2, 15) - 1)), (short) tpMap2.get(0));
    assertEquals((short) (0.1 * 1 * (Math.pow(2, 15) - 1)), (short) tpMap2.get(1));
    assertEquals((short) (0.1 * 2 * (Math.pow(2, 15) - 1)), (short) tpMap2.get(2));
    assertEquals((short) (0.1 * 3 * (Math.pow(2, 15) - 1)), (short) tpMap2.get(3));
    assertEquals((short) (0.1 * 4 * (Math.pow(2, 15) - 1)), (short) tpMap2.get(4));

    // check negatives, EOG and EOF flags
    HashMap<Integer, Short> tpMap = buffertest.getTPValues(1);
    assertEquals((short) (-0.1 * 1 * (Math.pow(2, 15))), (short) tpMap.get(1));
    assertEquals((short) (-0.1 * 2 * (Math.pow(2, 15))), (short) tpMap.get(2));
    assertEquals((short) (-0.1 * 3 * (Math.pow(2, 15))), (short) tpMap.get(3));
    assertEquals((short) (-0.1 * 4 * (Math.pow(2, 15))), (short) tpMap.get(4));
    assertEquals((short) (-0.1 * 5 * (Math.pow(2, 15))), (short) tpMap.get(5));

    try
    {
      assertEquals((short) (-0.1 * 0 * (Math.pow(2, 15))), (short) tpMap.get(0));
    } catch (NullPointerException ex)
    {
      System.out.println("Caught repeat zeroth channel write");
    }

  }

}
