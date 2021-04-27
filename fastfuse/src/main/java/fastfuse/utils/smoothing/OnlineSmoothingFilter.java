package fastfuse.utils.smoothing;

public interface OnlineSmoothingFilter<T>
{
  public T update(T value);

  public void reset();

  public int getCount();

  public T getCurrent();
}
