package clearvolume.renderer.listeners;

import coremem.enums.NativeTypeEnum;

import java.nio.ByteBuffer;

public interface VolumeCaptureListener
{

  void capturedVolume(ByteBuffer pCaptureBuffer, NativeTypeEnum pNativeTypeEnum, long pVolumeWidth, long pVolumeHeight, long pVolumeDepth, double pVoxelWidth, double pVoxelHeight, double pVoxelDepth);

}
