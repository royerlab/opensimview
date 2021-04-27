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

import java.io.UnsupportedEncodingException;

import sun.misc.Cleaner;

/**
 *
 * An array of strings that can store up to 2<SUP>63</SUP> elements.
 *
 * @author Piotr Wendykier (p.wendykier@icm.edu.pl)
 */
public class StringLargeArray extends LargeArray
{

    private static final long serialVersionUID = -4096759496772248522L;
    private String[] data;
    private ShortLargeArray stringLengths;
    private int maxStringLength;
    private long size;
    private byte[] byteArray;
    private static final String CHARSET = "UTF-8";
    private static final int CHARSET_SIZE = 4; //UTF-8 uses between 1 and 4 bytes to encode a single character 

    /**
     * Creates new instance of this class by wrapping a native pointer.
     * Providing an invalid pointer, parent or length will result in
     * unpredictable behavior and likely JVM crash. The assumption is that the
     * pointer is valid as long as the parent is not garbage collected.
     * 
     * @param parent class instance responsible for handling the pointer's life
     *            cycle, the created instance of LargeArray will prevent the GC
     *            from reclaiming the parent.
     * @param nativePointer native pointer to wrap.
     * @param length array length
     */
    public StringLargeArray(final Object parent,
                            final long nativePointer,
                            final long length)
    {
        super(parent, nativePointer, LargeArrayType.STRING, length);
    }

    /**
     * Creates new instance of this class. The maximal string length is set to
     * 100.
     *
     * @param length number of elements
     */
    public StringLargeArray(long length)
    {
        this(length, 100);
    }

    /**
     * Creates new instance of this class.
     *
     * @param length          number of elements
     * @param maxStringLength maximal length of the string, it is ignored when number of elements is smaller than LARGEST_32BIT_INDEX
     */
    public StringLargeArray(long length, int maxStringLength)
    {
        this(length, maxStringLength, true);
    }

    /**
     * Creates new instance of this class.
     *
     * @param length           number of elements
     * @param maxStringLength  maximal length of the string, it is ignored when number of elements is smaller than LARGEST_32BIT_INDEX
     * @param zeroNativeMemory if true, then the native memory is zeroed.
     */
    public StringLargeArray(long length, int maxStringLength, boolean zeroNativeMemory)
    {
        this.type = LargeArrayType.STRING;
        this.sizeof = 1;
        if (length <= 0) {
            throw new IllegalArgumentException(length + " is not a positive long value.");
        }
        if (maxStringLength <= 0) {
            throw new IllegalArgumentException(maxStringLength + " is not a positive int value.");
        }
        this.length = length;
        this.size = length * maxStringLength * CHARSET_SIZE;
        this.maxStringLength = maxStringLength;
        if (length > LARGEST_32BIT_INDEX) {
            System.gc();
            this.ptr = Utilities.UNSAFE.allocateMemory(this.size * this.sizeof);
            if (zeroNativeMemory) {
                zeroNativeMemory(this.size);
            }
            Cleaner.create(this, new Deallocator(this.ptr, this.size, this.sizeof));
            MemoryCounter.increaseCounter(this.size * this.sizeof);
            stringLengths = new ShortLargeArray(length);
            byteArray = new byte[maxStringLength * CHARSET_SIZE];
        } else {
            data = new String[(int) length];
        }
    }

    public StringLargeArray(long length, String constantValue)
    {
        this.type = LargeArrayType.DOUBLE;
        this.sizeof = 1;
        if (length <= 0) {
            throw new IllegalArgumentException(length + " is not a positive long value");
        }
        this.length = length;
        this.isConstant = true;
        this.data = new String[]{constantValue};
    }

    /**
     * Creates new instance of this class.
     *
     * @param data data array, this reference is used internally.
     */
    public StringLargeArray(String[] data)
    {
        this.type = LargeArrayType.STRING;
        this.sizeof = 1;
        this.length = data.length;
        this.data = data;
    }

    /**
     * Returns a deep copy of this instance. (The elements themselves are copied.)
     *
     * @return a clone of this instance
     */
    @Override
    public StringLargeArray clone()
    {
        if (isConstant()) {
            return new StringLargeArray(length, get(0));
        } else {
            StringLargeArray v = new StringLargeArray(size, maxStringLength, false);
            Utilities.arraycopy(this, 0, v, 0, size);
            return v;
        }
    }

    @Override
    public String get(long i)
    {
        if (ptr != 0) {
            short strLen = stringLengths.getShort(i);
            long offset = sizeof * i * maxStringLength * CHARSET_SIZE;
            for (int j = 0; j < strLen; j++) {
                byteArray[j] = Utilities.UNSAFE.getByte(ptr + offset + sizeof * j);
            }
            try {
                return new String(byteArray, 0, strLen, CHARSET);
            } catch (UnsupportedEncodingException ex) {
                return null;
            }
        } else {
            if (isConstant()) {
                return data[0];
            } else {
                return data[(int) i];
            }
        }
    }

    @Override
    public String getFromNative(long i)
    {
        short strLen = stringLengths.getShort(i);
        long offset = sizeof * i * maxStringLength * CHARSET_SIZE;
        for (int j = 0; j < strLen; j++) {
            byteArray[j] = Utilities.UNSAFE.getByte(ptr + offset + sizeof * j);
        }
        try {
            return new String(byteArray, 0, strLen, CHARSET);
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }

    @Override
    public boolean getBoolean(long i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte getByte(long i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public short getShort(long i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getInt(long i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getLong(long i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getFloat(long i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getDouble(long i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getData()
    {
        if (ptr != 0) {
            return null;
        } else {
            if (isConstant()) {
                if (length > getMaxSizeOf32bitArray()) return null;
                String[] out = new String[(int) length];
                for (int i = 0; i < length; i++) {
                    out[i] = data[0];
                }
                return out;
            } else {
                return data;
            }
        }
    }

    @Override
    public boolean[] getBooleanData()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean[] getBooleanData(boolean[] a, long startPos, long endPos, long step)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getByteData()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getByteData(byte[] a, long startPos, long endPos, long step)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public short[] getShortData()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public short[] getShortData(short[] a, long startPos, long endPos, long step)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int[] getIntData()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int[] getIntData(int[] a, long startPos, long endPos, long step)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long[] getLongData()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long[] getLongData(long[] a, long startPos, long endPos, long step)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float[] getFloatData()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float[] getFloatData(float[] a, long startPos, long endPos, long step)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double[] getDoubleData()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double[] getDoubleData(double[] a, long startPos, long endPos, long step)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setToNative(long i, Object value)
    {
        String s = (String) value;
        if (s.length() > maxStringLength) {
            throw new IllegalArgumentException("String  " + s + " is too long.");
        }
        byte[] tmp;
        try {
            tmp = s.getBytes(CHARSET);
        } catch (UnsupportedEncodingException ex) {
            return;
        }
        int strLen = tmp.length;
        if (strLen > Short.MAX_VALUE) {
            throw new IllegalArgumentException("String  " + s + " is too long.");
        }
        stringLengths.setShort(i, (short) strLen);
        long offset = sizeof * i * maxStringLength * CHARSET_SIZE;
        for (int j = 0; j < strLen; j++) {
            Utilities.UNSAFE.putByte(ptr + offset + sizeof * j, tmp[j]);
        }
    }

    @Override
    public void set(long i, Object o)
    {
        if (!(o instanceof String)) {
            throw new IllegalArgumentException(o + " is not a string.");
        }
        String s = (String) o;
        if (ptr != 0) {
            if (s.length() > maxStringLength) {
                throw new IllegalArgumentException("String  " + s + " is too long.");
            }
            byte[] tmp;
            try {
                tmp = s.getBytes(CHARSET);
            } catch (UnsupportedEncodingException ex) {
                return;
            }
            int strLen = tmp.length;
            if (strLen > Short.MAX_VALUE) {
                throw new IllegalArgumentException("String  " + s + " is too long.");
            }
            stringLengths.setShort(i, (short) strLen);
            long offset = sizeof * i * maxStringLength * CHARSET_SIZE;
            for (int j = 0; j < strLen; j++) {
                Utilities.UNSAFE.putByte(ptr + offset + sizeof * j, tmp[j]);
            }
        } else {
            if (isConstant()) {
                throw new IllegalAccessError("Constant arrays cannot be modified.");
            }
            data[(int) i] = s;
        }
    }

    @Override
    public void set_safe(long i, Object o)
    {
        if (i < 0 || i >= length) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(i));
        }
        if (!(o instanceof String)) {
            throw new IllegalArgumentException(o + " is not a string.");
        }
        String s = (String) o;
        if (s.length() > maxStringLength) {
            throw new IllegalArgumentException("String  " + s + " is too long.");
        }
        if (ptr != 0) {
            byte[] tmp;
            try {
                tmp = s.getBytes(CHARSET);
            } catch (UnsupportedEncodingException ex) {
                return;
            }
            int strLen = tmp.length;
            if (strLen > Short.MAX_VALUE) {
                throw new IllegalArgumentException("String  " + s + " is too long.");
            }
            stringLengths.setShort(i, (short) strLen);
            long offset = sizeof * i * maxStringLength * CHARSET_SIZE;
            for (int j = 0; j < strLen; j++) {
                Utilities.UNSAFE.putByte(ptr + offset + sizeof * j, tmp[j]);
            }
        } else {
            if (isConstant()) {
                throw new IllegalAccessError("Constant arrays cannot be modified.");
            }
            data[(int) i] = s;
        }
    }

    @Override
    public void setBoolean(long i, boolean value)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setByte(long i, byte value)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setShort(long i, short value)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setInt(long i, int value)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLong(long i, long value)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setFloat(long i, float value)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setDouble(long i, double value)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
