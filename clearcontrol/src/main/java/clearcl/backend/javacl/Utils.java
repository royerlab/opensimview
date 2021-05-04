package clearcl.backend.javacl;

import clearcl.ClearCLPeerPointer;
import clearcl.backend.BackendUtils;
import clearcl.backend.SizeOf;
import clearcl.exceptions.ClearCLUnsupportedException;
import com.nativelibs4java.opencl.CLPlatform.ContextProperties;
import com.nativelibs4java.opencl.JavaCL.OpenCLProbeLibrary;
import com.nativelibs4java.opencl.library.IOpenCLLibrary.cl_platform_id;
import com.nativelibs4java.opencl.library.OpenCLLibrary;
import org.bridj.BridJ;
import org.bridj.Platform;
import org.bridj.Pointer;
import org.bridj.Pointer.StringType;
import org.bridj.SizeT;
import org.bridj.util.ProcessUtils;
import org.bridj.util.StringUtils;

import java.nio.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.nativelibs4java.opencl.library.IOpenCLLibrary.CL_CONTEXT_PLATFORM;

/**
 * Utility class for JavaCL backend
 *
 * @author royer
 */
public class Utils
{

  /**
   * This static method initializes JavaCL. This code is 'stolen' from Olvier
   * Chafik 'JavaCL' class.
   *
   * @param pDebug to enable debugging.
   * @return OpenCL library
   */
  public static OpenCLLibrary initCL(boolean pDebug)
  {
    if (Platform.isLinux())
    {
      String amdAppBase = "/opt/AMDAPP/lib";
      BridJ.addLibraryPath(amdAppBase + "/" + (Platform.is64Bits() ? "x86_64" : "x86"));
      BridJ.addLibraryPath(amdAppBase);
    }
    boolean needsAdditionalSynchronization = false;
    {
      OpenCLProbeLibrary probe = null;
      try
      {
        try
        {
          BridJ.setNativeLibraryActualName("OpenCLProbe", "OpenCL");
          BridJ.register();
        } catch (Throwable th)
        {
        }

        probe = new OpenCLProbeLibrary();

        if (!OpenCLProbeLibrary.isValid())
        {
          BridJ.unregister(OpenCLProbeLibrary.class);
          // BridJ.setNativeLibraryActualName("OpenCLProbe", "OpenCL");
          String alt;
          if (Platform.is64Bits() && (BridJ.getNativeLibraryFile(alt = "atiocl64") != null || BridJ.getNativeLibraryFile(alt = "amdocl64") != null) || BridJ.getNativeLibraryFile(alt = "atiocl32") != null || BridJ.getNativeLibraryFile(alt = "atiocl") != null || BridJ.getNativeLibraryFile(alt = "amdocl32") != null || BridJ.getNativeLibraryFile(alt = "amdocl") != null)
          {
            log(Level.INFO, "Hacking around ATI's weird driver bugs (using atiocl/amdocl library instead of OpenCL)", null);
            BridJ.setNativeLibraryActualName("OpenCL", alt);
          }
          BridJ.register(OpenCLProbeLibrary.class);
        }

        if (OpenCLProbeLibrary.hasOpenCL1_0())
        {
          needsAdditionalSynchronization = true;
          log(Level.WARNING, "At least one OpenCL platform uses OpenCL 1.0, which is not thread-safe: will use (slower) synchronized low-level bindings.");
        }
      } finally
      {
        if (probe != null) BridJ.unregister(OpenCLProbeLibrary.class);
        probe = null;
      }
    }

    if (pDebug)
    {
      List<String> DEBUG_COMPILER_FLAGS;
      String JAVACL_DEBUG_COMPILER_FLAGS_PROP = "JAVACL_DEBUG_COMPILER_FLAGS";

      String debugArgs = System.getenv(JAVACL_DEBUG_COMPILER_FLAGS_PROP);
      if (debugArgs != null) DEBUG_COMPILER_FLAGS = Arrays.asList(debugArgs.split(" "));
      else if (Platform.isMacOSX()) DEBUG_COMPILER_FLAGS = Arrays.asList("-g");
      else DEBUG_COMPILER_FLAGS = Arrays.asList("-O0", "-g");

      int pid = ProcessUtils.getCurrentProcessId();
      log(Level.INFO, "Debug mode enabled with compiler flags \"" + StringUtils.implode(DEBUG_COMPILER_FLAGS, " ") + "\" (can be overridden with env. var. JAVACL_DEBUG_COMPILER_FLAGS_PROP)");
      log(Level.INFO, "You can debug your kernels with GDB using one of the following commands :\n" + "\tsudo gdb --tui --pid=" + pid + "\n" + "\tsudo ddd --debugger \"gdb --pid=" + pid + "\"\n" + "More info here :\n" + "\thttp://code.google.com/p/javacl/wiki/DebuggingKernels");

    }
    Class<? extends OpenCLLibrary> libraryClass = OpenCLLibrary.class;
    if (needsAdditionalSynchronization)
    {
      try
      {
        libraryClass = BridJ.subclassWithSynchronizedNativeMethods(libraryClass);
      } catch (Throwable ex)
      {
        throw new RuntimeException("Failed to create a synchronized version of the OpenCL API bindings: " + ex, ex);
      }
    }
    BridJ.register(libraryClass);
    try
    {
      OpenCLLibrary lOpenCLLibrary = libraryClass.newInstance();
      return lOpenCLLibrary;
    } catch (Throwable ex)
    {
      throw new RuntimeException("Failed to instantiate library " + libraryClass.getName() + ": " + ex, ex);
    }
  }

  private static void log(Level pInfo, String pString, Object pObject)
  {
    System.out.println(pInfo + " " + pString + " " + pObject);
  }

  private static void log(Level pInfo, String pString)
  {
    System.out.println(pInfo + " " + pString + " ");
  }

  /**
   * Returns the value of the device info parameter with the given name
   *
   * @param pOpenCLLibrary OpenCL library access
   * @param device         The device
   * @param paramName      The parameter name
   * @return The value
   */
  public static int getInt(OpenCLLibrary pOpenCLLibrary, OpenCLLibrary.cl_device_id device, int paramName)
  {
    return getInts(pOpenCLLibrary, device, paramName, 1)[0];
  }

  /**
   * Returns the values of the device info parameter with the given name
   *
   * @param pOpenCLLibrary OpenCL library access
   * @param device         The device
   * @param paramName      The parameter name
   * @param numValues      The number of values
   * @return The value
   */
  public static Integer[] getInts(OpenCLLibrary pOpenCLLibrary, OpenCLLibrary.cl_device_id device, int paramName, int numValues)
  {
    Pointer<Integer> lValues = Pointer.allocateInts(numValues);
    BackendUtils.checkOpenCLError(pOpenCLLibrary.clGetDeviceInfo(device, paramName, SizeOf.cl_int * numValues, lValues, null));
    return lValues.toArray();
  }

  /**
   * Returns the value of the device info parameter with the given name
   *
   * @param pOpenCLLibrary OpenCL library access
   * @param device         The device
   * @param paramName      The parameter name
   * @return The value
   */
  public static long getLong(OpenCLLibrary pOpenCLLibrary, OpenCLLibrary.cl_device_id device, int paramName)
  {
    return getLongs(pOpenCLLibrary, device, paramName, 1)[0];
  }

  /**
   * Returns the values of the device info parameter with the given name
   *
   * @param pOpenCLLibrary OpenCL library access
   * @param device         The device
   * @param paramName      The parameter name
   * @param numValues      The number of values
   * @return The value
   */
  public static Long[] getLongs(OpenCLLibrary pOpenCLLibrary, OpenCLLibrary.cl_device_id device, int paramName, int numValues)
  {
    Pointer<Long> lValues = Pointer.allocateLongs(numValues);
    BackendUtils.checkOpenCLError(pOpenCLLibrary.clGetDeviceInfo(device, paramName, SizeOf.cl_long * numValues, lValues, null));
    return lValues.toArray();
  }

  /**
   * Returns the value of the device info parameter with the given name
   *
   * @param pOpenCLLibrary OpenCL library access
   * @param pDevice        The device
   * @param pParamName     The parameter name
   * @return The value
   */
  public static String getString(OpenCLLibrary pOpenCLLibrary, OpenCLLibrary.cl_device_id pDevice, int pParamName)
  {
    // Obtain the length of the string that will be queried
    Pointer<SizeT> lSize = Pointer.allocateSizeT();
    BackendUtils.checkOpenCLError(pOpenCLLibrary.clGetDeviceInfo(pDevice, pParamName, 0, null, lSize));

    // Create a buffer of the appropriate size and fill it with the info
    long lLength = lSize.getSizeT();
    Pointer<Character> lBuffer = Pointer.allocateChars(lLength);
    BackendUtils.checkOpenCLError(pOpenCLLibrary.clGetDeviceInfo(pDevice, pParamName, lLength, lBuffer, null));

    // Create a string from the buffer (excluding the trailing \0 byte)
    return lBuffer.getString(StringType.C);
  }

  /**
   * Returns the value of the platform info parameter with the given name
   *
   * @param pOpenCLLibrary OpenCL library access
   * @param platform       The platform
   * @param paramName      The parameter name
   * @return The value
   */
  public static String getString(OpenCLLibrary pOpenCLLibrary, OpenCLLibrary.cl_platform_id platform, int paramName)
  {
    // Obtain the length of the string that will be queried
    Pointer<SizeT> lSizeT = Pointer.allocateSizeT();
    BackendUtils.checkOpenCLError(pOpenCLLibrary.clGetPlatformInfo(platform, paramName, 0, null, lSizeT));

    // Create a buffer of the appropriate size and fill it with the info
    long lLength = lSizeT.getSizeT();
    Pointer<Character> lBuffer = Pointer.allocateChars(lLength);
    BackendUtils.checkOpenCLError(pOpenCLLibrary.clGetPlatformInfo(platform, paramName, lLength, lBuffer, null));

    // Create a string from the buffer (excluding the trailing \0 byte)
    return lBuffer.getString(StringType.C);
  }

  /**
   * Returns the value of the device info parameter with the given name
   *
   * @param pOpenCLLibrary OpenCL library access
   * @param device         The device
   * @param paramName      The parameter name
   * @return The value
   */
  public static long getSize(OpenCLLibrary pOpenCLLibrary, OpenCLLibrary.cl_device_id device, int paramName)
  {
    return getSizes(pOpenCLLibrary, device, paramName, 1)[0];
  }

  /**
   * Returns the values of the device info parameter with the given name
   *
   * @param pOpenCLLibrary OpenCL library access
   * @param device         The device
   * @param paramName      The parameter name
   * @param numValues      The number of values
   * @return The value
   */
  @SuppressWarnings("deprecation")
  public static long[] getSizes(OpenCLLibrary pOpenCLLibrary, OpenCLLibrary.cl_device_id device, int paramName, int numValues)
  {
    // The size of the returned data has to depend on
    // the size of a size_t, which is handled here
    Pointer<Byte> lBuffer = Pointer.allocateBytes(numValues * SizeOf.size_t);
    BackendUtils.checkOpenCLError(pOpenCLLibrary.clGetDeviceInfo(device, paramName, SizeOf.size_t * numValues, lBuffer, null));
    long values[] = new long[numValues];
    if (SizeOf.size_t == 4)
    {
      for (int i = 0; i < numValues; i++)
      {
        values[i] = lBuffer.getIntAtOffset(i * SizeOf.size_t);
      }
    } else
    {
      for (int i = 0; i < numValues; i++)
      {
        values[i] = lBuffer.getLongAtOffset(i * SizeOf.size_t);
      }
    }
    return values;
  }

  /**
   * Returns boolean property for device
   *
   * @param pOpenCLLibrary OpenCL library access
   * @param pPointer       device pointer
   * @param pParam         parameter
   * @return boolean
   */
  public static boolean getBoolean(OpenCLLibrary pOpenCLLibrary, OpenCLLibrary.cl_device_id pPointer, int pParam)
  {
    return getInt(pOpenCLLibrary, pPointer, pParam) > 0;
  }

  /**
   * Converts device pointers from peer pointers to backend specific pointers,
   *
   * @param pDevicePointers device pointers
   * @return array of backend specific device pointers
   */
  public static Pointer<OpenCLLibrary.cl_device_id> convertDevicePointers(ClearCLPeerPointer... pDevicePointers)
  {
    Pointer<OpenCLLibrary.cl_device_id> lDevicesArrayPointer = Pointer.allocateArray(OpenCLLibrary.cl_device_id.class, pDevicePointers.length);

    for (int i = 0; i < pDevicePointers.length; i++)
      lDevicesArrayPointer.set(i, (OpenCLLibrary.cl_device_id) pDevicePointers[i].getPointer());

    return lDevicesArrayPointer;

  }

  /**
   * @param pOpenCLLibrary        OpenCL library access
   * @param pPlatform             platform
   * @param pContextPropertiesMap context properties map
   * @return context properties
   */
  public static long[] getContextProps(OpenCLLibrary pOpenCLLibrary, cl_platform_id pPlatform, Map<ContextProperties, Object> pContextPropertiesMap)
  {
    int nContextProperties = pContextPropertiesMap == null ? 0 : pContextPropertiesMap.size();
    final long[] properties = new long[(nContextProperties + 1) * 2 + 1];
    properties[0] = CL_CONTEXT_PLATFORM;
    properties[1] = pPlatform.getPeer();
    int iProp = 2;
    if (nContextProperties != 0)
    {
      for (Map.Entry<ContextProperties, Object> e : pContextPropertiesMap.entrySet())
      {
        // if (!(v instanceof Number)) throw new
        // IllegalArgumentException("Invalid context property value for '" +
        // e.getKey() + ": " + v);
        properties[iProp++] = e.getKey().value();
        Object v = e.getValue();
        if (v instanceof Number) properties[iProp++] = ((Number) v).longValue();
        else if (v instanceof Pointer) properties[iProp++] = ((Pointer<?>) v).getPeer();
        else throw new IllegalArgumentException("Cannot convert value " + v + " to a context property value !");
      }
    }
    // properties[iProp] = 0;
    return properties;
  }

  /**
   * Checks if a NIO buffer is direct - which is a requirement for this backend.
   *
   * @param pPeerPointer pointer to check
   */
  public static void checkDirectNIOBuffer(ClearCLPeerPointer pPeerPointer)
  {
    Object lPointer = pPeerPointer.getPointer();
    if (!(lPointer instanceof Buffer)) return;

    Buffer lBuffer = (Buffer) lPointer;
    if (!lBuffer.isDirect()) throw new ClearCLUnsupportedException("Only direct NIO buffers supported!");

    if (lBuffer instanceof CharBuffer)
    {
      CharBuffer lCharBuffer = (CharBuffer) lBuffer;
      if (lCharBuffer.order() == ByteOrder.nativeOrder()) return;
    } else if (lBuffer instanceof ShortBuffer)
    {
      ShortBuffer lShortBuffer = (ShortBuffer) lBuffer;
      if (lShortBuffer.order() == ByteOrder.nativeOrder()) return;
    } else if (lBuffer instanceof IntBuffer)
    {
      IntBuffer lIntBuffer = (IntBuffer) lBuffer;
      if (lIntBuffer.order() == ByteOrder.nativeOrder()) return;
    } else if (lBuffer instanceof LongBuffer)
    {
      LongBuffer lLongBuffer = (LongBuffer) lBuffer;
      if (lLongBuffer.order() == ByteOrder.nativeOrder()) return;
    } else if (lBuffer instanceof FloatBuffer)
    {
      FloatBuffer lFloatBuffer = (FloatBuffer) lBuffer;
      if (lFloatBuffer.order() == ByteOrder.nativeOrder()) return;
    } else if (lBuffer instanceof DoubleBuffer)
    {
      DoubleBuffer lDoubleBuffer = (DoubleBuffer) lBuffer;
      if (lDoubleBuffer.order() == ByteOrder.nativeOrder()) return;
    }

    throw new ClearCLUnsupportedException("Only direct NIO buffers with _native_ order supported!");
  }

  // public static <N extends Pointer<?>> N checkNullReturn(N pPointer)
  // {
  // if(pPointer.getPeer()==0)
  // throw new ClearCLNullPointerException();
  // return pPointer;
  // }

}
