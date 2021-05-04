package clearcl.backend.jocl;

import clearcl.ClearCLPeerPointer;
import clearcl.backend.BackendUtils;
import clearcl.backend.SizeOf;
import org.jocl.Pointer;
import org.jocl.cl_device_id;
import org.jocl.cl_platform_id;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.jocl.CL.clGetDeviceInfo;
import static org.jocl.CL.clGetPlatformInfo;

/**
 * Utility class for JOCL backend
 *
 * @author royer
 */
public class Utils
{

  /**
   * Returns the value of the device info parameter with the given name
   *
   * @param device    The device
   * @param paramName The parameter name
   * @return The value
   */
  public static int getInt(cl_device_id device, int paramName)
  {
    return getInts(device, paramName, 1)[0];
  }

  /**
   * Returns the values of the device info parameter with the given name
   *
   * @param device    The device
   * @param paramName The parameter name
   * @param numValues The number of values
   * @return The value
   */
  public static int[] getInts(cl_device_id device, int paramName, int numValues)
  {
    int values[] = new int[numValues];
    BackendUtils.checkOpenCLError(clGetDeviceInfo(device, paramName, SizeOf.cl_int * numValues, Pointer.to(values), null));
    return values;
  }

  /**
   * Returns the value of the device info parameter with the given name
   *
   * @param device    The device
   * @param paramName The parameter name
   * @return The value
   */
  public static long getLong(cl_device_id device, int paramName)
  {
    return getLongs(device, paramName, 1)[0];
  }

  /**
   * Returns the values of the device info parameter with the given name
   *
   * @param device    The device
   * @param paramName The parameter name
   * @param numValues The number of values
   * @return The value
   */
  public static long[] getLongs(cl_device_id device, int paramName, int numValues)
  {
    long values[] = new long[numValues];
    BackendUtils.checkOpenCLError(clGetDeviceInfo(device, paramName, SizeOf.cl_long * numValues, Pointer.to(values), null));
    return values;
  }

  /**
   * Returns the value of the device info parameter with the given name
   *
   * @param device    The device
   * @param paramName The parameter name
   * @return The value
   */
  public static String getString(cl_device_id device, int paramName)
  {
    // Obtain the length of the string that will be queried
    long size[] = new long[1];
    BackendUtils.checkOpenCLError(clGetDeviceInfo(device, paramName, 0, null, size));

    // Create a buffer of the appropriate size and fill it with the info
    byte buffer[] = new byte[(int) size[0]];
    BackendUtils.checkOpenCLError(clGetDeviceInfo(device, paramName, buffer.length, Pointer.to(buffer), null));

    // Create a string from the buffer (excluding the trailing \0 byte)
    return new String(buffer, 0, buffer.length - 1);
  }

  /**
   * Returns the value of the platform info parameter with the given name
   *
   * @param platform  The platform
   * @param paramName The parameter name
   * @return The value
   */
  public static String getString(cl_platform_id platform, int paramName)
  {
    // Obtain the length of the string that will be queried
    long size[] = new long[1];
    BackendUtils.checkOpenCLError(clGetPlatformInfo(platform, paramName, 0, null, size));

    // Create a buffer of the appropriate size and fill it with the info
    byte buffer[] = new byte[(int) size[0]];
    BackendUtils.checkOpenCLError(clGetPlatformInfo(platform, paramName, buffer.length, Pointer.to(buffer), null));

    // Create a string from the buffer (excluding the trailing \0 byte)
    return new String(buffer, 0, buffer.length - 1);
  }

  /**
   * Returns the value of the device info parameter with the given name
   *
   * @param device    The device
   * @param paramName The parameter name
   * @return The value
   */
  public static long getSize(cl_device_id device, int paramName)
  {
    return getSizes(device, paramName, 1)[0];
  }

  /**
   * Returns the values of the device info parameter with the given name
   *
   * @param device    The device
   * @param paramName The parameter name
   * @param numValues The number of values
   * @return The value
   */
  public static long[] getSizes(cl_device_id device, int paramName, int numValues)
  {
    // The size of the returned data has to depend on
    // the size of a size_t, which is handled here
    ByteBuffer buffer = ByteBuffer.allocate(numValues * SizeOf.size_t).order(ByteOrder.nativeOrder());
    BackendUtils.checkOpenCLError(clGetDeviceInfo(device, paramName, SizeOf.size_t * numValues, Pointer.to(buffer), null));
    long values[] = new long[numValues];
    if (SizeOf.size_t == 4)
    {
      for (int i = 0; i < numValues; i++)
      {
        values[i] = buffer.getInt(i * SizeOf.size_t);
      }
    } else
    {
      for (int i = 0; i < numValues; i++)
      {
        values[i] = buffer.getLong(i * SizeOf.size_t);
      }
    }
    return values;
  }

  /**
   * Returns boolean property for device
   *
   * @param pPointer device pointer
   * @param pParam   parameter
   * @return boolean
   */
  public static boolean getBoolean(cl_device_id pPointer, int pParam)
  {
    return getInt(pPointer, pParam) > 0;
  }

  /**
   * Converts device pointers from peer pointers to backend specific pointers,
   *
   * @param pClearCLDevicePeerPointers device pointers
   * @return array of backend specific device pointers
   */
  public static cl_device_id[] convertDevicePointers(ClearCLPeerPointer... pClearCLDevicePeerPointers)
  {
    return BackendUtils.checkExceptions(() ->
    {
      cl_device_id[] lJOCLDevicePointers = new cl_device_id[pClearCLDevicePeerPointers.length];

      for (int i = 0; i < pClearCLDevicePeerPointers.length; i++)
        lJOCLDevicePointers[i] = (cl_device_id) pClearCLDevicePeerPointers[i].getPointer();

      return lJOCLDevicePointers;
    });
  }

  // public static <N extends NativePointerObject> N checkNullReturn(N pPointer)
  // {
  // try
  // {
  // Field lField = pPointer.getClass().getDeclaredField("nativePointer");
  // //NoSuchFieldException
  // lField.setAccessible(true);
  // long lAddress = (Long) lField.get(pPointer);
  // if(lAddress==0)
  // throw new ClearCLNullPointerException();
  // return pPointer;
  // }
  // catch (NoSuchFieldException | SecurityException
  // | IllegalArgumentException | IllegalAccessException e)
  // {
  // throw new
  // ClearCLException("Problem while accessing JOCL native pointer address.",e);
  // }
  // }

}
