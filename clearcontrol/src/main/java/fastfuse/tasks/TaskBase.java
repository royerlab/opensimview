package fastfuse.tasks;

import clearcl.ClearCLContext;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import fastfuse.FastFusionEngineInterface;
import fastfuse.pool.FastFusionMemoryPool;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Base class providing common fields and methods for all task implementations
 *
 * @author royer
 */
public abstract class TaskBase implements TaskInterface
{

  private final HashSet<String> mRequiredImagesSlotKeysSet = new HashSet<>();

  private Class<?> mClass;
  private String mSourceFile;
  private ClearCLProgram mProgram;
  private HashMap<String, ClearCLKernel> mKernelMap = new HashMap<String, ClearCLKernel>();

  /**
   * Instantiates a fusion task given the keys of required images
   *
   * @param pSlotKeys list of slot keys
   */
  public TaskBase(String... pSlotKeys)
  {
    super();
    addRequiredImages(pSlotKeys);
  }

  protected void addRequiredImages(String... pSlotKeys)
  {
    for (String lSlotKey : pSlotKeys)
      mRequiredImagesSlotKeysSet.add(lSlotKey);
  }

  protected void setupProgram(Class<?> pClass, String pSourceFile)
  {
    mClass = pClass;
    mSourceFile = pSourceFile;
  }

  protected ClearCLKernel getKernel(ClearCLContext pContext, String pKernelName) throws IOException
  {
    return getKernel(pContext, pKernelName, null);
  }

  protected ClearCLKernel getKernel(ClearCLContext pContext, String pKernelName, Map<String, Object> pDefines) throws IOException
  {
    if (mKernelMap.get(pKernelName) != null) return mKernelMap.get(pKernelName);
    mProgram = pContext.createProgram(mClass, mSourceFile);
    if (pDefines != null)
    {
      for (Entry<String, Object> entry : pDefines.entrySet())
      {
        if (entry.getValue() instanceof String) mProgram.addDefine(entry.getKey(), (String) entry.getValue());
        else if (entry.getValue() instanceof Number) mProgram.addDefine(entry.getKey(), (Number) entry.getValue());
        else if (entry.getValue() == null) mProgram.addDefine(entry.getKey());
      }
    }
    mProgram.addBuildOptionAllMathOpt();
    mProgram.buildAndLog();
    ClearCLKernel lKernel = mProgram.createKernel(pKernelName);
    mKernelMap.put(pKernelName, lKernel);
    return lKernel;
  }

  protected void runKernel(ClearCLKernel lKernel, boolean pWaitToFinish)
  {
    FastFusionMemoryPool.get().freeMemoryIfNecessaryAndRun(() -> lKernel.run(pWaitToFinish), String.format("Couldn't free memory to run kernel '%s'", lKernel.getName()));
  }

  @Override
  public boolean checkIfRequiredImagesAvailable(Set<String> pAvailableImagesSlotKeys)
  {
    boolean lAllRequiredImagesAvailable = pAvailableImagesSlotKeys.containsAll(mRequiredImagesSlotKeysSet);

    return lAllRequiredImagesAvailable;
  }

  @Override
  public abstract boolean enqueue(FastFusionEngineInterface pFastFusionEngine, boolean pWaitToFinish);

  @Override
  public String toString()
  {
    return String.format("%30s [mRequiredImagesSlotKeysSet=%s, mKernels=%s]", getClass().getSimpleName(), mRequiredImagesSlotKeysSet, mKernelMap.toString());
  }

}
