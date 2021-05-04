package ecc100;

import com.google.common.collect.HashBasedTable;
import ecc100.bindings.EccInfo;
import ecc100.bindings.EccLibrary;
import org.bridj.Pointer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ECC100Controller
{

  private static final int cMaxNumberOfControllers = 2;
  private static final int cNumberOfAxisPerController = 3;
  private final ArrayList<Pointer<Integer>> mPointerToDeviceHandleList = new ArrayList<>();
  private final HashSet<Integer> mDeviceIdList = new HashSet<>();
  private final HashBasedTable<Integer, Integer, ECC100Axis> mDeviceIdAxisIndexToAxisMap = HashBasedTable.create();
  private int mNumberOfControllers;

  private volatile boolean mIsOpened = false;

  public ECC100Controller()
  {
    super();
  }

  public boolean open()
  {

    try
    {
      final Pointer<Pointer<EccInfo>> lPointerToPointerToInfoStruct = Pointer.allocatePointers(EccInfo.class, cMaxNumberOfControllers);
      for (int i = 0; i < cMaxNumberOfControllers; i++)
      {
        lPointerToPointerToInfoStruct.set(i, Pointer.allocate(EccInfo.class));
      }

      mNumberOfControllers = EccLibrary.ECC_Check(lPointerToPointerToInfoStruct);

      // System.out.println("mNumberOfControllers=" + mNumberOfControllers);

      for (int i = 0; i < mNumberOfControllers; i++)
      {
        // Pointer<EccInfo> lPointerToInfoStruct =
        // lPointerToPointerToInfoStruct.get(i);

        // if (lPointerToInfoStruct != null)
        {
          // EccInfo lEccInfo = lPointerToInfoStruct.get();
          // System.out.println("lEccInfo" + i + "->" + lEccInfo);

          final Pointer<Integer> lPointerToDeviceHandle = Pointer.allocateInt();
          EccLibrary.ECC_Connect(i, lPointerToDeviceHandle);

          mPointerToDeviceHandleList.add(lPointerToDeviceHandle);

          for (int j = 0; j < cNumberOfAxisPerController; j++)
          {
            final ECC100Axis lECC100Axis = new ECC100Axis(this, i, j);
            lECC100Axis.setLocked(false); // lEccInfo.locked() != 0);
            final int lDeviceId = i; // lEccInfo.id();
            mDeviceIdList.add(lDeviceId);
            mDeviceIdAxisIndexToAxisMap.put(lDeviceId, j, lECC100Axis);
          }
        }
      }

      Runtime.getRuntime().addShutdownHook(new Thread()
      {
        @Override
        public void run()
        {
          try
          {
            close();
          } catch (final Throwable e)
          {
            e.printStackTrace();
          }
        }
      });

      mIsOpened = true;
      return true;
    } catch (final Throwable e)
    {
      e.printStackTrace();
      return false;
    }
  }

  public void close()
  {
    if (!mIsOpened) return;

    for (final ECC100Axis lECC100Axis : mDeviceIdAxisIndexToAxisMap.values())
    {
      lECC100Axis.home();
    }

    for (final Pointer<Integer> lPointerToControllerDeviceHandle : mPointerToDeviceHandleList)
    {
      EccLibrary.ECC_Close(lPointerToControllerDeviceHandle.getInt());
      lPointerToControllerDeviceHandle.release();
    }

    mIsOpened = false;
  }

  protected int getControllerDeviceHandle(int pDeviceIndex)
  {
    return mPointerToDeviceHandleList.get(pDeviceIndex).getInt();
  }

  public List<Integer> getDeviceIdList()
  {
    return new ArrayList<Integer>(mDeviceIdList);
  }

  public ECC100Axis getAxis(int pDeviceId, int pAxisIndex)
  {
    return mDeviceIdAxisIndexToAxisMap.get(pDeviceId, pAxisIndex);
  }

  public boolean start()
  {
    final Collection<ECC100Axis> lAllECC100Axis = mDeviceIdAxisIndexToAxisMap.values();

    for (final ECC100Axis lECC100Axis : lAllECC100Axis)
      lECC100Axis.home();
    return true;
  }

  public boolean stop()
  {
    final Collection<ECC100Axis> lAllECC100Axis = mDeviceIdAxisIndexToAxisMap.values();

    for (final ECC100Axis lECC100Axis : lAllECC100Axis)
      lECC100Axis.stop();
    return true;
  }

}
