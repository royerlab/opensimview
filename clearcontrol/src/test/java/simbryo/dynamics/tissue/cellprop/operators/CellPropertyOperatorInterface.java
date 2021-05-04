package simbryo.dynamics.tissue.cellprop.operators;

import simbryo.dynamics.tissue.TissueDynamics;
import simbryo.dynamics.tissue.cellprop.CellProperty;

import java.io.Serializable;

/**
 * Cell property operators can modify the values of a set of properties over
 * time.
 *
 * @param <CP> cell property type
 * @author royer
 */
public interface CellPropertyOperatorInterface<CP extends CellProperty> extends Serializable
{

  /**
   * Apply a simulation step to the provided cell properties .
   *
   * @param pBeginId
   * @param pEndId
   * @param pTissueDynamics
   * @param pCellProperty
   */
  void apply(int pBeginId, int pEndId, TissueDynamics pTissueDynamics, @SuppressWarnings("unchecked") CP... pCellProperty);

}
