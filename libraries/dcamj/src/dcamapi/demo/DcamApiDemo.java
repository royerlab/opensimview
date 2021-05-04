package dcamapi.demo;

import dcamapi.*;
import dcamapi.DcamapiLibrary.DCAMERR;
import dcamapi.DcamapiLibrary.DCAM_IDSTR;
import org.bridj.BridJ;
import org.bridj.IntValuedEnum;
import org.bridj.Pointer;
import org.junit.Test;

import static org.bridj.Pointer.allocateBytes;
import static org.bridj.Pointer.pointerTo;
import static org.junit.Assert.assertTrue;

public class DcamApiDemo
{

  @Test
  public void demo()
  {
    final DCAMAPI_INIT lDCAMAPI_INIT = new DCAMAPI_INIT();
    lDCAMAPI_INIT.size(BridJ.sizeOf(DCAMAPI_INIT.class));
    final IntValuedEnum<DCAMERR> dcamapiInit = DcamapiLibrary.dcamapiInit(pointerTo(lDCAMAPI_INIT));

    assertTrue(dcamapiInit.toString().contains("DCAMERR_SUCCESS"));
    assertTrue(lDCAMAPI_INIT.iDeviceCount() > 0);
    System.out.format("nb of devices:=%d \n", lDCAMAPI_INIT.iDeviceCount());

    /*
     * DCAMDEV_STRING	param;
    memset( &param, 0, sizeof(param) );
    param.size		= sizeof(param);
    param.text		= text;
    param.textbytes	= textbytes;
    param.iString	= idStr;
    
    DCAMERR	err;
    err = dcamdev_getstring( hdcam, &param );
     */

    final Pointer<Byte> model = allocateBytes(256);

    final DCAMDEV_STRING lDCAMDEV_STRING = new DCAMDEV_STRING();
    lDCAMDEV_STRING.size(BridJ.sizeOf(DCAMDEV_STRING.class));
    lDCAMDEV_STRING.iString(DCAM_IDSTR.DCAM_IDSTR_MODEL.value());
    lDCAMDEV_STRING.text(model);
    lDCAMDEV_STRING.textbytes(model.getValidBytes());

    // dcamdev_getstring
    DcamapiLibrary.dcamdevGetstring((Pointer<HDCAM_struct>) Pointer.NULL, pointerTo(lDCAMDEV_STRING));

    final String lModel = new String(model.getBytes());
    System.out.println(lModel);
    assertTrue(lModel.contains("C11440"));

    {
      final DCAMDEV_OPEN lDCAMDEV_OPEN = new DCAMDEV_OPEN();
      final long size = BridJ.sizeOf(DCAMDEV_OPEN.class);
      assertTrue(size == 16);
      lDCAMDEV_OPEN.size(size);

      lDCAMDEV_OPEN.index(0);
      final IntValuedEnum<DCAMERR> dcamdevOpen = DcamapiLibrary.dcamdevOpen(pointerTo(lDCAMDEV_OPEN));
      System.out.format("%s \n", dcamdevOpen.toString());

      assertTrue(dcamdevOpen.toString().contains("DCAMERR_SUCCESS"));
    }

    DcamapiLibrary.dcamapiUninit();

  }

}
