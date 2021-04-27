package clearcl.backend;

import java.nio.Buffer;

import clearcl.ClearCLPeerPointer;
import clearcl.enums.BuildStatus;
import clearcl.enums.DeviceInfo;
import clearcl.enums.DeviceType;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.ImageType;
import clearcl.enums.KernelAccessType;
import clearcl.enums.MemAllocMode;
import clearcl.exceptions.ClearCLUnsupportedException;
import coremem.ContiguousMemoryInterface;

/**
 * ClearCL backend base class providing common fields and methods for al
 * backends.
 *
 * @author royer
 */
public abstract class ClearCLBackendBase implements
                                         ClearCLBackendInterface
{

  @Override
  public int getNumberOfPlatforms()
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public ClearCLPeerPointer getPlatformPeerPointer(int pPlatformIndex)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public String getPlatformName(ClearCLPeerPointer pPlatformPeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public int getNumberOfDevicesForPlatform(ClearCLPeerPointer pPlatformPeerPointer,
                                           DeviceType pDeviceType)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public int getNumberOfDevicesForPlatform(ClearCLPeerPointer pPlatformPeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public ClearCLPeerPointer getDevicePeerPointer(ClearCLPeerPointer pPlatformPeerPointer,
                                                 DeviceType pDeviceType,
                                                 int pDeviceIndex)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public ClearCLPeerPointer getDevicePeerPointer(ClearCLPeerPointer pPlatformPeerPointer,
                                                 int pDeviceIndex)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public String getDeviceName(ClearCLPeerPointer pDevicePeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public DeviceType getDeviceType(ClearCLPeerPointer pDevicePeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public String getDeviceVersion(ClearCLPeerPointer pDevicePeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public boolean imageSupport(ClearCLPeerPointer pDevicePointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public String getDeviceExtensions(ClearCLPeerPointer pDevicePeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public long getDeviceInfo(ClearCLPeerPointer pDevicePointer,
                            DeviceInfo pDeviceSpec)
  {
    switch (pDeviceSpec)
    {
    case MaxGlobalMemory:
      return getDeviceInfoLong(pDevicePointer,
                               BackendUtils.CL_DEVICE_GLOBAL_MEM_SIZE);
    case LocalMemSize:
      return getDeviceInfoLong(pDevicePointer,
                               BackendUtils.CL_DEVICE_LOCAL_MEM_SIZE);
    case MaxClockFreq:
      return getDeviceInfoInt(pDevicePointer,
                              BackendUtils.CL_DEVICE_MAX_CLOCK_FREQUENCY);
    case ComputeUnits:
      return getDeviceInfoInt(pDevicePointer,
                              BackendUtils.CL_DEVICE_MAX_COMPUTE_UNITS);
    case MaxMemoryAllocationSize:
      return getDeviceInfoLong(pDevicePointer,
                               BackendUtils.CL_DEVICE_MAX_MEM_ALLOC_SIZE);
    case MaxWorkGroupSize:
      return getDeviceInfoLong(pDevicePointer,
                               BackendUtils.CL_DEVICE_MAX_WORK_GROUP_SIZE);
    default:
      return -1;
    }

  }

  @Override
  public ClearCLPeerPointer getContextPeerPointer(ClearCLPeerPointer pPlatformPeerPointer,
                                                  ClearCLPeerPointer... pDevicePeerPointers)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public ClearCLPeerPointer getQueuePeerPointer(ClearCLPeerPointer pDevicePeerPointer,
                                                ClearCLPeerPointer pContextPeerPointer,
                                                boolean pInOrder)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public ClearCLPeerPointer getBufferPeerPointer(ClearCLPeerPointer pDevicePointer,
                                                 ClearCLPeerPointer pContextPeerPointer,
                                                 MemAllocMode pMemAllocMode,
                                                 HostAccessType pHostAccessType,
                                                 KernelAccessType pKernelAccessType,
                                                 long pBufferSizeInBytes)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public ClearCLPeerPointer getImagePeerPointer(ClearCLPeerPointer pDevicePointer,
                                                ClearCLPeerPointer pContextPeerPointer,
                                                MemAllocMode pMemAllocMode,
                                                HostAccessType pHostAccessType,
                                                KernelAccessType pKernelAccessType,
                                                ImageType pImageType,
                                                ImageChannelOrder pImageChannelOrder,
                                                ImageChannelDataType pImageChannelDataType,
                                                long... pDimensions)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public ClearCLPeerPointer getProgramPeerPointer(ClearCLPeerPointer pContextPeerPointer,
                                                  String... pSourceCode)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public boolean buildProgram(ClearCLPeerPointer pProgramPointer,
                              String pOptions)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public BuildStatus getBuildStatus(ClearCLPeerPointer pDevicePeerPointer,
                                    ClearCLPeerPointer pProgramPeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public String getBuildLog(ClearCLPeerPointer pDevicePeerPointer,
                            ClearCLPeerPointer pProgramPeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public ClearCLPeerPointer getKernelPeerPointer(ClearCLPeerPointer pProgramPeerPointer,
                                                 String pKernelName)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void setKernelArgument(ClearCLPeerPointer pKernelPeerPointer,
                                int pIndex,
                                Object pObject)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void enqueueKernelExecution(ClearCLPeerPointer pQueuePeerPointer,
                                     ClearCLPeerPointer pKernelPeerPointer,
                                     int pNumberOfDimensions,
                                     long[] pGlobalOffsets,
                                     long[] pGlobalSizes,
                                     long[] pLocalSizes)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void enqueueReadFromBuffer(ClearCLPeerPointer pQueuePeerPointer,
                                    ClearCLPeerPointer pBufferPeerPointer,
                                    boolean pBlockingRead,
                                    long pOffsetInBuffer,
                                    long pLengthInBuffer,
                                    ClearCLPeerPointer pHostMemPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void enqueueWriteToBuffer(ClearCLPeerPointer pQueuePeerPointer,
                                   ClearCLPeerPointer pBufferPeerPointer,
                                   boolean pBlockingWrite,
                                   long pOffsetInBuffer,
                                   long pLengthInBytes,
                                   ClearCLPeerPointer pHostMemPeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void enqueueReadFromBufferRegion(ClearCLPeerPointer pQueuePeerPointer,
                                          ClearCLPeerPointer pBufferPeerPointer,
                                          boolean pBlockingRead,
                                          long[] pBufferOrigin,
                                          long[] pHostOrigin,
                                          long[] pRegion,
                                          ClearCLPeerPointer pHostMemPeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void enqueueWriteToBufferRegion(ClearCLPeerPointer pQueuePeerPointer,
                                         ClearCLPeerPointer pBufferPeerPointer,
                                         boolean pBlockingWrite,
                                         long[] pBufferOrigin,
                                         long[] pHostOrigin,
                                         long[] pRegion,
                                         ClearCLPeerPointer pHostMemPeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void enqueueFillBuffer(ClearCLPeerPointer pQueuePeerPointer,
                                ClearCLPeerPointer pBufferPeerPointer,
                                boolean pBlockingFill,
                                long pOffsetInBytes,
                                long pLengthInBytes,
                                byte[] pPattern)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void enqueueCopyBuffer(ClearCLPeerPointer pQueuePeerPointer,
                                ClearCLPeerPointer pSrcBufferPeerPointer,
                                ClearCLPeerPointer pDstBufferPeerPointer,
                                boolean pBlockingCopy,
                                long pSrcOffsetInBytes,
                                long pDstOffsetInBytes,
                                long pLengthToCopyInBytes)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void enqueueCopyBufferRegion(ClearCLPeerPointer pQueuePeerPointer,
                                      ClearCLPeerPointer pSrcBufferPeerPointer,
                                      ClearCLPeerPointer pDstBufferPeerPointer,
                                      boolean pBlockingCopy,
                                      long[] pSrcOrigin,
                                      long[] pDstOrigin,
                                      long[] pRegion)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void enqueueCopyBufferToImage(ClearCLPeerPointer pQueuePeerPointer,
                                       ClearCLPeerPointer pSrcBufferPeerPointer,
                                       ClearCLPeerPointer pDstImagePeerPointer,
                                       boolean pBlockingCopy,
                                       long pSrcOffsetInBytes,
                                       long[] pDstOrigin,
                                       long[] pDstRegion)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void enqueueCopyImageToBuffer(ClearCLPeerPointer pQueuePeerPointer,
                                       ClearCLPeerPointer pSrcImagePeerPointer,
                                       ClearCLPeerPointer pDstBufferPeerPointer,
                                       boolean pBlockingCopy,
                                       long[] pSrcOrigin,
                                       long[] pSrcRegion,
                                       long pDstOffset)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void enqueueReadFromImage(ClearCLPeerPointer pQueuePeerPointer,
                                   ClearCLPeerPointer pImagePeerPointer,
                                   boolean pReadWrite,
                                   long[] pOrigin,
                                   long[] pRegion,
                                   ClearCLPeerPointer pHostMemPeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void enqueueWriteToImage(ClearCLPeerPointer pQueuePeerPointer,
                                  ClearCLPeerPointer pImagePeerPointer,
                                  boolean pBlockingWrite,
                                  long[] pOrigin,
                                  long[] pRegion,
                                  ClearCLPeerPointer pHostMemPeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void enqueueFillImage(ClearCLPeerPointer pQueuePeerPointer,
                               ClearCLPeerPointer pImagePeerPointer,
                               boolean pBlockingFill,
                               long[] pOrigin,
                               long[] pRegion,
                               byte[] pColor)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void enqueueCopyImage(ClearCLPeerPointer pQueuePeerPointer,
                               ClearCLPeerPointer pSrcImagePeerPointer,
                               ClearCLPeerPointer pDstImagePeerPointer,
                               boolean pBlockingCopy,
                               long[] pSrcOrigin,
                               long[] pDstOrigin,
                               long[] pRegion)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void releaseBuffer(ClearCLPeerPointer pPeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void releaseContext(ClearCLPeerPointer pPeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void releaseDevice(ClearCLPeerPointer pPeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void releaseImage(ClearCLPeerPointer pPeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void releaseKernel(ClearCLPeerPointer pPeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void releaseProgram(ClearCLPeerPointer pPeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void releaseQueue(ClearCLPeerPointer pPeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public ClearCLPeerPointer wrap(Buffer pBuffer)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public ClearCLPeerPointer wrap(ContiguousMemoryInterface pContiguousMemory)
  {
    throw new ClearCLUnsupportedException();
  }

  @Override
  public void waitQueueToFinish(ClearCLPeerPointer pQueuePeerPointer)
  {
    throw new ClearCLUnsupportedException();
  }

}
