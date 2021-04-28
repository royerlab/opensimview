package aptj;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

import java.util.concurrent.TimeUnit;

import org.bridj.CLong;
import org.bridj.Pointer;

import aptj.bindings.APTLibrary;

/**
 * APTJ device
 *
 * @author royer
 */
public class APTJDevice
{
  private final APTJDeviceFactory mAPTJDeviceFactory;
	private final APTJDeviceType mAPTJDeviceType;
	private final long mDeviceSerialNum;
	private final String mModelString, mSWVerString, mNotesString;
	private final float mMinPosition, mMaxPosition, mPitch;
	private final APTJUnits mUnits;
	private final float mMaxAccel, mMaxVelocity;


	/**
	 * Instantiates an APTJ device
	 * @param pAPTJDeviceFactory parent factory
	 * @param pAPTJDeviceType APTJ device type
	 * @param pDeviceSerialNum device serial number
	 * @throws APTJExeption exception
	 */
	public APTJDevice(APTJDeviceFactory pAPTJDeviceFactory,
	                  APTJDeviceType pAPTJDeviceType,
										long pDeviceSerialNum) throws APTJExeption
	{
		mAPTJDeviceFactory = pAPTJDeviceFactory;
    mAPTJDeviceType = pAPTJDeviceType;
		mDeviceSerialNum = pDeviceSerialNum;

		APTJDeviceFactory.checkError(APTLibrary.InitHWDevice(getSerialNumber()));
		APTJDeviceFactory.checkError(APTLibrary.MOT_Identify(getSerialNumber()));/**/

		{
			Pointer<Character> lPointerModelString = Pointer.allocateChars(1024);
			Pointer<Character> lPointerSWVerString = Pointer.allocateChars(1024);
			Pointer<Character> lPointerNotesString = Pointer.allocateChars(1024);

			APTJDeviceFactory.checkError(APTLibrary.GetHWInfo(getSerialNumber(),
																									lPointerModelString,
																									1024,
																									lPointerSWVerString,
																									1024,
																									lPointerNotesString,
																									1024));

			mModelString = lPointerModelString.getCString().trim();
			mSWVerString = lPointerSWVerString.getCString().trim();
			mNotesString = lPointerNotesString.getCString().trim();

			lPointerModelString.release();
			lPointerSWVerString.release();
			lPointerNotesString.release();
		}

		{
			Pointer<Float> lMinPosPointer = Pointer.allocateFloat();
			Pointer<Float> lMaxPosPointer = Pointer.allocateFloat();
			Pointer<CLong> lUnitPointer = Pointer.allocateCLong();
			Pointer<Float> lPitchPointer = Pointer.allocateFloat();

			APTJDeviceFactory.checkError(APTLibrary.MOT_GetStageAxisInfo(	getSerialNumber(),
																															lMinPosPointer,
																															lMaxPosPointer,
																															lUnitPointer,
																															lPitchPointer));/**/

			mMinPosition = lMinPosPointer.get();
			mMaxPosition = lMaxPosPointer.get();
			mPitch = lPitchPointer.get();
			mUnits = (((lUnitPointer.get().intValue()) == 1) ? APTJUnits.mm
																											: APTJUnits.deg);

			lMinPosPointer.release();
			lMaxPosPointer.release();
			lUnitPointer.release();
			lPitchPointer.release();
		}

		{
			Pointer<Float> lMaxAccnLimitPointer = Pointer.allocateFloat();
			Pointer<Float> lMaxVelLimitPointer = Pointer.allocateFloat();

			APTJDeviceFactory.checkError(APTLibrary.MOT_GetVelParamLimits(getSerialNumber(),
																															lMaxAccnLimitPointer,
																															lMaxVelLimitPointer));/**/

			mMaxAccel = lMaxAccnLimitPointer.get();
			mMaxVelocity = lMaxVelLimitPointer.get();

			lMaxAccnLimitPointer.release();
			lMaxVelLimitPointer.release();
		}

	}
	
  /**
   * Returns the parent factory
   * @return parent factory
   */
  public APTJDeviceFactory getAPTJDeviceFactory()
  {
    return mAPTJDeviceFactory;
  }

	/**
	 * Home device
	 * @throws APTJExeption exception
	 */
	public void home() throws APTJExeption
	{
		stopIfNeeded();
		APTJDeviceFactory.checkError(APTLibrary.MOT_MoveHome(	getSerialNumber(),
																										0));
	}

	/**
	 * Stop device
	 * @throws APTJExeption exception
	 */
	public void stop() throws APTJExeption
	{
		APTJDeviceFactory.checkError(APTLibrary.MOT_StopProfiled(getSerialNumber()));
	}

	/**
	 * Move with given velocity
	 * @param pVelocity velocity
	 * @throws APTJExeption exception
	 */
	public void move(double pVelocity) throws APTJExeption
	{
		move(pVelocity, getAcceleration());
	}

	/**
	 * Move with given velocity and acceleration
	 * @param pVelocity velocity
	 * @param pAcceleration acceleration
	 * @throws APTJExeption exception
	 */
	public void move(double pVelocity, double pAcceleration) throws APTJExeption
	{
		APTJDeviceFactory.checkError(APTLibrary.MOT_SetVelParams(	getSerialNumber(),
																												0,
																												(float) getAcceleration(),
																												(float) abs(pVelocity)));

		int lDirection = signum(pVelocity) > 0 ? 1 : 2;

		APTJDeviceFactory.checkError(APTLibrary.MOT_MoveVelocity(	getSerialNumber(),
																												lDirection));/**/
	}

	/**
	 * Sets speed
	 * @param pSpeed speed
	 * @throws APTJExeption exception
	 */
	public void setSpeed(double pSpeed) throws APTJExeption
	{
		APTJDeviceFactory.checkError(APTLibrary.MOT_SetVelParams(	getSerialNumber(),
																												0,
																												(float) getAcceleration(),
																												(float) pSpeed));
	}

	/**
	 * Returns the current speed
	 * @return speed
	 * @throws APTJExeption exception
	 */
	public double getSpeed() throws APTJExeption
	{
		Pointer<Float> lMinVelPointer = Pointer.allocateFloat();
		Pointer<Float> lAccnPointer = Pointer.allocateFloat();
		Pointer<Float> lMaxVelPointer = Pointer.allocateFloat();

		APTJDeviceFactory.checkError(APTLibrary.MOT_GetVelParams(	getSerialNumber(),
																												lMinVelPointer,
																												lAccnPointer,
																												lMaxVelPointer));/**/

		double lSpeed = lMaxVelPointer.get();

		lMinVelPointer.release();
		lAccnPointer.release();
		lMaxVelPointer.release();

		return lSpeed;
	}

	/**
	 * Sets the current acceleration
	 * @param pAcceleration acceleration
	 * @throws APTJExeption exception
	 */
	public void setAcceleration(double pAcceleration) throws APTJExeption
	{
		APTJDeviceFactory.checkError(APTLibrary.MOT_SetVelParams(	getSerialNumber(),
																												0,
																												(float) pAcceleration,
																												(float) getSpeed()));
	}

	/**
	 * Returns current acceleration
	 * @return current acceleration
	 * @throws APTJExeption exception
	 */
	public double getAcceleration() throws APTJExeption
	{
		Pointer<Float> lMinVelPointer = Pointer.allocateFloat();
		Pointer<Float> lAccnPointer = Pointer.allocateFloat();
		Pointer<Float> lMaxVelPointer = Pointer.allocateFloat();

		APTJDeviceFactory.checkError(APTLibrary.MOT_GetVelParams(	getSerialNumber(),
																												lMinVelPointer,
																												lAccnPointer,
																												lMaxVelPointer));/**/

		double lAcceleration = lAccnPointer.get();

		lMinVelPointer.release();
		lAccnPointer.release();
		lMaxVelPointer.release();

		return lAcceleration;
	}

	/**
	 * Move to new position
	 * @param pNewPosition new position
	 * @throws APTJExeption exception
	 */
	public void moveTo(double pNewPosition) throws APTJExeption
	{
		stopIfNeeded();
		APTJDeviceFactory.checkError(APTLibrary.MOT_MoveAbsoluteEx(	getSerialNumber(),
																													(float) pNewPosition,
																													0));
	}

	/**
	 * Move by a delta to a new position
	 * @param pDeltaPosition delta position
	 * @throws APTJExeption exception
	 */
	public void moveBy(double pDeltaPosition) throws APTJExeption
	{
		stopIfNeeded();
		APTJDeviceFactory.checkError(APTLibrary.MOT_MoveRelativeEx(	getSerialNumber(),
																													(float) pDeltaPosition,
																													0));
	}

	
	/**
	 * Returns the current position
	 * @return current position
	 * @throws APTJExeption exception
	 */
	public double getCurrentPosition() throws APTJExeption
	{
		Pointer<Float> lPositionPointer = Pointer.allocateFloat();

		APTJDeviceFactory.checkError(APTLibrary.MOT_GetPosition(getSerialNumber(),
																											lPositionPointer));
		double lCurrentPosition = lPositionPointer.get();

		lPositionPointer.release();

		return lCurrentPosition;
	}

	/**
	 * Waits until current movement finishes 
	 * @param pPollPeriod poll period
	 * @param pTimeOut time out
	 * @param pTimeUnit time unit
	 * @return true ->  no timeout, false -> timeout
	 * @throws APTJExeption exception
	 */
	public boolean waitWhileMoving(	long pPollPeriod,
																	long pTimeOut,
																	TimeUnit pTimeUnit) throws APTJExeption
	{
		long lPeriodInMilliseconds = TimeUnit.MILLISECONDS.convert(	pPollPeriod,
																																pTimeUnit);
		long lNumberOfPeriods = 1 + (pTimeOut / lPeriodInMilliseconds);
		for (long t = 0; t < lNumberOfPeriods; t++)
		{
			if (!isMoving())
				return true;
			try
			{
				Thread.sleep(lPeriodInMilliseconds);
			}
			catch (InterruptedException e)
			{
			}
		}
		return false;
	}

	private void stopIfNeeded() throws APTJExeption
	{
		if (isLimitAttained())
			stop();
	}

	/**
	 * Returns true if the limit has been attained
	 * @return  true -> limit reached
	 * @throws APTJExeption exception
	 */
	public boolean isLimitAttained() throws APTJExeption
	{

		long lStatusBits = getStatusBits();

		boolean lCheckBit1 = checkBit(lStatusBits, 1 - 1);
		boolean lCheckBit2 = checkBit(lStatusBits, 2 - 1);
		boolean lCheckBit3 = checkBit(lStatusBits, 3 - 1);
		boolean lCheckBit4 = checkBit(lStatusBits, 4 - 1);

		boolean lIsLimitAttained = lCheckBit1 || lCheckBit2
																|| lCheckBit3
																|| lCheckBit4;

		return lIsLimitAttained;
	}

	/**
	 * Returns true if the device is moving
	 * @return true
	 * @throws APTJExeption exception
	 */
	public boolean isMoving() throws APTJExeption
	{

		long lStatusBits = getStatusBits();

		boolean lCheckBit1 = checkBit(lStatusBits, 5 - 1);
		boolean lCheckBit2 = checkBit(lStatusBits, 6 - 1);
		boolean lCheckBit3 = checkBit(lStatusBits, 7 - 1);
		boolean lCheckBit4 = checkBit(lStatusBits, 8 - 1);
		boolean lCheckBit5 = checkBit(lStatusBits, 10 - 1);

		/*System.out.println("lCheckBit1=" + lCheckBit1);
		System.out.println("lCheckBit2=" + lCheckBit2);
		System.out.println("lCheckBit3=" + lCheckBit3);
		System.out.println("lCheckBit4=" + lCheckBit4);
		System.out.println("lCheckBit5=" + lCheckBit5);/**/

		boolean lIsMoving = lCheckBit1 || lCheckBit2
												|| lCheckBit3
												|| lCheckBit4
												|| lCheckBit5;

		return lIsMoving;
	}

	private static boolean checkBit(long pValue, int pBitIndex)
	{
		return ((pValue & (1L << pBitIndex)) != 0);

	}

	private long getStatusBits() throws APTJExeption
	{
		Pointer<CLong> lStatusBitsPointer = Pointer.allocateCLong();

		APTJDeviceFactory.checkError(APTLibrary.MOT_GetStatusBits(getSerialNumber(),
																												lStatusBitsPointer));

		/*System.out.println("status bits: " + toBinaryString(lStatusBitsPointer.get()
																																				.longValue()));/**/

		long lStatusBits = lStatusBitsPointer.get().longValue();

		lStatusBitsPointer.release();

		return lStatusBits;
	}

	@SuppressWarnings("unused")
  private static String toBinaryString(long pLongValue)
	{
		return new StringBuilder(Long.toBinaryString(pLongValue)).reverse()
																															.toString();
	}

	/**
	 * Returns the device type
	 * @return device type
	 */
	public APTJDeviceType getDeviceType()
	{
		return mAPTJDeviceType;
	}

	/**
	 * Returns the device serial number
	 * @return device serial number
	 */
	public long getSerialNumber()
	{
		return mDeviceSerialNum;
	}

	/**
	 * Returns the model string
	 * @return model string
	 */
	public String getModelString()
	{
		return mModelString;
	}

	/**
	 * Returns the software version string
	 * @return software version string
	 */
	public String getSoftwareVersionString()
	{
		return mSWVerString;
	}

	
	/**
	 * Returns the notes string
	 * @return notes string
	 */
	public String getNotesString()
	{
		return mNotesString;
	}

	/**
	 * Returns lower limit position 
	 * @return lower limit position 
	 */
	public float getMinPosition()
	{
		return mMinPosition;
	}

	/**
	 * Returns higher limit position 
	 * @return higher limit position 
	 */
	public float getMaxPosition()
	{
		return mMaxPosition;
	}

	/**
	 * Returns units
	 * @return units
	 */
	public APTJUnits getUnits()
	{
		return mUnits;
	}

	/**
	 * Returns device's pitch
	 * @return pitch
	 */
	public float getPitch()
	{
		return mPitch;
	}

	/**
	 * Returns max acceleration
	 * @return  max acceleration
	 */
	public float getMaxAcceleration()
	{
		return mMaxAccel;
	}

	/**
	 * Returns max velocity
	 * @return max velocity
	 */
	public float getMaxVelocity()
	{
		return mMaxVelocity;
	}

	@Override
	public String toString()
	{
		return String.format(	"APTJDevice [mAPTJDeviceType=%s, mDeviceSerialNum=%s, mModelString=%s, mSWVerString=%s, mNotesString=%s, mLowPosition=%s, mHighPosition=%s, mPitch=%s, mUnits=%s, mMaxAccel=%s, mMaxVelocity=%s]",
													mAPTJDeviceType,
													mDeviceSerialNum,
													mModelString,
													mSWVerString,
													mNotesString,
													mMinPosition,
													mMaxPosition,
													mPitch,
													mUnits,
													mMaxAccel,
													mMaxVelocity);
	}



}
