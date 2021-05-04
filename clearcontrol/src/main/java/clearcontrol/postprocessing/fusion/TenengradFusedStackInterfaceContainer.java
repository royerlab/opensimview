package clearcontrol.postprocessing.fusion;

import clearcontrol.warehouse.containers.StackInterfaceContainer;

/**
 * TenengradFusedStackInterfaceContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public class TenengradFusedStackInterfaceContainer extends StackInterfaceContainer
{

  public TenengradFusedStackInterfaceContainer(long pTimePoint)
  {
    super(pTimePoint);
  }

  @Override
  public boolean isDataComplete()
  {
    return true;
  }
}
