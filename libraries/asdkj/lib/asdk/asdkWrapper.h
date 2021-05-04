/*************************************************************************
 * ALPAO - SDK - Driver
 * @brief Wrapper for C code
 ************************************************************************/

#ifndef ASDK_WRAPPER_H_
#define ASDK_WRAPPER_H_

#include "asdkType.h"

/** Import/Export macro */
#if defined(ASDK_EXPORTS)
# define ASDK_API ACS_API_EXPORTS
#else
# define ASDK_API ACS_API_IMPORTS
#endif

/**
 * @file
 * Public declarations SDK C API
 */

#ifdef __cplusplus
 using namespace acs;
 namespace acs {
    typedef class DM asdkDM;
 }

  extern "C" {

#else /* C prototype for class acs::DM */
 typedef struct DM asdkDM;
#endif
    
/**
 * Initialise DM connection.
 *
 * @param[in]  serialName Serial names of the DM.
 *
 * @return On success, pointer to DM object is returned. Otherwise, NULL is
 * returned and asdkGetLastError() can be used to get error description.
 */
ASDK_API asdkDM * asdkInit( CStrConst serialName );

/**
 * Release DM.
 *
 * @param[in] pDm Pointer to DM object.
 *
 * @return On success, SUCCESS is returned. Otherwise, FAILURE is
 * returned and asdkGetLastError() can be used to get error description.
 */
ASDK_API COMPL_STAT asdkRelease( asdkDM *pDm );

/* Send Intensity */
/**
 * Send arrays to electronics.
 *
 * @param[in] pDm   Pointer to DM object.
 * @param[in] value Array of nAct*nDm with values to send for each DM,
 *                   valid range = [-1:1].
 *
 * @return On success, SUCCESS is returned. Otherwise, FAILURE is
 * returned and asdkGetLastError() can be used to get error description.
 */
ASDK_API COMPL_STAT asdkSend( asdkDM *pDm, const Scalar *value );

/**
 * Reset mirror values.
 *
 * @param[in] pDm   Pointer to DM object.
 *
 * @return On success, SUCCESS is returned. Otherwise, FAILURE is
 * returned and asdkGetLastError() can be used to get error description.
 */
ASDK_API COMPL_STAT asdkReset( asdkDM *pDm );

/* Send Pattern */
/**
 * Send Asynchronous Pattern.
 *
 * @param[in] pDm      Pointer to DM object.
 * @param[in] pattern  Array of nAct * nPattern elements with patterns to send.
 * @param[in] nPattern Number of patterns to send.
 * @param[in] nRepeat  Number of times the data in the buffer is to be sent. A value
 *                     of 0 means that digital output operation proceeds indefinitely.
 * 
 * @warning If you use pattern generation with several DMs, but only a single DAQ card,
 *          a DM will be queued until the end of execution of the preceding DM.
 *
 * @return On success, SUCCESS is returned. Otherwise, FAILURE is
 * returned and asdkGetLastError() can be used to get error description.
 */
ASDK_API COMPL_STAT asdkSendPattern( asdkDM *pDm, const Scalar *pattern, UInt nPattern, UInt nRepeat );

/**
 * Stop asynchronous transfer.
 *
 * @param[in] pDm Pointer to DM object.
 *
 * @warning This function is dependent on the interface, all DMs on the same
 *          interface can be stopped.
 *
 * @return On success, SUCCESS is returned. Otherwise, FAILURE is
 * returned and asdkGetLastError() can be used to get error description.
 */
ASDK_API COMPL_STAT asdkStop( asdkDM *pDm );


/* Accessors */
/**
 * Get value of one parameter 
 *
 * @param[in]  pDm      Pointer to DM object.
 * @param[in]  command  Parameter name.
 * @param[out] value    Returned value.
 *
 * @return On success, SUCCESS is returned. Otherwise, FAILURE is
 * returned and asdkGetLastError() can be used to get an error description.
 */
ASDK_API COMPL_STAT asdkGet( asdkDM *pDm, CStrConst command, Scalar * value );

/**
 * Set value of one parameter 
 *
 * @param[in] pDm      Pointer to DM object.
 * @param[in] command  Parameter name.
 * @param[in] value    New value.
 *
 * @return On success, acecsSUCCESS is returned. Otherwise, FAILURE is
 * returned and asdkGetLastError() can be used to get an error description.
 */
ASDK_API COMPL_STAT asdkSet( asdkDM *pDm, CStrConst command, Scalar value );

/**
 * Get value of one parameter 
 *
 * @param[in] pDm      Pointer to DM object.
 * @param[in] command  Parameter name.
 * @param[in] cstr     New value for the parameter.
 *
 * @return On success, SUCCESS is returned. Otherwise, FAILURE is
 * returned and asdkGetLastError() can be used to get an error description.
 */
ASDK_API COMPL_STAT asdkSetString( asdkDM *pDm, CStrConst command, CStrConst cstr );

/* Error handling */
/**
 * @brief Displays the last error in stack.
 *
 * It display on standard output (stdout or stderr), the latest error currently
 * stored in the error stack.
 */
ASDK_API void asdkPrintLastError();

/**
 * @brief Get the last error status.
 *
 * @param[out] errorNo Error number.
 * @param[out] errMsg  Pointer to the error message.
 * @param[in]  errSize Size of the errMsg buffer.
 *
 * @return On success, SUCCESS is returned. Otherwise, if stack is empty,
 * FAILURE is returned.
 */
ASDK_API COMPL_STAT asdkGetLastError( UInt *errorNo, CString errMsg, Size_T errSize );

#ifdef __cplusplus
}
#endif

#endif /* ASDK_WRAPPER_H_ */
