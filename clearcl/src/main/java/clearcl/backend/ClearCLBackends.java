package clearcl.backend;

import java.util.ArrayList;

import clearcl.backend.javacl.ClearCLBackendJavaCL;
import clearcl.backend.jocl.ClearCLBackendJOCL;
import clearcl.benchmark.Benchmark;
import clearcl.enums.BenchmarkTest;
import clearcl.util.OsCheck;

/**
 * Static methods to get the best ClearCL backend.
 *
 * @author royer
 */
public class ClearCLBackends
{

  /**
   * This flag controls verbosity of the backend.
   */
  public static boolean sStdOutVerbose = false;

  /**
   * Returns the list of all available ClearCL backends.
   * 
   * @return list of available backends
   */
  public static final ArrayList<ClearCLBackendInterface> getBackendList()
  {
    ArrayList<ClearCLBackendInterface> lList = new ArrayList<>();
    try
    {
      lList.add(new ClearCLBackendJOCL());
    }
    catch (Throwable e)
    {
      System.err.println("Failed to instanciate JOCL backend during backend enumeration (not critical)");
    }
    try
    {
      lList.add(new ClearCLBackendJavaCL());
    }
    catch (Throwable e)
    {
      System.err.println("Failed to instanciate JavaCL backend during backend enumeration (not critical)");
    }

    return lList;
  }

  /**
   * Tests whether the given backend can run some basic kernels. This is a good
   * way to check if the backend is actually operational on a given platform.
   * 
   * @param pClearCLBackend
   *          backend
   * @return true if backend functional, false otherwise
   */
  public static final boolean isFunctionalBackend(ClearCLBackendInterface pClearCLBackend)
  {
    boolean lFunctional = true;
    lFunctional &= Benchmark.getFastestDevice(pClearCLBackend,
                                              BenchmarkTest.Buffer,
                                              2) != null;

    return lFunctional;
  }

  /**
   * Returns the first functional backend that can be identified. The returned
   * backend is tested to be able to run some basic kernels.
   * 
   * @return functional backend, or null if none can be found.
   */
  public static final ClearCLBackendInterface getFunctionalBackend()
  {
    ArrayList<ClearCLBackendInterface> lBackendList =
                                                    getBackendList();

    for (ClearCLBackendInterface lClearCLBackend : lBackendList)
    {
      if (isFunctionalBackend(lClearCLBackend))
        return lClearCLBackend;
    }
    return null;
  }

  /**
   * Returns the best backend. The definition of best means: i) compatible with
   * the OS and OS version. ii) offers the highest OpenCL version. iii) Highest
   * compatibility with CoreMem and native memory access. Moreover, we check if
   * the backends can actually run some basic kernels.
   * 
   * @return best ClearCL backend available.
   */
  public static final ClearCLBackendInterface getBestBackend()
  {
    ClearCLBackendInterface lClearCLBackend;

    switch (OsCheck.getOperatingSystemType())
    {
    case Linux:
      print("Linux");
      lClearCLBackend = getFunctionalBackend();
      break;
    case MacOS:
      print("MacOS");
      lClearCLBackend = new ClearCLBackendJOCL();
      break;
    case Windows:
      print("Windows");
      lClearCLBackend = getFunctionalBackend();
      break;
    case Other:
      print("Other");
      lClearCLBackend = getFunctionalBackend();
      break;
    default:
      print("Unknown");
      lClearCLBackend = getFunctionalBackend();
      break;
    }

    if (lClearCLBackend == null)
      lClearCLBackend = new ClearCLBackendJOCL();

    println(" --> Using backend: "
            + lClearCLBackend.getClass().getSimpleName());

    return lClearCLBackend;
  }

  private static void print(String pString)
  {
    if (sStdOutVerbose)
      System.out.print(pString);
  }

  private static void println(String pString)
  {
    if (sStdOutVerbose)
      System.out.println(pString);
  }
}
