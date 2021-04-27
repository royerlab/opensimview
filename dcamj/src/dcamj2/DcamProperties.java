package dcamj2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import dcamapi.DCAM_PROPERTYATTR;
import dcamapi.DcamapiLibrary;
import dcamapi.DcamapiLibrary.DCAMERR;
import dcamapi.DcamapiLibrary.DCAMIDPROP;
import dcamapi.DcamapiLibrary.DCAMPROPATTRIBUTE;
import dcamapi.DcamapiLibrary.DCAMPROPMODEVALUE;
import dcamapi.DcamapiLibrary.DCAMPROPUNIT;

import org.bridj.BridJ;
import org.bridj.FlagSet;
import org.bridj.IntValuedEnum;
import org.bridj.Pointer;
import org.bridj.Pointer.StringType;

/**
 * Dcam properties
 *
 * @author royer
 */
public class DcamProperties extends DcamBase
{
  private final DcamDevice mDcamDevice;

  private final HashMap<String, DcamProperty> mPropertyMap =
                                                           new HashMap<String, DcamProperty>();

  /**
   * Dcam properties
   * 
   * @param pDcamDevice
   *          Dcam camera device
   */
  DcamProperties(final DcamDevice pDcamDevice)
  {
    mDcamDevice = pDcamDevice;
    updatePropertyList();
  }

  /**
   * Lists all properties on standard out
   */
  public void listAllProperties()
  {
    for (final Entry<String, DcamProperty> lEntry : mPropertyMap.entrySet())
    {
      final String lName = lEntry.getKey();
      final DcamProperty lDcamProperty = lEntry.getValue();
      System.out.format("DcamJ: property: '%s' \n%s \n",
                        lName,
                        lDcamProperty.toString());
    }
  }

  /**
   * Updates property list
   * 
   * @return true: success, false otherwise
   */
  public final boolean updatePropertyList()
  {
    boolean lSuccess = true;

    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    final Pointer<IntValuedEnum<DcamapiLibrary.DCAMIDPROP>> lPointerToPropertyId =
                                                                                 (Pointer) Pointer.allocateCLong();

    while (DcamLibrary.hasSucceeded(DcamapiLibrary.dcampropGetnextid(mDcamDevice.getHDCAMPointer(),
                                                                     lPointerToPropertyId,
                                                                     DcamapiLibrary.DCAMPROPOPTION.DCAMPROP_OPTION_SUPPORT.value)))
    {

      final DcamProperty lDcamProperty = new DcamProperty();

      lDcamProperty.id = lPointerToPropertyId.getCLong();

      {
        final Pointer<Byte> lNameBytes = Pointer.allocateBytes(64);
        final IntValuedEnum<DCAMERR> lError =
                                            DcamapiLibrary.dcampropGetname(mDcamDevice.getHDCAMPointer(),
                                                                           lPointerToPropertyId.getCLong(),
                                                                           lNameBytes,
                                                                           64L);
        final boolean lSuccessGetName =
                                      addErrorToListAndCheckHasSucceeded(lError);
        lSuccess &= lSuccessGetName;
        if (!lSuccessGetName)
        {
          break;
        }

        lDcamProperty.name = lNameBytes.getString(StringType.C);
      }

      {
        final DCAM_PROPERTYATTR lDCAM_PROPERTYATTR =
                                                   new DCAM_PROPERTYATTR();
        lDCAM_PROPERTYATTR.cbSize(BridJ.sizeOf(DCAM_PROPERTYATTR.class));
        lDCAM_PROPERTYATTR.iProp(lPointerToPropertyId.getCLong());

        @SuppressWarnings("unused")
        final IntValuedEnum<DcamapiLibrary.DCAMERR> lError =
                                                           DcamapiLibrary.dcampropGetattr(mDcamDevice.getHDCAMPointer(),
                                                                                          Pointer.getPointer(lDCAM_PROPERTYATTR));
        final boolean lSuccessGetAttribute = true; // always works...
        lSuccess &= lSuccessGetAttribute;

        if (lSuccessGetAttribute)
        {

          final FlagSet<DCAMPROPATTRIBUTE> lFlagSetForAttribute =
                                                                FlagSet.createFlagSet(lDCAM_PROPERTYATTR.attribute()
                                                                                                        .value(),
                                                                                      DCAMPROPATTRIBUTE.class);

          final FlagSet<DCAMPROPUNIT> lFlagSetForUnit =
                                                      FlagSet.createFlagSet(lDCAM_PROPERTYATTR.iUnit()
                                                                                              .value(),
                                                                            DCAMPROPUNIT.class);
          /*
          final FlagSet<DCAMPROPATTRIBUTE> lFlagSetForAttribute = FlagSet.fromValue(lDCAM_PROPERTYATTR.attribute()
          																																														.value(),
          																																					DCAMPROPATTRIBUTE.class);
          
          final FlagSet<DCAMPROPUNIT> lFlagSetForUnit = FlagSet.fromValue(lDCAM_PROPERTYATTR.iUnit()
          																																									.value(),
          																																DCAMPROPUNIT.class);/**/

          lDcamProperty.attribute = lFlagSetForAttribute;
          lDcamProperty.writable =
                                 lFlagSetForAttribute.has(DCAMPROPATTRIBUTE.DCAMPROP_ATTR_WRITABLE);

          if (lFlagSetForAttribute.has(DCAMPROPATTRIBUTE.DCAMPROP_TYPE_LONG))
          {
            lDcamProperty.mode = "long";
          }
          else if (lFlagSetForAttribute.has(DCAMPROPATTRIBUTE.DCAMPROP_TYPE_REAL))
          {
            lDcamProperty.mode = "real";
          }
          else if (lFlagSetForAttribute.has(DCAMPROPATTRIBUTE.DCAMPROP_TYPE_MODE))
          {
            lDcamProperty.mode = "mode";
          }

          lDcamProperty.writable =
                                 lFlagSetForAttribute.has(DCAMPROPATTRIBUTE.DCAMPROP_ATTR_WRITABLE);
          lDcamProperty.readable =
                                 lFlagSetForAttribute.has(DCAMPROPATTRIBUTE.DCAMPROP_ATTR_READABLE);

          final Iterator<DCAMPROPUNIT> lIterator =
                                                 lFlagSetForUnit.iterator();
          if (lIterator.hasNext())
          {
            lDcamProperty.unit = lIterator.next();
          }
          else
          {
            lDcamProperty.unit = null;
          }

          lDcamProperty.valuemin = lDCAM_PROPERTYATTR.valuemin();
          lDcamProperty.valuemax = lDCAM_PROPERTYATTR.valuemax();
          lDcamProperty.valuestep = lDCAM_PROPERTYATTR.valuestep();
          lDcamProperty.valuedefault =
                                     lDCAM_PROPERTYATTR.valuedefault();
        }
      }

      mPropertyMap.put(lDcamProperty.name, lDcamProperty);

    }

    return lSuccess;
  }

  /**
   * Returns property list
   * 
   * @return property list
   */
  public final Collection<DcamProperty> getPropertyList()
  {
    return mPropertyMap.values();
  }

  /**
   * Returns property for a given name
   * 
   * @param pPropertyName
   *          property name
   * @return property
   */
  public final DcamProperty getProperty(final String pPropertyName)
  {
    return mPropertyMap.get(pPropertyName);
  }

  /**
   * Returns a property's default value
   * 
   * @param pPropertyName
   *          property name
   * @return default value
   */
  public final double getPropertyDefaultValue(final String pPropertyName)
  {
    return getProperty(pPropertyName).valuedefault;
  }

  /**
   * Returns a property's min value.
   * 
   * @param pPropertyName
   *          property name
   * @return min value
   */
  public final double getPropertyMinValue(final String pPropertyName)
  {
    return getProperty(pPropertyName).valuemin;
  }

  /**
   * Returns a property's max value.
   * 
   * @param pPropertyName
   *          property name
   * @return max value
   */
  public final double getPropertyMaxValue(final String pPropertyName)
  {
    return getProperty(pPropertyName).valuemax;
  }

  /**
   * Returns a property's steps value.
   * 
   * @param pPropertyName
   *          property name
   * @return steps value
   */
  public final double getPropertySteps(final String pPropertyName)
  {
    return getProperty(pPropertyName).valuestep;
  }

  /**
   * Returns a property's writable flag.
   * 
   * @param pPropertyName
   *          property name
   * @return true: writable
   */
  public final boolean isPropertyWritable(final String pPropertyName)
  {
    return getProperty(pPropertyName).writable;
  }

  /**
   * Returns a property's readable flag.
   * 
   * @param pPropertyName
   *          property name
   * @return true: readable
   */
  public final boolean isPropertyReadable(final String pPropertyName)
  {
    return getProperty(pPropertyName).readable;
  }

  /**
   * Returns whether a given property is of real (double) type.
   * 
   * @param pPropertyName
   *          property name
   * @return true: double type
   */
  public final boolean isPropertyReal(final String pPropertyName)
  {
    return getProperty(pPropertyName).mode == "real";
  }

  /**
   * Returns whether a given property is of integer (long) type.
   * 
   * @param pPropertyName
   *          property name
   * @return true: long type
   */
  public final boolean isPropertyLong(final String pPropertyName)
  {
    return getProperty(pPropertyName).mode == "long";
  }

  /**
   * Returns property name
   * 
   * @param pPropertyName
   *          property name
   * @return property name
   */
  public final double getDoublePropertyValue(final String pPropertyName)
  {
    final DcamProperty lProperty = getProperty(pPropertyName);

    final Pointer<Double> lPointerToDouble = Pointer.allocateDouble();

    final IntValuedEnum<DCAMERR> lError =
                                        DcamapiLibrary.dcampropGetvalue(mDcamDevice.getHDCAMPointer(),
                                                                        lProperty.id,
                                                                        lPointerToDouble);
    final boolean lSuccess =
                           addErrorToListAndCheckHasSucceeded(lError);

    if (!lSuccess)
    {
      return Double.NaN;
    }

    final double lValue = lPointerToDouble.getDouble();

    return lValue;
  }

  /**
   * Sets a property value
   * 
   * @param pPropertyName
   *          property name
   * @param pValue
   *          value
   * @return true: success, false otherwise
   */
  public final boolean setDoublePropertyValue(final String pPropertyName,
                                              final double pValue)
  {
    final DcamProperty lProperty = getProperty(pPropertyName);

    final IntValuedEnum<DCAMERR> lError =
                                        DcamapiLibrary.dcampropSetvalue(mDcamDevice.getHDCAMPointer(),
                                                                        lProperty.id,
                                                                        pValue);
    final boolean lSuccess =
                           addErrorToListAndCheckHasSucceeded(lError);

    return lSuccess;
  }

  double getDoublePropertyValue(final DCAMIDPROP pDCAMIDPROP)
  {
    final Pointer<Double> lPointerToDouble = Pointer.allocateDouble();

    final IntValuedEnum<DCAMERR> lError =
                                        DcamapiLibrary.dcampropGetvalue(mDcamDevice.getHDCAMPointer(),
                                                                        pDCAMIDPROP.value,
                                                                        lPointerToDouble);
    final boolean lSuccess =
                           addErrorToListAndCheckHasSucceeded(lError);

    if (!lSuccess)
    {
      return Double.NaN;
    }

    final double lValue = lPointerToDouble.getDouble();

    return lValue;
  }

  boolean setDoublePropertyValue(final DCAMIDPROP pDCAMIDPROP,
                                 final double pValue)
  {

    final IntValuedEnum<DCAMERR> lError =
                                        DcamapiLibrary.dcampropSetvalue(mDcamDevice.getHDCAMPointer(),
                                                                        pDCAMIDPROP.value,
                                                                        pValue);
    final boolean lSuccess =
                           addErrorToListAndCheckHasSucceeded(lError);

    return lSuccess;
  }

  final boolean setModePropertyValue(final DCAMIDPROP pDCAMIDPROP,
                                     final DCAMPROPMODEVALUE pDCAMPROPMODEVALUE)
  {

    final IntValuedEnum<DCAMERR> lError =
                                        DcamapiLibrary.dcampropSetvalue(mDcamDevice.getHDCAMPointer(),
                                                                        pDCAMIDPROP.value,
                                                                        pDCAMPROPMODEVALUE.value);
    final boolean lSuccess =
                           addErrorToListAndCheckHasSucceeded(lError);

    return lSuccess;
  }

  double setAndGetDoublePropertyValue(final DCAMIDPROP pDCAMIDPROP,
                                      final double pValue)
  {
    final Pointer<Double> lPointerToDouble = Pointer.allocateDouble();
    lPointerToDouble.set(pValue);

    final IntValuedEnum<DCAMERR> lError =
                                        DcamapiLibrary.dcampropSetgetvalue(mDcamDevice.getHDCAMPointer(),
                                                                           pDCAMIDPROP.value,
                                                                           lPointerToDouble,
                                                                           0);
    final boolean lSuccess =
                           addErrorToListAndCheckHasSucceeded(lError);

    if (!lSuccess)
    {
      return Double.NaN;
    }

    final double lValue = lPointerToDouble.getDouble();

    return lValue;
  }

}
