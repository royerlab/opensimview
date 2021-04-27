package clearcl.backend.javacl;

import static org.bridj.Pointer.pointerToSizeTs;

import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLImage;
import clearcl.ClearCLLocalMemory;
import clearcl.ClearCLPeerPointer;
import clearcl.backend.BackendUtils;
import clearcl.backend.ClearCLBackendBase;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.backend.SizeOf;
import clearcl.enums.BuildStatus;
import clearcl.enums.DeviceType;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.ImageType;
import clearcl.enums.KernelAccessType;
import clearcl.enums.MemAllocMode;
import clearcl.exceptions.ClearCLUnsupportedException;
import clearcl.exceptions.OpenCLException;

import com.nativelibs4java.opencl.CLPlatform.ContextProperties;
import com.nativelibs4java.opencl.library.IOpenCLLibrary;
import com.nativelibs4java.opencl.library.OpenCLLibrary;
import com.nativelibs4java.opencl.library.cl_image_desc;
import com.nativelibs4java.opencl.library.cl_image_format;

import coremem.ContiguousMemoryInterface;
import coremem.fragmented.FragmentedMemoryInterface;
import coremem.util.Size;

import org.bridj.Pointer;
import org.bridj.Pointer.StringType;
import org.bridj.SizeT;

/**
 * CLearCL JOCL backend. Uses the JavaCL library to access OpenCL functions.
 *
 * @author royer
 */
public class ClearCLBackendJavaCL extends ClearCLBackendBase
                                  implements ClearCLBackendInterface
{

  private OpenCLLibrary mOpenCLLibrary;

  private ThreadLocal<Pointer<?>> mTempPointerThreadLocal =
                                                          new ThreadLocal<>();

  /**
   * Instanciates a JavaCL backend with no debug functionality
   */
  public ClearCLBackendJavaCL()
  {
    this(false);
  }

  /**
   * Instanciates a JavaCL backend with debug functionality
   * 
   * @param pDebug
   *          true -> debug theoretically possible using GDB on certain
   *          platforms
   */
  public ClearCLBackendJavaCL(boolean pDebug)
  {
    super();
    mOpenCLLibrary = Utils.initCL(pDebug);
  }

  @Override
  public int getNumberOfPlatforms()
  {
    return BackendUtils.checkExceptions(() -> {
      Pointer<Integer> lNumPlatformsPointer = Pointer.allocateInt();
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clGetPlatformIDs(0,
                                                                    null,
                                                                    lNumPlatformsPointer));
      int lNumberOfPlatforms = lNumPlatformsPointer.getInt();
      lNumPlatformsPointer.release();
      return lNumberOfPlatforms;
    });
  }

  @Override
  public ClearCLPeerPointer getPlatformPeerPointer(int pPlatformIndex)
  {
    return BackendUtils.checkExceptions(() -> {
      int lNumberOfPlatforms = getNumberOfPlatforms();
      Pointer<OpenCLLibrary.cl_platform_id> lPlatformsList =
                                                           Pointer.allocateArray(OpenCLLibrary.cl_platform_id.class,
                                                                                 lNumberOfPlatforms);
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clGetPlatformIDs(lNumberOfPlatforms,
                                                                    lPlatformsList,
                                                                    null));
      OpenCLLibrary.cl_platform_id lPlatform =
                                             lPlatformsList.get(pPlatformIndex);
      return new ClearCLPeerPointer(lPlatform);
    });
  }

  @Override
  public int getNumberOfDevicesForPlatform(ClearCLPeerPointer pPlatformPointer,
                                           DeviceType pDeviceType)
  {
    return BackendUtils.checkExceptions(() -> {
      long lDeviceType = 0;
      if (pDeviceType == DeviceType.CPU)
        lDeviceType = IOpenCLLibrary.CL_DEVICE_TYPE_CPU;
      else if (pDeviceType == DeviceType.GPU)
        lDeviceType = IOpenCLLibrary.CL_DEVICE_TYPE_GPU;

      return getNumberOfDevicesForPlatform(pPlatformPointer,
                                           lDeviceType);
    });
  }

  @Override
  public int getNumberOfDevicesForPlatform(ClearCLPeerPointer pPlatformPointer)
  {
    return BackendUtils.checkExceptions(() -> {
      return getNumberOfDevicesForPlatform(pPlatformPointer,
                                           IOpenCLLibrary.CL_DEVICE_TYPE_ALL);
    });
  }

  private int getNumberOfDevicesForPlatform(ClearCLPeerPointer pPlatformPointer,
                                            long pDeviceType)
  {
    return BackendUtils.checkExceptions(() -> {
      Pointer<Integer> lNumDevicesArray = Pointer.allocateInt();
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clGetDeviceIDs((OpenCLLibrary.cl_platform_id) (pPlatformPointer.getPointer()),
                                                                  pDeviceType,
                                                                  0,
                                                                  null,
                                                                  lNumDevicesArray));
      int numDevices = lNumDevicesArray.getInt();
      lNumDevicesArray.release();
      return numDevices;
    });
  }

  @Override
  public ClearCLPeerPointer getDevicePeerPointer(ClearCLPeerPointer pPlatformPointer,
                                                 DeviceType pDeviceType,
                                                 int pDeviceIndex)
  {
    return BackendUtils.checkExceptions(() -> {
      long lDeviceType = 0;
      if (pDeviceType == DeviceType.CPU)
        lDeviceType = IOpenCLLibrary.CL_DEVICE_TYPE_CPU;
      else if (pDeviceType == DeviceType.GPU)
        lDeviceType = IOpenCLLibrary.CL_DEVICE_TYPE_CPU;

      return getDeviceId(pPlatformPointer, lDeviceType, pDeviceIndex);
    });
  }

  @Override
  public ClearCLPeerPointer getDevicePeerPointer(ClearCLPeerPointer pPlatformPointer,
                                                 int pDeviceIndex)
  {
    return getDeviceId(pPlatformPointer,
                       IOpenCLLibrary.CL_DEVICE_TYPE_ALL,
                       pDeviceIndex);
  }

  private ClearCLPeerPointer getDeviceId(ClearCLPeerPointer pPlatformPointer,
                                         long pDeviceType,
                                         int pDeviceIndex)
  {
    return BackendUtils.checkExceptions(() -> {
      int lNumberOfDevicesForPlatform =
                                      getNumberOfDevicesForPlatform(pPlatformPointer);
      Pointer<OpenCLLibrary.cl_device_id> lDeviceIdArray =
                                                         Pointer.allocateArray(OpenCLLibrary.cl_device_id.class,
                                                                               lNumberOfDevicesForPlatform);
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clGetDeviceIDs((OpenCLLibrary.cl_platform_id) pPlatformPointer.getPointer(),
                                                                  pDeviceType,
                                                                  lNumberOfDevicesForPlatform,
                                                                  lDeviceIdArray,
                                                                  null));
      OpenCLLibrary.cl_device_id device =
                                        lDeviceIdArray.get(pDeviceIndex);
      return new ClearCLPeerPointer(device);
    });
  }

  @Override
  public String getPlatformName(ClearCLPeerPointer pPlatformPointer)
  {
    return getPlatformInfo(pPlatformPointer,
                           IOpenCLLibrary.CL_PLATFORM_NAME);
  }

  private String getPlatformInfo(ClearCLPeerPointer pPlatformPointer,
                                 int pInfoId)
  {
    return BackendUtils.checkExceptions(() -> {
      return Utils.getString(mOpenCLLibrary,
                             (OpenCLLibrary.cl_platform_id) pPlatformPointer.getPointer(),
                             pInfoId);
    });
  }

  @Override
  public String getDeviceName(ClearCLPeerPointer pDevicePointer)
  {
    return getDeviceInfo(pDevicePointer,
                         IOpenCLLibrary.CL_DEVICE_NAME);
  }

  @Override
  public DeviceType getDeviceType(ClearCLPeerPointer pDevicePointer)
  {
    return BackendUtils.checkExceptions(() -> {
      long lDeviceType =
                       getDeviceInfoLong(pDevicePointer,
                                         IOpenCLLibrary.CL_DEVICE_TYPE);
      if (lDeviceType == IOpenCLLibrary.CL_DEVICE_TYPE_CPU)
        return DeviceType.CPU;
      else if (lDeviceType == IOpenCLLibrary.CL_DEVICE_TYPE_GPU)
        return DeviceType.GPU;
      else
        return DeviceType.OTHER;
    });
  }

  @Override
  public String getDeviceVersion(ClearCLPeerPointer pDevicePointer)
  {
    return getDeviceInfo(pDevicePointer,
                         IOpenCLLibrary.CL_DEVICE_OPENCL_C_VERSION);
  }

  @Override
  public String getDeviceExtensions(ClearCLPeerPointer pDevicePointer)
  {
    return getDeviceInfo(pDevicePointer,
                         IOpenCLLibrary.CL_DEVICE_EXTENSIONS);
  }

  private String getDeviceInfo(ClearCLPeerPointer pDevicePointer,
                               int pInfoId)
  {
    return BackendUtils.checkExceptions(() -> {
      return Utils.getString(mOpenCLLibrary,
                             (OpenCLLibrary.cl_device_id) pDevicePointer.getPointer(),
                             pInfoId);
    });
  }

  @Override
  public long getDeviceInfoLong(ClearCLPeerPointer pPointer,
                                int pInfoId)
  {
    return BackendUtils.checkExceptions(() -> {
      return Utils.getLong(mOpenCLLibrary,
                           (OpenCLLibrary.cl_device_id) pPointer.getPointer(),
                           pInfoId);
    });
  }

  @Override
  public long getDeviceInfoInt(ClearCLPeerPointer pPointer,
                               int pInfoId)
  {
    return BackendUtils.checkExceptions(() -> {
      return Utils.getInt(mOpenCLLibrary,
                          (OpenCLLibrary.cl_device_id) pPointer.getPointer(),
                          pInfoId);
    });
  }

  @Override
  public ClearCLPeerPointer getContextPeerPointer(ClearCLPeerPointer pPlatformPointer,
                                                  ClearCLPeerPointer... pDevicePointers)
  {
    return BackendUtils.checkExceptions(() -> {

      OpenCLLibrary.cl_platform_id lPlatform =
                                             (OpenCLLibrary.cl_platform_id) pPlatformPointer.getPointer();

      // Initialize the context properties
      Map<ContextProperties, Object> lContextProperties =
                                                        new HashMap<>();
      lContextProperties.put(ContextProperties.Platform, lPlatform);

      long[] props = Utils.getContextProps(mOpenCLLibrary,
                                           lPlatform,
                                           lContextProperties);
      Pointer<SizeT> lContextPropertiesPointer =
                                               props == null ? null
                                                             : pointerToSizeTs(props);

      Pointer<OpenCLLibrary.cl_device_id> lDevicesArrayPointer =
                                                               Utils.convertDevicePointers(pDevicePointers);

      Pointer<Integer> lErrorCode = Pointer.allocateInt();

      OpenCLLibrary.cl_context context =
                                       mOpenCLLibrary.clCreateContext(lContextPropertiesPointer,
                                                                      pDevicePointers.length,
                                                                      lDevicesArrayPointer,
                                                                      null,
                                                                      null,
                                                                      lErrorCode);

      BackendUtils.checkOpenCLErrorCode(lErrorCode.get());

      return new ClearCLPeerPointer(context);
    });
  }

  @Override
  public ClearCLPeerPointer getQueuePeerPointer(ClearCLPeerPointer pDevicePointer,
                                                ClearCLPeerPointer pContextPointer,
                                                boolean pInOrder)
  {
    return BackendUtils.checkExceptions(() -> {
      Pointer<Integer> lErrorCode = Pointer.allocateInt();

      OpenCLLibrary.cl_command_queue commandQueue =
                                                  mOpenCLLibrary.clCreateCommandQueue((OpenCLLibrary.cl_context) pContextPointer.getPointer(),
                                                                                      (OpenCLLibrary.cl_device_id) pDevicePointer.getPointer(),
                                                                                      pInOrder ? 0
                                                                                               : IOpenCLLibrary.CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE,
                                                                                      lErrorCode);

      BackendUtils.checkOpenCLErrorCode(lErrorCode.get());

      ClearCLPeerPointer lCommandQueuePointer =
                                              new ClearCLPeerPointer(commandQueue);

      return lCommandQueuePointer;
    });
  }

  @Override
  public ClearCLPeerPointer getBufferPeerPointer(ClearCLPeerPointer pDevicePointer,
                                                 ClearCLPeerPointer pContextPointer,
                                                 MemAllocMode pMemAllocMode,
                                                 HostAccessType pHostAccessType,
                                                 KernelAccessType pKernelAccessType,
                                                 long pBufferSize)
  {
    return BackendUtils.checkExceptions(() -> {

      String lDeviceVersion = getDeviceVersion(pDevicePointer);

      boolean lOpenCL1p1o0 = (lDeviceVersion.contains("1.0")
                              || lDeviceVersion.contains("1.1"));

      long lMemFlags =
                     BackendUtils.getMemTypeFlags(pMemAllocMode,
                                                  pHostAccessType,
                                                  lOpenCL1p1o0 ? KernelAccessType.Undefined
                                                               : pKernelAccessType);

      Pointer<Integer> lErrorCode = Pointer.allocateInt();

      OpenCLLibrary.cl_mem lBufferPointer = null;
      try
      {
        lBufferPointer =
                       mOpenCLLibrary.clCreateBuffer((OpenCLLibrary.cl_context) pContextPointer.getPointer(),
                                                     lMemFlags,
                                                     pBufferSize,
                                                     null,
                                                     lErrorCode);
      }
      catch (IllegalArgumentException e)
      {
        if (e.getMessage()
             .contains("Pointer instance cannot have NULL peer"))
          throw new OpenCLException(-61);
      }

      BackendUtils.checkOpenCLErrorCode(lErrorCode.get());

      ClearCLPeerPointer lClearCLPointer =
                                         new ClearCLPeerPointer(lBufferPointer);
      return lClearCLPointer;
    });
  }

  @Override
  public ClearCLPeerPointer getImagePeerPointer(ClearCLPeerPointer pDevicePointer,
                                                ClearCLPeerPointer pContextPointer,
                                                MemAllocMode pMemAllocMode,
                                                HostAccessType pHostAccessType,
                                                KernelAccessType pKernelAccessType,
                                                ImageType pImageType,
                                                ImageChannelOrder pImageChannelOrder,
                                                ImageChannelDataType pImageChannelDataType,
                                                long... pDimensions)
  {
    String lDeviceVersion = getDeviceVersion(pDevicePointer);

    if (lDeviceVersion.contains("1.0")
        || lDeviceVersion.contains("1.1"))
    {

      cl_image_format lImageFormat = new cl_image_format();
      lImageFormat.image_channel_order(BackendUtils.getImageChannelOrderFlags(pImageChannelOrder));
      lImageFormat.image_channel_data_type(BackendUtils.getImageChannelDataTypeFlags(pImageChannelDataType));

      long image_width = (pDimensions[0]);
      long image_height =
                        (pDimensions.length < 2 ? 1 : pDimensions[1]);
      long image_depth =
                       (pDimensions.length < 3 ? 1 : pDimensions[2]);

      long lMemFlags =
                     BackendUtils.getMemTypeFlags(pMemAllocMode,
                                                  pHostAccessType,
                                                  pKernelAccessType);

      Pointer<Integer> lErrorCode = Pointer.allocateInt();

      OpenCLLibrary.cl_mem lImageMem = null;
      try
      {
        if (pDimensions.length <= 2)
        {
          lImageMem =
                    mOpenCLLibrary.clCreateImage2D((OpenCLLibrary.cl_context) pContextPointer.getPointer(),
                                                   lMemFlags,
                                                   Pointer.getPointer(lImageFormat),
                                                   image_width,
                                                   image_height,
                                                   0,
                                                   null,
                                                   lErrorCode);
        }
        else if (pDimensions.length == 3)
        {
          lImageMem =
                    mOpenCLLibrary.clCreateImage3D((OpenCLLibrary.cl_context) pContextPointer.getPointer(),
                                                   lMemFlags,
                                                   Pointer.getPointer(lImageFormat),
                                                   image_width,
                                                   image_height,
                                                   image_depth,
                                                   0,
                                                   0,
                                                   null,
                                                   lErrorCode);
        }
      }
      catch (IllegalArgumentException e)
      {
        if (e.getMessage()
             .contains("Pointer instance cannot have NULL peer"))
          throw new OpenCLException(-6);
      }

      BackendUtils.checkOpenCLErrorCode(lErrorCode.get());

      ClearCLPeerPointer lClearCLPeerPointer =
                                             new ClearCLPeerPointer(lImageMem);

      return lClearCLPeerPointer;
    }
    else if (lDeviceVersion.contains("1.2"))
    {

      cl_image_format lImageFormat = new cl_image_format();
      lImageFormat.image_channel_order(BackendUtils.getImageChannelOrderFlags(pImageChannelOrder));
      lImageFormat.image_channel_data_type(BackendUtils.getImageChannelDataTypeFlags(pImageChannelDataType));

      cl_image_desc lImageDescription = new cl_image_desc();
      lImageDescription.image_width(pDimensions[0]);
      lImageDescription.image_height(pDimensions.length < 2 ? 1
                                                            : pDimensions[1]);
      lImageDescription.image_depth(pDimensions.length < 3 ? 1
                                                           : pDimensions[2]);
      lImageDescription.image_type(BackendUtils.getImageTypeFlags(pImageType));

      long lMemFlags =
                     BackendUtils.getMemTypeFlags(pMemAllocMode,
                                                  pHostAccessType,
                                                  pKernelAccessType);

      Pointer<Integer> lErrorCode = Pointer.allocateInt();

      OpenCLLibrary.cl_mem lImageMem = null;
      try
      {
        lImageMem =
                  mOpenCLLibrary.clCreateImage((OpenCLLibrary.cl_context) pContextPointer.getPointer(),
                                               lMemFlags,
                                               Pointer.getPointer(lImageFormat),
                                               Pointer.getPointer(lImageDescription),
                                               null,
                                               lErrorCode);
      }
      catch (IllegalArgumentException e)
      {
        if (e.getMessage()
             .contains("Pointer instance cannot have NULL peer"))
          throw new OpenCLException(-6);
      }

      BackendUtils.checkOpenCLErrorCode(lErrorCode.get());

      ClearCLPeerPointer lClearCLPeerPointer =
                                             new ClearCLPeerPointer(lImageMem);

      return lClearCLPeerPointer;
    }
    return null;
  }

  @Override
  public ClearCLPeerPointer getProgramPeerPointer(ClearCLPeerPointer pContextPointer,
                                                  String... pSourceCode)
  {
    return BackendUtils.checkExceptions(() -> {

      Pointer<Pointer<Byte>> lStringPointers =
                                             Pointer.pointerToCStrings(pSourceCode);

      Pointer<Integer> lErrorCode = Pointer.allocateInt();

      OpenCLLibrary.cl_program program =
                                       mOpenCLLibrary.clCreateProgramWithSource((OpenCLLibrary.cl_context) pContextPointer.getPointer(),
                                                                                pSourceCode.length,
                                                                                lStringPointers,
                                                                                null,
                                                                                lErrorCode);
      BackendUtils.checkOpenCLErrorCode(lErrorCode.get());

      ClearCLPeerPointer lClearCLPointer =
                                         new ClearCLPeerPointer(program);
      return lClearCLPointer;
    });
  }

  @Override
  public boolean buildProgram(ClearCLPeerPointer pProgramPointer,
                              String pOptions)
  {
    return BackendUtils.checkExceptions(() -> {

      String lOptions = (pOptions == null
                         || pOptions.isEmpty()) ? null : pOptions;
      Pointer<Byte> lOptionsStringPointers =
                                           Pointer.pointerToCString(lOptions);

      int lError =
                 mOpenCLLibrary.clBuildProgram((OpenCLLibrary.cl_program) pProgramPointer.getPointer(),
                                               0,
                                               null,
                                               lOptionsStringPointers,
                                               null,
                                               null);

      return lError == 0;
    });
  }

  @Override
  public BuildStatus getBuildStatus(ClearCLPeerPointer pDevicePointer,
                                    ClearCLPeerPointer pProgramPointer)
  {
    return BackendUtils.checkExceptions(() -> {
      Pointer<Integer> lStatusPointer = Pointer.allocateInt();
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clGetProgramBuildInfo((OpenCLLibrary.cl_program) pProgramPointer.getPointer(),
                                                                         (OpenCLLibrary.cl_device_id) pDevicePointer.getPointer(),
                                                                         IOpenCLLibrary.CL_PROGRAM_BUILD_STATUS,
                                                                         1 * SizeOf.cl_int,
                                                                         lStatusPointer,
                                                                         null));

      BuildStatus lBuildStatus = null;

      switch (lStatusPointer.getInt())
      {
      case IOpenCLLibrary.CL_BUILD_NONE:
        lBuildStatus = BuildStatus.None;
        break;
      case IOpenCLLibrary.CL_BUILD_ERROR:
        lBuildStatus = BuildStatus.Error;
        break;
      case IOpenCLLibrary.CL_BUILD_SUCCESS:
        lBuildStatus = BuildStatus.Success;
        break;
      case IOpenCLLibrary.CL_BUILD_IN_PROGRESS:
        lBuildStatus = BuildStatus.InProgress;
        break;
      }

      return lBuildStatus;
    });
  }

  @Override
  public String getBuildLog(ClearCLPeerPointer pDevicePointer,
                            ClearCLPeerPointer pProgramPointer)
  {
    return getBuildProgramInfo(pDevicePointer,
                               pProgramPointer,
                               IOpenCLLibrary.CL_PROGRAM_BUILD_LOG);
  }

  private String getBuildProgramInfo(ClearCLPeerPointer pDevicePointer,
                                     ClearCLPeerPointer pProgramPointer,
                                     int pInfoName)
  {
    return BackendUtils.checkExceptions(() -> {
      // Obtain the length of the string that will be queried
      Pointer<SizeT> lSizePointer = Pointer.allocateSizeT();
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clGetProgramBuildInfo((OpenCLLibrary.cl_program) pProgramPointer.getPointer(),
                                                                         (OpenCLLibrary.cl_device_id) pDevicePointer.getPointer(),
                                                                         pInfoName,
                                                                         0,
                                                                         null,
                                                                         lSizePointer));

      // Create a buffer of the appropriate size and fill it with the info
      long lSize = lSizePointer.getSizeT();
      Pointer<Byte> lBufferPointer = Pointer.allocateBytes(lSize);
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clGetProgramBuildInfo((OpenCLLibrary.cl_program) pProgramPointer.getPointer(),
                                                                         (OpenCLLibrary.cl_device_id) pDevicePointer.getPointer(),
                                                                         pInfoName,
                                                                         lSize,
                                                                         lBufferPointer,
                                                                         null));

      // Create a string from the buffer (excluding the trailing \0 byte)
      return lBufferPointer.getString(StringType.C);
    });

  }

  @Override
  public ClearCLPeerPointer getKernelPeerPointer(ClearCLPeerPointer pProgramPointer,
                                                 String pKernelName)
  {
    return BackendUtils.checkExceptions(() -> {

      Pointer<Integer> lErrorCode = Pointer.allocateInt();

      OpenCLLibrary.cl_kernel kernel =
                                     mOpenCLLibrary.clCreateKernel((OpenCLLibrary.cl_program) pProgramPointer.getPointer(),
                                                                   Pointer.pointerToCString(pKernelName),
                                                                   lErrorCode);
      BackendUtils.checkOpenCLErrorCode(lErrorCode.get());

      ClearCLPeerPointer lClearCLPointer =
                                         new ClearCLPeerPointer(kernel);
      return lClearCLPointer;
    });
  }

  @SuppressWarnings(
  { "deprecation", "unchecked" })
  @Override
  public void setKernelArgument(ClearCLPeerPointer pKernelPeerPointer,
                                int pIndex,
                                Object pObject)
  {
    BackendUtils.checkExceptions(() -> {

      Pointer<?> lTempPointer = mTempPointerThreadLocal.get();

      if (lTempPointer == null)
      {

        lTempPointer = Pointer.allocateBytes(1024)
                              .withoutValidityInformation();
        mTempPointerThreadLocal.set(lTempPointer);
      }

      OpenCLLibrary.cl_kernel lKernelPointer =
                                             (OpenCLLibrary.cl_kernel) pKernelPeerPointer.getPointer();

      long lObjectSize = Size.of(pObject);

      // PRIMITIVE TYPES
      if (pObject instanceof Byte)

        BackendUtils.checkOpenCLError(mOpenCLLibrary.clSetKernelArg(lKernelPointer,
                                                                    pIndex,
                                                                    lObjectSize,
                                                                    lTempPointer.setByte((byte) pObject)));
      else if (pObject instanceof Character)
        BackendUtils.checkOpenCLError(mOpenCLLibrary.clSetKernelArg(lKernelPointer,
                                                                    pIndex,
                                                                    lObjectSize,
                                                                    lTempPointer.setChar((char) pObject)));
      else if (pObject instanceof Short)
        BackendUtils.checkOpenCLError(mOpenCLLibrary.clSetKernelArg(lKernelPointer,
                                                                    pIndex,
                                                                    lObjectSize,
                                                                    lTempPointer.setShort((short) pObject)));
      else if (pObject instanceof Integer)
        BackendUtils.checkOpenCLError(mOpenCLLibrary.clSetKernelArg(lKernelPointer,
                                                                    pIndex,
                                                                    lObjectSize,
                                                                    lTempPointer.setInt((int) pObject)));
      else if (pObject instanceof Long)
        BackendUtils.checkOpenCLError(mOpenCLLibrary.clSetKernelArg(lKernelPointer,
                                                                    pIndex,
                                                                    lObjectSize,
                                                                    lTempPointer.setLong((long) pObject)));
      else if (pObject instanceof Float)
        BackendUtils.checkOpenCLError(mOpenCLLibrary.clSetKernelArg(lKernelPointer,
                                                                    pIndex,
                                                                    lObjectSize,
                                                                    lTempPointer.setFloat((float) pObject)));
      else if (pObject instanceof Double)
        BackendUtils.checkOpenCLError(mOpenCLLibrary.clSetKernelArg(lKernelPointer,
                                                                    pIndex,
                                                                    lObjectSize,
                                                                    lTempPointer.setDouble((double) pObject)));
      // ARRAY TYPES
      else if (pObject instanceof byte[])
        BackendUtils.checkOpenCLError(mOpenCLLibrary.clSetKernelArg(lKernelPointer,
                                                                    pIndex,
                                                                    lObjectSize,
                                                                    Pointer.pointerToBytes((byte[]) pObject)));
      else if (pObject instanceof char[])
        BackendUtils.checkOpenCLError(mOpenCLLibrary.clSetKernelArg(lKernelPointer,
                                                                    pIndex,
                                                                    lObjectSize,
                                                                    Pointer.pointerToChars((char[]) pObject)));
      else if (pObject instanceof short[])
        BackendUtils.checkOpenCLError(mOpenCLLibrary.clSetKernelArg(lKernelPointer,
                                                                    pIndex,
                                                                    lObjectSize,
                                                                    Pointer.pointerToShorts((short[]) pObject)));
      else if (pObject instanceof int[])
        BackendUtils.checkOpenCLError(mOpenCLLibrary.clSetKernelArg(lKernelPointer,
                                                                    pIndex,
                                                                    lObjectSize,
                                                                    Pointer.pointerToInts((int[]) pObject)));
      else if (pObject instanceof long[])
        BackendUtils.checkOpenCLError(mOpenCLLibrary.clSetKernelArg(lKernelPointer,
                                                                    pIndex,
                                                                    lObjectSize,
                                                                    Pointer.pointerToLongs((long[]) pObject)));
      else if (pObject instanceof float[])
        BackendUtils.checkOpenCLError(mOpenCLLibrary.clSetKernelArg(lKernelPointer,
                                                                    pIndex,
                                                                    lObjectSize,
                                                                    Pointer.pointerToFloats((float[]) pObject)));
      else if (pObject instanceof double[])
        BackendUtils.checkOpenCLError(mOpenCLLibrary.clSetKernelArg(lKernelPointer,
                                                                    pIndex,
                                                                    lObjectSize,
                                                                    Pointer.pointerToDoubles((double[]) pObject)));
      else if (pObject instanceof ClearCLBuffer)
      {
        ClearCLBuffer lClearCLBuffer = (ClearCLBuffer) pObject;
        OpenCLLibrary.cl_mem lCLMem =
                                    (OpenCLLibrary.cl_mem) lClearCLBuffer.getPeerPointer()
                                                                         .getPointer();

        BackendUtils.checkOpenCLError(mOpenCLLibrary.clSetKernelArg(lKernelPointer,
                                                                    pIndex,
                                                                    SizeOf.cl_mem,
                                                                    Pointer.pointerToPointer(lCLMem)));
      }
      else if (pObject instanceof ClearCLImage)
      {
        ClearCLImage lClearCLImage = (ClearCLImage) pObject;
        OpenCLLibrary.cl_mem lCLmem =
                                    (OpenCLLibrary.cl_mem) lClearCLImage.getPeerPointer()
                                                                        .getPointer();
        BackendUtils.checkOpenCLError(mOpenCLLibrary.clSetKernelArg(lKernelPointer,
                                                                    pIndex,
                                                                    SizeOf.cl_mem,
                                                                    Pointer.pointerToPointer(lCLmem)));
      }
      else if (pObject instanceof ClearCLLocalMemory)
      {
        ClearCLLocalMemory lClearCLLocalMemory =
                                               (ClearCLLocalMemory) pObject;
        BackendUtils.checkOpenCLError(mOpenCLLibrary.clSetKernelArg(lKernelPointer,
                                                                    pIndex,
                                                                    lClearCLLocalMemory.getSizeInBytes(),
                                                                    Pointer.NULL));
      }

    });

  }

  @Override
  public void enqueueKernelExecution(ClearCLPeerPointer pQueuePointer,
                                     ClearCLPeerPointer pKernelPointer,
                                     int pNumberOfDimension,
                                     long[] pGlobalOffsets,
                                     long[] pGlobalSizes,
                                     long[] pLocalSizes)
  {

    BackendUtils.checkExceptions(() -> {

      BackendUtils.checkOpenCLError(mOpenCLLibrary.clEnqueueNDRangeKernel((OpenCLLibrary.cl_command_queue) pQueuePointer.getPointer(),
                                                                          (OpenCLLibrary.cl_kernel) pKernelPointer.getPointer(),
                                                                          pNumberOfDimension,
                                                                          Pointer.pointerToSizeTs(pGlobalOffsets),
                                                                          Pointer.pointerToSizeTs(pGlobalSizes),
                                                                          Pointer.pointerToSizeTs(pLocalSizes),
                                                                          0,
                                                                          null,
                                                                          null));
    });

  }

  @SuppressWarnings("rawtypes")
  @Override
  public void enqueueReadFromBuffer(ClearCLPeerPointer pQueuePointer,
                                    ClearCLPeerPointer pBufferPointer,
                                    boolean pBlockingRead,
                                    long pOffsetInBuffer,
                                    long pLengthInBytes,
                                    ClearCLPeerPointer pHostMemPointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clEnqueueReadBuffer((OpenCLLibrary.cl_command_queue) pQueuePointer.getPointer(),
                                                                       (OpenCLLibrary.cl_mem) pBufferPointer.getPointer(),
                                                                       pBlockingRead ? 1
                                                                                     : 0,
                                                                       pOffsetInBuffer,
                                                                       pLengthInBytes,
                                                                       (Pointer) pHostMemPointer.getPointer(),
                                                                       0,
                                                                       null,
                                                                       null));
    });
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void enqueueWriteToBuffer(ClearCLPeerPointer pQueuePointer,
                                   ClearCLPeerPointer pBufferPointer,
                                   boolean pBlockingWrite,
                                   long pOffsetInBuffer,
                                   long pLengthInBytes,
                                   ClearCLPeerPointer pHostMemPointer)
  {
    BackendUtils.checkExceptions(() -> {

      BackendUtils.checkOpenCLError(mOpenCLLibrary.clEnqueueWriteBuffer((OpenCLLibrary.cl_command_queue) pQueuePointer.getPointer(),
                                                                        (OpenCLLibrary.cl_mem) pBufferPointer.getPointer(),
                                                                        pBlockingWrite ? 1
                                                                                       : 0,
                                                                        pOffsetInBuffer,
                                                                        pLengthInBytes,
                                                                        (Pointer) pHostMemPointer.getPointer(),
                                                                        0,
                                                                        null,
                                                                        null));
    });
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void enqueueReadFromBufferRegion(ClearCLPeerPointer pQueuePointer,
                                          ClearCLPeerPointer pBufferPointer,
                                          boolean pBlockingRead,
                                          long[] pBufferOrigin,
                                          long[] pHostOrigin,
                                          long[] pRegion,
                                          ClearCLPeerPointer pHostMemPointer)
  {
    BackendUtils.checkExceptions(() -> {
      Utils.checkDirectNIOBuffer(pHostMemPointer);
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clEnqueueReadBufferRect((OpenCLLibrary.cl_command_queue) pQueuePointer.getPointer(),
                                                                           (OpenCLLibrary.cl_mem) pBufferPointer.getPointer(),
                                                                           pBlockingRead ? 1
                                                                                         : 0,
                                                                           Pointer.pointerToSizeTs(pBufferOrigin),
                                                                           Pointer.pointerToSizeTs(pHostOrigin),
                                                                           Pointer.pointerToSizeTs(pRegion),
                                                                           0,
                                                                           0,
                                                                           0,
                                                                           0,
                                                                           (Pointer) pHostMemPointer.getPointer(),
                                                                           0,
                                                                           null,
                                                                           null));
    });
  }

  @Override
  public void enqueueWriteToBufferRegion(ClearCLPeerPointer pQueuePointer,
                                         ClearCLPeerPointer pBufferPointer,
                                         boolean pBlockingWrite,
                                         long[] pBufferOrigin,
                                         long[] pHostOrigin,
                                         long[] pRegion,
                                         ClearCLPeerPointer pHostMemPointer)
  {
    BackendUtils.checkExceptions(() -> {

      BackendUtils.checkOpenCLError(mOpenCLLibrary.clEnqueueWriteBufferRect((OpenCLLibrary.cl_command_queue) pQueuePointer.getPointer(),
                                                                            (OpenCLLibrary.cl_mem) pBufferPointer.getPointer(),
                                                                            pBlockingWrite ? 1
                                                                                           : 0,
                                                                            Pointer.pointerToSizeTs(pBufferOrigin),
                                                                            Pointer.pointerToSizeTs(pHostOrigin),
                                                                            Pointer.pointerToSizeTs(pRegion),
                                                                            0,
                                                                            0,
                                                                            0,
                                                                            0,
                                                                            (Pointer<?>) pHostMemPointer.getPointer(),
                                                                            0,
                                                                            null,
                                                                            null));
    });
  }

  @Override
  public void enqueueFillBuffer(ClearCLPeerPointer pQueuePointer,
                                ClearCLPeerPointer pBufferPointer,
                                boolean pBlockingFill,
                                long pOffsetInBytes,
                                long pLengthInBytes,
                                byte[] pPattern)
  {
    BackendUtils.checkExceptions(() -> {

      Pointer<Byte> lPatternPointer =
                                    Pointer.pointerToBytes(pPattern);

      BackendUtils.checkOpenCLError(mOpenCLLibrary.clEnqueueFillBuffer((OpenCLLibrary.cl_command_queue) pQueuePointer.getPointer(),
                                                                       (OpenCLLibrary.cl_mem) pBufferPointer.getPointer(),
                                                                       lPatternPointer,
                                                                       pPattern.length,
                                                                       pOffsetInBytes,
                                                                       pLengthInBytes,
                                                                       0,
                                                                       null,
                                                                       null));

      if (pBlockingFill)
        waitQueueToFinish(pQueuePointer);

    });
  }

  @Override
  public void enqueueCopyBuffer(ClearCLPeerPointer pQueuePointer,
                                ClearCLPeerPointer pSrcBufferPointer,
                                ClearCLPeerPointer pDstBufferPointer,
                                boolean pBlockingCopy,
                                long pSrcOffsetInBytes,
                                long pDstOffsetInBytes,
                                long pLengthToCopyInBytes)
  {
    BackendUtils.checkExceptions(() -> {

      BackendUtils.checkOpenCLError(mOpenCLLibrary.clEnqueueCopyBuffer((OpenCLLibrary.cl_command_queue) pQueuePointer.getPointer(),
                                                                       (OpenCLLibrary.cl_mem) pSrcBufferPointer.getPointer(),
                                                                       (OpenCLLibrary.cl_mem) pDstBufferPointer.getPointer(),
                                                                       pSrcOffsetInBytes,
                                                                       pDstOffsetInBytes,
                                                                       pLengthToCopyInBytes,
                                                                       0,
                                                                       null,
                                                                       null));

      if (pBlockingCopy)
        waitQueueToFinish(pQueuePointer);

    });
  }

  @Override
  public void enqueueCopyBufferRegion(ClearCLPeerPointer pQueuePointer,
                                      ClearCLPeerPointer pSrcBufferPointer,
                                      ClearCLPeerPointer pDstBufferPointer,
                                      boolean pBlockingCopy,
                                      long[] pSrcOrigin,
                                      long[] pDstOrigin,
                                      long[] pRegion)
  {
    BackendUtils.checkExceptions(() -> {

      BackendUtils.checkOpenCLError(mOpenCLLibrary.clEnqueueCopyBufferRect((OpenCLLibrary.cl_command_queue) pQueuePointer.getPointer(),
                                                                           (OpenCLLibrary.cl_mem) pSrcBufferPointer.getPointer(),
                                                                           (OpenCLLibrary.cl_mem) pDstBufferPointer.getPointer(),
                                                                           Pointer.pointerToSizeTs(pSrcOrigin),
                                                                           Pointer.pointerToSizeTs(pDstOrigin),
                                                                           Pointer.pointerToSizeTs(pRegion),
                                                                           0,
                                                                           0,
                                                                           0,
                                                                           0,
                                                                           0,
                                                                           null,
                                                                           null));

      if (pBlockingCopy)
        waitQueueToFinish(pQueuePointer);

    });
  }

  @Override
  public void enqueueCopyBufferToImage(ClearCLPeerPointer pQueuePointer,
                                       ClearCLPeerPointer pSrcBufferPointer,
                                       ClearCLPeerPointer pDstImagePointer,
                                       boolean pBlockingCopy,
                                       long pSrcOffsetInBytes,
                                       long[] pDstOrigin,
                                       long[] pDstRegion)
  {
    BackendUtils.checkExceptions(() -> {

      BackendUtils.checkOpenCLError(mOpenCLLibrary.clEnqueueCopyBufferToImage((OpenCLLibrary.cl_command_queue) pQueuePointer.getPointer(),
                                                                              (OpenCLLibrary.cl_mem) pSrcBufferPointer.getPointer(),
                                                                              (OpenCLLibrary.cl_mem) pDstImagePointer.getPointer(),
                                                                              pSrcOffsetInBytes,
                                                                              Pointer.pointerToSizeTs(pDstOrigin),
                                                                              Pointer.pointerToSizeTs(pDstRegion),
                                                                              0,
                                                                              null,
                                                                              null));

      if (pBlockingCopy)
        waitQueueToFinish(pQueuePointer);

    });
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void enqueueReadFromImage(ClearCLPeerPointer pQueuePointer,
                                   ClearCLPeerPointer pImagePointer,
                                   boolean pReadWrite,
                                   long[] pOrigin,
                                   long[] pRegion,
                                   ClearCLPeerPointer pHostMemPointer)
  {
    BackendUtils.checkExceptions(() -> {
      Utils.checkDirectNIOBuffer(pHostMemPointer);
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clEnqueueReadImage((OpenCLLibrary.cl_command_queue) pQueuePointer.getPointer(),
                                                                      (OpenCLLibrary.cl_mem) pImagePointer.getPointer(),
                                                                      pReadWrite ? 1
                                                                                 : 0,
                                                                      Pointer.pointerToSizeTs(pOrigin),
                                                                      Pointer.pointerToSizeTs(pRegion),
                                                                      0,
                                                                      0,
                                                                      (Pointer) pHostMemPointer.getPointer(),
                                                                      0,
                                                                      null,
                                                                      null));
    });
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void enqueueWriteToImage(ClearCLPeerPointer pQueuePointer,
                                  ClearCLPeerPointer pImagePointer,
                                  boolean pBlockingWrite,
                                  long[] pOrigin,
                                  long[] pRegion,
                                  ClearCLPeerPointer pHostMemPointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clEnqueueWriteImage((OpenCLLibrary.cl_command_queue) pQueuePointer.getPointer(),
                                                                       (OpenCLLibrary.cl_mem) pImagePointer.getPointer(),
                                                                       pBlockingWrite ? 1
                                                                                      : 0,
                                                                       Pointer.pointerToSizeTs(pOrigin),
                                                                       Pointer.pointerToSizeTs(pRegion),
                                                                       0,
                                                                       0,
                                                                       (Pointer) pHostMemPointer.getPointer(),
                                                                       0,
                                                                       null,
                                                                       null));
    });
  }

  @Override
  public void enqueueFillImage(ClearCLPeerPointer pQueuePointer,
                               ClearCLPeerPointer pImagePointer,
                               boolean pBlockingFill,
                               long[] pOrigin,
                               long[] pRegion,
                               byte[] pValues)
  {
    BackendUtils.checkExceptions(() -> {

      Pointer<Byte> lValuesPointer = Pointer.pointerToBytes(pValues);

      BackendUtils.checkOpenCLError(mOpenCLLibrary.clEnqueueFillImage((OpenCLLibrary.cl_command_queue) pQueuePointer.getPointer(),
                                                                      (OpenCLLibrary.cl_mem) pImagePointer.getPointer(),
                                                                      lValuesPointer,
                                                                      Pointer.pointerToSizeTs(pOrigin),
                                                                      Pointer.pointerToSizeTs(pRegion),
                                                                      0,
                                                                      null,
                                                                      null));

      if (pBlockingFill)
        waitQueueToFinish(pQueuePointer);

    });
  }

  @Override
  public void enqueueCopyImage(ClearCLPeerPointer pQueuePointer,
                               ClearCLPeerPointer pSrcBImagePointer,
                               ClearCLPeerPointer pDstImagePointer,
                               boolean pBlockingCopy,
                               long[] pSrcOrigin,
                               long[] pDstOrigin,
                               long[] pRegion)
  {
    BackendUtils.checkExceptions(() -> {

      BackendUtils.checkOpenCLError(mOpenCLLibrary.clEnqueueCopyImage((OpenCLLibrary.cl_command_queue) pQueuePointer.getPointer(),
                                                                      (OpenCLLibrary.cl_mem) pSrcBImagePointer.getPointer(),
                                                                      (OpenCLLibrary.cl_mem) pDstImagePointer.getPointer(),
                                                                      Pointer.pointerToSizeTs(pSrcOrigin),
                                                                      Pointer.pointerToSizeTs(pDstOrigin),
                                                                      Pointer.pointerToSizeTs(pRegion),
                                                                      0,
                                                                      null,
                                                                      null));

      if (pBlockingCopy)
        waitQueueToFinish(pQueuePointer);

    });
  }

  @Override
  public void enqueueCopyImageToBuffer(ClearCLPeerPointer pQueuePointer,
                                       ClearCLPeerPointer pSrcImagePointer,
                                       ClearCLPeerPointer pDstBufferPointer,
                                       boolean pBlockingCopy,
                                       long[] pSrcOrigin,
                                       long[] pSrcRegion,
                                       long pDstOffset)
  {
    BackendUtils.checkExceptions(() -> {

      BackendUtils.checkOpenCLError(mOpenCLLibrary.clEnqueueCopyImageToBuffer((OpenCLLibrary.cl_command_queue) pQueuePointer.getPointer(),
                                                                              (OpenCLLibrary.cl_mem) pSrcImagePointer.getPointer(),
                                                                              (OpenCLLibrary.cl_mem) pDstBufferPointer.getPointer(),
                                                                              Pointer.pointerToSizeTs(pSrcOrigin),
                                                                              Pointer.pointerToSizeTs(pSrcRegion),
                                                                              pDstOffset,
                                                                              0,
                                                                              null,
                                                                              null));

      if (pBlockingCopy)
        waitQueueToFinish(pQueuePointer);

    });
  }

  @Override
  public ClearCLPeerPointer wrap(Buffer pBuffer)
  {
    return BackendUtils.checkExceptions(() -> {
      Pointer<?> lPointer = Pointer.pointerToBuffer(pBuffer);
      ClearCLPeerPointer lPeerPointer =
                                      new ClearCLPeerPointer(lPointer);
      return lPeerPointer;
    });
  }

  @Override
  public ClearCLPeerPointer wrap(ContiguousMemoryInterface pContiguousMemory)
  {
    return BackendUtils.checkExceptions(() -> {
      Pointer<Byte> lByteBuffer =
                                pContiguousMemory.getBridJPointer(Byte.class);
      ClearCLPeerPointer lPeerPointer =
                                      new ClearCLPeerPointer(lByteBuffer);
      return lPeerPointer;
    });
  }

  @Override
  public ClearCLPeerPointer wrap(FragmentedMemoryInterface pFragmentedMemory)
  {
    throw new ClearCLUnsupportedException("fragmented buffers not supported.");
  }

  @Override
  public void releaseBuffer(ClearCLPeerPointer pPeerPointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clReleaseMemObject((OpenCLLibrary.cl_mem) pPeerPointer.getPointer()));
    });
  }

  @Override
  public void releaseContext(ClearCLPeerPointer pPeerPointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clReleaseContext((OpenCLLibrary.cl_context) pPeerPointer.getPointer()));
    });
  }

  @Override
  public void releaseDevice(ClearCLPeerPointer pPeerPointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clReleaseDevice((OpenCLLibrary.cl_device_id) pPeerPointer.getPointer()));
    });

  }

  @Override
  public void releaseImage(ClearCLPeerPointer pPeerPointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clReleaseMemObject((OpenCLLibrary.cl_mem) pPeerPointer.getPointer()));
    });
  }

  @Override
  public void releaseKernel(ClearCLPeerPointer pPeerPointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clReleaseKernel((OpenCLLibrary.cl_kernel) pPeerPointer.getPointer()));
    });
  }

  @Override
  public void releaseProgram(ClearCLPeerPointer pPeerPointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clReleaseProgram((OpenCLLibrary.cl_program) pPeerPointer.getPointer()));
    });
  }

  @Override
  public void releaseQueue(ClearCLPeerPointer pPeerPointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clReleaseCommandQueue((OpenCLLibrary.cl_command_queue) pPeerPointer.getPointer()));
    });
  }

  @Override
  public void waitQueueToFinish(ClearCLPeerPointer pQueuePointer)
  {
    BackendUtils.checkExceptions(() -> {
      BackendUtils.checkOpenCLError(mOpenCLLibrary.clFinish((OpenCLLibrary.cl_command_queue) pQueuePointer.getPointer()));
    });
  }

}
