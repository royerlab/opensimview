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

import sun.misc.Cleaner;

/**
 *
 * An array of bits (0 and 1) that can store up to 2<SUP>63</SUP> elements.
 *
 * @author Piotr Wendykier (p.wendykier@icm.edu.pl)
 */
public class BitLargeArray extends LargeArray
{

    private static final long serialVersionUID = -3499412355469845345L;
    private byte[] data;
    private long size;
    private final Object LOCK = new Object();


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
    public BitLargeArray(final Object parent,
                         final long nativePointer,
                      final long length)
    {
        super(parent, nativePointer, LargeArrayType.BIT, length);
    }

    /**
     * Creates new instance of this class.
     *
     * @param length number of elements
     */
    public BitLargeArray(long length)
    {
        this(length, true);
    }

    /**
     * Creates new instance of this class.
     *
     * @param length number of elements
     * @param zeroNativeMemory if true, then the native memory is zeroed.
     */
    public BitLargeArray(long length, boolean zeroNativeMemory)
    {
        this.type = LargeArrayType.BIT;
        this.sizeof = 1;
        if (length <= 0) {
            throw new IllegalArgumentException(length + " is not a positive long value");
        }
        this.length = length;
        long tmp = (length - 1l) / 8l;
        this.size = tmp + 1;
        if (length > LARGEST_32BIT_INDEX) {
            System.gc();
            this.ptr = Utilities.UNSAFE.allocateMemory(this.size * this.sizeof);
            if (zeroNativeMemory) {
                zeroNativeMemory(this.size);
            }
            Cleaner.create(this,
                           new LargeArray.Deallocator(this.ptr,
                                                      this.size,
                                                      this.sizeof));
            MemoryCounter.increaseCounter(this.size * this.sizeof);
        }
        else {
            data = new byte[(int) this.size];
        }
    }

    public BitLargeArray(long length, byte constantValue)
    {
        this.type = LargeArrayType.BIT;
        this.sizeof = 1;
        if (length <= 0) {
            throw new IllegalArgumentException(length + " is not a positive long value");
        }
        if (constantValue < 0 || constantValue > 1) {
            throw new IllegalArgumentException("constantValue has to be 0 or 1");
        }
        this.length = length;
        this.isConstant = true;
        this.data = new byte[]
        { constantValue };
    }

    /**
     * Creates new instance of this class.
     *
     * @param data data array, this reference is not used internally.
     */
    public BitLargeArray(boolean[] data)
    {
        this(data.length);
        if (ptr != 0) {
            for (int i = 0; i < data.length; i++) {
                int v = 0;
                if (data[i]) {
                    v = 1;
                }
                long index = i / 8l;
                long ii = i % 8l;
                byte oldV = Utilities.UNSAFE.getByte(ptr + index);
                oldV = (byte) (((0xFF7F >> ii) & oldV) & 0x00FF);
                byte newV = (byte) ((v << (8l - (ii + 1l))) | oldV);
                Utilities.UNSAFE.putByte(ptr + index, newV);
            }
        }
        else {
            for (int i = 0; i < data.length; i++) {
                int v = 0;
                if (data[i]) {
                    v = 1;
                }
                int index = i / 8;
                int ii = i % 8;
                byte oldV = this.data[index];
                oldV = (byte) (((0xFF7F >> ii) & oldV) & 0x00FF);
                byte newV = (byte) ((v << (8 - (ii + 1))) | oldV);
                this.data[index] = newV;
            }
        }
    }

    /**
     * Returns a deep copy of this instance. (The elements themselves are
     * copied.)
     *
     * @return a clone of this instance
     */
    @Override
    public BitLargeArray clone()
    {
        if (isConstant()) {
            return new BitLargeArray(length, getByte(0));
        }
        else {
            BitLargeArray v = new BitLargeArray(length, false);
            Utilities.arraycopy(this, 0, v, 0, length);
            return v;
        }
    }

    @Override
    public Boolean get(long i)
    {
        return getBoolean(i);
    }

    @Override
    public Boolean getFromNative(long i)
    {
        long index = i / 8l;
        Utilities.UNSAFE.monitorEnter(LOCK);
        byte v = Utilities.UNSAFE.getByte(ptr + index);
        Utilities.UNSAFE.monitorExit(LOCK);
        long ii = i % 8l;
        int value = v >> (8l - (ii + 1l)) & 0x0001;
        return value == 1;
    }

    @Override
    public boolean getBoolean(long i)
    {
        if (ptr != 0) {
            long index = i / 8l;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte v = Utilities.UNSAFE.getByte(ptr + index);
            Utilities.UNSAFE.monitorExit(LOCK);
            long ii = i % 8l;
            int value = v >> (8l - (ii + 1l)) & 0x0001;
            return value == 1;
        }
        else {
            if (isConstant()) {
                return data[0] == 1;
            }
            else {
                int index = (int) i / 8;
                Utilities.UNSAFE.monitorEnter(LOCK);
                byte v = data[index];
                Utilities.UNSAFE.monitorExit(LOCK);
                int ii = (int) i % 8;
                int value = v >> (8 - (ii + 1)) & 0x0001;
                return value == 1;
            }
        }
    }

    @Override
    public byte getByte(long i)
    {
        if (ptr != 0) {
            long index = i / 8l;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte v = Utilities.UNSAFE.getByte(ptr + index);
            Utilities.UNSAFE.monitorExit(LOCK);
            long ii = i % 8l;
            int value = v >> (8l - (ii + 1l)) & 0x0001;
            return (byte) value;
        }
        else {
            if (isConstant()) {
                return data[0];
            }
            else {
                int index = (int) i / 8;
                Utilities.UNSAFE.monitorEnter(LOCK);
                byte v = data[index];
                Utilities.UNSAFE.monitorExit(LOCK);
                int ii = (int) i % 8;
                int value = v >> (8 - (ii + 1)) & 0x0001;
                return (byte) value;
            }
        }
    }

    @Override
    public short getShort(long i)
    {
        if (ptr != 0) {
            long index = i / 8l;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte v = Utilities.UNSAFE.getByte(ptr + index);
            Utilities.UNSAFE.monitorEnter(LOCK);
            long ii = i % 8l;
            int value = v >> (8l - (ii + 1l)) & 0x0001;
            return (short) value;
        }
        else {
            if (isConstant()) {
                return data[0];
            }
            else {
                int index = (int) i / 8;
                Utilities.UNSAFE.monitorEnter(LOCK);
                byte v = data[index];
                Utilities.UNSAFE.monitorExit(LOCK);
                int ii = (int) i % 8;
                int value = v >> (8 - (ii + 1)) & 0x0001;
                return (short) value;
            }
        }
    }

    @Override
    public int getInt(long i)
    {
        if (ptr != 0) {
            long index = i / 8l;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte v = Utilities.UNSAFE.getByte(ptr + index);
            Utilities.UNSAFE.monitorExit(LOCK);
            long ii = i % 8l;
            int value = v >> (8l - (ii + 1l)) & 0x0001;
            return value;
        }
        else {
            if (isConstant()) {
                return data[0];
            }
            else {
                int index = (int) i / 8;
                Utilities.UNSAFE.monitorEnter(LOCK);
                byte v = data[index];
                Utilities.UNSAFE.monitorExit(LOCK);
                int ii = (int) i % 8;
                int value = v >> (8 - (ii + 1)) & 0x0001;
                return value;
            }
        }
    }

    @Override
    public long getLong(long i)
    {
        if (ptr != 0) {
            long index = i / 8l;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte v = Utilities.UNSAFE.getByte(ptr + index);
            Utilities.UNSAFE.monitorExit(LOCK);
            long ii = i % 8l;
            int value = v >> (8l - (ii + 1l)) & 0x0001;
            return value;
        }
        else {
            if (isConstant()) {
                return data[0];
            }
            else {
                int index = (int) i / 8;
                Utilities.UNSAFE.monitorEnter(LOCK);
                byte v = data[index];
                Utilities.UNSAFE.monitorExit(LOCK);
                int ii = (int) i % 8;
                int value = v >> (8 - (ii + 1)) & 0x0001;
                return value;
            }
        }
    }

    @Override
    public float getFloat(long i)
    {
        if (ptr != 0) {
            long index = i / 8l;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte v = Utilities.UNSAFE.getByte(ptr + index);
            Utilities.UNSAFE.monitorExit(LOCK);
            long ii = i % 8l;
            int value = v >> (8l - (ii + 1l)) & 0x0001;
            return value;
        }
        else {
            if (isConstant()) {
                return data[0];
            }
            else {
                int index = (int) i / 8;
                Utilities.UNSAFE.monitorEnter(LOCK);
                byte v = data[index];
                Utilities.UNSAFE.monitorExit(LOCK);
                int ii = (int) i % 8;
                int value = v >> (8 - (ii + 1)) & 0x0001;
                return value;
            }
        }
    }

    @Override
    public double getDouble(long i)
    {
        if (ptr != 0) {
            long index = i / 8l;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte v = Utilities.UNSAFE.getByte(ptr + index);
            Utilities.UNSAFE.monitorExit(LOCK);
            long ii = i % 8l;
            int value = v >> (8l - (ii + 1l)) & 0x0001;
            return value;
        }
        else {
            if (isConstant()) {
                return data[0];
            }
            else {
                int index = (int) i / 8;
                Utilities.UNSAFE.monitorEnter(LOCK);
                byte v = data[index];
                Utilities.UNSAFE.monitorExit(LOCK);
                int ii = (int) i % 8;
                int value = v >> (8 - (ii + 1)) & 0x0001;
                return value;
            }
        }
    }

    @Override
    public byte[] getData()
    {
        if (ptr != 0) {
            return null;
        }
        else {
            if (isConstant()) {
                if (length > getMaxSizeOf32bitArray())
                    return null;
                byte[] out = new byte[(int) length];
                for (int i = 0; i < length; i++) {
                    out[i] = data[0];
                }
                return out;
            }
            else {
                return data;
            }
        }
    }

    @Override
    public boolean[] getBooleanData()
    {
        if (ptr != 0) {
            return null;
        }
        else {
            if (isConstant()) {
                if (length > getMaxSizeOf32bitArray())
                    return null;
                boolean[] out = new boolean[(int) length];
                boolean elem = data[0] == 1;
                for (int i = 0; i < length; i++) {
                    out[i] = elem;
                }
                return out;
            }
            else {
                boolean[] out = new boolean[(int) length];
                byte v;
                int ii;
                for (int i = 0; i < out.length; i++) {
                    v = data[i / 8];
                    ii = i % 8;
                    int value = v >> (8 - (ii + 1)) & 0x0001;
                    out[i] = value == 1;
                }
                return out;
            }
        }
    }

    @Override
    public boolean[] getBooleanData(boolean[] a,
                                    long startPos,
                                    long endPos,
                                    long step)
    {
        if (startPos < 0 || startPos >= length) {
            throw new ArrayIndexOutOfBoundsException("startPos < 0 || startPos >= length");
        }
        if (endPos < 0 || endPos > length || endPos < startPos) {
            throw new ArrayIndexOutOfBoundsException("endPos < 0 || endPos > length || endPos < startPos");
        }
        if (step < 1) {
            throw new IllegalArgumentException("step < 1");
        }

        long len = (long) Math.ceil((endPos - startPos) / (double) step);
        if (len > getMaxSizeOf32bitArray()) {
            return null;
        }
        else {
            boolean[] out;
            if (a != null && a.length >= len) {
                out = a;
            }
            else {
                out = new boolean[(int) len];
            }
            int idx = 0;
            if (ptr != 0) {
                for (long i = startPos; i < endPos; i += step) {
                    long index = i / 8l;
                    byte v = Utilities.UNSAFE.getByte(ptr + index);
                    long ii = i % 8l;
                    int value = v >> (8l - (ii + 1l)) & 0x0001;
                    out[idx++] = value == 1;
                }
            }
            else {
                if (isConstant()) {
                    boolean elem = data[0] == 1;
                    for (long i = startPos; i < endPos; i += step) {
                        out[idx++] = elem;
                    }
                }
                else {
                    for (long i = startPos; i < endPos; i += step) {
                        int index = (int) i / 8;
                        byte v = data[index];
                        int ii = (int) i % 8;
                        int value = v >> (8 - (ii + 1)) & 0x0001;
                        out[idx++] = value == 1;
                    }
                }
            }
            return out;
        }
    }

    @Override
    public byte[] getByteData()
    {
        if (ptr != 0) {
            return null;
        }
        else {
            if (isConstant()) {
                if (length > getMaxSizeOf32bitArray())
                    return null;
                byte[] out = new byte[(int) length];
                for (int i = 0; i < length; i++) {
                    out[i] = data[0];
                }
                return out;
            }
            else {
                byte[] out = new byte[(int) length];
                byte v;
                int ii;
                for (int i = 0; i < out.length; i++) {
                    v = data[i / 8];
                    ii = i % 8;
                    out[i] = (byte) (v >> (8 - (ii + 1)) & 0x0001);
                }
                return out;
            }
        }
    }

    @Override
    public byte[] getByteData(byte[] a,
                              long startPos,
                              long endPos,
                              long step)
    {
        if (startPos < 0 || startPos >= length) {
            throw new ArrayIndexOutOfBoundsException("startPos < 0 || startPos >= length");
        }
        if (endPos < 0 || endPos > length || endPos < startPos) {
            throw new ArrayIndexOutOfBoundsException("endPos < 0 || endPos > length || endPos < startPos");
        }
        if (step < 1) {
            throw new IllegalArgumentException("step < 1");
        }

        long len = (long) Math.ceil((endPos - startPos) / (double) step);
        if (len > getMaxSizeOf32bitArray()) {
            return null;
        }
        else {
            byte[] out;
            if (a != null && a.length >= len) {
                out = a;
            }
            else {
                out = new byte[(int) len];
            }
            int idx = 0;
            if (ptr != 0) {
                for (long i = startPos; i < endPos; i += step) {
                    long index = i / 8l;
                    byte v = Utilities.UNSAFE.getByte(ptr + index);
                    long ii = i % 8l;
                    out[idx++] = (byte) (v >> (8l - (ii + 1l)) & 0x0001);
                }
            }
            else {
                if (isConstant()) {
                    for (long i = startPos; i < endPos; i += step) {
                        out[idx++] = data[0];
                    }
                }
                else {
                    for (long i = startPos; i < endPos; i += step) {
                        int index = (int) i / 8;
                        byte v = data[index];
                        int ii = (int) i % 8;
                        out[idx++] = (byte) (v >> (8 - (ii + 1)) & 0x0001);
                    }
                }
            }
            return out;
        }
    }

    @Override
    public short[] getShortData()
    {
        if (ptr != 0) {
            return null;
        }
        else {
            if (isConstant()) {
                if (length > getMaxSizeOf32bitArray())
                    return null;
                short[] out = new short[(int) length];
                for (int i = 0; i < length; i++) {
                    out[i] = data[0];
                }
                return out;
            }
            else {
                short[] out = new short[(int) length];
                byte v;
                int ii;
                for (int i = 0; i < out.length; i++) {
                    v = data[i / 8];
                    ii = i % 8;
                    out[i] = (short) (v >> (8 - (ii + 1)) & 0x0001);
                }
                return out;
            }
        }
    }

    @Override
    public short[] getShortData(short[] a,
                                long startPos,
                                long endPos,
                                long step)
    {
        if (startPos < 0 || startPos >= length) {
            throw new ArrayIndexOutOfBoundsException("startPos < 0 || startPos >= length");
        }
        if (endPos < 0 || endPos > length || endPos < startPos) {
            throw new ArrayIndexOutOfBoundsException("endPos < 0 || endPos > length || endPos < startPos");
        }
        if (step < 1) {
            throw new IllegalArgumentException("step < 1");
        }

        long len = (long) Math.ceil((endPos - startPos) / (double) step);
        if (len > getMaxSizeOf32bitArray()) {
            return null;
        }
        else {
            short[] out;
            if (a != null && a.length >= len) {
                out = a;
            }
            else {
                out = new short[(int) len];
            }
            int idx = 0;
            if (ptr != 0) {
                for (long i = startPos; i < endPos; i += step) {
                    long index = i / 8l;
                    byte v = Utilities.UNSAFE.getByte(ptr + index);
                    long ii = i % 8l;
                    out[idx++] = (short) (v >> (8l - (ii + 1l)) & 0x0001);
                }
            }
            else {
                if (isConstant()) {
                    for (long i = startPos; i < endPos; i += step) {
                        out[idx++] = data[0];
                    }
                }
                else {
                    for (long i = startPos; i < endPos; i += step) {
                        int index = (int) i / 8;
                        byte v = data[index];
                        int ii = (int) i % 8;
                        out[idx++] = (short) (v >> (8 - (ii + 1)) & 0x0001);
                    }
                }
            }
            return out;
        }
    }

    @Override
    public int[] getIntData()
    {
        if (ptr != 0) {
            return null;
        }
        else {
            if (isConstant()) {
                if (length > getMaxSizeOf32bitArray())
                    return null;
                int[] out = new int[(int) length];
                for (int i = 0; i < length; i++) {
                    out[i] = data[0];
                }
                return out;
            }
            else {
                int[] out = new int[(int) length];
                byte v;
                int ii;
                for (int i = 0; i < out.length; i++) {
                    v = data[i / 8];
                    ii = i % 8;
                    out[i] = v >> (8 - (ii + 1)) & 0x0001;
                }
                return out;
            }
        }
    }

    @Override
    public int[] getIntData(int[] a,
                            long startPos,
                            long endPos,
                            long step)
    {
        if (startPos < 0 || startPos >= length) {
            throw new ArrayIndexOutOfBoundsException("startPos < 0 || startPos >= length");
        }
        if (endPos < 0 || endPos > length || endPos < startPos) {
            throw new ArrayIndexOutOfBoundsException("endPos < 0 || endPos > length || endPos < startPos");
        }
        if (step < 1) {
            throw new IllegalArgumentException("step < 1");
        }

        long len = (long) Math.ceil((endPos - startPos) / (double) step);
        if (len > getMaxSizeOf32bitArray()) {
            return null;
        }
        else {
            int[] out;
            if (a != null && a.length >= len) {
                out = a;
            }
            else {
                out = new int[(int) len];
            }
            int idx = 0;
            if (ptr != 0) {
                for (long i = startPos; i < endPos; i += step) {
                    long index = i / 8l;
                    byte v = Utilities.UNSAFE.getByte(ptr + index);
                    long ii = i % 8l;
                    out[idx++] = v >> (8l - (ii + 1l)) & 0x0001;
                }
            }
            else {
                if (isConstant()) {
                    for (long i = startPos; i < endPos; i += step) {
                        out[idx++] = data[0];
                    }
                }
                else {
                    for (long i = startPos; i < endPos; i += step) {
                        int index = (int) i / 8;
                        byte v = data[index];
                        int ii = (int) i % 8;
                        out[idx++] = v >> (8 - (ii + 1)) & 0x0001;
                    }
                }
            }
            return out;
        }
    }

    @Override
    public long[] getLongData()
    {
        if (ptr != 0) {
            return null;
        }
        else {
            if (isConstant()) {
                if (length > getMaxSizeOf32bitArray())
                    return null;
                long[] out = new long[(int) length];
                for (int i = 0; i < length; i++) {
                    out[i] = data[0];
                }
                return out;
            }
            else {
                long[] out = new long[(int) length];
                byte v;
                int ii;
                for (int i = 0; i < out.length; i++) {
                    v = data[i / 8];
                    ii = i % 8;
                    out[i] = v >> (8 - (ii + 1)) & 0x0001;
                }
                return out;
            }
        }
    }

    @Override
    public long[] getLongData(long[] a,
                              long startPos,
                              long endPos,
                              long step)
    {
        if (startPos < 0 || startPos >= length) {
            throw new ArrayIndexOutOfBoundsException("startPos < 0 || startPos >= length");
        }
        if (endPos < 0 || endPos > length || endPos < startPos) {
            throw new ArrayIndexOutOfBoundsException("endPos < 0 || endPos > length || endPos < startPos");
        }
        if (step < 1) {
            throw new IllegalArgumentException("step < 1");
        }

        long len = (long) Math.ceil((endPos - startPos) / (double) step);
        if (len > getMaxSizeOf32bitArray()) {
            return null;
        }
        else {
            long[] out;
            if (a != null && a.length >= len) {
                out = a;
            }
            else {
                out = new long[(int) len];
            }
            int idx = 0;
            if (ptr != 0) {
                for (long i = startPos; i < endPos; i += step) {
                    long index = i / 8l;
                    byte v = Utilities.UNSAFE.getByte(ptr + index);
                    long ii = i % 8l;
                    out[idx++] = v >> (8l - (ii + 1l)) & 0x0001;
                }
            }
            else {
                if (isConstant()) {
                    for (long i = startPos; i < endPos; i += step) {
                        out[idx++] = data[0];
                    }
                }
                else {
                    for (long i = startPos; i < endPos; i += step) {
                        int index = (int) i / 8;
                        byte v = data[index];
                        int ii = (int) i % 8;
                        out[idx++] = v >> (8 - (ii + 1)) & 0x0001;
                    }
                }
            }
            return out;
        }
    }

    @Override
    public float[] getFloatData()
    {
        if (ptr != 0) {
            return null;
        }
        else {
            if (isConstant()) {
                if (length > getMaxSizeOf32bitArray())
                    return null;
                float[] out = new float[(int) length];
                for (int i = 0; i < length; i++) {
                    out[i] = data[0];
                }
                return out;
            }
            else {
                float[] out = new float[(int) length];
                byte v;
                int ii;
                for (int i = 0; i < out.length; i++) {
                    v = data[i / 8];
                    ii = i % 8;
                    out[i] = v >> (8 - (ii + 1)) & 0x0001;
                }
                return out;
            }
        }
    }

    @Override
    public float[] getFloatData(float[] a,
                                long startPos,
                                long endPos,
                                long step)
    {
        if (startPos < 0 || startPos >= length) {
            throw new ArrayIndexOutOfBoundsException("startPos < 0 || startPos >= length");
        }
        if (endPos < 0 || endPos > length || endPos < startPos) {
            throw new ArrayIndexOutOfBoundsException("endPos < 0 || endPos > length || endPos < startPos");
        }
        if (step < 1) {
            throw new IllegalArgumentException("step < 1");
        }

        long len = (long) Math.ceil((endPos - startPos) / (double) step);
        if (len > getMaxSizeOf32bitArray()) {
            return null;
        }
        else {
            float[] out;
            if (a != null && a.length >= len) {
                out = a;
            }
            else {
                out = new float[(int) len];
            }
            int idx = 0;
            if (ptr != 0) {
                for (long i = startPos; i < endPos; i += step) {
                    long index = i / 8l;
                    byte v = Utilities.UNSAFE.getByte(ptr + index);
                    long ii = i % 8l;
                    out[idx++] = v >> (8l - (ii + 1l)) & 0x0001;
                }
            }
            else {
                if (isConstant()) {
                    for (long i = startPos; i < endPos; i += step) {
                        out[idx++] = data[0];
                    }
                }
                else {
                    for (long i = startPos; i < endPos; i += step) {
                        int index = (int) i / 8;
                        byte v = data[index];
                        int ii = (int) i % 8;
                        out[idx++] = v >> (8 - (ii + 1)) & 0x0001;
                    }
                }
            }
            return out;
        }
    }

    @Override
    public double[] getDoubleData()
    {
        if (ptr != 0) {
            return null;
        }
        else {
            if (isConstant()) {
                if (length > getMaxSizeOf32bitArray())
                    return null;
                double[] out = new double[(int) length];
                for (int i = 0; i < length; i++) {
                    out[i] = data[0];
                }
                return out;
            }
            else {
                double[] out = new double[(int) length];
                byte v;
                int ii;
                for (int i = 0; i < out.length; i++) {
                    v = data[i / 8];
                    ii = i % 8;
                    out[i] = v >> (8 - (ii + 1)) & 0x0001;
                }
                return out;
            }
        }
    }

    @Override
    public double[] getDoubleData(double[] a,
                                  long startPos,
                                  long endPos,
                                  long step)
    {
        if (startPos < 0 || startPos >= length) {
            throw new ArrayIndexOutOfBoundsException("startPos < 0 || startPos >= length");
        }
        if (endPos < 0 || endPos > length || endPos < startPos) {
            throw new ArrayIndexOutOfBoundsException("endPos < 0 || endPos > length || endPos < startPos");
        }
        if (step < 1) {
            throw new IllegalArgumentException("step < 1");
        }

        long len = (long) Math.ceil((endPos - startPos) / (double) step);
        if (len > getMaxSizeOf32bitArray()) {
            return null;
        }
        else {
            double[] out;
            if (a != null && a.length >= len) {
                out = a;
            }
            else {
                out = new double[(int) len];
            }
            int idx = 0;
            if (ptr != 0) {
                for (long i = startPos; i < endPos; i += step) {
                    long index = i / 8l;
                    byte v = Utilities.UNSAFE.getByte(ptr + index);
                    long ii = i % 8l;
                    out[idx++] = v >> (8l - (ii + 1l)) & 0x0001;
                }
            }
            else {
                if (isConstant()) {
                    for (long i = startPos; i < endPos; i += step) {
                        out[idx++] = data[0];
                    }
                }
                else {
                    for (long i = startPos; i < endPos; i += step) {
                        int index = (int) i / 8;
                        byte v = data[index];
                        int ii = (int) i % 8;
                        out[idx++] = v >> (8 - (ii + 1)) & 0x0001;
                    }
                }
            }
            return out;
        }
    }

    @Override
    public void setToNative(long i, Object value)
    {
        int v = 0;
        if ((Boolean) value) {
            v = 1;
        }
        long index = i / 8l;
        long ii = i % 8l;
        Utilities.UNSAFE.monitorEnter(LOCK);
        byte oldV = Utilities.UNSAFE.getByte(ptr + index);
        oldV = (byte) (((0xFF7F >> ii) & oldV) & 0x00FF);
        byte newV = (byte) ((v << (8l - (ii + 1l))) | oldV);
        Utilities.UNSAFE.putByte(ptr + index, newV);
        Utilities.UNSAFE.monitorExit(LOCK);

    }

    @Override
    public void setBoolean(long i, boolean value)
    {
        if (ptr != 0) {
            int v = 0;
            if (value) {
                v = 1;
            }
            long index = i / 8l;
            long ii = i % 8l;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte oldV = Utilities.UNSAFE.getByte(ptr + index);
            oldV = (byte) (((0xFF7F >> ii) & oldV) & 0x00FF);
            byte newV = (byte) ((v << (8l - (ii + 1l))) | oldV);
            Utilities.UNSAFE.putByte(ptr + index, newV);
            Utilities.UNSAFE.monitorExit(LOCK);
        }
        else {
            if (isConstant()) {
                throw new IllegalAccessError("Constant arrays cannot be modified.");
            }
            int v = 0;
            if (value) {
                v = 1;
            }
            int index = (int) i / 8;
            int ii = (int) i % 8;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte oldV = this.data[index];
            oldV = (byte) (((0xFF7F >> ii) & oldV) & 0x00FF);
            byte newV = (byte) ((v << (8 - (ii + 1))) | oldV);
            this.data[index] = newV;
            Utilities.UNSAFE.monitorExit(LOCK);
        }
    }

    @Override
    public void setByte(long i, byte value)
    {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException("The value has to be 0 or 1.");
        }
        if (ptr != 0) {
            int v = value & 0xFF;
            long index = i / 8l;
            long ii = i % 8l;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte oldV = Utilities.UNSAFE.getByte(ptr + index);
            oldV = (byte) (((0xFF7F >> ii) & oldV) & 0x00FF);
            byte newV = (byte) ((v << (8l - (ii + 1l))) | oldV);
            Utilities.UNSAFE.putByte(ptr + index, newV);
            Utilities.UNSAFE.monitorExit(LOCK);
        }
        else {
            if (isConstant()) {
                throw new IllegalAccessError("Constant arrays cannot be modified.");
            }
            int v = value & 0xFF;
            int index = (int) i / 8;
            int ii = (int) i % 8;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte oldV = this.data[index];
            oldV = (byte) (((0xFF7F >> ii) & oldV) & 0x00FF);
            byte newV = (byte) ((v << (8 - (ii + 1))) | oldV);
            this.data[index] = newV;
            Utilities.UNSAFE.monitorExit(LOCK);
        }
    }

    @Override
    public void setShort(long i, short value)
    {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException("The value has to be 0 or 1.");
        }
        if (ptr != 0) {
            int v = (byte) value & 0xFF;
            long index = i / 8l;
            long ii = i % 8l;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte oldV = Utilities.UNSAFE.getByte(ptr + index);
            oldV = (byte) (((0xFF7F >> ii) & oldV) & 0x00FF);
            byte newV = (byte) ((v << (8l - (ii + 1l))) | oldV);
            Utilities.UNSAFE.putByte(ptr + index, newV);
            Utilities.UNSAFE.monitorExit(LOCK);
        }
        else {
            if (isConstant()) {
                throw new IllegalAccessError("Constant arrays cannot be modified.");
            }
            int v = (byte) value & 0xFF;
            int index = (int) i / 8;
            int ii = (int) i % 8;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte oldV = this.data[index];
            oldV = (byte) (((0xFF7F >> ii) & oldV) & 0x00FF);
            byte newV = (byte) ((v << (8 - (ii + 1))) | oldV);
            this.data[index] = newV;
            Utilities.UNSAFE.monitorExit(LOCK);
        }
    }

    @Override
    public void setInt(long i, int value)
    {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException("The value has to be 0 or 1.");
        }
        if (ptr != 0) {
            long index = i / 8l;
            long ii = i % 8l;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte oldV = Utilities.UNSAFE.getByte(ptr + index);
            oldV = (byte) (((0xFF7F >> ii) & oldV) & 0x00FF);
            byte newV = (byte) ((value << (8l - (ii + 1l))) | oldV);
            Utilities.UNSAFE.putByte(ptr + index, newV);
            Utilities.UNSAFE.monitorExit(LOCK);
        }
        else {
            if (isConstant()) {
                throw new IllegalAccessError("Constant arrays cannot be modified.");
            }
            int index = (int) i / 8;
            int ii = (int) i % 8;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte oldV = this.data[index];
            oldV = (byte) (((0xFF7F >> ii) & oldV) & 0x00FF);
            byte newV = (byte) ((value << (8 - (ii + 1))) | oldV);
            this.data[index] = newV;
            Utilities.UNSAFE.monitorExit(LOCK);
        }
    }

    @Override
    public void setLong(long i, long value)
    {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException("The value has to be 0 or 1.");
        }
        int v = (int) value;
        if (ptr != 0) {
            long index = i / 8l;
            long ii = i % 8l;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte oldV = Utilities.UNSAFE.getByte(ptr + index);
            oldV = (byte) (((0xFF7F >> ii) & oldV) & 0x00FF);
            byte newV = (byte) ((v << (8l - (ii + 1l))) | oldV);
            Utilities.UNSAFE.putByte(ptr + index, newV);
            Utilities.UNSAFE.monitorExit(LOCK);
        }
        else {
            if (isConstant()) {
                throw new IllegalAccessError("Constant arrays cannot be modified.");
            }
            int index = (int) i / 8;
            int ii = (int) i % 8;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte oldV = this.data[index];
            oldV = (byte) (((0xFF7F >> ii) & oldV) & 0x00FF);
            byte newV = (byte) ((v << (8 - (ii + 1))) | oldV);
            this.data[index] = newV;
            Utilities.UNSAFE.monitorExit(LOCK);
        }
    }

    @Override
    public void setFloat(long i, float value)
    {
        if (value != 0.0 && value != 1.0) {
            throw new IllegalArgumentException("The value has to be 0 or 1.");
        }
        int v = (int) value;
        if (ptr != 0) {
            long index = i / 8l;
            long ii = i % 8l;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte oldV = Utilities.UNSAFE.getByte(ptr + index);
            oldV = (byte) (((0xFF7F >> ii) & oldV) & 0x00FF);
            byte newV = (byte) ((v << (8l - (ii + 1l))) | oldV);
            Utilities.UNSAFE.putByte(ptr + index, newV);
            Utilities.UNSAFE.monitorExit(LOCK);
        }
        else {
            if (isConstant()) {
                throw new IllegalAccessError("Constant arrays cannot be modified.");
            }
            int index = (int) i / 8;
            int ii = (int) i % 8;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte oldV = this.data[index];
            oldV = (byte) (((0xFF7F >> ii) & oldV) & 0x00FF);
            byte newV = (byte) ((v << (8 - (ii + 1))) | oldV);
            this.data[index] = newV;
            Utilities.UNSAFE.monitorExit(LOCK);
        }
    }

    @Override
    public void setDouble(long i, double value)
    {
        if (value != 0.0 && value != 1.0) {
            throw new IllegalArgumentException("The value has to be 0 or 1.");
        }
        int v = (int) value;
        if (ptr != 0) {
            long index = i / 8l;
            long ii = i % 8l;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte oldV = Utilities.UNSAFE.getByte(ptr + index);
            oldV = (byte) (((0xFF7F >> ii) & oldV) & 0x00FF);
            byte newV = (byte) ((v << (8l - (ii + 1l))) | oldV);
            Utilities.UNSAFE.putByte(ptr + index, newV);
            Utilities.UNSAFE.monitorExit(LOCK);
        }
        else {
            if (isConstant()) {
                throw new IllegalAccessError("Constant arrays cannot be modified.");
            }
            int index = (int) i / 8;
            int ii = (int) i % 8;
            Utilities.UNSAFE.monitorEnter(LOCK);
            byte oldV = this.data[index];
            oldV = (byte) (((0xFF7F >> ii) & oldV) & 0x00FF);
            byte newV = (byte) ((v << (8 - (ii + 1))) | oldV);
            this.data[index] = newV;
            Utilities.UNSAFE.monitorExit(LOCK);
        }
    }
}
