package simbryo.dynamics.tissue;

import simbryo.dynamics.tissue.cellprop.CellProperty;
import simbryo.dynamics.tissue.cellprop.VectorCellProperty;
import simbryo.dynamics.tissue.cellprop.operators.CellPropertyOperatorInterface;
import simbryo.dynamics.tissue.recorder.TissueRecorder;
import simbryo.particles.ParticleSystem;
import simbryo.particles.forcefield.interaction.impl.CollisionForceField;
import simbryo.particles.viewer.ParticleViewerInterface;
import simbryo.particles.viewer.three.ParticleViewer3D;
import simbryo.util.DoubleBufferingFloatArray;

import java.util.ArrayList;

/**
 * Tissue dynamics extend from a particle system with standard dynamics
 * appropriate for simulating the motion of cells, as well as features such as
 * morphogens and target radii.
 *
 * @author royer
 */
public class TissueDynamics extends ParticleSystem implements TissueDynamicsInterface
{
  private static final long serialVersionUID = 1L;

  protected static final int cMaximumNumberOfCells = 100000;
  protected static final float V = 0.0001f;
  protected static final float Rt = 0.01f;
  protected static final float Rm = 0.005f;

  protected static final float Db = 0.9f;

  protected static final float Fg = 0.000001f;

  protected static final float Ar = 0.05f;
  protected static final float Ab = 0.01f;

  protected static final float Fb = 0.00001f;

  private final DoubleBufferingFloatArray mTargetRadii;

  private final DoubleBufferingFloatArray mBrightnesses;
  private final DoubleBufferingFloatArray mTargetBrightnesses;

  protected final CollisionForceField mCollisionForceField;

  protected ArrayList<CellProperty> mCellPropertyList = new ArrayList<>();

  protected volatile long mTimeStepIndex = 0;
  // protected final Sequence mSequence = new Sequence();

  private transient ParticleViewer3D mParticleViewer3D;

  private TissueRecorder mTissueRecorder;

  /**
   * Constructs a tissue of given dimensions (2D or 3D), dimension, grid size,
   * max number of particle per neighborhood cell, collision force between
   * particles, and drag.
   *
   * @param pCollisionForce                  collision force
   * @param pDrag                            drag coeficient
   * @param pMaxNumberOfParticlesPerGridCell max number of particles per grid cell
   * @param pGridDimensions                  grid dimensions
   */
  public TissueDynamics(float pCollisionForce, float pDrag, int pMaxNumberOfParticlesPerGridCell, int... pGridDimensions)
  {
    super(pMaxNumberOfParticlesPerGridCell, cMaximumNumberOfCells, pGridDimensions);

    mTargetRadii = new DoubleBufferingFloatArray(cMaximumNumberOfCells);

    mBrightnesses = new DoubleBufferingFloatArray(cMaximumNumberOfCells);

    mTargetBrightnesses = new DoubleBufferingFloatArray(cMaximumNumberOfCells);

    mCollisionForceField = new CollisionForceField(pCollisionForce, pDrag, false);

    for (int i = 0; i < cMaximumNumberOfCells; i++)
    {
      setRadius(i, 1.0f);
      setTargetRadius(i, 1.0f);
      setBrightness(i, 1.0f);
      setTargetBrightness(i, 1.0f);
    }
  }

  public void setRecorder(TissueRecorder pTissueRecorder)
  {
      mTissueRecorder = pTissueRecorder;
  }

  public TissueRecorder getRecorder()
  {
    return mTissueRecorder;
  }


  /**
   * Returns the current time step index.
   *
   * @return current time step index.
   */
  @Override
  public long getTimeStepIndex()
  {
    return mTimeStepIndex;
  }

  @Override
  public DoubleBufferingFloatArray getBrightnesses()
  {
    return mBrightnesses;
  }

  /**
   * Sets the brightness.
   *
   */
  @Override
  public void setBrightness(int pParticleId, float pBrightness)
  {
    float[] lBrightnesses = mBrightnesses.getCurrentArray();
    lBrightnesses[pParticleId] = pBrightness;
  }

  /**
   * Returns the brightness.
   *
   */
  @Override
  public float getBrightness(int pParticleId)
  {
    return mBrightnesses.getCurrentArray()[pParticleId];
  }

  /**
   * Sets the target brightness.
   *
   */
  @Override
  public void setTargetBrightness(int pParticleId, float pTargetBrightness)
  {
    float[] lTargetBrightnesses = mTargetBrightnesses.getCurrentArray();
    lTargetBrightnesses[pParticleId] = pTargetBrightness;
  }

  /**
   * Returns the target brightness.
   *
   */
  @Override
  public float getTargetBrightness(int pParticleId)
  {
    return mTargetBrightnesses.getCurrentArray()[pParticleId];
  }

  /**
   * Sets the target radius for a given particle id.
   *
   * @param pParticleId   particle id
   * @param pTargetRadius target radius
   */
  public void setTargetRadius(int pParticleId, float pTargetRadius)
  {
    mTargetRadii.getCurrentArray()[pParticleId] = pTargetRadius;
  }

  /**
   * Returns the target radius for a given particle id.
   *
   * @param pParticleId particle id
   * @return target radius
   */
  public float getTargetRadius(int pParticleId)
  {
    return mTargetRadii.getCurrentArray()[pParticleId];
  }

  @Override
  public void copyParticle(int pSourceParticleId, int pDestinationParticleId)
  {
    super.copyParticle(pSourceParticleId, pDestinationParticleId);
    mTargetRadii.getCurrentArray()[pDestinationParticleId] = mTargetRadii.getCurrentArray()[pDestinationParticleId];
  }

  @Override
  public int cloneParticle(int pSourceParticleId, float pNoiseFactor)
  {
    int lNewParticleId = super.cloneParticle(pSourceParticleId, pNoiseFactor);

    if (lNewParticleId<0)
      return lNewParticleId;

    mTargetRadii.getCurrentArray()[lNewParticleId] = mTargetRadii.getCurrentArray()[pSourceParticleId];
    mTargetBrightnesses.getCurrentArray()[lNewParticleId] = mTargetBrightnesses.getCurrentArray()[pSourceParticleId];

    for (CellProperty lMorphogen : mCellPropertyList)
    {
      lMorphogen.copyValue(pSourceParticleId, lNewParticleId);
    }

    return lNewParticleId;
  }


  public int cellDivision(final int pId)
  {
    // Check if cell is already dying:
    if (isCellDying(pId))
      return -1;

    // Create a daughter cell, we keep the original cell as one of the daughters:
    int lNewCellId = cloneParticle(pId, 0.001f);

    // Check that both cells could be created:
    if (lNewCellId < 0)
      return -1;

    // record cell division:
    if (mTissueRecorder != null)
      mTissueRecorder.recordCellDivisionEvent(pId, lNewCellId);

    return lNewCellId;
  }

  public void cellDeath(final int pId)
  {
    if (isCellDying(pId))
      return;

    setTargetBrightness(pId, 0f);
    setTargetRadius(pId,0f);
    // record cell death:
    if (mTissueRecorder != null)
      mTissueRecorder.recordCellDeathEvent(pId);
  }

  public boolean isCellDying(final int pId)
  {
    return getTargetBrightness(pId) < 0.001f && getTargetRadius(pId) < 0.001f;
  }

  public boolean isCellDead(final int pId)
  {
    return getBrightness(pId) < 0.001f && getRadius(pId) < 0.001f;
  }

  /**
   * Adds a new 1D cell property to this tissue.
   *
   * @return the new cell property.
   */
  protected CellProperty addCellProperty()
  {
    return addVectorCellProperty(1);
  }

  /**
   * Adds a new nD vector cell property to this tissue.
   *
   * @param pDimension
   * @return the new vector cell property.
   */
  protected VectorCellProperty addVectorCellProperty(int pDimension)
  {
    VectorCellProperty lVectorCellProperty = new VectorCellProperty(this, pDimension);
    mCellPropertyList.add(lVectorCellProperty);
    return lVectorCellProperty;
  }

  /**
   * Applies a number of simulation steps to the tissue.
   *
   * @param pNumberOfSteps number of simulation steps.
   */
  @Override
  public void simulationSteps(int pNumberOfSteps)
  {
    for (int i = 0; i < pNumberOfSteps; i++)
    {
      addBrownianMotion(Fb);
      smoothToTargetRadius(Ar);
      smoothToTargetBrightness(Ab);
      applyForceField(mCollisionForceField);
      intergrateEuler();
      enforceBounds(Db);
      updateNeighborhoodGrid();
      mTimeStepIndex++;
      // mSequence.step(pDeltaTime);
    }

    // record new cell positions after update
    if (mTissueRecorder != null)
      mTissueRecorder.recordPositions();

    if (mParticleViewer3D != null) mParticleViewer3D.updateDisplay(true);

  }

  /**
   * Smoothly converges current particle radii to the target radii.
   *
   * @param pAlpha exponential coefficient.
   */
  private void smoothToTargetRadius(float pAlpha)
  {
    final float[] lRadiiReadArray = mRadii.getReadArray();
    final float[] lRadiiWriteArray = mRadii.getWriteArray();
    final float[] lTargetRadiiArray = mTargetRadii.getCurrentArray();
    final int lNumberOfParticles = getNumberOfParticles();

    for (int id = 0; id < lNumberOfParticles; id++)
    {
      lRadiiWriteArray[id] = (1 - pAlpha) * lRadiiReadArray[id] + pAlpha * lTargetRadiiArray[id];
    }

    mRadii.swap();
  }

  /**
   * Smoothly converges current particle brightness to the target brightness.
   *
   * @param pAlpha exponential coefficient.
   */
  private void smoothToTargetBrightness(float pAlpha)
  {
    final float[] lBrightnessesReadArray = mBrightnesses.getReadArray();
    final float[] lBrightnessesWriteArray = mBrightnesses.getWriteArray();
    final float[] lTargetBrightnessesArray = mTargetBrightnesses.getCurrentArray();
    final int lNumberOfParticles = getNumberOfParticles();

    for (int id = 0; id < lNumberOfParticles; id++)
    {
      lBrightnessesWriteArray[id] = (1 - pAlpha) * lBrightnessesReadArray[id] + pAlpha * lTargetBrightnessesArray[id];
    }

    mBrightnesses.swap();
  }

  /**
   * opens the 3D viewer for this tissue.
   *
   * @return 3D viewer.
   */
  public ParticleViewer3D open3DViewer()
  {
    if (mParticleViewer3D == null)
      mParticleViewer3D = ParticleViewer3D.view(this, "Viewing: " + getClass().getSimpleName(), 768, 768);
    return mParticleViewer3D;
  }

  /**
   * returns the current 3D viewer for this embryo.
   *
   * @return 3D viewer.
   */
  public ParticleViewerInterface getViewer()
  {
    return mParticleViewer3D;
  }

  /**
   * Applies a single simulation step for an operator and a set of cell
   * properties.
   *
   * @param pOperator       operator
   * @param pCellProperties a list of cell properties
   */
  @SuppressWarnings("unchecked")
  public <CP extends CellProperty> void applyOperator(CellPropertyOperatorInterface<CP> pOperator, CP... pCellProperties)
  {
    apply(0, getNumberOfParticles(), pOperator, pCellProperties);
  }

  /**
   * Applies a single simulation step for an operator and a set of cell
   * properties for a given range of cell ids.
   *
   * @param pBeginId        begin id
   * @param pEndId          end id
   * @param pOperator       operator
   * @param pCellProperties a list of cell properties
   */
  @SuppressWarnings("unchecked")
  public <CP extends CellProperty> void apply(int pBeginId, int pEndId, CellPropertyOperatorInterface<CP> pOperator, CP... pCellProperties)
  {
    pOperator.apply(pBeginId, pEndId, this, pCellProperties);
  }

}
