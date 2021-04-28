//	Defines APT.DLL application programmers interface for accessing the APT system.
//	This interface can be included by C or C++ code.


//#include <Windows.h>	// This may need uncommenting in some environments.

#ifdef __cplusplus
extern "C" {
#endif  /* __cplusplus */


// >>>>>>>>>>>>>>>>> MACRO DEFINITIONS <<<<<<<<<<<<<<<<<<

// lHWType definitions - used with GetNumHWUnitsEx & GetHWSerialNumEx.
#define HWTYPE_BSC001		11	// 1 Ch benchtop stepper driver
#define HWTYPE_BSC101		12	// 1 Ch benchtop stepper driver
#define HWTYPE_BSC002		13	// 2 Ch benchtop stepper driver
#define HWTYPE_BDC101		14	// 1 Ch benchtop DC servo driver
#define HWTYPE_SCC001		21	// 1 Ch stepper driver card (used within BSC102,103 units)
#define HWTYPE_DCC001		22	// 1 Ch DC servo driver card (used within BDC102,103 units)
#define HWTYPE_ODC001		24	// 1 Ch DC servo driver cube
#define HWTYPE_OST001		25	// 1 Ch stepper driver cube
#define HWTYPE_MST601		26	// 2 Ch modular stepper driver module
#define HWTYPE_TST001		29	// 1 Ch Stepper driver T-Cube
#define HWTYPE_TDC001		31	// 1 Ch DC servo driver T-Cube
#define HWTYPE_LTSXXX		42	// LTS300/LTS150 Long Travel Integrated Driver/Stages
#define HWTYPE_L490MZ		43	// L490MZ Integrated Driver/Labjack
#define HWTYPE_BBD10X		44	// 1/2/3 Ch benchtop brushless DC servo driver

// Channel idents - used with MOT_SetChannel.
#define CHAN1_INDEX			0		// Channel 1.
#define CHAN2_INDEX			1		// Channel 2.

// Home direction (lDirection) - used with MOT_Set(Get)HomeParams.
#define HOME_FWD			1		// Home in the forward direction.
#define HOME_REV			2		// Home in the reverse direction.

// Home limit switch (lLimSwitch) - used with MOT_Set(Get)HomeParams.
#define HOMELIMSW_FWD		4		// Use forward limit switch for home datum.
#define HOMELIMSW_REV		1		// Use reverse limit switch for home datum.

// Stage units (lUnits) - used with MOT_Set(Get)StageAxisInfo.
#define STAGE_UNITS_MM		1		// Stage units are in mm.
#define STAGE_UNITS_DEG		2		// Stage units are in degrees.

// Hardware limit switch settings (lRevLimSwitch, lFwdLimSwitch) - used with MOT_Set(Get)HWLimSwitches
#define HWLIMSWITCH_IGNORE				1		// Ignore limit switch (e.g. for stages with only one or no limit switches).
#define HWLIMSWITCH_MAKES				2		// Limit switch is activated when electrical continuity is detected.
#define HWLIMSWITCH_BREAKS				3		// Limit switch is activated when electrical continuity is broken.
#define HWLIMSWITCH_MAKES_HOMEONLY		4		// As per HWLIMSWITCH_MAKES except switch is ignored other than when homing (e.g. to support rotation stages).
#define HWLIMSWITCH_BREAKS_HOMEONLY		5		// As per HWLIMSWITCH_BREAKS except switch is ignored other than when homing (e.g. to support rotation stages).

// Move direction (lDirection) - used with MOT_MoveVelocity.
#define MOVE_FWD			1		// Move forward.
#define MOVE_REV			2		// Move reverse.

// Profile mode settings - used with MOT_Set(Get)DCProfileModeParams.
#define DC_PROFILEMODE_TRAPEZOIDAL		0
#define DC_PROFILEMODE_SCURVE			2

// Joystick direction sense settings - used with MOT_Set(Get)DCJoystickParams.
#define DC_JS_DIRSENSE_POS			1
#define DC_JS_DIRSENSE_NEG			2

// >>>>>>>>>>>>>>>>> EXPORTED FUNCTIONS <<<<<<<<<<<<<<<<<<

// System Level Exports.
long __stdcall APTInit(void);
long __stdcall APTCleanUp(void);
long __stdcall GetNumHWUnitsEx(long lHWType, long *plNumUnits);
long __stdcall GetHWSerialNumEx(long lHWType, long lIndex, long *plSerialNum);
long __stdcall GetHWInfo(long lSerialNum, wchar_t *szModel, long lModelLen, wchar_t *szSWVer, long lSWVerLen, wchar_t *szHWNotes, long lHWNotesLen);
long __stdcall InitHWDevice(long lSerialNum);
long __stdcall EnableEventDlg(BOOL bEnable);

// Low Level Motor Specific Exports.
long __stdcall MOT_LLSetEncoderCount(long lSerialNum, long lEncCount);
long __stdcall MOT_LLGetEncoderCount(long lSerialNum, long *plEncCount);

// Motor Specific Exports.
long __stdcall MOT_SetChannel(long lSerialNum, long lChanID);
long __stdcall MOT_Identify(long lSerialNum);
long __stdcall MOT_EnableHWChannel(long lSerialNum);
long __stdcall MOT_DisableHWChannel(long lSerialNum);
long __stdcall MOT_SetVelParams(long lSerialNum, float fMinVel, float fAccn, float fMaxVel);
long __stdcall MOT_GetVelParams(long lSerialNum, float *pfMinVel, float *pfAccn, float *pfMaxVel);
long __stdcall MOT_GetVelParamLimits(long lSerialNum, float *pfMaxAccn, float *pfMaxVel);
long __stdcall MOT_SetHomeParams(long lSerialNum, long lDirection, long lLimSwitch, float fHomeVel, float fZeroOffset);
long __stdcall MOT_GetHomeParams(long lSerialNum, long *plDirection, long *plLimSwitch, float *pfHomeVel, float *pfZeroOffset);
long __stdcall MOT_GetStatusBits(long lSerialNum, long *plStatusBits);

long __stdcall MOT_SetBLashDist(long lSerialNum, float fBLashDist);
long __stdcall MOT_GetBLashDist(long lSerialNum, float *pfBLashDist);
long __stdcall MOT_SetMotorParams(long lSerialNum, long lStepsPerRev, long lGearBoxRatio);
long __stdcall MOT_GetMotorParams(long lSerialNum, long *plStepsPerRev, long *plGearBoxRatio);
long __stdcall MOT_SetStageAxisInfo(long lSerialNum, float fMinPos, float fMaxPos, long lUnits, float fPitch);
long __stdcall MOT_GetStageAxisInfo(long lSerialNum, float *pfMinPos, float *pfMaxPos, long *plUnits, float *pfPitch);
long __stdcall MOT_SetHWLimSwitches(long lSerialNum, long lRevLimSwitch, long lFwdLimSwitch);
long __stdcall MOT_GetHWLimSwitches(long lSerialNum, long *plRevLimSwitch, long *plFwdLimSwitch);
long __stdcall MOT_SetPIDParams(long lSerialNum, long lProp, long lInt, long lDeriv, long lIntLimit);
long __stdcall MOT_GetPIDParams(long lSerialNum, long *plProp, long *plInt, long *plDeriv, long *plIntLimit);

long __stdcall MOT_GetPosition(long lSerialNum, float *pfPosition);
long __stdcall MOT_MoveHome(long lSerialNum, BOOL bWait);
long __stdcall MOT_MoveRelativeEx(long lSerialNum, float fRelDist, BOOL bWait);
long __stdcall MOT_MoveAbsoluteEx(long lSerialNum, float fAbsPos, BOOL bWait);
long __stdcall MOT_MoveVelocity(long lSerialNum, long lDirection);
long __stdcall MOT_StopProfiled(long lSerialNum);

// Brushless DC Servo Specific Exports.
long __stdcall MOT_SetDCCurrentLoopParams(long lSerialNum, long lProp, long lInt, long lIntLim, long lIntDeadBand, long lFFwd);
long __stdcall MOT_GetDCCurrentLoopParams(long lSerialNum, long *plProp, long *plInt, long *plIntLim, long *plIntDeadBand, long *plFFwd);
long __stdcall MOT_SetDCPositionLoopParams(long lSerialNum, long lProp, long lInt, long lIntLim, long lDeriv, long lDerivTime, long lLoopGain, long lVelFFwd, long lAccFFwd, long lPosErrLim);
long __stdcall MOT_GetDCPositionLoopParams(long lSerialNum, long *plProp, long *plInt, long *plIntLim, long *plDeriv, long *plDerivTime, long *plLoopGain, long *plVelFFwd, long *plAccFFwd, long *plPosErrLim);
long __stdcall MOT_SetDCMotorOutputParams(long lSerialNum, float fContCurrLim, float fEnergyLim, float fMotorLim, float fMotorBias);
long __stdcall MOT_GetDCMotorOutputParams(long lSerialNum,  float *pfContCurrLim, float *pfEnergyLim, float *pfMotorLim, float *pfMotorBias);
long __stdcall MOT_SetDCTrackSettleParams(long lSerialNum, long lSettleTime, long lSettleWnd, long lTrackWnd);
long __stdcall MOT_GetDCTrackSettleParams(long lSerialNum, long *plSettleTime, long *plSettleWnd, long *plTrackWnd);
long __stdcall MOT_SetDCProfileModeParams(long lSerialNum, long lProfMode, float fJerk);
long __stdcall MOT_GetDCProfileModeParams(long lSerialNum, long *plProfMode,float *pfJerk);
long __stdcall MOT_SetDCJoystickParams(long lSerialNum, float fMaxVelLO, float fMaxVelHI, float fAccnLO, float fAccnHI, long lDirSense);
long __stdcall MOT_GetDCJoystickParams(long lSerialNum, float *pfMaxVelLO, float *pfMaxVelHI, float *pfAccnLO, float *pfAccnHI, long *plDirSense);
long __stdcall MOT_SetDCSettledCurrentLoopParams(long lSerialNum, long lSettledProp, long lSettledInt, long lSettledIntLim, long lSettledIntDeadBand, long lSettledFFwd);
long __stdcall MOT_GetDCSettledCurrentLoopParams(long lSerialNum, long *plSettledProp, long *plSettledInt, long *plSettledIntLim, long *plSettledIntDeadBand, long *plSettledFFwd);

#ifdef __cplusplus
}
#endif
