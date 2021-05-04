package clearcl.backend;

import clearcl.enums.*;
import clearcl.exceptions.ClearCLException;
import clearcl.exceptions.OpenCLException;

import java.util.concurrent.Callable;

/**
 * utility class providing OpenCL constants and methods for all backends.
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public class BackendUtils
{
  // cl_mem_flags - bitfield
  public static final long CL_MEM_READ_WRITE = (1 << 0);
  public static final long CL_MEM_WRITE_ONLY = (1 << 1);
  public static final long CL_MEM_READ_ONLY = (1 << 2);
  public static final long CL_MEM_USE_HOST_PTR = (1 << 3);
  public static final long CL_MEM_ALLOC_HOST_PTR = (1 << 4);
  public static final long CL_MEM_COPY_HOST_PTR = (1 << 5);
  // OPENCL_1_2
  public static final long CL_MEM_HOST_WRITE_ONLY = (1 << 7);
  public static final long CL_MEM_HOST_READ_ONLY = (1 << 8);
  public static final long CL_MEM_HOST_NO_ACCESS = (1 << 9);
  // OPENCL_2_0
  public static final long CL_MEM_SVM_FINE_GRAIN_BUFFER = (1 << 10); /* used by cl_svm_mem_flags only */
  public static final long CL_MEM_SVM_ATOMICS = (1 << 11); /* used by cl_svm_mem_flags only */

  // OPENCL_1_2
  /* cl_mem_migration_flags - bitfield */
  public static final long CL_MIGRATE_MEM_OBJECT_HOST = (1 << 0);
  public static final long CL_MIGRATE_MEM_OBJECT_CONTENT_UNDEFINED = (1 << 1);

  // cl_mem_object_type
  public static final int CL_MEM_OBJECT_BUFFER = 0x10F0;
  public static final int CL_MEM_OBJECT_IMAGE2D = 0x10F1;
  public static final int CL_MEM_OBJECT_IMAGE3D = 0x10F2;
  // OPENCL_1_2
  public static final int CL_MEM_OBJECT_IMAGE2D_ARRAY = 0x10F3;
  public static final int CL_MEM_OBJECT_IMAGE1D = 0x10F4;
  public static final int CL_MEM_OBJECT_IMAGE1D_ARRAY = 0x10F5;
  public static final int CL_MEM_OBJECT_IMAGE1D_BUFFER = 0x10F6;
  // OPENCL_2_0
  public static final int CL_MEM_OBJECT_PIPE = 0x10F7;

  // cl_channel_order
  public static final int CL_R = 0x10B0;
  public static final int CL_A = 0x10B1;
  public static final int CL_RG = 0x10B2;
  public static final int CL_RA = 0x10B3;
  public static final int CL_RGB = 0x10B4;
  public static final int CL_RGBA = 0x10B5;
  public static final int CL_BGRA = 0x10B6;
  public static final int CL_ARGB = 0x10B7;
  public static final int CL_INTENSITY = 0x10B8;
  public static final int CL_LUMINANCE = 0x10B9;
  // OPENCL_1_1
  public static final int CL_Rx = 0x10BA;
  public static final int CL_RGx = 0x10BB;
  public static final int CL_RGBx = 0x10BC;
  // OPENCL_2_0
  public static final int CL_DEPTH = 0x10BD;
  public static final int CL_DEPTH_STENCIL = 0x10BE;
  public static final int CL_sRGB = 0x10BF;
  public static final int CL_sRGBx = 0x10C0;
  public static final int CL_sRGBA = 0x10C1;
  public static final int CL_sBGRA = 0x10C2;
  public static final int CL_ABGR = 0x10C3;

  // cl_channel_type
  public static final int CL_SNORM_INT8 = 0x10D0;
  public static final int CL_SNORM_INT16 = 0x10D1;
  public static final int CL_UNORM_INT8 = 0x10D2;
  public static final int CL_UNORM_INT16 = 0x10D3;
  public static final int CL_UNORM_SHORT_565 = 0x10D4;
  public static final int CL_UNORM_SHORT_555 = 0x10D5;
  public static final int CL_UNORM_INT_101010 = 0x10D6;
  public static final int CL_SIGNED_INT8 = 0x10D7;
  public static final int CL_SIGNED_INT16 = 0x10D8;
  public static final int CL_SIGNED_INT32 = 0x10D9;
  public static final int CL_UNSIGNED_INT8 = 0x10DA;
  public static final int CL_UNSIGNED_INT16 = 0x10DB;
  public static final int CL_UNSIGNED_INT32 = 0x10DC;
  public static final int CL_HALF_FLOAT = 0x10DD;
  public static final int CL_FLOAT = 0x10DE;
  // OPENCL_2_0
  public static final int CL_UNORM_INT24 = 0x10DF;

  // cl_device_info
  public static final int CL_DEVICE_TYPE = 0x1000;
  public static final int CL_DEVICE_VENDOR_ID = 0x1001;
  public static final int CL_DEVICE_MAX_COMPUTE_UNITS = 0x1002;
  public static final int CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS = 0x1003;
  public static final int CL_DEVICE_MAX_WORK_GROUP_SIZE = 0x1004;
  public static final int CL_DEVICE_MAX_WORK_ITEM_SIZES = 0x1005;
  public static final int CL_DEVICE_PREFERRED_VECTOR_WIDTH_CHAR = 0x1006;
  public static final int CL_DEVICE_PREFERRED_VECTOR_WIDTH_SHORT = 0x1007;
  public static final int CL_DEVICE_PREFERRED_VECTOR_WIDTH_INT = 0x1008;
  public static final int CL_DEVICE_PREFERRED_VECTOR_WIDTH_LONG = 0x1009;
  public static final int CL_DEVICE_PREFERRED_VECTOR_WIDTH_FLOAT = 0x100A;
  public static final int CL_DEVICE_PREFERRED_VECTOR_WIDTH_DOUBLE = 0x100B;
  public static final int CL_DEVICE_MAX_CLOCK_FREQUENCY = 0x100C;
  public static final int CL_DEVICE_ADDRESS_BITS = 0x100D;
  public static final int CL_DEVICE_MAX_READ_IMAGE_ARGS = 0x100E;
  public static final int CL_DEVICE_MAX_WRITE_IMAGE_ARGS = 0x100F;
  public static final int CL_DEVICE_MAX_MEM_ALLOC_SIZE = 0x1010;
  public static final int CL_DEVICE_IMAGE2D_MAX_WIDTH = 0x1011;
  public static final int CL_DEVICE_IMAGE2D_MAX_HEIGHT = 0x1012;
  public static final int CL_DEVICE_IMAGE3D_MAX_WIDTH = 0x1013;
  public static final int CL_DEVICE_IMAGE3D_MAX_HEIGHT = 0x1014;
  public static final int CL_DEVICE_IMAGE3D_MAX_DEPTH = 0x1015;
  public static final int CL_DEVICE_IMAGE_SUPPORT = 0x1016;
  public static final int CL_DEVICE_MAX_PARAMETER_SIZE = 0x1017;
  public static final int CL_DEVICE_MAX_SAMPLERS = 0x1018;
  public static final int CL_DEVICE_MEM_BASE_ADDR_ALIGN = 0x1019;
  public static final int CL_DEVICE_MIN_DATA_TYPE_ALIGN_SIZE = 0x101A;
  public static final int CL_DEVICE_SINGLE_FP_CONFIG = 0x101B;
  public static final int CL_DEVICE_GLOBAL_MEM_CACHE_TYPE = 0x101C;
  public static final int CL_DEVICE_GLOBAL_MEM_CACHELINE_SIZE = 0x101D;
  public static final int CL_DEVICE_GLOBAL_MEM_CACHE_SIZE = 0x101E;
  public static final int CL_DEVICE_GLOBAL_MEM_SIZE = 0x101F;
  public static final int CL_DEVICE_MAX_CONSTANT_BUFFER_SIZE = 0x1020;
  public static final int CL_DEVICE_MAX_CONSTANT_ARGS = 0x1021;
  public static final int CL_DEVICE_LOCAL_MEM_TYPE = 0x1022;
  public static final int CL_DEVICE_LOCAL_MEM_SIZE = 0x1023;
  public static final int CL_DEVICE_ERROR_CORRECTION_SUPPORT = 0x1024;
  public static final int CL_DEVICE_PROFILING_TIMER_RESOLUTION = 0x1025;
  public static final int CL_DEVICE_ENDIAN_LITTLE = 0x1026;
  public static final int CL_DEVICE_AVAILABLE = 0x1027;
  public static final int CL_DEVICE_COMPILER_AVAILABLE = 0x1028;
  public static final int CL_DEVICE_EXECUTION_CAPABILITIES = 0x1029;

  public static final <T> T checkExceptions(Callable<T> pCallable)
  {
    try
    {
      return pCallable.call();
    } catch (Throwable e)
    {
      if (e instanceof ClearCLException) throw (ClearCLException) e;
      else throw new ClearCLException(e.getMessage(), e);
    }
  }

  public static final void checkExceptions(Runnable pRunnable)
  {
    try
    {
      pRunnable.run();
    } catch (Throwable e)
    {
      if (e instanceof ClearCLException) throw (ClearCLException) e;
      else throw new ClearCLException(e.getMessage(), e);
    }
  }

  public static long getMemTypeFlags(MemAllocMode pMemAllocMode, HostAccessType pHostAccessType, KernelAccessType pKernelAccessType)
  {
    long lMemFlags = 0;

    switch (pMemAllocMode)
    {
      case AllocateHostPointer:
        lMemFlags |= CL_MEM_ALLOC_HOST_PTR;
        break;
      case UseHostPointer:
        lMemFlags |= CL_MEM_USE_HOST_PTR;
        break;
      case Best:
        lMemFlags |= CL_MEM_ALLOC_HOST_PTR;
        break;
      case None:
        break;
      default:
        break;
    }

    switch (pHostAccessType)
    {
      case ReadOnly:
        lMemFlags |= CL_MEM_HOST_READ_ONLY;
        break;
      case WriteOnly:
        lMemFlags |= CL_MEM_HOST_WRITE_ONLY;
        break;
      case ReadWrite:
        lMemFlags |= 0;
        break;
      case NoAccess:
        lMemFlags |= CL_MEM_HOST_NO_ACCESS;
        break;
      case Undefined:
        break;
    }

    switch (pKernelAccessType)
    {
      case ReadOnly:
        lMemFlags |= CL_MEM_READ_ONLY;
        break;
      case WriteOnly:
        lMemFlags |= CL_MEM_WRITE_ONLY;
        break;
      case ReadWrite:
        lMemFlags |= CL_MEM_READ_WRITE;
        break;
      case Undefined:
    }
    return lMemFlags;
  }

  public static int getImageTypeFlags(ImageType pImageType)
  {
    int lImageTypeFlags = 0;
    switch (pImageType)
    {
      case IMAGE1D:
        lImageTypeFlags |= CL_MEM_OBJECT_IMAGE1D;
        break;
      case IMAGE2D:
        lImageTypeFlags |= CL_MEM_OBJECT_IMAGE2D;
        break;
      case IMAGE3D:
        lImageTypeFlags |= CL_MEM_OBJECT_IMAGE3D;
        break;
    }
    return lImageTypeFlags;
  }

  public static int getImageChannelOrderFlags(ImageChannelOrder pImageChannelOrder)
  {
    int lImageChannelOrderFlags = 0;
    switch (pImageChannelOrder)
    {
      case Intensity:
        lImageChannelOrderFlags |= CL_INTENSITY;
        break;
      case Luminance:
        lImageChannelOrderFlags |= CL_LUMINANCE;
        break;
      case A:
        lImageChannelOrderFlags |= CL_A;
        break;
      case R:
        lImageChannelOrderFlags |= CL_R;
        break;
      case RA:
        lImageChannelOrderFlags |= CL_RA;
        break;
      case RG:
        lImageChannelOrderFlags |= CL_RG;
        break;
      case RGB:
        lImageChannelOrderFlags |= CL_RGB;
        break;
      case ARGB:
        lImageChannelOrderFlags |= CL_ARGB;
        break;
      case BGRA:
        lImageChannelOrderFlags |= CL_BGRA;
        break;
      case RGBA:
        lImageChannelOrderFlags |= CL_RGBA;
        break;

    }
    return lImageChannelOrderFlags;
  }

  public static int getImageChannelDataTypeFlags(ImageChannelDataType pImageDataChannelType)
  {
    int lImageChannelOrderFlags = 0;
    switch (pImageDataChannelType)
    {
      case Float:
        lImageChannelOrderFlags |= CL_FLOAT;
        break;
      case HalfFloat:
        lImageChannelOrderFlags |= CL_HALF_FLOAT;
        break;
      case SignedInt16:
        lImageChannelOrderFlags |= CL_SIGNED_INT16;
        break;
      case SignedInt32:
        lImageChannelOrderFlags |= CL_SIGNED_INT32;
        break;
      case SignedInt8:
        lImageChannelOrderFlags |= CL_SIGNED_INT8;
        break;
      case SignedNormalizedInt16:
        lImageChannelOrderFlags |= CL_SNORM_INT16;
        break;
      case SignedNormalizedInt8:
        lImageChannelOrderFlags |= CL_SNORM_INT8;
        break;
      case UnsignedNormalizedInt16:
        lImageChannelOrderFlags |= CL_UNORM_INT16;
        break;
      case UnsignedNormalizedInt8:
        lImageChannelOrderFlags |= CL_UNORM_INT8;
        break;
      case UnsignedInt16:
        lImageChannelOrderFlags |= CL_UNSIGNED_INT16;
        break;
      case UnsignedInt32:
        lImageChannelOrderFlags |= CL_UNSIGNED_INT32;
        break;
      case UnsignedInt8:
        lImageChannelOrderFlags |= CL_UNSIGNED_INT8;
        break;

    }
    return lImageChannelOrderFlags;
  }

  public static int checkOpenCLError(int pErrorCode)
  {
    if (pErrorCode != 0) throw new OpenCLException(pErrorCode);
    return pErrorCode;
  }

  public static void checkOpenCLErrorCode(int pErrorCode)
  {
    OpenCLException lOpenCLException = new OpenCLException(pErrorCode);
    lOpenCLException.throwIfError();
  }

}
