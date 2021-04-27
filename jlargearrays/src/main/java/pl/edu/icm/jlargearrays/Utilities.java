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

import java.lang.reflect.Field;

/**
 *
 * Utilities.
 *
 * @author Piotr Wendykier (p.wendykier@icm.edu.pl)
 */
public class Utilities
{

    /**
     * An object for performing low-level, unsafe operations.
     */
    public static final sun.misc.Unsafe UNSAFE;

    static {
        Object theUnsafe = null;
        Exception exception = null;
        try {
            Class<?> uc = Class.forName("sun.misc.Unsafe");
            Field f = uc.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            theUnsafe = f.get(uc);
        } catch (ClassNotFoundException e) {
            exception = e;
        } catch (IllegalAccessException e) {
            exception = e;
        } catch (IllegalArgumentException e) {
            exception = e;
        } catch (NoSuchFieldException e) {
            exception = e;
        } catch (SecurityException e) {
            exception = e;
        }
        UNSAFE = (sun.misc.Unsafe) theUnsafe;
        if (UNSAFE == null) {
            throw new Error("Could not obtain access to sun.misc.Unsafe", exception);
        }
    }

    private Utilities()
    {
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Both arrays need to be of the same type. Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final LargeArray src, final long srcPos, final LargeArray dest, final long destPos, final long length)
    {
        if (src.getType() != dest.getType()) {
            throw new IllegalArgumentException("The type of source array is different than the type of destimation array.");
        }
        switch (src.getType()) {
            case BIT:
                arraycopy((BitLargeArray) src, srcPos, (BitLargeArray) dest, destPos, length);
                break;
            case BYTE:
                arraycopy((ByteLargeArray) src, srcPos, (ByteLargeArray) dest, destPos, length);
                break;
            case SHORT:
                arraycopy((ShortLargeArray) src, srcPos, (ShortLargeArray) dest, destPos, length);
                break;
            case INT:
                arraycopy((IntLargeArray) src, srcPos, (IntLargeArray) dest, destPos, length);
                break;
            case LONG:
                arraycopy((LongLargeArray) src, srcPos, (LongLargeArray) dest, destPos, length);
                break;
            case FLOAT:
                arraycopy((FloatLargeArray) src, srcPos, (FloatLargeArray) dest, destPos, length);
                break;
            case DOUBLE:
                arraycopy((DoubleLargeArray) src, srcPos, (DoubleLargeArray) dest, destPos, length);
                break;
            case STRING:
                arraycopy((StringLargeArray) src, srcPos, (StringLargeArray) dest, destPos, length);
                break;
            default:
                throw new IllegalArgumentException("Invalid array type.");
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final BitLargeArray src, final long srcPos, final BitLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int nthreads = Runtime.getRuntime().availableProcessors();
        if (nthreads < 2 || length < 100000) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.setByte(j, src.getByte(i));
            }
        } else {
            long k = length / nthreads;
            Thread[] threads = new Thread[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setByte(destPos + k, src.getByte(srcPos + k));
                        }
                    }
                });
                threads[j].start();
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    threads[j].join();
                    threads[j] = null;
                }
            } catch (InterruptedException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.setByte(j, src.getByte(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final boolean[] src, final int srcPos, final BitLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }

        int i = srcPos;
        int nthreads = Runtime.getRuntime().availableProcessors();
        if (nthreads < 2 || length < 100000) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.setBoolean(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Thread[] threads = new Thread[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setBoolean(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });
                threads[j].start();
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    threads[j].join();
                    threads[j] = null;
                }
            } catch (InterruptedException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.setBoolean(j, src[i++]);
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final ByteLargeArray src, final long srcPos, final ByteLargeArray dest, final long destPos, final long length)
    {

        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int nthreads = Runtime.getRuntime().availableProcessors();
        if (nthreads < 2 || length < 100000) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.setByte(j, src.getByte(i));
            }
        } else {
            long k = length / nthreads;
            Thread[] threads = new Thread[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setByte(destPos + k, src.getByte(srcPos + k));
                        }
                    }
                });
                threads[j].start();
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    threads[j].join();
                    threads[j] = null;
                }
            } catch (InterruptedException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.setByte(j, src.getByte(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final byte[] src, final int srcPos, final ByteLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = Runtime.getRuntime().availableProcessors();
        if (nthreads < 2 || length < 100000) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.setByte(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Thread[] threads = new Thread[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setByte(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });
                threads[j].start();
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    threads[j].join();
                    threads[j] = null;
                }
            } catch (InterruptedException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.setByte(j, src[i++]);
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final ShortLargeArray src, final long srcPos, final ShortLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }

        int nthreads = Runtime.getRuntime().availableProcessors();
        if (nthreads < 2 || length < 100000) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.setShort(j, src.getShort(i));
            }
        } else {
            long k = length / nthreads;
            Thread[] threads = new Thread[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setShort(destPos + k, src.getShort(srcPos + k));
                        }
                    }
                });
                threads[j].start();
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    threads[j].join();
                    threads[j] = null;
                }
            } catch (InterruptedException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.setShort(j, src.getShort(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final short[] src, final int srcPos, final ShortLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = Runtime.getRuntime().availableProcessors();
        if (nthreads < 2 || length < 100000) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.setShort(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Thread[] threads = new Thread[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setShort(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });
                threads[j].start();
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    threads[j].join();
                    threads[j] = null;
                }
            } catch (InterruptedException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.setShort(j, src[i++]);
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final IntLargeArray src, final long srcPos, final IntLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int nthreads = Runtime.getRuntime().availableProcessors();
        if (nthreads < 2 || length < 100000) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.setInt(j, src.getInt(i));
            }
        } else {
            long k = length / nthreads;
            Thread[] threads = new Thread[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setInt(destPos + k, src.getInt(srcPos + k));
                        }
                    }
                });
                threads[j].start();
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    threads[j].join();
                    threads[j] = null;
                }
            } catch (InterruptedException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.setInt(j, src.getInt(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final int[] src, final int srcPos, final IntLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = Runtime.getRuntime().availableProcessors();
        if (nthreads < 2 || length < 100000) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.setInt(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Thread[] threads = new Thread[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setInt(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });
                threads[j].start();
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    threads[j].join();
                    threads[j] = null;
                }
            } catch (InterruptedException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.setInt(j, src[i++]);
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final LongLargeArray src, final long srcPos, final LongLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int nthreads = Runtime.getRuntime().availableProcessors();
        if (nthreads < 2 || length < 100000) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.setLong(j, src.getLong(i));
            }
        } else {
            long k = length / nthreads;
            Thread[] threads = new Thread[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setLong(destPos + k, src.getLong(srcPos + k));
                        }
                    }
                });
                threads[j].start();
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    threads[j].join();
                    threads[j] = null;
                }
            } catch (InterruptedException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.setLong(j, src.getLong(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final long[] src, final int srcPos, final LongLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = Runtime.getRuntime().availableProcessors();
        if (nthreads < 2 || length < 100000) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.setLong(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Thread[] threads = new Thread[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setLong(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });
                threads[j].start();
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    threads[j].join();
                    threads[j] = null;
                }
            } catch (InterruptedException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.setLong(j, src[i++]);
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final FloatLargeArray src, final long srcPos, final FloatLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int nthreads = Runtime.getRuntime().availableProcessors();
        if (nthreads < 2 || length < 100000) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.setFloat(j, src.getFloat(i));
            }
        } else {
            long k = length / nthreads;
            Thread[] threads = new Thread[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setFloat(destPos + k, src.getFloat(srcPos + k));
                        }
                    }
                });
                threads[j].start();
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    threads[j].join();
                    threads[j] = null;
                }
            } catch (InterruptedException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.setFloat(j, src.getFloat(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final float[] src, final int srcPos, final FloatLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = Runtime.getRuntime().availableProcessors();
        if (nthreads < 2 || length < 100000) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.setFloat(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Thread[] threads = new Thread[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setFloat(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });
                threads[j].start();
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    threads[j].join();
                    threads[j] = null;
                }
            } catch (InterruptedException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.setFloat(j, src[i++]);
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final DoubleLargeArray src, final long srcPos, final DoubleLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int nthreads = Runtime.getRuntime().availableProcessors();
        if (nthreads < 2 || length < 100000) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.setDouble(j, src.getDouble(i));
            }
        } else {
            long k = length / nthreads;
            Thread[] threads = new Thread[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setDouble(destPos + k, src.getDouble(srcPos + k));
                        }
                    }
                });
                threads[j].start();
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    threads[j].join();
                    threads[j] = null;
                }
            } catch (InterruptedException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.setDouble(j, src.getDouble(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final double[] src, final int srcPos, final DoubleLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = Runtime.getRuntime().availableProcessors();
        if (nthreads < 2 || length < 100000) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.setDouble(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Thread[] threads = new Thread[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.setDouble(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });
                threads[j].start();
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    threads[j].join();
                    threads[j] = null;
                }
            } catch (InterruptedException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.setDouble(j, src[i++]);
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final StringLargeArray src, final long srcPos, final StringLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length()) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length()");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int nthreads = Runtime.getRuntime().availableProcessors();
        if (nthreads < 2 || length < 100000) {
            for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                dest.set(j, src.get(i));
            }
        } else {
            long k = length / nthreads;
            Thread[] threads = new Thread[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.set(destPos + k, src.get(srcPos + k));
                        }
                    }
                });
                threads[j].start();
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    threads[j].join();
                    threads[j] = null;
                }
            } catch (InterruptedException ex) {
                for (long i = srcPos, j = destPos; i < srcPos + length; i++, j++) {
                    dest.set(j, src.get(i));
                }
            }
        }
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * Array bounds are checked.
     *
     * @param src     the source array.
     * @param srcPos  starting position in the source array.
     * @param dest    the destination array.
     * @param destPos starting position in the destination data.
     * @param length  the number of array elements to be copied.
     */
    public static void arraycopy(final String[] src, final int srcPos, final StringLargeArray dest, final long destPos, final long length)
    {
        if (srcPos < 0 || srcPos >= src.length) {
            throw new ArrayIndexOutOfBoundsException("srcPos < 0 || srcPos >= src.length");
        }
        if (destPos < 0 || destPos >= dest.length()) {
            throw new ArrayIndexOutOfBoundsException("destPos < 0 || destPos >= dest.length()");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        if (dest.isConstant()) {
            throw new IllegalArgumentException("Constant arrays cannot be modified.");
        }
        int i = srcPos;
        int nthreads = Runtime.getRuntime().availableProcessors();
        if (nthreads < 2 || length < 100000) {
            for (long j = destPos; j < destPos + length; j++) {
                dest.set(j, src[i++]);
            }
        } else {
            long k = length / nthreads;
            Thread[] threads = new Thread[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        for (long k = firstIdx; k < lastIdx; k++) {
                            dest.set(destPos + k, src[srcPos + (int) k]);
                        }
                    }
                });
                threads[j].start();
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    threads[j].join();
                    threads[j] = null;
                }
            } catch (InterruptedException ex) {
                for (long j = destPos; j < destPos + length; j++) {
                    dest.set(j, src[i++]);
                }
            }
        }
    }

    /**
     * Creates a new instance of LargeArray. The native memory is zeroed.
     *
     * @param type   the type of LargeArray
     * @param length number of elements
     *
     * @return new instance of LargeArray
     */
    public static LargeArray create(LargeArrayType type, long length)
    {
        return create(type, length, true);
    }

    /**
     * Creates a new instance of LargeArray
     *
     * @param type             the type of LargeArray
     * @param length           number of elements
     * @param zeroNativeMemory if true, then the native memory is zeroed
     *
     * @return new instance of LargeArray
     */
    public static LargeArray create(LargeArrayType type, long length, boolean zeroNativeMemory)
    {
        switch (type) {
            case BIT:
                return new BitLargeArray(length, zeroNativeMemory);
            case BYTE:
                return new ByteLargeArray(length, zeroNativeMemory);
            case SHORT:
                return new ShortLargeArray(length, zeroNativeMemory);
            case INT:
                return new IntLargeArray(length, zeroNativeMemory);
            case LONG:
                return new LongLargeArray(length, zeroNativeMemory);
            case FLOAT:
                return new FloatLargeArray(length, zeroNativeMemory);
            case DOUBLE:
                return new DoubleLargeArray(length, zeroNativeMemory);
            case STRING:
                return new StringLargeArray(length, 100, zeroNativeMemory);
            default:
                throw new IllegalArgumentException("Invalid array type.");
        }
    }

    /**
     * Converts LargeArray to a given type.
     *
     * @param src  the source array
     * @param type the type of LargeArray
     *
     * @return LargeArray of a specified type
     */
    public static LargeArray convert(final LargeArray src, final LargeArrayType type)
    {
        if (src.getType() == type) {
            return src;
        }
        if (src.isConstant()) {
            switch (type) {
                case BIT:
                    return new BitLargeArray(src.length(), src.getByte(0));
                case BYTE:
                    return new ByteLargeArray(src.length(), src.getByte(0));
                case SHORT:
                    return new ShortLargeArray(src.length(), src.getShort(0));
                case INT:
                    return new IntLargeArray(src.length(), src.getInt(0));
                case LONG:
                    return new LongLargeArray(src.length(), src.getLong(0));
                case FLOAT:
                    return new FloatLargeArray(src.length(), src.getFloat(0));
                case DOUBLE:
                    return new DoubleLargeArray(src.length(), src.getDouble(0));
                default:
                    throw new IllegalArgumentException("Invalid array type.");
            }
        }
        long length = src.length;
        final LargeArray out = create(type, length, false);
        int nthreads = Runtime.getRuntime().availableProcessors();
        if (nthreads < 2 || length < 100000) {
            switch (type) {
                case BIT:
                case BYTE:
                    for (long i = 0; i < length; i++) {
                        out.setByte(i, src.getByte(i));
                    }
                    break;
                case SHORT:
                    for (long i = 0; i < length; i++) {
                        out.setShort(i, src.getShort(i));
                    }
                    break;
                case INT:
                    for (long i = 0; i < length; i++) {
                        out.setInt(i, src.getInt(i));
                    }
                    break;
                case LONG:
                    for (long i = 0; i < length; i++) {
                        out.setLong(i, src.getLong(i));
                    }
                    break;
                case FLOAT:
                    for (long i = 0; i < length; i++) {
                        out.setFloat(i, src.getFloat(i));
                    }
                    break;
                case DOUBLE:
                    for (long i = 0; i < length; i++) {
                        out.setDouble(i, src.getDouble(i));
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid array type.");
            }
        } else {
            long k = length / nthreads;
            Thread[] threads = new Thread[nthreads];
            for (int j = 0; j < nthreads; j++) {
                final long firstIdx = j * k;
                final long lastIdx = (j == nthreads - 1) ? length : firstIdx + k;
                threads[j] = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        switch (type) {
                            case BYTE:
                                for (long i = firstIdx; i < lastIdx; i++) {
                                    out.setByte(i, src.getByte(i));
                                }
                                break;
                            case SHORT:
                                for (long i = firstIdx; i < lastIdx; i++) {
                                    out.setShort(i, src.getShort(i));
                                }
                                break;
                            case INT:
                                for (long i = firstIdx; i < lastIdx; i++) {
                                    out.setInt(i, src.getInt(i));
                                }
                                break;
                            case LONG:
                                for (long i = firstIdx; i < lastIdx; i++) {
                                    out.setLong(i, src.getLong(i));
                                }
                                break;
                            case FLOAT:
                                for (long i = firstIdx; i < lastIdx; i++) {
                                    out.setFloat(i, src.getFloat(i));
                                }
                                break;
                            case DOUBLE:
                                for (long i = firstIdx; i < lastIdx; i++) {
                                    out.setDouble(i, src.getDouble(i));
                                }
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid array type.");
                        }
                    }
                });
                threads[j].start();
            }
            try {
                for (int j = 0; j < nthreads; j++) {
                    threads[j].join();
                    threads[j] = null;
                }
            } catch (InterruptedException ex) {
                switch (type) {
                    case BIT:
                    case BYTE:
                        for (long i = 0; i < length; i++) {
                            out.setByte(i, src.getByte(i));
                        }
                        break;
                    case SHORT:
                        for (long i = 0; i < length; i++) {
                            out.setShort(i, src.getShort(i));
                        }
                        break;
                    case INT:
                        for (long i = 0; i < length; i++) {
                            out.setInt(i, src.getInt(i));
                        }
                        break;
                    case LONG:
                        for (long i = 0; i < length; i++) {
                            out.setLong(i, src.getLong(i));
                        }
                        break;
                    case FLOAT:
                        for (long i = 0; i < length; i++) {
                            out.setFloat(i, src.getFloat(i));
                        }
                        break;
                    case DOUBLE:
                        for (long i = 0; i < length; i++) {
                            out.setDouble(i, src.getDouble(i));
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid array type.");
                }
            }
        }
        return out;
    }
}
