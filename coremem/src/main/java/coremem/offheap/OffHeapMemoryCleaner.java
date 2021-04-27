package coremem.offheap;

import java.util.Arrays;

import coremem.rgc.Cleaner;

/**
 * This GC cleaner is used to release off-heap memory after the corresponding
 * holding object has been garbage collected. This is an internal class and
 * should not be used directly.
 *
 * @author royer
 */
public class OffHeapMemoryCleaner implements Cleaner
{
  private final Long mAddressToClean;
  private final String mName;
  private final StackTraceElement[] mAllocationStackTrace;
  private final Long mSignature;

  /**
   * Creates a cleaner given an address, signature, name and allocation
   * stacktrace.
   * 
   * The name and allocation stacktrace are useful to figure out where a
   * problematic memory region was allocated.
   * 
   * @param pAddress
   *          address to free
   * @param pSignature
   *          corresponding signature
   * @param pName
   *          name of memory
   * @param pAllocationStackTrace
   *          allocation stack trace at allocation point
   */
  public OffHeapMemoryCleaner(Long pAddress,
                              Long pSignature,
                              String pName,
                              StackTraceElement[] pAllocationStackTrace)
  {
    mAddressToClean = pAddress;
    mSignature = pSignature;
    mName = pName;
    mAllocationStackTrace = pAllocationStackTrace;
  }

  @Override
  public void run()
  {
    try
    {
      if (mAddressToClean == null)
      {
        format("Cleaning memory: address is null! \n");/**/
        return;
      }
      if (OffHeapMemoryAccess.isAllocatedMemory(mAddressToClean,
                                                mSignature))
      {
        format("Cleaning memory: name=%s, address=%s, signature=%d \n",
               mName,
               mAddressToClean,
               mSignature);/**/
        try
        {
          OffHeapMemoryAccess.freeMemory(mAddressToClean);
          format("Cleaned successfully memory! name=%s, address=%s, signature=%d \n",
                 mName,
                 mAddressToClean,
                 mSignature);/**/
        }
        catch (final Throwable e)
        {
          e.printStackTrace();
        }
      }
      else if (OffHeapMemoryAccess.getSignature(mAddressToClean) != mSignature)
      {
        formatWarning("INFO: Attempted to clean memory with wrong signature! name=%s, address=%d, signature=%d \n",
                      mName,
                      mAddressToClean,
                      mSignature);
        formatWarning("Stack:\n %s \n",
                      Arrays.toString(mAllocationStackTrace)
                            .replaceAll(", ", "\n"));/**/

      }
      else if (OffHeapMemoryAccess.getSignature(mAddressToClean) == mSignature)
      {
        formatWarning("INFO: Attempted to clean already freed memory! name=%s, address=%d, signature=%d \n",
                      mName,
                      mAddressToClean,
                      mSignature);
        formatWarning("Stack:\n %s \n",
                      Arrays.toString(mAllocationStackTrace)
                            .replaceAll(", ", "\n"));/**/

      }
      else
      {
        format("Cleaning memory: not cleaning, unallocated memory or invalid signature."
               + "\n");/**/
      }
    }
    catch (final Exception e)
    {
      e.printStackTrace();
      System.err.println(Arrays.toString(mAllocationStackTrace));
    }
  }

  private void format(String format, Object... args)
  {
    // System.out.format(format, args);
  }

  private void formatWarning(String format, Object... args)
  {
    // System.err.format(format, args);
  }

}
