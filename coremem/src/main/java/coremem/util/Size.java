package coremem.util;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import coremem.enums.NativeTypeEnum;
import coremem.exceptions.UnknownSizeOfException;
import coremem.interfaces.SizedInBytes;

import org.bridj.Pointer;

/**
 * This class has static methods to compute the size in bytes of java primitive
 * types, primitive arrays, NIO buffers and more...
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public class Size
{

  public static int DOUBLE = 8;
  public static int FLOAT = 4;
  public static int HALFFLOAT = 2;
  public static int LONG = 8;
  public static int INT = 4;
  public static int SHORT = 2;
  public static int CHAR = 2;
  public static int BYTE = 1;

  public static int DOUBLESHIFT = 3;
  public static int FLOATSHIFT = 2;
  public static int LONGSHIFT = 3;
  public static int INTSHIFT = 2;
  public static int SHORTSHIFT = 1;
  public static int CHARSHIFT = 1;
  public static int BYTESHIFT = 0;

  /**
   * returns the size in bytes of the object (or class of an object). This works
   * for objects such as: byte.class, float[]{1,2,3},...
   * 
   * @param pObject
   *          object, can be byte, byte.class, or byte[], NIO buffer object...
   * @return size in bytes
   */
  public static long of(final Object pObject)
  {
    long lSize = ofPrimitiveObject(pObject);
    if (lSize != -1)
      return lSize;
    if (pObject instanceof Class<?>)
    {
      return ofPrimitiveClass((Class<?>) pObject);
    }
    else
    {

      if (pObject.getClass().isArray())
      {
        return ofPrimitive1DArray(pObject);
      }
      else if (pObject instanceof Buffer)
      {
        return ofBuffer((Buffer) pObject);
      }
      else if (pObject instanceof SizedInBytes)
      {
        return ofSizedInBytes((SizedInBytes) pObject);
      }
      else if (pObject instanceof Pointer)
      {
        return ofBridJPointer((Pointer<?>) pObject);
      }
      else if (pObject instanceof NativeTypeEnum)
      {
        return ofNativeTypeEnum((NativeTypeEnum) pObject);
      }
      else if (pObject instanceof String)
      {
        return ofString((String) pObject);
      }
    }

    return -1;
  }

  /**
   * Size in bytes for this primitive.
   * 
   * @param pPrimitive
   *          primitive
   * @return size in bytes
   */
  public static int of(final byte pPrimitive)
  {
    return BYTE;
  }

  /**
   * Size in bytes for this primitive.
   * 
   * @param pPrimitive
   *          primitive
   * @return size in bytes
   */
  public static int of(final short pPrimitive)
  {
    return SHORT;
  }

  /**
   * Size in bytes for this primitive.
   * 
   * @param pPrimitive
   *          primitive
   * @return size in bytes
   */
  public static int of(final char pPrimitive)
  {
    return CHAR;
  }

  /**
   * Size in bytes for this primitive.
   * 
   * @param pPrimitive
   *          primitive
   * @return size in bytes
   */
  public static int of(final int pPrimitive)
  {
    return INT;
  }

  /**
   * Size in bytes for this primitive.
   * 
   * @param pPrimitive
   *          primitive
   * @return size in bytes
   */
  public static int of(final long pPrimitive)
  {
    return LONG;
  }

  /**
   * Size in bytes for this primitive.
   * 
   * @param pPrimitive
   *          primitive
   * @return size in bytes
   */
  public static int of(final float pPrimitive)
  {
    return FLOAT;
  }

  /**
   * Size in bytes for this primitive.
   * 
   * @param pPrimitive
   *          primitive
   * @return size in bytes
   */
  public static int of(final double pPrimitive)
  {
    return DOUBLE;
  }

  private static long ofPrimitiveObject(Object pObject)
  {
    if (pObject instanceof Character)
      return CHAR;
    else if (pObject instanceof Byte)
      return BYTE;
    else if (pObject instanceof Short)
      return SHORT;
    else if (pObject instanceof Integer)
      return INT;
    else if (pObject instanceof Long)
      return LONG;
    else if (pObject instanceof Float)
      return FLOAT;
    else if (pObject instanceof Double)
      return DOUBLE;
    else
      return -1;

  }

  /**
   * Returns the size in bytes of this primitive type class
   * 
   * @param pClass
   *          primitive type class
   * @return size in bytes
   */
  public static int ofPrimitiveClass(final Class<?> pClass)
  {
    if (pClass == Character.class || pClass == char.class
        || pClass == Character.TYPE)
      return CHAR;
    else if (pClass == Byte.class || pClass == byte.class
             || pClass == Byte.TYPE)
      return BYTE;
    else if (pClass == Short.class || pClass == short.class
             || pClass == Short.TYPE)
      return SHORT;
    else if (pClass == Integer.class || pClass == int.class
             || pClass == Integer.TYPE)
      return INT;
    else if (pClass == Long.class || pClass == long.class
             || pClass == Long.TYPE)
      return LONG;
    else if (pClass == Float.class || pClass == float.class
             || pClass == Float.TYPE)
      return FLOAT;
    else if (pClass == Double.class || pClass == double.class
             || pClass == Double.TYPE)
      return DOUBLE;
    else
      return fromString(pClass.getSimpleName());

  }

  /**
   * Returns the size in bytes of a 1D primitive array
   * 
   * @param pArray
   *          1D primitive array
   * @return array size in bytes
   */
  public static int ofPrimitive1DArray(final Object pArray)
  {
    if (!pArray.getClass().isArray())
      throw new UnknownSizeOfException("This is not an array:"
                                       + pArray.getClass()
                                               .toString());

    if (pArray instanceof char[])
      return CHAR * ((char[]) pArray).length;
    else if (pArray instanceof byte[])
      return BYTE * ((byte[]) pArray).length;
    else if (pArray instanceof short[])
      return SHORT * ((short[]) pArray).length;
    else if (pArray instanceof int[])
      return INT * ((int[]) pArray).length;
    else if (pArray instanceof long[])
      return LONG * ((long[]) pArray).length;
    else if (pArray instanceof float[])
      return FLOAT * ((float[]) pArray).length;
    else if (pArray instanceof double[])
      return DOUBLE * ((double[]) pArray).length;
    else
      new UnknownSizeOfException("Unknown array type");

    return -1;
  }

  /**
   * Returns the size in bytes of a NIO Buffer
   * 
   * @param pBuffer
   *          NIO buffer
   * @return size in bytes
   */
  public static long ofBuffer(final Buffer pBuffer)
  {
    final int lCapacity = pBuffer.capacity();
    if (pBuffer instanceof ByteBuffer)
      return of(byte.class) * lCapacity;

    else if (pBuffer instanceof CharBuffer)
      return of(char.class) * lCapacity;

    else if (pBuffer instanceof ShortBuffer)
      return of(short.class) * lCapacity;

    else if (pBuffer instanceof IntBuffer)
      return of(int.class) * lCapacity;

    else if (pBuffer instanceof LongBuffer)
      return of(long.class) * lCapacity;

    else if (pBuffer instanceof FloatBuffer)
      return of(float.class) * lCapacity;

    else if (pBuffer instanceof DoubleBuffer)
      return of(double.class) * lCapacity;

    else
      throw new UnknownSizeOfException("Invalid NIO Buffer!");

  }

  /**
   * Returns the size in bytes of this BridJ pointer
   * 
   * @param pPointer
   *          BridJ pointer
   * @return size in bytes
   */
  public static long ofBridJPointer(final Pointer<?> pPointer)
  {
    long lValidBytes = pPointer.getValidBytes();
    if (lValidBytes < 0)
      new UnknownSizeOfException("This BridJ pointer has no defined length in bytes!");
    return lValidBytes;
  }

  /**
   * Returns the size in bytes of an object implementing SizedInBytes.
   * 
   * @param pSizedInBytes
   *          object
   * @return size in bytes
   */
  public static long ofSizedInBytes(final SizedInBytes pSizedInBytes)
  {
    return pSizedInBytes.getSizeInBytes();
  }

  /**
   * Return the size in bytes for a given native type
   * 
   * @param pNativeType
   *          native type
   * @return size in bytes
   */
  public static int ofNativeTypeEnum(NativeTypeEnum pNativeType)
  {
    if (pNativeType == NativeTypeEnum.Byte)
      return ofPrimitiveClass(byte.class);

    else if (pNativeType == NativeTypeEnum.UnsignedByte)
      return ofPrimitiveClass(byte.class);

    else if (pNativeType == NativeTypeEnum.Short)
      return ofPrimitiveClass(short.class);

    else if (pNativeType == NativeTypeEnum.UnsignedShort)
      return ofPrimitiveClass(short.class);

    else if (pNativeType == NativeTypeEnum.Int)
      return ofPrimitiveClass(int.class);

    else if (pNativeType == NativeTypeEnum.UnsignedInt)
      return ofPrimitiveClass(int.class);

    else if (pNativeType == NativeTypeEnum.Long)
      return ofPrimitiveClass(long.class);

    else if (pNativeType == NativeTypeEnum.UnsignedLong)
      return ofPrimitiveClass(long.class);

    else if (pNativeType == NativeTypeEnum.HalfFloat)
      return ofPrimitiveClass(float.class) / 2;

    else if (pNativeType == NativeTypeEnum.Float)
      return ofPrimitiveClass(float.class);

    else if (pNativeType == NativeTypeEnum.Double)
      return ofPrimitiveClass(double.class);

    else
      throw new UnknownSizeOfException("Invalid Class!");
  }

  /**
   * Returns the size in bytes of a Java String
   * 
   * @param pString
   *          string
   * @return size in bytes
   */
  public static long ofString(String pString)
  {
    return pString.length() * ofPrimitiveClass(char.class);
  }

  /**
   * Returns the size in bytes corresponding to a type described by a string
   * (type name as string).
   * 
   * @param pTypeName
   *          type name
   * @return size in bytes
   */
  public static int fromString(String pTypeName)
  {
    pTypeName = pTypeName.toLowerCase();
    pTypeName = pTypeName.replaceAll("type", "");

    if (pTypeName.contains("unsignedbyte")
        || pTypeName.contains("ubyte"))
      return ofPrimitiveClass(byte.class);

    else if (pTypeName.contains("byte"))
      return ofPrimitiveClass(byte.class);

    else if (pTypeName.contains("unsignedshort")
             || pTypeName.contains("ushort"))
      return ofPrimitiveClass(short.class);

    else if (pTypeName.contains("short"))
      return ofPrimitiveClass(short.class);

    else if (pTypeName.contains("unsignedint")
             || pTypeName.contains("uint"))
      return ofPrimitiveClass(int.class);

    else if (pTypeName.contains("int"))
      return ofPrimitiveClass(int.class);

    else if (pTypeName.contains("unsignedlong")
             || pTypeName.contains("ulong"))
      return ofPrimitiveClass(long.class);

    else if (pTypeName.contains("long"))
      return ofPrimitiveClass(long.class);

    else if (pTypeName.contains("halffloat"))
      return ofPrimitiveClass(float.class) / 2;

    else if (pTypeName.contains("float"))
      return ofPrimitiveClass(float.class);

    else if (pTypeName.contains("double"))
      return ofPrimitiveClass(double.class);

    else
      throw new UnknownSizeOfException("Invalid type name!");
  }

  /**
   * Returns the class of an integral primitive type given a number of bytes
   * 
   * @param pNumberOfBytes
   *          number of bytes in type
   * @param pSigned
   *          is signed?
   * @return integral primitive type class
   */
  public static Class<?> integralTypeFromSize(final int pNumberOfBytes,
                                              final boolean pSigned)
  {
    switch (pNumberOfBytes)
    {
    case 1:
      return byte.class;
    case 2:
      if (pSigned)
        return short.class;
      else
        return char.class;
    case 4:
      return int.class;
    case 8:
      return long.class;
    }
    throw new UnknownSizeOfException("Invalid number of bytes!");
  }

  /**
   * Returns a primitive float type given a number of bytes.
   * 
   * @param pNumberOfBytes
   *          number of bytes in type
   * @return float primitive type class
   */
  public static Class<?> floatTypeFromSize(final int pNumberOfBytes)
  {
    switch (pNumberOfBytes)
    {
    case 4:
      return float.class;
    case 8:
      return double.class;
    }
    throw new UnknownSizeOfException("Invalid number of bytes!");
  }

}
