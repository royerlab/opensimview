package coremem.rgc;

/**
 * A cleaner is a Runnable that 'cleans' the ressources associated to a
 * referent.
 * 
 * IMPORTANT: cleaners MUST NOT have a reference of the object that they clean
 * ressources for. Otherwise, these objects will never be garbage collected...
 * 
 *
 * 
 * @author royer
 */
public interface Cleaner extends Runnable
{
  @Override
  public void run();
}
