/******************************************************************
 *
 *  Project:        ECC100 DLL
 *
 *  Filename:       ecc.h
 *
 *  Zweck:          Positioner Schnittstelle
 *
 *  Autor:          NHands GmbH & Co KG
 *
 *   Datum | Autor | Beschreibung
 * --------+-------+--------------------------------
 * 25.04.12 HK      erstellt
 */

/*****************************************************************************/
/** @mainpage ECC DLL
 *
 *  \ref ecc.h "The ecc.dll" is a library that allows custom programming for the ECC100 
 *  piezo controller.
 *  ECC100 devices can be searched by @ref ECC_Check, connected by @ref ECC_Connect
 *  and released by @ref ECC_Close functions.
 *  It is possible to handle multiple devices simultaneous. the @ref ECC_Connect 
 *  function gives a unique handle for the selected device.
 *
 *  Functions that allow set and get operations are named ECC_control... .
 *  A boolean parameter (Bln32 set) determines the way which is meant.
 *
 *  Documentation for dll functions can be found
 *  \ref ecc.h "here".
 */
/*****************************************************************************/

/******************************************************************/
/** @file ecc.h
 *  ECC100 DLL
 *
 *  Defines functions for connecting and controlling the ECC100
 */
/******************************************************************/
/* $Id: ecc.h,v 1.11 2012/11/20 16:09:10 zaphod Exp $ */




#ifndef __ECC_H__
#define __ECC_H__


/** Definitions for the windows DLL interface                                        */
#ifdef unix
#define NCB_API
#else
#ifdef  NCB_EXPORTS
#define NCB_API __declspec(dllexport) _stdcall  /**< For internal use of this header */
#else
#define NCB_API __declspec(dllimport) _stdcall  /**< For external use of this header */
#endif
#endif




#ifdef __cplusplus
extern "C" {
#endif

typedef int Bln32;                              /**< Boolean compatible to older C      */
typedef int Int32;                              /**< Basic type                         */

/** Return values of functions */
#define NCB_Ok                   0              /**< No error                              */
#define NCB_Error              (-1)             /**< Unspecified error                     */
#define NCB_Timeout              1              /**< Communication timeout                 */
#define NCB_NotConnected         2              /**< No active connection to device        */
#define NCB_DriverError          3              /**< Error in comunication with driver     */
#define NCB_DeviceLocked         7              /**< Device is already in use by other     */
#define NCB_InvalidParam         9              /**< Parameter out of range                */
#define NCB_FeatureNotAvailable 10              /**< Feature only available in pro version */


/** @brief  Actor types                                                              */
typedef enum {
  ECC_actorLinear,                           /**< Actor is of linear type            */
  ECC_actorGonio,                            /**< Actor is of goniometer type        */
  ECC_actorRot                               /**< Actor is of rotator type           */
} ECC_actorType;


/** @brief  Information about a discovered device                                    */
struct EccInfo{
  int   id;                                  /**< Programmed ID of the device        */
  Bln32 locked;                              /**< Device locked by other program     */
};



/** @brief Check devices
 *
 *  Checks whether the USB driver is accessible and how many devices are connected.
 *  The function returns with a pointer to an array of struct @ref EccInfo. The return code
 *  tells how many devices were found (and so how many elements the array contains).
 *  The struct contains information wheteher a device is accessible (unlocked) or is already
 *  in use by someone else (locked). The device ID is a number programmed by the user
 *  to distinguish between multiple devices. It is valid only if the device is unlocked.
 *
 *  The function must not be called as long as a device is connected by @ref ECC_Connect.
 *  
 *  @param  info  Output: Pointer to array of struct 'EccInfo'. The parameter may be NULL
 *                to ignore the devices array.
 *  @return Count of found devices
 */
Int32 NCB_API ECC_Check( EccInfo** info );



/** @brief Inquire device properties
 *
 *  The function provides an alternative (and more labview friendly) way to
 *  inspect the devices found by @ref ECC_Check . It returns the locking state
 *  and device ID as provided in the info array of @ref ECC_Check .
 *  @param  deviceNo  Index of the device, must be smaller than the number of found devices.
 *  @param  devId     Output: Device ID, only valid if unlocked.
 *  @param  locked    Output: Locking state.
 */
Int32 NCB_API ECC_getDeviceInfo( Int32 deviceNo, Int32 * devId, Bln32 * locked );



/** @brief Release info
 *
 *  Releases the memory allocated by Ecc_Check for the info array.
 */
void NCB_API ECC_ReleaseInfo();




/** @brief Connect device
 *
 *  Initializes and connects the selected ECC (0..n).
 *  This has to be done before any other function (except ECC_Check and ECC_ReleaseInfo) can be called.
 *  The returned handle is used to address the device in further communication.
 *
 *  @param  deviceNo: Index of the device to connect (0..n)
 *  @param  deviceHandle: Pointer to (user)memory which will retrieve the Handle.
 *
 *  @return Result of function
 */
Int32 NCB_API ECC_Connect( Int32 deviceNo, Int32* deviceHandle );


/** @brief Close connection
 *
 *  Closes the connection to the device.
 *
 *  @param  deviceHandle: Handle of device
 *
 *  @return  Result:  NCB_Ok (exclusive result value)
 */
Int32 NCB_API ECC_Close( Int32 deviceHandle );






/** @brief Control output stage
 *
 *  Controls the output relais of the selected axis.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  enable        Switches the output relais
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlOutput( Int32 deviceHandle, 
                                 Int32 axis,
                                 Bln32* enable,
                                 Bln32 set );

/** @brief Control amplitude
 *
 *  Controls the amplitude of the actuator signal.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  amplitude     Amplitude in mV
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlAmplitude( Int32 deviceHandle, 
                                    Int32 axis,
                                    Int32* amplitude,
                                    Bln32 set );

/** @brief Control frequency
 *
 *  Controls the frequency of the actuator signal.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  frequency     Frequency in mHz
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlFrequency( Int32 deviceHandle,
                                    Int32 axis,
                                    Int32* frequency,
                                    Bln32 set );


/** @brief Control actor selection
 *
 *  Selects the actor to be used on selected axis from actor presets
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  actor         Actor selection [0..255]
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlActorSelection( Int32 deviceHandle,
                                         Int32 axis,
                                         Int32* actor,
                                         Bln32 set );

/** @brief Get actor name
 *
 *  Get the name of actual selected actor
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  name          Name of the actor coded as NULL-terminated c-string. User must provide at least 20 bytes allocated memory.
 *  @return               Result of function
 */
Int32 NCB_API ECC_getActorName( Int32 deviceHandle, 
                                Int32 axis,
                                char* name );



/** @brief Get actor type
 *
 *  Get the type of actual selected actor
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  type          Type of the actor. See @ref ECC_actorType
 *  @return               Result of function
 */
Int32 NCB_API ECC_getActorType( Int32 deviceHandle, 
                                Int32 axis,
                                ECC_actorType* type );


/** @brief Save params
 *
 *  Saves user parameters to persistant flash in controller. Parameters that will be saved are amplitude, 
 *  frequency and actor selection of each axis.
 *
 *  @param  deviceHandle  Handle of device

 *  @return               Result of function
 */
Int32 NCB_API ECC_setSaveParams( Int32 deviceHandle );



/** @brief Writing flash status
 *
 *  Retrieves the status of writing to flash. Indicates whether a write access
 *  to the flash is performing.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  writing       status
 *  @return               Result of function
 */
Int32 NCB_API ECC_getStatusFlash( Int32 deviceHandle, 
                                  Bln32* writing );



/** @brief Reset position
 *
 *  Resets the actual position to zero and marks the reference position as invalid.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @return               Result of function
 */
Int32 NCB_API ECC_setReset( Int32 deviceHandle,
                            Int32 axis );


/** @brief Control actor approach
 *
 *  Controls the approach of the actor to the target position
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  enable        Enables/ disables the approach
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlMove( Int32 deviceHandle,
                               Int32 axis,
                               Bln32* enable,
                               Bln32 set );


/** @brief Single step
 *
 *  Triggers a single step in desired direction.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  backward      Selects the desired direction. False triggers a forward step,
 *                        true a backward step.
 *  @return               Result of function
 */
Int32 NCB_API ECC_setSingleStep( Int32 deviceHandle,
                                 Int32 axis,
                                 Bln32 backward );


/** @brief Control continous movement forward
 *
 *  Controls continous movement in forward direction. If enabled a potential present movement
 *  in the opposite direction is stopped. The parameter "false" stops all movement of the axis regardless
 *  its direction.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  enable        Enables/ disables the movement
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlContinousFwd( Int32 deviceHandle,
                                       Int32 axis,
                                       Bln32* enable,
                                       Bln32 set );



/** @brief Control continous movement backward
 *
 *  Controls continous movement in backward direction. If enabled a potential present movement
 *  in the opposite direction is stopped. The parameter "false" stops all movement of the axis regardless
 *  its direction.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  enable        Enables/ disables the movement
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlContinousBkwd( Int32 deviceHandle,
                                        Int32 axis,
                                        Bln32* enable,
                                        Bln32 set );

/** @brief Control target position
 *
 *  Controls the target position for the approach function, see @ref ECC_controlMove .
 *  For linear type actors the position is defined in nm for goniometer an rotator type
 *  actors it is µ°.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  target        Target position in nm or µ° depending on actor type.
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlTargetPosition( Int32 deviceHandle,
                                         Int32 axis,
                                         Int32* target,
                                         Bln32 set );



/** @brief Reference status
 *
 *  Retrieves the status of the reference position. It may be valid or invalid.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  valid         Status of the reference position.
 *  @return               Result of function
 */
Int32 NCB_API ECC_getStatusReference( Int32 deviceHandle,
                                      Int32 axis,
                                      Bln32* valid );



/** @brief Moving status
 *
 *  Retrieves the status of the output stage. Moving means the actor is actively driven by the output stage
 *  either for approaching or continous/single stepping and the output is active. Pending means the output
 *  stage is driving but the output is deactivted i.e. by EOT or ECC_controlOutput.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  moving        Moving status, 0: Idle; 1: Moving; 2: Pending
 *  @return               Result of function
 */
Int32 NCB_API ECC_getStatusMoving( Int32 deviceHandle,
                                   Int32 axis,
                                   Int32* moving );



/** @brief Error status
 *
 *  Retrieves the error status. Indicates a sensor malfunction.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  error         Error status. "True" means the occurrence of an error.
 *  @return               Result of function
 */
Int32 NCB_API ECC_getStatusError( Int32 deviceHandle,
                                  Int32 axis,
                                  Bln32* error );



/** @brief Connected status
 *
 *  Retrieves the connected status. Indicates whether an actor is eletrically connected
 *  to the controller.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  connected     status
 *  @return               Result of function
 */
Int32 NCB_API ECC_getStatusConnected( Int32 deviceHandle,
                                      Int32 axis,
                                      Bln32* connected );




/** @brief Reference position
 *
 *  Retrieves the reference position. See @ref ECC_getStatusReference for 
 *  determining the validity.
 *  For linear type actors the position is defined in nm for goniometer an rotator type
 *  actors it is µ°.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  reference     Reference position in nm or µ° depending on actor type.
 *  @return               Result of function
 */
Int32 NCB_API ECC_getReferencePosition( Int32 deviceHandle,
                                        Int32 axis,
                                        Int32* reference );



/** @brief Actor position
 *
 *  Retrieves the current actor position.
 *  For linear type actors the position is defined in nm for goniometer an rotator type
 *  actors it is µ°.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  position      Actor position in nm or µ° depending on actor type.
 *  @return               Result of function
 */
Int32 NCB_API ECC_getPosition( Int32 deviceHandle,
                               Int32 axis,
                               Int32* position );


/** @brief Reference Auto Update
 *
 *  When set, every time the reference marking is hit the reference position
 *  will be updated. When this function is disabled, the reference marking will
 *  be considered only the first time and after then ignored.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  enable        enable/disable auto update
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return            Result of function
 */
Int32 NCB_API ECC_controlReferenceAutoUpdate( Int32 deviceHandle,
                                              Int32 axis,
                                              Bln32* enable,
                                              Bln32 set);


/** @brief Auto Reset
 *
 *  Resets the position for every time the reference position is detected.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  enable        enables/ disable functionality
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlAutoReset( Int32 deviceHandle,
                                    Int32 axis,
                                    Bln32* enable,
                                    Bln32 set);


/** @brief Target Range
 *
 *  Defines the range around the target position in which the flag target status
 *  become active.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  range         range in nm or µ° depending on actor type.
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlTargetRange( Int32 deviceHandle,
                                      Int32 axis,
                                      Int32* range,
                                      Bln32 set);

/** @brief Target status
 *
 *  Retrieves the target status. Indicates whether the actual position is within
 *  the target range.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  target        actual position is within target range
 *  @return               Result of function
 */
Int32 NCB_API ECC_getStatusTargetRange( Int32 deviceHandle,
                                        Int32 axis,
                                        Bln32* target );


/** @brief Firmware version
 *
 *  Retrieves the version of actual firmware
 *
 *  @param  deviceHandle  Handle of device
 *  @param  version       version number
 *  @return               Result of function
 */
Int32 NCB_API ECC_getFirmwareVersion( Int32 deviceHandle,
                                      Int32* version );


/** @brief Device id
 *
 *  Sets and retrieve the device identifier. A set will save the
 *  id persistent in flash of the device.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  id            iud of the device.
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlDeviceId( Int32 deviceHandle,
                                   Int32* id,
                                   Bln32 set);


/** @brief EOT status forward
 *
 *  Retrieves the status of the end of travel (EOT) detection in forward direction.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  EotDetected   status
 *  @return               Result of function
 */
Int32 NCB_API ECC_getStatusEotFwd( Int32 deviceHandle,
                                   Int32 axis,
                                   Bln32* EotDetected);

/** @brief EOT status backward
 *
 *  Retrieves the status of the end of travel (EOT) detection in backward direction.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  EotDetected   status
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_getStatusEotBkwd( Int32 deviceHandle,
                                    Int32 axis,
                                    Bln32* EotDetected);

/** @brief Output deactivate on EOT
 *
 *  Defines the behavior of the output on EOT. If enabled, the output of the axis will be deactivated on positive EOT detection.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  enable        true:  Output will be deactiveted on EOT.
                          false: Output stays active on EOT
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlEotOutputDeactive( Int32 deviceHandle,
                                            Int32 axis,
                                            Bln32* enable,
                                            Bln32 set);




/** @brief Fixed output voltage
 *
 *  Controls the DC level on the output.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  voltage       Output voltage in µV
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlFixOutputVoltage( Int32 deviceHandle,
                                           Int32 axis,
                                           Int32* voltage,
                                           Bln32 set);



/** @brief Trigger input enable
 *
 *  Controls the input trigger for steps.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  enable        true:  External trigger enabled
 *                        false: External trigger disabled
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlExtTrigger( Int32 deviceHandle,
                                     Int32 axis,
                                     Bln32* enable,
                                     Bln32 set);



/** @brief AQuadB input enable
 *
 *  Controls the AQuadB input for setpoint parameter.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  enable        true:  AQuadB input enabled
 *                        false: AQuadB input disabled
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlAQuadBIn( Int32 deviceHandle,
                                   Int32 axis,
                                   Bln32* enable,
                                   Bln32 set);

/** @brief AQuadB input resolution
 *
 *  Controls the AQuadB input resolution for setpoint parameter.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  resolution    resolution in nm
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlAQuadBInResolution( Int32 deviceHandle,
                                             Int32 axis,
                                             Int32* resolution,
                                             Bln32 set);


/** @brief AQuadB output enable
 *
 *  Controls the AQuadB output for position indication.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  enable        true:  AQuadB output enabled
 *                        false: AQuadB output disabled
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlAQuadBOut( Int32 deviceHandle,
                                    Int32 axis,
                                    Bln32* enable,
                                    Bln32 set);

/** @brief AQuadB output resolution
 *
 *  Controls the AQuadB output resolution for position indication.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  resolution    resolution in nm
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlAQuadBOutResolution( Int32 deviceHandle,
                                              Int32 axis,
                                              Int32* resolution,
                                              Bln32 set);

/** @brief AQuadB output clock
 *
 *  Controls the clock for AQuadB output.
 *
 *  @param  deviceHandle  Handle of device
 *  @param  axis          Number of the axis to be configured
 *  @param  clock         Clock in multiples of 20ns. Minimum 2 (40ns), maximum 65535 (1,310700ms)
 *  @param  set           1: Send the supplied values to the controller
 *                        0: Ignore input; only retreive the results
 *  @return               Result of function
 */
Int32 NCB_API ECC_controlAQuadBOutClock( Int32 deviceHandle,
                                         Int32 axis,
                                         Int32* clock,
                                         Bln32 set);



#ifdef __cplusplus
}
#endif

#endif 


