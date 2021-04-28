package aptj.bindings.demo;

import static org.junit.Assert.assertEquals;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.bridj.CLong;
import org.bridj.Pointer;
import org.junit.Test;

import aptj.bindings.APTLibrary;

/**
 * APTJ Library tests
 *
 * @author royer
 */
public class APTLibraryDemo
{

	/**
	 * Test
	 * @throws InterruptedException NA
	 */
	@Test
	public void test() throws InterruptedException
	{
		// APTLibrary.APTCleanUp();
		APTLibrary.APTInit();

		Pointer<CLong> lPointerNumDevices = Pointer.allocateCLong();

		APTLibrary.GetNumHWUnitsEx(	APTLibrary.HWTYPE_TST001,
																lPointerNumDevices);

		long lNumberOfDevices = lPointerNumDevices.getCLong();
		lPointerNumDevices.release();

		System.out.println(lNumberOfDevices);

		BidiMap<Integer, Long> lIndexToSerialBidiMap = new DualHashBidiMap<>();

		for (int i = 0; i < lNumberOfDevices; i++)
		{
			Pointer<CLong> lPointerSerialNum = Pointer.allocateCLong();
			APTLibrary.GetHWSerialNumEx(APTLibrary.HWTYPE_TST001,
																	i,
																	lPointerSerialNum);
			long lSerialNumber = lPointerSerialNum.getCLong();
			lPointerSerialNum.release();

			System.out.format("_________________________________________________\n");
			System.out.format("device %d has serial num: %d \n",
												i,
												lSerialNumber);

			lIndexToSerialBidiMap.put(i, lSerialNumber);

			Pointer<Character> lPointerModelString = Pointer.allocateChars(1024);
			Pointer<Character> lPointerSWVerString = Pointer.allocateChars(1024);
			Pointer<Character> lPointerNotesString = Pointer.allocateChars(1024);

			APTLibrary.GetHWInfo(	lSerialNumber,
														lPointerModelString,
														1024,
														lPointerSWVerString,
														1024,
														lPointerNotesString,
														1024);

			String lModelString = lPointerModelString.getCString().trim();
			String lSWVerString = lPointerSWVerString.getCString().trim();
			String lNotesString = lPointerNotesString.getCString().trim();

			System.out.println(lModelString);
			System.out.println(lSWVerString);
			System.out.println(lNotesString);

			checkError(APTLibrary.InitHWDevice(lSerialNumber));
			// APTLibrary.EnableEventDlg(1);

			Pointer<Float> lMinPosPointer = Pointer.allocateFloat();
			Pointer<Float> lMaxPosPointer = Pointer.allocateFloat();
			Pointer<CLong> lUnitPointer = Pointer.allocateCLong();
			Pointer<Float> lPitchPointer = Pointer.allocateFloat();

			checkError(APTLibrary.MOT_GetStageAxisInfo(	lSerialNumber,
																									lMinPosPointer,
																									lMaxPosPointer,
																									lUnitPointer,
																									lPitchPointer));/**/

			System.out.println("Minimum pos: " + lMinPosPointer.get());
			System.out.println("Maximum pos: " + lMaxPosPointer.get());
			System.out.println("Units      : " + (((lUnitPointer.get().intValue()) == 1) ? "mm"
																																									: "deg"));
			System.out.println("Pitch      : " + lPitchPointer.get());

			lMinPosPointer.release();
			lMaxPosPointer.release();
			lUnitPointer.release();
			lPitchPointer.release();

			Pointer<Float> lMinVelPointer = Pointer.allocateFloat();
			Pointer<Float> lAccnPointer = Pointer.allocateFloat();
			Pointer<Float> lMaxVelPointer = Pointer.allocateFloat();

			checkError(APTLibrary.MOT_GetVelParams(	lSerialNumber,
																							lMinVelPointer,
																							lAccnPointer,
																							lMaxVelPointer));/**/

			System.out.println("Minimum velocity: " + lMinVelPointer.get());
			System.out.println("Acceleration    : " + lAccnPointer.get());
			System.out.println("Maximum velocity: " + lMaxVelPointer.get());

			lMinVelPointer.release();
			lAccnPointer.release();
			lMaxVelPointer.release();

			Pointer<Float> lMaxAccnLimitPointer = Pointer.allocateFloat();
			Pointer<Float> lMaxVelLimitPointer = Pointer.allocateFloat();

			checkError(APTLibrary.MOT_GetVelParamLimits(lSerialNumber,
																									lMaxAccnLimitPointer,
																									lMaxVelLimitPointer));/**/

			System.out.println("Maximum acceleration (limit) : " + lMaxAccnLimitPointer.get());
			System.out.println("Maximum velocity     (limit) : " + lMaxVelLimitPointer.get());

			lMaxAccnLimitPointer.release();
			lMaxVelLimitPointer.release();

			checkError(APTLibrary.MOT_Identify(lSerialNumber));/**/

			Thread.sleep(1000);
		}

		System.out.format("_________________________________________________\n");
		for (int i = 0; i < lNumberOfDevices; i++)
		{
			System.out.println("move " + i);

			long lSerialNumber = lIndexToSerialBidiMap.get(i);

			checkError(APTLibrary.MOT_MoveVelocity(	lSerialNumber,
																							1 + (((i % 2) == 1)	? 1
																																	: -1)));/**/
		}
		Thread.sleep(3 * 1000);

		System.out.format("_________________________________________________\n");
		for (int i = 0; i < lNumberOfDevices; i++)
		{
			System.out.println("move " + i);

			long lSerialNumber = lIndexToSerialBidiMap.get(i);

			checkError(APTLibrary.MOT_SetVelParams(	lSerialNumber,
																							0,
																							1.5f,
																							1.5f));
		}/**/
		Thread.sleep(3 * 1000);

		System.out.format("_________________________________________________\n");
		for (int i = 0; i < lNumberOfDevices; i++)
		{
			System.out.println("move " + i);

			long lSerialNumber = lIndexToSerialBidiMap.get(i);

			checkError(APTLibrary.MOT_MoveVelocity(	lSerialNumber,
																							1 + (((i % 2) == 0)	? 1
																																	: -1)));/**/
		}
		Thread.sleep(2 * 1000);

		System.out.format("_________________________________________________\n");
		for (int i = 0; i < lNumberOfDevices; i++)
		{
			System.out.println("stop " + i);

			long lSerialNumber = lIndexToSerialBidiMap.get(i);

			checkError(APTLibrary.MOT_StopProfiled(lSerialNumber));
		}
		Thread.sleep(2 * 1000);

		System.out.format("_________________________________________________\n");
		for (int i = 0; i < lNumberOfDevices; i++)
		{
			System.out.println("move to 2 " + i);

			long lSerialNumber = lIndexToSerialBidiMap.get(i);

			checkError(APTLibrary.MOT_MoveAbsoluteEx(lSerialNumber, 2f, 0));
		}
		Thread.sleep(16 * 1000);

		System.out.format("_________________________________________________\n");
		for (int i = 0; i < lNumberOfDevices; i++)
		{
			System.out.println("move home " + i);

			long lSerialNumber = lIndexToSerialBidiMap.get(i);

			checkError(APTLibrary.MOT_MoveHome(lSerialNumber, 0));// (i ==
																														// lNumberOfDevices
																														// - 1) ?
			// 1 : 0

		}

		System.out.format("_________________________________________________\n");
		Pointer<Float> lPositionPointer = Pointer.allocateFloat();
		Pointer<CLong> lStatusBitsPointer = Pointer.allocateCLong();
		for (int t = 0; t < 40; t++)
		{
			for (int i = 0; i < lNumberOfDevices; i++)
			{
				System.out.print("status: " + i + " -> ");

				long lSerialNumber = lIndexToSerialBidiMap.get(i);
				checkError(APTLibrary.MOT_GetPosition(lSerialNumber,
																							lPositionPointer));
				System.out.print("position: " + lPositionPointer.get());

				checkError(APTLibrary.MOT_GetStatusBits(lSerialNumber,
																								lStatusBitsPointer));
				System.out.println("position: " + toBinaryString(lStatusBitsPointer.get()
																																						.longValue()));
			}
			Thread.sleep(500);
		}

		assertEquals(lPositionPointer.get(), 0, 0.1);

		System.out.println("finished test");

		APTLibrary.APTCleanUp();/**/
	}

	private String toBinaryString(long pLongValue)
	{
		return new StringBuilder(Long.toBinaryString(pLongValue)).reverse()
																															.toString();
	}

	private void checkError(long pReturnCode)
	{
		if (pReturnCode != 0)
			System.out.println("Return code=" + pReturnCode);
	}
}
