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
import coremem.ContiguousMemoryInterface;
import coremem.fragmented.FragmentedMemoryInterface;

/**
 * ClearCL backend interface
 * 
 * 
 * @author royer
 */
public interface ClearCLBackendInterface
{

  /**
   * Returns the number of OpenCL platforms available
   * 
   * @return number of platforms
   */
  int getNumberOfPlatforms();

  /**
   * Returns the platform peer pointer for a given platform index.
   * 
   * @param pPlatformIndex
   *          platform index
   * @return platform peeer pointer
   */
  ClearCLPeerPointer getPlatformPeerPointer(int pPlatformIndex);

  /**
   * Returns a platform name for a given platform peer pointer.
   * 
   * @param pPlatformPeerPointer
   * @return platform name
   */
  String getPlatformName(ClearCLPeerPointer pPlatformPeerPointer);

  /**
   * Returns the number of devices of a given type for a platform.
   * 
   * @param pPlatformPeerPointer
   *          platform peer pointer
   * @param pDeviceType
   *          device type
   * @return number of devices per platform of given type
   */
  int getNumberOfDevicesForPlatform(ClearCLPeerPointer pPlatformPeerPointer,
                                    DeviceType pDeviceType);

  /**
   * Returns the number of devices for a platform.
   * 
   * @param pPlatformPeerPointer
   *          platform peer pointer
   * @return number of devices per platform.
   */
  int getNumberOfDevicesForPlatform(ClearCLPeerPointer pPlatformPeerPointer);

  /**
   * Returns the device peer pointer for a given platform, device type, and
   * device index.
   * 
   * @param pPlatformPeerPointer
   *          platform peer pointer
   * @param pDeviceType
   *          device type
   * @param pDeviceIndex
   *          device index
   * @return device peer pointer
   */
  ClearCLPeerPointer getDevicePeerPointer(ClearCLPeerPointer pPlatformPeerPointer,
                                          DeviceType pDeviceType,
                                          int pDeviceIndex);

  /**
   * Returns the device peer pointer for a given device index.
   * 
   * @param pPlatformPeerPointer
   *          platform peer pointer
   * @param pDeviceIndex
   *          device index
   * @return device peer pointer
   */
  ClearCLPeerPointer getDevicePeerPointer(ClearCLPeerPointer pPlatformPeerPointer,
                                          int pDeviceIndex);

  /**
   * Returns device name.
   * 
   * @param pDevicePeerPointer
   *          device peer pointer
   * @return device name
   */
  String getDeviceName(ClearCLPeerPointer pDevicePeerPointer);

  /**
   * Returns device type.
   * 
   * @param pDevicePeerPointer
   *          device peer pointer
   * @return device type
   */
  DeviceType getDeviceType(ClearCLPeerPointer pDevicePeerPointer);

  /**
   * Returns device version.
   * 
   * @param pDevicePeerPointer
   *          device peer pointer
   * @return device version
   */
  String getDeviceVersion(ClearCLPeerPointer pDevicePeerPointer);

  /**
   * Returns true if this device supports images.
   * 
   * @param pDevicePointer
   *          device peer pointer
   * @return true if device supports images
   */
  boolean imageSupport(ClearCLPeerPointer pDevicePointer);

  /**
   * Returns device extensions string.
   * 
   * @param pDevicePeerPointer
   *          device peer pointer
   * @return device extensions string
   */
  String getDeviceExtensions(ClearCLPeerPointer pDevicePeerPointer);

  /**
   * Returns a device's info long
   * 
   * @param pDevicePeerPointer
   *          device peer pointer
   * @param pInfoId
   *          info id
   * @return info long
   */
  long getDeviceInfoLong(ClearCLPeerPointer pDevicePeerPointer,
                         int pInfoId);

  /**
   * Returns a device's info int
   * 
   * @param pDevicePeerPointer
   *          device peer pointer
   * @param pInfoId
   *          info id
   * @return info int
   */
  long getDeviceInfoInt(ClearCLPeerPointer pDevicePeerPointer,
                        int pInfoId);

  /**
   * Returns a device specification or limit (e.g. max global memory)
   * 
   * @param pDevicePointer
   * @param pDeviceSpec
   * @return specification or limit
   */
  long getDeviceInfo(ClearCLPeerPointer pDevicePointer,
                     DeviceInfo pDeviceSpec);

  /**
   * Returns a context peer pointer for a given platform and devices
   * 
   * @param pPlatformPeerPointer
   *          platform peer pointer
   * @param pDevicePeerPointers
   *          device peer pointers
   * @return context peer pointer
   */
  ClearCLPeerPointer getContextPeerPointer(ClearCLPeerPointer pPlatformPeerPointer,
                                           ClearCLPeerPointer... pDevicePeerPointers);

  /**
   * Returns queue peer pointer for given device and context.
   * 
   * @param pDevicePeerPointer
   *          device peer pointer
   * @param pContextPeerPointer
   *          context peer pointer
   * @param pInOrder
   *          true -> queue keep enqueued tasks in order, false otherwise
   * @return queue peer pointer
   */
  ClearCLPeerPointer getQueuePeerPointer(ClearCLPeerPointer pDevicePeerPointer,
                                         ClearCLPeerPointer pContextPeerPointer,
                                         boolean pInOrder);

  /**
   * Returns buffer peer pointer for a given context, access policy and size in
   * bytes.
   * 
   * @param pDevicePointer
   *          device peer pointer
   * @param pContextPeerPointer
   *          context peer pointer
   * @param pMemAllocMode
   *          memory allocation mode
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pBufferSizeInBytes
   *          buffer size in bytes
   * @return buffer peer pointer
   */
  ClearCLPeerPointer getBufferPeerPointer(ClearCLPeerPointer pDevicePointer,
                                          ClearCLPeerPointer pContextPeerPointer,
                                          MemAllocMode pMemAllocMode,
                                          HostAccessType pHostAccessType,
                                          KernelAccessType pKernelAccessType,
                                          long pBufferSizeInBytes);

  /**
   * Returns image peer pointer for given context, access policy, image type,
   * and dimensions.
   * 
   * @param pDevicePointer
   *          device peer pointer
   * @param pContextPeerPointer
   *          context peer pointer
   * @param pMemAllocMode
   *          memory allocation mode
   * @param pHostAccessType
   *          host access type
   * @param pKernelAccessType
   *          kernel access type
   * @param pImageType
   *          image type
   * @param pImageChannelOrder
   *          image channel order
   * @param pImageChannelDataType
   *          image channel data type
   * @param pDimensions
   *          vararg of dimensions (width,height,depth,...)
   * @return image peer pointer
   */
  ClearCLPeerPointer getImagePeerPointer(ClearCLPeerPointer pDevicePointer,
                                         ClearCLPeerPointer pContextPeerPointer,
                                         MemAllocMode pMemAllocMode,
                                         HostAccessType pHostAccessType,
                                         KernelAccessType pKernelAccessType,
                                         ImageType pImageType,
                                         ImageChannelOrder pImageChannelOrder,
                                         ImageChannelDataType pImageChannelDataType,
                                         long... pDimensions);

  /**
   * Returns program peer pointer for given context and list of source code
   * strings.
   * 
   * @param pContextPeerPointer
   *          context peer pointer
   * @param pSourceCode
   *          list of source code strings
   * @return program peer pointer
   */
  ClearCLPeerPointer getProgramPeerPointer(ClearCLPeerPointer pContextPeerPointer,
                                           String... pSourceCode);

  /**
   * Builds program with options.
   * 
   * @param pProgramPointer
   *          program peer pointer
   * @param pOptions
   *          building options
   * @return true if building succeeded;
   */
  boolean buildProgram(ClearCLPeerPointer pProgramPointer,
                       String pOptions);

  /**
   * Returns last building status for a given program on a given device.
   * 
   * @param pDevicePeerPointer
   *          device peer pointer
   * @param pProgramPeerPointer
   *          program peer pointer
   * @return build status
   */
  BuildStatus getBuildStatus(ClearCLPeerPointer pDevicePeerPointer,
                             ClearCLPeerPointer pProgramPeerPointer);

  /**
   * Returns last build log given program on a given device.
   * 
   * @param pDevicePeerPointer
   *          device peer pointer
   * @param pProgramPeerPointer
   *          program peer pointer
   * @return last build log
   */
  String getBuildLog(ClearCLPeerPointer pDevicePeerPointer,
                     ClearCLPeerPointer pProgramPeerPointer);

  /**
   * Returns kernel peer pointer for a given function/kernel in a given program.
   * 
   * @param pProgramPeerPointer
   *          program peer pointer
   * @param pKernelName
   *          kernel name
   * @return kernel peer pointer
   */
  ClearCLPeerPointer getKernelPeerPointer(ClearCLPeerPointer pProgramPeerPointer,
                                          String pKernelName);

  /**
   * Sets the argument at a given index for a given kernel.
   * 
   * @param pKernelPeerPointer
   *          kernel peer pointer
   * @param pIndex
   *          argument index
   * @param pObject
   *          argument itself
   */
  void setKernelArgument(ClearCLPeerPointer pKernelPeerPointer,
                         int pIndex,
                         Object pObject);

  /**
   * Enqueues execution of a kernel on a given queue for a set of kernel run
   * parameters
   * 
   * @param pQueuePeerPointer
   *          queue peer pointer
   * @param pKernelPeerPointer
   *          kernel peer pointer
   * @param pNumberOfDimensions
   *          number of dimensions
   * @param pGlobalOffsets
   *          global offsets
   * @param pGlobalSizes
   *          global sizes
   * @param pLocalSizes
   *          local sizes
   */
  void enqueueKernelExecution(ClearCLPeerPointer pQueuePeerPointer,
                              ClearCLPeerPointer pKernelPeerPointer,
                              int pNumberOfDimensions,
                              long[] pGlobalOffsets,
                              long[] pGlobalSizes,
                              long[] pLocalSizes);

  /**
   * Enqueues read from buffer.
   * 
   * @param pQueuePeerPointer
   *          queue peer pointer
   * @param pBufferPeerPointer
   *          buffer peer pointer
   * @param pBlockingRead
   * @param pOffsetInBuffer
   * @param pLengthInBuffer
   * @param pHostMemPointer
   */
  void enqueueReadFromBuffer(ClearCLPeerPointer pQueuePeerPointer,
                             ClearCLPeerPointer pBufferPeerPointer,
                             boolean pBlockingRead,
                             long pOffsetInBuffer,
                             long pLengthInBuffer,
                             ClearCLPeerPointer pHostMemPointer);

  /**
   * Enqueues write to buffer.
   * 
   * @param pQueuePeerPointer
   *          queue peer pointer
   * @param pBufferPeerPointer
   *          buffer peer pointer
   * @param pBlockingWrite
   * @param pOffsetInBuffer
   * @param pLengthInBytes
   * @param pHostMemPeerPointer
   */
  void enqueueWriteToBuffer(ClearCLPeerPointer pQueuePeerPointer,
                            ClearCLPeerPointer pBufferPeerPointer,
                            boolean pBlockingWrite,
                            long pOffsetInBuffer,
                            long pLengthInBytes,
                            ClearCLPeerPointer pHostMemPeerPointer);

  /**
   * Enqueues read from buffer region.
   * 
   * @param pQueuePeerPointer
   *          queue peer pointer
   * @param pBufferPeerPointer
   *          buffer peer pointer
   * @param pBlockingRead
   * @param pBufferOrigin
   * @param pHostOrigin
   * @param pRegion
   * @param pHostMemPeerPointer
   */
  void enqueueReadFromBufferRegion(ClearCLPeerPointer pQueuePeerPointer,
                                   ClearCLPeerPointer pBufferPeerPointer,
                                   boolean pBlockingRead,
                                   long[] pBufferOrigin,
                                   long[] pHostOrigin,
                                   long[] pRegion,
                                   ClearCLPeerPointer pHostMemPeerPointer);

  /**
   * Enqueues write to buffer region.
   * 
   * @param pQueuePeerPointer
   *          queue peer pointer
   * @param pBufferPeerPointer
   *          buffer peer pointer
   * @param pBlockingWrite
   * @param pBufferOrigin
   * @param pHostOrigin
   * @param pRegion
   * @param pHostMemPeerPointer
   */
  void enqueueWriteToBufferRegion(ClearCLPeerPointer pQueuePeerPointer,
                                  ClearCLPeerPointer pBufferPeerPointer,
                                  boolean pBlockingWrite,
                                  long[] pBufferOrigin,
                                  long[] pHostOrigin,
                                  long[] pRegion,
                                  ClearCLPeerPointer pHostMemPeerPointer);

  /**
   * Enqueues fill buffer region.
   * 
   * @param pQueuePeerPointer
   *          queue peer pointer
   * @param pBufferPeerPointer
   *          buffer peer
   * @param pBlockingFill
   * @param pOffsetInBytes
   * @param pLengthInBytes
   * @param pPattern
   */
  void enqueueFillBuffer(ClearCLPeerPointer pQueuePeerPointer,
                         ClearCLPeerPointer pBufferPeerPointer,
                         boolean pBlockingFill,
                         long pOffsetInBytes,
                         long pLengthInBytes,
                         byte[] pPattern);

  /**
   * Enqueues copy buffer.
   * 
   * @param pQueuePeerPointer
   *          queue peer pointer
   * @param pSrcBufferPeerPointer
   *          source buffer peer pointer
   * @param pDstBufferPeerPointer
   *          destination buffer peer pointer
   * @param pBlockingCopy
   * @param pSrcOffsetInBytes
   * @param pDstOffsetInBytes
   * @param pLengthToCopyInBytes
   */
  void enqueueCopyBuffer(ClearCLPeerPointer pQueuePeerPointer,
                         ClearCLPeerPointer pSrcBufferPeerPointer,
                         ClearCLPeerPointer pDstBufferPeerPointer,
                         boolean pBlockingCopy,
                         long pSrcOffsetInBytes,
                         long pDstOffsetInBytes,
                         long pLengthToCopyInBytes);

  /**
   * Enqueues copy buffer region.
   * 
   * @param pQueuePeerPointer
   *          queue peer pointer
   * @param pSrcBufferPeerPointer
   *          source buffer peer pointer
   * @param pDstBufferPeerPointer
   *          destination buffer peer pointer
   * @param pBlockingCopy
   * @param pSrcOrigin
   * @param pDstOrigin
   * @param pRegion
   */
  void enqueueCopyBufferRegion(ClearCLPeerPointer pQueuePeerPointer,
                               ClearCLPeerPointer pSrcBufferPeerPointer,
                               ClearCLPeerPointer pDstBufferPeerPointer,
                               boolean pBlockingCopy,
                               long[] pSrcOrigin,
                               long[] pDstOrigin,
                               long[] pRegion);

  /**
   * Enqueues copy buffer to image region.
   * 
   * @param pQueuePeerPointer
   *          queue peer pointer
   * @param pSrcBufferPeerPointer
   *          source buffer peer pointer
   * @param pDstImagePeerPointer
   *          destination image peer pointer
   * @param pBlockingCopy
   * @param pSrcOffsetInBytes
   * @param pDstOrigin
   * @param pDstRegion
   */
  void enqueueCopyBufferToImage(ClearCLPeerPointer pQueuePeerPointer,
                                ClearCLPeerPointer pSrcBufferPeerPointer,
                                ClearCLPeerPointer pDstImagePeerPointer,
                                boolean pBlockingCopy,
                                long pSrcOffsetInBytes,
                                long[] pDstOrigin,
                                long[] pDstRegion);

  /**
   * Enqueues copy image to buffer region.
   * 
   * @param pQueuePeerPointer
   *          queue peer pointer
   * @param pSrcImagePeerPointer
   *          source image peer pointer
   * @param pDstBufferPeerPointer
   *          destination buffer peer pointer
   * @param pBlockingCopy
   * @param pSrcOrigin
   * @param pSrcRegion
   * @param pDstOffset
   */
  void enqueueCopyImageToBuffer(ClearCLPeerPointer pQueuePeerPointer,
                                ClearCLPeerPointer pSrcImagePeerPointer,
                                ClearCLPeerPointer pDstBufferPeerPointer,
                                boolean pBlockingCopy,
                                long[] pSrcOrigin,
                                long[] pSrcRegion,
                                long pDstOffset);

  /**
   * Enqueues read from image region.
   * 
   * @param pQueuePeerPointer
   *          queue peer pointer
   * @param pImagePeerPointer
   *          image peer pointer
   * @param pReadWrite
   * @param pOrigin
   * @param pRegion
   * @param pHostMemPeerPointer
   *          host memory peer pointer
   */
  void enqueueReadFromImage(ClearCLPeerPointer pQueuePeerPointer,
                            ClearCLPeerPointer pImagePeerPointer,
                            boolean pReadWrite,
                            long[] pOrigin,
                            long[] pRegion,
                            ClearCLPeerPointer pHostMemPeerPointer);

  /**
   * Enqueues write to image
   * 
   * @param pQueuePeerPointer
   *          queue peer pointer
   * @param pImagePeerPointer
   *          image peer pointer
   * @param pBlockingWrite
   * @param pOrigin
   * @param pRegion
   * @param pHostMemPeerPointer
   *          host memory peer pointer
   */
  void enqueueWriteToImage(ClearCLPeerPointer pQueuePeerPointer,
                           ClearCLPeerPointer pImagePeerPointer,
                           boolean pBlockingWrite,
                           long[] pOrigin,
                           long[] pRegion,
                           ClearCLPeerPointer pHostMemPeerPointer);

  /**
   * Enqueues fill image
   * 
   * @param pQueuePeerPointer
   *          queue peer pointer
   * @param pImagePeerPointer
   *          image peer pointer
   * @param pBlockingFill
   * @param pOrigin
   * @param pRegion
   * @param pColor
   */
  void enqueueFillImage(ClearCLPeerPointer pQueuePeerPointer,
                        ClearCLPeerPointer pImagePeerPointer,
                        boolean pBlockingFill,
                        long[] pOrigin,
                        long[] pRegion,
                        byte[] pColor);

  /**
   * Enqueues copy image to another image.
   * 
   * @param pQueuePeerPointer
   *          queue peer pointer
   * @param pSrcImagePeerPointer
   *          source image peer pointer
   * @param pDstImagePeerPointer
   *          destination image peer pointer
   * @param pBlockingCopy
   * @param pSrcOrigin
   * @param pDstOrigin
   * @param pRegion
   */
  void enqueueCopyImage(ClearCLPeerPointer pQueuePeerPointer,
                        ClearCLPeerPointer pSrcImagePeerPointer,
                        ClearCLPeerPointer pDstImagePeerPointer,
                        boolean pBlockingCopy,
                        long[] pSrcOrigin,
                        long[] pDstOrigin,
                        long[] pRegion);

  /**
   * Releases buffer.
   * 
   * @param pPeerPointer
   *          buffer peer pointer
   */
  void releaseBuffer(ClearCLPeerPointer pPeerPointer);

  /**
   * Releases context
   * 
   * @param pPeerPointer
   *          context peer pointer
   */
  void releaseContext(ClearCLPeerPointer pPeerPointer);

  /**
   * releases device
   * 
   * @param pPeerPointer
   *          device peer pointer
   */
  void releaseDevice(ClearCLPeerPointer pPeerPointer);

  /**
   * releases image
   * 
   * @param pPeerPointer
   *          image peer pointer
   */
  void releaseImage(ClearCLPeerPointer pPeerPointer);

  /**
   * Releases kernel
   * 
   * @param pPeerPointer
   *          kernel peer pointer
   */
  void releaseKernel(ClearCLPeerPointer pPeerPointer);

  /**
   * Releases program
   * 
   * @param pPeerPointer
   *          program peer pointer
   */
  void releaseProgram(ClearCLPeerPointer pPeerPointer);

  /**
   * releases queue
   * 
   * @param pPeerPointer
   *          queue peer pointer
   */
  void releaseQueue(ClearCLPeerPointer pPeerPointer);

  /**
   * Wraps a NIO buffer inside of a peer pointer.
   * 
   * @param pBuffer
   *          NIO buffer
   * @return corresponding buffer peer pointer
   */
  ClearCLPeerPointer wrap(Buffer pBuffer);

  /**
   * Wraps a CoreMem contiguous buffer inside of a peer pointer.
   * 
   * @param pContiguousMemory
   *          CoreMem contiguous buffer
   * @return corresponding buffer peer pointer
   */
  ClearCLPeerPointer wrap(ContiguousMemoryInterface pContiguousMemory);

  /**
   * Wraps a CoreMem fragmented buffer inside of a peer pointer.
   * 
   * @param pFragmentedMemory
   *          CoreMem fragmented buffer
   * @return corresponding buffer peer pointer
   */
  ClearCLPeerPointer wrap(FragmentedMemoryInterface pFragmentedMemory);

  /**
   * Waits for queue to finish all enqueued tasks.
   * 
   * @param pQueuePeerPointer
   *          queue peer pointer
   */
  void waitQueueToFinish(ClearCLPeerPointer pQueuePeerPointer);

}
