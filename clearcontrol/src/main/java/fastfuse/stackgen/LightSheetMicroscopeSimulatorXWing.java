package fastfuse.stackgen;

import clearcl.ClearCLContext;
import simbryo.synthoscopy.microscope.lightsheet.LightSheetMicroscopeSimulatorOrtho;

import javax.vecmath.Matrix4f;

/**
 * XWing microscope simulator
 *
 * @author royer
 */
public class LightSheetMicroscopeSimulatorXWing extends LightSheetMicroscopeSimulatorOrtho
{

  /**
   * Instantiates an XWing microscope
   *
   * @param pContext                   ClearCL context
   * @param pCameraMisalignementMatrix misalignement matrix between the two cameras (applied to second
   *                                   camera)
   * @param pMaxCameraResolution       max camera dimensions
   * @param pMainPhantomDimensions     main phantom dimensions
   */
  public LightSheetMicroscopeSimulatorXWing(ClearCLContext pContext, Matrix4f pCameraMisalignementMatrix, int pMaxCameraResolution, long... pMainPhantomDimensions)
  {

    super(pContext, pCameraMisalignementMatrix, 2, 4, pMaxCameraResolution, pMainPhantomDimensions);

  }

}
