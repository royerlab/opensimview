package fastfuse.tasks;

import fastfuse.FastFusionEngineInterface;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class MemoryReleaseTask extends TaskBase implements TaskInterface
{

  private final String[] mImageKeysToRelease;

  public MemoryReleaseTask(String pImageKeyRequired, String... pImageKeysToRelease)
  {
    this(Arrays.asList(pImageKeyRequired), pImageKeysToRelease);
  }

  public MemoryReleaseTask(List<String> pImageKeysRequired, String... pImageKeysToRelease)
  {
    super((String[]) pImageKeysRequired.toArray());
    mImageKeysToRelease = pImageKeysToRelease;
  }

  @Override
  public boolean enqueue(FastFusionEngineInterface pFastFusionEngine, boolean pWaitToFinish)
  {
    // remove from fusion engine and release memory
    Stream.of(mImageKeysToRelease).forEach(pFastFusionEngine::removeImage);
    return true;
  }

}
