package clearcl.enums;

/**
 * OpenCL device type
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public enum DeviceType
{
 CPU, GPU, OTHER;

  /**
   * Returns true if CPU device.
   * 
   * @return true if CPU
   */
  public boolean isCPU()
  {
    return this == CPU;
  }

  /**
   * Returns true if GPU device.s
   * 
   * @return true if GPU
   */
  public boolean isGPU()
  {
    return this == GPU;
  }
}
