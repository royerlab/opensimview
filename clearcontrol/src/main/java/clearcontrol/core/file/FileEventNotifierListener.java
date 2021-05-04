package clearcontrol.core.file;

import clearcontrol.core.file.FileEventNotifier.FileEventKind;

import java.io.File;

/**
 * File event notifier listener
 *
 * @author royer
 */
public interface FileEventNotifierListener
{

  /**
   * Notifies of a file event originating from a given file event notifier, a
   * given file, and event kind
   *
   * @param pThis      file event notifier from which the event originates
   * @param pFile      concerned file
   * @param pEventKind event kind
   */
  void fileEvent(FileEventNotifier pThis, File pFile, FileEventKind pEventKind);

}
