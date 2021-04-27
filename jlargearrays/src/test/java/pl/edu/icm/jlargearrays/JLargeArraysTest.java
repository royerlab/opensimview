/* ***** BEGIN LICENSE BLOCK *****
 * JLargeArrays
 * Copyright (C) 2013 onward University of Warsaw, ICM
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ***** END LICENSE BLOCK ***** */
package pl.edu.icm.jlargearrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests.
 *
 * @author Piotr Wendykier (p.wendykier@icm.edu.pl)
 */
public class JLargeArraysTest extends TestCase
{

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public JLargeArraysTest(String testName)
    {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(JLargeArraysTest.class);
    }

    public void testBitLargeArrayConstant()
    {
        BitLargeArray a = new BitLargeArray(1l << 33, (byte) 1);
        assertTrue(a.getBoolean(0));
        assertTrue(a.getBoolean(a.length() - 1));
        Throwable e = null;
        try {
            a.setBoolean(0, false);
        } catch (IllegalAccessError ex) {
            e = ex;
        }
        assertTrue(e instanceof IllegalAccessError);
        assertNull(a.getData());
    }

    public void testBitLargeArrayGetSet()
    {
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        BitLargeArray a = new BitLargeArray(10);
        long idx = 5;
        boolean val = true;
        a.setBoolean(idx, val);
        assertEquals(val, a.getBoolean(idx));
        idx = 6;
        a.set(idx, val);
        assertEquals(val, (a.get(idx)).booleanValue());
        LargeArray.setMaxSizeOf32bitArray(1);
        a = new BitLargeArray(10);
        a.setBoolean(idx, val);
        assertEquals(val, a.getBoolean(idx));
        idx = 6;
        a.set(idx, val);
        assertEquals(val, (a.get(idx)).booleanValue());

    }

    public void testBitLargeArrayGetSetNative()
    {
        LargeArray.setMaxSizeOf32bitArray(1);
        BitLargeArray a = new BitLargeArray(10);
        long idx = 5;
        boolean val = true;
        a.setToNative(idx, val);
        assertEquals(val, (boolean) a.getFromNative(idx));
    }

    public void testBitLargeArrayGetData()
    {
        boolean[] data = new boolean[]{true, false, false, false, true, true, true, false, true, true};
        long startPos = 2;
        long endPos = 7;
        long step = 2;
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        BitLargeArray a = new BitLargeArray(data);
        boolean[] res = a.getBooleanData(null, startPos, endPos, step);
        int idx = 0;
        for (long i = startPos; i < endPos; i += step) {
            assertEquals(data[(int) i], res[idx++]);
        }
        LargeArray.setMaxSizeOf32bitArray(data.length - 1);
        a = new BitLargeArray(data);
        res = a.getBooleanData(null, startPos, endPos, step);
        idx = 0;
        for (long i = startPos; i < endPos; i += step) {
            assertEquals(data[(int) i], res[idx++]);
        }
    }

    public void testBitArraycopy()
    {
        boolean[] data = new boolean[1000000];
        for (int i = 0; i < data.length; i++) {
            data[i] = i % 5 == 0;
        }
        int startPos = 2;
        int length = data.length - 2;
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        BitLargeArray a = new BitLargeArray(data);
        BitLargeArray b = new BitLargeArray(2 * data.length);
        Utilities.arraycopy(a, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getBoolean(i));
        }
        b = new BitLargeArray(2 * data.length);
        Utilities.arraycopy(data, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getBoolean(i));
        }
        LargeArray.setMaxSizeOf32bitArray(data.length - 1);
        a = new BitLargeArray(data);
        b = new BitLargeArray(2 * data.length);
        Utilities.arraycopy(a, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getBoolean(i));
        }
        b = new BitLargeArray(2 * data.length);
        Utilities.arraycopy(data, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getBoolean(i));
        }
    }

    public void testBitConvert()
    {
        boolean[] data = new boolean[]{true, false, false, false, true, true, true, false, true, true};
        BitLargeArray a = new BitLargeArray(data);
        ByteLargeArray b = (ByteLargeArray) Utilities.convert(a, LargeArrayType.BYTE);
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i] == true ? 1 : 0, b.getByte(i));
        }
    }

    public void testByteLargeArrayConstant()
    {
        ByteLargeArray a = new ByteLargeArray(1l << 33, (byte) 2);
        assertEquals(2, a.getByte(0));
        assertEquals(2, a.getByte(a.length() - 1));
        Throwable e = null;
        try {
            a.setByte(0, (byte) 3);
        } catch (IllegalAccessError ex) {
            e = ex;
        }
        assertTrue(e instanceof IllegalAccessError);
        assertNull(a.getData());
    }

    public void testByteLargeArrayGetSet()
    {
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        ByteLargeArray a = new ByteLargeArray(10);
        long idx = 5;
        byte val = -100;
        a.setByte(idx, val);
        assertEquals(val, a.getByte(idx));
        idx = 6;
        a.set(idx, val);
        assertEquals(val, (a.get(idx)).byteValue());
        LargeArray.setMaxSizeOf32bitArray(1);
        a = new ByteLargeArray(10);
        a.setByte(idx, val);
        assertEquals(val, a.getByte(idx));
        idx = 6;
        a.set(idx, val);
        assertEquals(val, (a.get(idx)).byteValue());
    }

    public void testByteLargeArrayGetSetNative()
    {
        LargeArray.setMaxSizeOf32bitArray(1);
        ByteLargeArray a = new ByteLargeArray(10);
        long idx = 5;
        byte val = -100;
        a.setToNative(idx, val);
        assertEquals(val, (byte) a.getFromNative(idx));
    }

    public void testByteLargeArrayGetData()
    {
        byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        long startPos = 2;
        long endPos = 7;
        long step = 2;
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        ByteLargeArray a = new ByteLargeArray(data);
        byte[] res = a.getByteData(null, startPos, endPos, step);
        int idx = 0;
        for (long i = startPos; i < endPos; i += step) {
            assertEquals(data[(int) i], res[idx++]);
        }
        LargeArray.setMaxSizeOf32bitArray(data.length - 1);
        a = new ByteLargeArray(data);
        res = a.getByteData(null, startPos, endPos, step);
        idx = 0;
        for (long i = startPos; i < endPos; i += step) {
            assertEquals(data[(int) i], res[idx++]);
        }
    }

    public void testByteArraycopy()
    {
        byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int startPos = 2;
        int length = 8;
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        ByteLargeArray a = new ByteLargeArray(data);
        ByteLargeArray b = new ByteLargeArray(2 * data.length);
        Utilities.arraycopy(a, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getByte(i));
        }
        b = new ByteLargeArray(2 * data.length);
        Utilities.arraycopy(data, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getByte(i));
        }
        LargeArray.setMaxSizeOf32bitArray(data.length - 1);
        a = new ByteLargeArray(data);
        b = new ByteLargeArray(2 * data.length);
        Utilities.arraycopy(a, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getByte(i));
        }
        b = new ByteLargeArray(2 * data.length);
        Utilities.arraycopy(data, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getByte(i));
        }
    }

    public void testByteConvert()
    {
        byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        ByteLargeArray a = new ByteLargeArray(data);
        ShortLargeArray b = (ShortLargeArray) Utilities.convert(a, LargeArrayType.SHORT);
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i], b.getShort(i));
        }
    }
    
    public void testShortLargeArrayConstant()
    {
        ShortLargeArray a = new ShortLargeArray(1l << 33, (short)2);
        assertEquals(2, a.getShort(0));
        assertEquals(2, a.getShort(a.length() - 1));
        Throwable e = null;
        try {
            a.setShort(0, (short)3);
        } catch (IllegalAccessError ex) {
            e = ex;
        }
        assertTrue(e instanceof IllegalAccessError);
        assertNull(a.getData());
    }

    public void testShortLargeArrayGetSet()
    {
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        ShortLargeArray a = new ShortLargeArray(10);
        long idx = 5;
        short val = -100;
        a.setShort(idx, val);
        assertEquals(val, a.getShort(idx));
        idx = 6;
        a.set(idx, val);
        assertEquals(val, (a.get(idx)).shortValue());
        LargeArray.setMaxSizeOf32bitArray(1);
        a = new ShortLargeArray(10);
        a.setShort(idx, val);
        assertEquals(val, a.getShort(idx));
        idx = 6;
        a.set(idx, val);
        assertEquals(val, (a.get(idx)).shortValue());
    }

    public void testShortLargeArrayGetSetNative()
    {
        LargeArray.setMaxSizeOf32bitArray(1);
        ShortLargeArray a = new ShortLargeArray(10);
        long idx = 5;
        short val = -100;
        a.setToNative(idx, val);
        assertEquals(val, (short) a.getFromNative(idx));
    }

    public void testShortLargeArrayGetData()
    {
        short[] data = new short[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        long startPos = 2;
        long endPos = 7;
        long step = 2;
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        ShortLargeArray a = new ShortLargeArray(data);
        short[] res = a.getShortData(null, startPos, endPos, step);
        int idx = 0;
        for (long i = startPos; i < endPos; i += step) {
            assertEquals(data[(int) i], res[idx++]);
        }
        LargeArray.setMaxSizeOf32bitArray(data.length - 1);
        a = new ShortLargeArray(data);
        res = a.getShortData(null, startPos, endPos, step);
        idx = 0;
        for (long i = startPos; i < endPos; i += step) {
            assertEquals(data[(int) i], res[idx++]);
        }
    }

    public void testShortArraycopy()
    {
        short[] data = new short[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int startPos = 2;
        int length = 8;
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        ShortLargeArray a = new ShortLargeArray(data);
        ShortLargeArray b = new ShortLargeArray(2 * data.length);
        Utilities.arraycopy(a, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getShort(i));
        }
        b = new ShortLargeArray(2 * data.length);
        Utilities.arraycopy(data, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getShort(i));
        }
        LargeArray.setMaxSizeOf32bitArray(data.length - 1);
        a = new ShortLargeArray(data);
        b = new ShortLargeArray(2 * data.length);
        Utilities.arraycopy(a, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getShort(i));
        }
        b = new ShortLargeArray(2 * data.length);
        Utilities.arraycopy(data, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getShort(i));
        }
    }

    public void testShortConvert()
    {
        short[] data = new short[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        ShortLargeArray a = new ShortLargeArray(data);
        IntLargeArray b = (IntLargeArray) Utilities.convert(a, LargeArrayType.INT);
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i], b.getInt(i));
        }
    }

    public void testIntLargeArrayGetSet()
    {
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        IntLargeArray a = new IntLargeArray(10);
        long idx = 5;
        int val = -100;
        a.setInt(idx, val);
        assertEquals(val, a.getInt(idx));
        idx = 6;
        a.set(idx, val);
        assertEquals(val, (a.get(idx)).intValue());
        LargeArray.setMaxSizeOf32bitArray(1);
        a = new IntLargeArray(10);
        a.setInt(idx, val);
        assertEquals(val, a.getInt(idx));
        idx = 6;
        a.set(idx, val);
        assertEquals(val, (a.get(idx)).intValue());
    }

    public void testIntLargeArrayGetSetNative()
    {
        LargeArray.setMaxSizeOf32bitArray(1);
        IntLargeArray a = new IntLargeArray(10);
        long idx = 5;
        int val = -100;
        a.setToNative(idx, val);
        assertEquals(val, (int) a.getFromNative(idx));
    }

    public void testIntLargeArrayConstant()
    {
        IntLargeArray a = new IntLargeArray(1l << 33, 2);
        assertEquals(2, a.getInt(0));
        assertEquals(2, a.getInt(a.length() - 1));
        Throwable e = null;
        try {
            a.setInt(0, 3);
        } catch (IllegalAccessError ex) {
            e = ex;
        }
        assertTrue(e instanceof IllegalAccessError);
        assertNull(a.getData());
    }

    public void testIntLargeArrayGetData()
    {
        int[] data = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        long startPos = 2;
        long endPos = 7;
        long step = 2;
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        IntLargeArray a = new IntLargeArray(data);
        int[] res = a.getIntData(null, startPos, endPos, step);
        int idx = 0;
        for (long i = startPos; i < endPos; i += step) {
            assertEquals(data[(int) i], res[idx++]);
        }
        LargeArray.setMaxSizeOf32bitArray(data.length - 1);
        a = new IntLargeArray(data);
        res = a.getIntData(null, startPos, endPos, step);
        idx = 0;
        for (long i = startPos; i < endPos; i += step) {
            assertEquals(data[(int) i], res[idx++]);
        }
    }

    public void testIntArraycopy()
    {
        int[] data = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int startPos = 2;
        int length = 8;
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        IntLargeArray a = new IntLargeArray(data);
        IntLargeArray b = new IntLargeArray(2 * data.length);
        Utilities.arraycopy(a, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getInt(i));
        }
        b = new IntLargeArray(2 * data.length);
        Utilities.arraycopy(data, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getInt(i));
        }
        LargeArray.setMaxSizeOf32bitArray(data.length - 1);
        a = new IntLargeArray(data);
        b = new IntLargeArray(2 * data.length);
        Utilities.arraycopy(a, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getInt(i));
        }
        b = new IntLargeArray(2 * data.length);
        Utilities.arraycopy(data, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getInt(i));
        }
    }

    public void testIntConvert()
    {
        int[] data = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        IntLargeArray a = new IntLargeArray(data);
        LongLargeArray b = (LongLargeArray) Utilities.convert(a, LargeArrayType.LONG);
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i], b.getLong(i));
        }
    }

    public void testLongLargeArrayConstant()
    {
        LongLargeArray a = new LongLargeArray(1l << 33, 2);
        assertEquals(2, a.getLong(0));
        assertEquals(2, a.getLong(a.length() - 1));
        Throwable e = null;
        try {
            a.setLong(0, 3);
        } catch (IllegalAccessError ex) {
            e = ex;
        }
        assertTrue(e instanceof IllegalAccessError);
        assertNull(a.getData());
    }

    public void testLongLargeArrayGetSet()
    {
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        LongLargeArray a = new LongLargeArray(10);
        long idx = 5;
        int val = -100;
        a.setLong(idx, val);
        assertEquals(val, a.getLong(idx));
        idx = 6;
        a.set(idx, val);
        assertEquals(val, (a.get(idx)).longValue());
        LargeArray.setMaxSizeOf32bitArray(1);
        a = new LongLargeArray(10);
        a.setLong(idx, val);
        assertEquals(val, a.getLong(idx));
        idx = 6;
        a.set(idx, val);
        assertEquals(val, (a.get(idx)).longValue());
    }

    public void testLongLargeArrayGetSetNative()
    {
        LargeArray.setMaxSizeOf32bitArray(1);
        LongLargeArray a = new LongLargeArray(10);
        long idx = 5;
        long val = -100;
        a.setToNative(idx, val);
        assertEquals(val, (long) a.getFromNative(idx));
    }

    public void testLongLargeArrayGetData()
    {
        long[] data = new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        long startPos = 2;
        long endPos = 7;
        long step = 2;
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        LongLargeArray a = new LongLargeArray(data);
        long[] res = a.getLongData(null, startPos, endPos, step);
        int idx = 0;
        for (long i = startPos; i < endPos; i += step) {
            assertEquals(data[(int) i], res[idx++]);
        }
        LargeArray.setMaxSizeOf32bitArray(data.length - 1);
        a = new LongLargeArray(data);
        res = a.getLongData(null, startPos, endPos, step);
        idx = 0;
        for (long i = startPos; i < endPos; i += step) {
            assertEquals(data[(int) i], res[idx++]);
        }
    }

    public void testLongArraycopy()
    {
        long[] data = new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int startPos = 2;
        int length = 8;
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        LongLargeArray a = new LongLargeArray(data);
        LongLargeArray b = new LongLargeArray(2 * data.length);
        Utilities.arraycopy(a, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getLong(i));
        }
        b = new LongLargeArray(2 * data.length);
        Utilities.arraycopy(data, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getLong(i));
        }
        LargeArray.setMaxSizeOf32bitArray(data.length - 1);
        a = new LongLargeArray(data);
        b = new LongLargeArray(2 * data.length);
        Utilities.arraycopy(a, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getLong(i));
        }
        b = new LongLargeArray(2 * data.length);
        Utilities.arraycopy(data, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getLong(i));
        }
    }

    public void testLongConvert()
    {
        long[] data = new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        LongLargeArray a = new LongLargeArray(data);
        FloatLargeArray b = (FloatLargeArray) Utilities.convert(a, LargeArrayType.FLOAT);
        for (int i = 0; i < data.length; i++) {
            assertEquals((float) data[i], b.getFloat(i));
        }
    }

    public void testFloatLargeArrayConstant()
    {
        FloatLargeArray a = new FloatLargeArray(1l << 33, 2.5f);
        assertEquals(2.5f, a.getFloat(0));
        assertEquals(2.5f, a.getFloat(a.length() - 1));
        Throwable e = null;
        try {
            a.setFloat(0, 3.5f);
        } catch (IllegalAccessError ex) {
            e = ex;
        }
        assertTrue(e instanceof IllegalAccessError);
        assertNull(a.getData());
    }

    public void testFloatLargeArrayGetSet()
    {
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        FloatLargeArray a = new FloatLargeArray(10);
        long idx = 5;
        float val = 3.4f;
        a.setFloat(idx, val);
        assertEquals(val, a.getFloat(idx), 0.0);
        idx = 6;
        a.set(idx, val);
        assertEquals(val, a.get(idx));
        LargeArray.setMaxSizeOf32bitArray(1);
        a = new FloatLargeArray(10);
        a.setFloat(idx, val);
        assertEquals(val, a.getFloat(idx), 0.0);
        idx = 6;
        a.set(idx, val);
        assertEquals(val, a.get(idx));
    }

    public void testFloatLargeArrayGetSetNative()
    {
        LargeArray.setMaxSizeOf32bitArray(1);
        FloatLargeArray a = new FloatLargeArray(10);
        long idx = 5;
        float val = 3.4f;
        a.setToNative(idx, val);
        assertEquals(val, a.getFromNative(idx), 0.0);
    }

    public void testFloatLargeArrayGetData()
    {
        float[] data = new float[]{1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f, 7.7f, 8.8f, 9.9f, 10.10f};
        long startPos = 2;
        long endPos = 7;
        long step = 2;
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        FloatLargeArray a = new FloatLargeArray(data);
        float[] res = a.getFloatData(null, startPos, endPos, step);
        int idx = 0;
        for (long i = startPos; i < endPos; i += step) {
            assertEquals(data[(int) i], res[idx++]);
        }
        LargeArray.setMaxSizeOf32bitArray(data.length - 1);
        a = new FloatLargeArray(data);
        res = a.getFloatData(null, startPos, endPos, step);
        idx = 0;
        for (long i = startPos; i < endPos; i += step) {
            assertEquals(data[(int) i], res[idx++]);
        }
    }

    public void testFloatArraycopy()
    {
        float[] data = new float[]{1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f, 7.7f, 8.8f, 9.9f, 10.10f};
        int startPos = 2;
        int length = 8;
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        FloatLargeArray a = new FloatLargeArray(data);
        FloatLargeArray b = new FloatLargeArray(2 * data.length);
        Utilities.arraycopy(a, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getFloat(i));
        }
        b = new FloatLargeArray(2 * data.length);
        Utilities.arraycopy(data, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getFloat(i));
        }
        LargeArray.setMaxSizeOf32bitArray(data.length - 1);
        a = new FloatLargeArray(data);
        b = new FloatLargeArray(2 * data.length);
        Utilities.arraycopy(a, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getFloat(i));
        }
        b = new FloatLargeArray(2 * data.length);
        Utilities.arraycopy(data, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getFloat(i));
        }
    }

    public void testFloatConvert()
    {
        float[] data = new float[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        FloatLargeArray a = new FloatLargeArray(data);
        DoubleLargeArray b = (DoubleLargeArray) Utilities.convert(a, LargeArrayType.DOUBLE);
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i], b.getDouble(i), 0.0);
        }
    }

    public void testDoubleLargeArrayConstant()
    {
        DoubleLargeArray a = new DoubleLargeArray(1l << 33, 2.5);
        assertEquals(2.5, a.getDouble(0));
        assertEquals(2.5, a.getDouble(a.length() - 1));
        Throwable e = null;
        try {
            a.setDouble(0, 3.5);
        } catch (IllegalAccessError ex) {
            e = ex;
        }
        assertTrue(e instanceof IllegalAccessError);
        assertNull(a.getData());
    }

    public void testDoubleLargeArrayGetSet()
    {
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        DoubleLargeArray a = new DoubleLargeArray(10);
        long idx = 5;
        double val = 3.4;
        a.setDouble(idx, val);
        assertEquals(val, a.getDouble(idx), 0.0);
        idx = 6;
        a.set(idx, val);
        assertEquals(val, a.get(idx));
        LargeArray.setMaxSizeOf32bitArray(1);
        a = new DoubleLargeArray(10);
        a.setDouble(idx, val);
        assertEquals(val, a.getDouble(idx), 0.0);
        idx = 6;
        a.set(idx, val);
        assertEquals(val, a.get(idx));
    }

    public void testDoubleLargeArrayGetSetNative()
    {
        LargeArray.setMaxSizeOf32bitArray(1);
        DoubleLargeArray a = new DoubleLargeArray(10);
        long idx = 5;
        double val = 3.4;
        a.setToNative(idx, val);
        assertEquals(val, a.getFromNative(idx), 0.0);
    }

    public void testDoubleLargeArrayGetData()
    {
        double[] data = new double[]{1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7, 8.8, 9.9, 10.10};
        long startPos = 2;
        long endPos = 7;
        long step = 2;
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        DoubleLargeArray a = new DoubleLargeArray(data);
        double[] res = a.getDoubleData(null, startPos, endPos, step);
        int idx = 0;
        for (long i = startPos; i < endPos; i += step) {
            assertEquals(data[(int) i], res[idx++]);
        }
        LargeArray.setMaxSizeOf32bitArray(data.length - 1);
        a = new DoubleLargeArray(data);
        res = a.getDoubleData(null, startPos, endPos, step);
        idx = 0;
        for (long i = startPos; i < endPos; i += step) {
            assertEquals(data[(int) i], res[idx++]);
        }
    }

    public void testDoubleArraycopy()
    {
        double[] data = new double[]{1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7, 8.8, 9.9, 10.10};
        int startPos = 2;
        int length = 8;
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        DoubleLargeArray a = new DoubleLargeArray(data);
        DoubleLargeArray b = new DoubleLargeArray(2 * data.length);
        Utilities.arraycopy(a, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getDouble(i));
        }
        b = new DoubleLargeArray(2 * data.length);
        Utilities.arraycopy(data, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getDouble(i));
        }
        LargeArray.setMaxSizeOf32bitArray(data.length - 1);
        a = new DoubleLargeArray(data);
        b = new DoubleLargeArray(2 * data.length);
        Utilities.arraycopy(a, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getDouble(i));
        }
        b = new DoubleLargeArray(2 * data.length);
        Utilities.arraycopy(data, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.getDouble(i));
        }
    }

    public void testDoubleConvert()
    {
        double[] data = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        DoubleLargeArray a = new DoubleLargeArray(data);
        FloatLargeArray b = (FloatLargeArray) Utilities.convert(a, LargeArrayType.FLOAT);
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i], b.getFloat(i), 0.0);
        }
    }
    
     public void testStringLargeArrayConstant()
    {
        StringLargeArray a = new StringLargeArray(1l << 33, "test0123ąęćńżź");
        assertEquals("test0123ąęćńżź", a.get(0));
        assertEquals("test0123ąęćńżź", a.get(a.length() - 1));
        Throwable e = null;
        try {
            a.set(0, "test0123ąęćńżź");
        } catch (IllegalAccessError ex) {
            e = ex;
        }
        assertTrue(e instanceof IllegalAccessError);
        assertNull(a.getData());
    }


    public void testStringLargeArrayGetSet()
    {
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        StringLargeArray a = new StringLargeArray(10, 14);
        long idx = 5;
        String val1 = "test0123ąęćńżź";
        String val2 = "test";
        a.set(idx, val1);
        assertEquals(val1, a.get(idx));
        a.set(idx, val2);
        assertEquals(val2, a.get(idx));
        LargeArray.setMaxSizeOf32bitArray(1);
        a = new StringLargeArray(10, 14);
        a.set(idx, val1);
        assertEquals(val1, a.get(idx));
        a.set(idx, val2);
        assertEquals(val2, a.get(idx));
    }

    public void testStringLargeArrayGetSetNative()
    {
        LargeArray.setMaxSizeOf32bitArray(1);
        StringLargeArray a = new StringLargeArray(10, 14);
        long idx = 5;
        String val1 = "test0123ąęćńżź";
        String val2 = "test";
        a.setToNative(idx, val1);
        assertEquals(val1, a.getFromNative(idx));
        a.setToNative(idx, val2);
        assertEquals(val2, a.getFromNative(idx));
    }

    public void testStringArraycopy()
    {
        String[] data = new String[]{"a", "ab", "abc", "ąęć", "1234", "test string", "ANOTHER TEST STRING", "", "\n", "\r"};
        int startPos = 2;
        int length = 8;
        LargeArray.setMaxSizeOf32bitArray(1073741824);
        StringLargeArray a = new StringLargeArray(data);
        StringLargeArray b = new StringLargeArray(2 * data.length, 20);
        Utilities.arraycopy(a, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.get(i));
        }
        b = new StringLargeArray(2 * data.length, 20);
        Utilities.arraycopy(data, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.get(i));
        }
        LargeArray.setMaxSizeOf32bitArray(data.length - 1);
        a = new StringLargeArray(data);
        b = new StringLargeArray(2 * data.length, 20);
        Utilities.arraycopy(a, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.get(i));
        }
        b = new StringLargeArray(2 * data.length, 20);
        Utilities.arraycopy(data, startPos, b, 0, length);
        for (int i = 0; i < length; i++) {
            assertEquals(data[startPos + i], b.get(i));
        }
    }

    public void testLargeArrayNativePointerInterOp()
    {
        BitLargeArray a = new BitLargeArray(1l << 33, (byte) 1);
        assertTrue(a.getBoolean(0));
        assertTrue(a.getBoolean(a.length() - 1));
        Throwable e = null;
        try {
            a.setBoolean(0, false);
        }
        catch (IllegalAccessError ex) {
            e = ex;
        }
        assertTrue(e instanceof IllegalAccessError);
        assertNull(a.getData());
    }

}
