package dcamj1;

import dcamapi.DcamapiLibrary.DCAMPROPATTRIBUTE;
import dcamapi.DcamapiLibrary.DCAMPROPUNIT;

import org.bridj.FlagSet;

public class DcamProperty
{

  public FlagSet<DCAMPROPATTRIBUTE> attribute;
  public FlagSet<DCAMPROPATTRIBUTE> type;

  public long id;

  public String name;
  public boolean writable;
  public boolean readable;

  public String mode;
  public DCAMPROPUNIT unit;

  public double valuemin;
  public double valuemax;
  public double valuestep;
  public double valuedefault;

  @Override
  public String toString()
  {
    return String.format("DcamProperty:\n[\n name=%s,\n unit=%s,\n mode=%s,\n writable=%s,\n readable=%s,\n valuemin=%s,\n valuemax=%s,\n valuestep=%s,\n valuedefault=%s,\n attribute=%s,\n type=%s\n]\n",
                         name,
                         unit,
                         mode,
                         writable,
                         readable,
                         valuemin,
                         valuemax,
                         valuestep,
                         valuedefault,
                         attribute,
                         type);
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (name == null ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final DcamProperty other = (DcamProperty) obj;
    if (name == null)
    {
      if (other.name != null)
      {
        return false;
      }
    }
    else if (!name.equals(other.name))
    {
      return false;
    }
    return true;
  }

}
