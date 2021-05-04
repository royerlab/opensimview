package clearcl.interfaces;

import clearcl.ClearCLQueue;
import clearcl.abs.ClearCLMemBase;

/**
 * ClearCLMem change listeners receive notification that a potential change in a
 * image or buffer.
 *
 * @author royer
 */
public interface ClearCLMemChangeListener
{

  /**
   * Called when the image or buffer has changed. It may happen that this is
   * called but no effective change of the data occured, however, if there is a
   * real change then a call is made.
   *
   * @param pQueue          queue on whichb change happened (or null if no queue involved)
   * @param pClearCLMemBase buffer or image that changed.
   */
  public void change(ClearCLQueue pQueue, ClearCLMemBase pClearCLMemBase);

}
