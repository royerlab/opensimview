package clearcl.enums;

//https://software.intel.com/en-us/articles/getting-the-most-from-opencl-12-how-to-increase-performance-by-minimizing-buffer-copies-on-intel-processor-graphics

/**
 * OpenCL memory allocation mode
 *
 * @author royer
 */
public enum MemAllocMode
{
  /**
   * No special allocation/copy mode.
   */
  None,

  /**
   * CL_MEM_USE_HOST_PTR: Buffer was already created in existing application code
   * and the alignment and size rules were followed when the buffer was
   * allocated, or you want control over the buffer allocation and do not want to
   * rely on OpenCL. In cases when you don't want to incur the cost of a copy
   * that would take place with CL_MEM_ALLOC_HOST_PTR | CL_MEM_COPY_HOST_PTR. In
   * cases when data can be safely overwritten by OpenCL or you know the data
   * will not be overwritten because your application controls any writes to the
   * buffer. /
   **/
  UseHostPointer,

  /**
   * CL_MEM_ALLOC_HOST_PTR: You want the OpenCL runtime to handle the alignment
   * and size requirements. In cases when you may be reading data from a file or
   * another I/O stream. A brand new application being written to use OpenCL and
   * not a port from existing code. Buffer will be initialized in host or device
   * code and not by a library decoupled from your control. Don't forget to map
   * and unmap the buffer during initialization.
   **/
  AllocateHostPointer,

  /**
   * Automatically determine best allocation mode based on device
   */
  Best

}
