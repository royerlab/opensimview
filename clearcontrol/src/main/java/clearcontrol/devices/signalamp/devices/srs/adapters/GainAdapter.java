package clearcontrol.devices.signalamp.devices.srs.adapters;

import clearcontrol.com.serial.adapters.SerialTextDeviceAdapter;
import clearcontrol.devices.signalamp.devices.srs.SIM900MainframeDevice;
import clearcontrol.devices.signalamp.devices.srs.adapters.protocol.ProtocolSIM;

import static java.lang.Math.*;

public class GainAdapter extends SIMAdapter implements SerialTextDeviceAdapter<Number>
{

  public GainAdapter(SIM900MainframeDevice pSim900MainframeDevice, int pPort)
  {
    super(pSim900MainframeDevice, pPort, ProtocolSIM.cGain);
  }

  @Override
  public Number clampSetValue(Number pValue)
  {
    double lSign = signum(pValue.doubleValue());
    double lAbs = abs(pValue.doubleValue());
    lAbs = min(max(lAbs, 0.01), 19.99);
    return lSign * lAbs;
  }

}
