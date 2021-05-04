package clearcontrol.state.tables;

import clearcontrol.core.device.change.ChangeListeningBase;
import clearcontrol.core.math.interpolation.AbstractInterpolationTable;
import clearcontrol.core.math.interpolation.LinearInterpolationTable;
import clearcontrol.core.math.interpolation.Row;
import clearcontrol.core.math.interpolation.SplineInterpolationTable;
import clearcontrol.LightSheetDOF;

import java.util.ArrayList;

/**
 * Interpolation tables
 *
 * @author royer
 */
public class InterpolationTables extends ChangeListeningBase<InterpolationTables> implements Cloneable
{
  private int mNumberOfLightSheetDevices;
  private int mNumberOfDetectionArmDevices;
  private ArrayList<AbstractInterpolationTable> mInterpolationTableList = new ArrayList<AbstractInterpolationTable>();

  /**
   * Instanciates an interpolation table given a number of detection arms and lightsheets
   *
   * @param pNumberOfDetectionArmDevices number of detection arms
   * @param pNumberOfLightSheetDevices   number of lightsheets
   */
  public InterpolationTables(int pNumberOfDetectionArmDevices, int pNumberOfLightSheetDevices)
  {
    super();

    mNumberOfDetectionArmDevices = pNumberOfDetectionArmDevices;
    mNumberOfLightSheetDevices = pNumberOfLightSheetDevices;

    AbstractInterpolationTable lInterpolationTableDZ = new LinearInterpolationTable(mNumberOfDetectionArmDevices);

    AbstractInterpolationTable lInterpolationTableIX = new LinearInterpolationTable(mNumberOfLightSheetDevices);
    AbstractInterpolationTable lInterpolationTableIY = new LinearInterpolationTable(mNumberOfLightSheetDevices);
    AbstractInterpolationTable lInterpolationTableIZ = new LinearInterpolationTable(mNumberOfLightSheetDevices);

    AbstractInterpolationTable lInterpolationTableIA = new LinearInterpolationTable(mNumberOfLightSheetDevices);
    AbstractInterpolationTable lInterpolationTableIB = new LinearInterpolationTable(mNumberOfLightSheetDevices);
    AbstractInterpolationTable lInterpolationTableIW = new LinearInterpolationTable(mNumberOfLightSheetDevices);
    AbstractInterpolationTable lInterpolationTableIH = new LinearInterpolationTable(mNumberOfLightSheetDevices);
    AbstractInterpolationTable lInterpolationTableIP = new LinearInterpolationTable(mNumberOfLightSheetDevices);
    AbstractInterpolationTable lInterpolationTableII = new LinearInterpolationTable(mNumberOfLightSheetDevices);

    mInterpolationTableList.add(lInterpolationTableDZ);
    mInterpolationTableList.add(lInterpolationTableIX);
    mInterpolationTableList.add(lInterpolationTableIY);
    mInterpolationTableList.add(lInterpolationTableIZ);

    mInterpolationTableList.add(lInterpolationTableIA);
    mInterpolationTableList.add(lInterpolationTableIB);
    mInterpolationTableList.add(lInterpolationTableIW);
    mInterpolationTableList.add(lInterpolationTableIH);
    mInterpolationTableList.add(lInterpolationTableIP);
    mInterpolationTableList.add(lInterpolationTableII);
  }

  /**
   * Instantiate an interpolation table that is a copy of an existing interpolation
   * table.
   *
   * @param pInterpolationTable existing interpolation table
   */
  public InterpolationTables(InterpolationTables pInterpolationTable)
  {
    set(pInterpolationTable);
  }

  @Override
  public InterpolationTables clone()
  {
    return new InterpolationTables(this);
  }

  /**
   * Sets this interpolation table to be identical to the given interpolation table. (Uses
   * a deep copy of each interpolation table)
   *
   * @param pInterpolationTable existing interpolation table
   */
  public void set(InterpolationTables pInterpolationTable)
  {
    mNumberOfDetectionArmDevices = pInterpolationTable.mNumberOfDetectionArmDevices;
    mNumberOfLightSheetDevices = pInterpolationTable.mNumberOfLightSheetDevices;

    mInterpolationTableList = new ArrayList<>();

    for (AbstractInterpolationTable lSplineInterpolationTable : pInterpolationTable.mInterpolationTableList)
    {
      // I would explain the following 6 lines if I could. Sorry, Robert
      if (lSplineInterpolationTable instanceof LinearInterpolationTable)
      {
        mInterpolationTableList.add(((LinearInterpolationTable) lSplineInterpolationTable).clone());
      } else if (lSplineInterpolationTable instanceof SplineInterpolationTable)
      {
        mInterpolationTableList.add(((SplineInterpolationTable) lSplineInterpolationTable).clone());
      }
    }
  }

  /**
   * Adds a control plane at a given z position
   *
   * @param pZ z position
   */
  public void addControlPlane(double pZ)
  {
    for (AbstractInterpolationTable lSplineInterpolationTable : mInterpolationTableList)
      lSplineInterpolationTable.addRow(pZ);
    notifyListeners(this);
  }

  /**
   * Adds a control plane by using interpolated values from a given set of tables
   *
   * @param pInterpolationTables tables to use
   * @param pZ                   control plane position
   */
  public void addControlPlane(InterpolationTables pInterpolationTables, double pZ)
  {
    int lNumberOfTables = mInterpolationTableList.size();

    for (int j = 0; j < lNumberOfTables; j++)
    {
      AbstractInterpolationTable lSplineInterpolationTable = mInterpolationTableList.get(j);
      AbstractInterpolationTable lOtherSplineInterpolationTable = pInterpolationTables.mInterpolationTableList.get(j);

      Row lRow = lSplineInterpolationTable.addRow(pZ);

      int lNumberOfColumns = lRow.getNumberOfColumns();

      for (int c = 0; c < lNumberOfColumns; c++)
      {
        double lValue = lOtherSplineInterpolationTable.getInterpolatedValue(c, pZ);
        lRow.setY(c, lValue);
      }

    }
    notifyListeners(this);

  }

  /**
   * Adds a control plane after a given z poistion (but before the next one)
   *
   * @param pZ z position
   */
  public void addControlPlaneAfter(double pZ)
  {
    for (AbstractInterpolationTable lSplineInterpolationTable : mInterpolationTableList)
      lSplineInterpolationTable.addRowAfter(pZ);
    notifyListeners(this);
  }

  /**
   * Removes the nearest control plane to a given Z value.
   *
   * @param pZ Z value
   */
  public void removeControlPlane(double pZ)
  {
    for (AbstractInterpolationTable lSplineInterpolationTable : mInterpolationTableList)
      lSplineInterpolationTable.removeRow(pZ);
    notifyListeners(this);
  }

  /**
   * Changes the Z value of a given control plane
   *
   * @param pControlPlaneIndex control plane index
   * @param pNewZ              new z value
   */
  public void changeControlPlane(int pControlPlaneIndex, double pNewZ)
  {
    for (AbstractInterpolationTable lSplineInterpolationTable : mInterpolationTableList)
      lSplineInterpolationTable.moveRow(pControlPlaneIndex, pNewZ);
    notifyListeners(this);
  }

  /**
   * Removes all control planes
   */
  public void removeAllControlPlanes()
  {
    for (AbstractInterpolationTable lSplineInterpolationTable : mInterpolationTableList)
      lSplineInterpolationTable.clear();

    notifyListeners(this);
  }

  /**
   * Returns the number of control planes
   *
   * @return number of contol planes
   */
  public int getNumberOfControlPlanes()
  {
    return mInterpolationTableList.get(0).getNumberOfRows();
  }

  /**
   * Returns the number of devices for a given lightsheet DOF
   *
   * @param pLightSheetDOF lightsheet DOF
   * @return number of devices for a given lightsheet DOF
   */
  public int getNumberOfDevices(LightSheetDOF pLightSheetDOF)
  {
    return getTable(pLightSheetDOF).getNumberOfColumns();
  }

  /**
   * Returns the z value for a given control plane index
   *
   * @param pControlPlaneIndex control plane index
   * @return z value
   */
  public double getZ(int pControlPlaneIndex)
  {
    // we are interested in getting the Z position (X in table) _not_ the DZ
    // value!
    double lZ = getTable(LightSheetDOF.DZ).getRow(pControlPlaneIndex).getX();
    return lZ;
  }

  /**
   * Returns min z value
   *
   * @return min z value
   */
  public double getMinZ()
  {
    return getTable(LightSheetDOF.DZ).getMinX();
  }

  /**
   * Returns max z value
   *
   * @return max z value
   */
  public double getMaxZ()
  {
    return getTable(LightSheetDOF.DZ).getMaxX();
  }

  /**
   * Returns interpolated value at a given position Z
   *
   * @param pLightSheetDOF DOF
   * @param pDeviceIndex   device index
   * @param pZ             position at which to sample
   * @return interpolated value
   */
  public double getInterpolated(LightSheetDOF pLightSheetDOF, int pDeviceIndex, double pZ)
  {
    return getTable(pLightSheetDOF).getInterpolatedValue(pDeviceIndex, pZ);
  }

  /**
   * Returns the value at a given control plane for a given device index
   *
   * @param pLightSheetDOF     DOF
   * @param pControlPlaneIndex control plane index
   * @param pDeviceIndex       device index
   * @return value at control plane
   */
  public double get(LightSheetDOF pLightSheetDOF, int pControlPlaneIndex, int pDeviceIndex)
  {
    return getTable(pLightSheetDOF).getY(pControlPlaneIndex, pDeviceIndex);
  }

  /**
   * Sets the value of a DOF for a given control plane index.
   *
   * @param pLightSheetDOF     DOF
   * @param pControlPlaneIndex control plane index
   * @param pDeviceIndex       device index
   * @param pValue             value to set
   */
  public void set(LightSheetDOF pLightSheetDOF, int pControlPlaneIndex, int pDeviceIndex, double pValue)
  {
    getTable(pLightSheetDOF).setY(pControlPlaneIndex, pDeviceIndex, pValue);
    notifyListeners(this);
  }

  /**
   * Adds a delta value for a given DOF, control plane index, and device index.
   *
   * @param pLightSheetDOF     DOF
   * @param pControlPlaneIndex control plane index
   * @param pDeviceIndex       device index
   * @param pDeltaValue        delta value
   */
  public void add(LightSheetDOF pLightSheetDOF, int pControlPlaneIndex, int pDeviceIndex, double pDeltaValue)
  {
    getTable(pLightSheetDOF).addY(pControlPlaneIndex, pDeviceIndex, pDeltaValue);
    notifyListeners(this);
  }

  /**
   * Sets the value of a DOF for a given control plane index.
   *
   * @param pLightSheetDOF     DOF
   * @param pControlPlaneIndex control plane index
   * @param pValue             value to set
   */
  public void set(LightSheetDOF pLightSheetDOF, int pControlPlaneIndex, double pValue)
  {
    getTable(pLightSheetDOF).setY(pControlPlaneIndex, pValue);
    notifyListeners(this);
  }

  /**
   * Sets the value for a given DOF uniformely
   *
   * @param pLightSheetDOF DOF
   * @param pValue         value
   */
  public void set(LightSheetDOF pLightSheetDOF, double pValue)
  {
    getTable(pLightSheetDOF).setY(pValue);
    notifyListeners(this);
  }

  private AbstractInterpolationTable getTable(LightSheetDOF pLightSheetDOF)
  {
    return mInterpolationTableList.get(pLightSheetDOF.ordinal());
  }

}
