package simbryo.dynamics.tissue;

import simbryo.SimulationInterface;
import simbryo.particles.ParticleSystemInterface;
import simbryo.util.DoubleBufferingFloatArray;

import java.io.Serializable;

/**
 * Tissue dynamics interface
 *
 * @author royer
 */
public interface TissueDynamicsInterface extends ParticleSystemInterface, SimulationInterface, Serializable
{

  DoubleBufferingFloatArray getBrightnesses();

  void setBrightness(int pParticleId, float pBrightness);

  float getBrightness(int pParticleId);

  void setTargetBrightness(int pParticleId, float pTargetBrightness);

  float getTargetBrightness(int pParticleId);
}
