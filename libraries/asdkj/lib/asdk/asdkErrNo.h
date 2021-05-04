/*************************************************************************
 * ALPAO - SDK - Common
 * @brief Error number list
 ************************************************************************/

#ifndef ASDK_ERRNO_H_
#define ASDK_ERRNO_H_
/**
 * Namespace: Alpao Common Software
 */
namespace acs
{

/**
 * @addtogroup asdk
 * @{
 */

typedef enum {
    NoASDKError = 0x0000,     // No error or warning in the stack
    NoMoreAlloc,              // Out of memory
    TooManyDM,                // Cannot handle more DM
    NActMismatch,             // All DM handled using MultiDM should have the same number of actuators
    AlreadyLoad,              // The configuration file is already loaded

    /* Send / Get / Set */
    CommandNotFound = 0x0010, // Command not found (check spelling and access)
    InvalidRange,             // Parameter value not in range

    /* Initialisation */
    CannotLoadDll = 0x0020,   // Interface DLL cannot be loaded
    InvalidItfDll,            // Interface DLL is not part of ADSK or corrupted
    NoInterface,              // No hardware interface is connected
    
    NoCfgReader,              // Default configuration reader cannot be found
    CannotOpenCfg,            // Cannot open configuration file
    CannotReadCfg,            // Cannot read configuration file
    UnknowCmdCfg,             // Invalid command in configuration file
    
    CannotOpenACfg,           // Cannot open the ASCII configuration file
    UnknowCmdACfg,            // Invalid command in ASCII configuration file
    NoValueForCmdACfg,        // ASCII command without parameter

    /* Legacy C */
    InvalidNumDM = 0x0030,    // Invalid number of DM (negative value)
    InvalidIndex,             // Invalid index
    NotYetSupported,          // Command is not yet supported

    /* Hardware interface */
    MissingParameter = 0x0100,// N/A
    ItfNotConnected,          // Interface is in offline state
    DOTimeOut,                // Output data time-out (previous transfer not finished)
    DITimeOut,                // Input data time-out
    DOGeneric,                // Generic digital output error (from interface)
    DIGeneric,                // Generic digital input error (from interface)
    DOAsyncCheck,             // Cannot check digital write status
    DIAsyncCheck,             // Cannot check digital read status
    DOBufferClear,            // N/A
    DIBufferClear,            // N/A
    NotSupported,             // Function not supported by the current interface
    DriverApi,                // Driver error on interface initialisation
    OutBufferSize,            // Size of listened data is unknown
	AckTimeOut,               // Acknowledge time-out (Ethernet)
	TrigInTimeOut,            // Trigger input time-out (Ethernet)
} ASDK_ERR_T;

/** @} */

}    // End namespace

#endif /* !ASDK_ERRNO_H_ */
