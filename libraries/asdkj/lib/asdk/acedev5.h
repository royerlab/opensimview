/*************************************************************************
 * ALPAO - SDK - Driver Legacy
 * @brief Compatibility layer for SDK v2.*
 ************************************************************************/

#ifndef ASDK_ACE_DEV5_H_
#define ASDK_ACE_DEV5_H_

/**
 * @file
 * Public declarations of Alpao Core Engine Digital Electronic v5 (acedev5).
 */

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Macro to export or import functions from library
 */
#ifndef DOXYGEN /* Code fragment ignored by Doxygen */
# if defined(WIN32)
#  define ACS_API_EXPORTS __declspec(dllexport)
#  define ACS_API_IMPORTS __declspec(dllimport)
# elif __GNUC__ >= 4 
#  define ACS_API_EXPORTS __attribute__((__visibility__("default")))
#  define ACS_API_IMPORTS 
# endif

# if defined(ASDK_EXPORTS)
#  define ACEDEV5_API ACS_API_EXPORTS
# else
#  define ACEDEV5_API ACS_API_IMPORTS
# endif

#endif /*DOXYGEN*/
    
/**
 * Completion status returned by subroutines
 */
typedef enum
{
    acecsFAILURE = -1,     /**< Completion failure. */
    acecsSUCCESS           /**< Successful completion */
} acecsCOMPL_STAT;

/* Init / Release functions */
/**
 * Initialise DMs connections.
 *
 * @param[in]  nDm   Number of DM to initialize.
 * @param[out] dmIds Array of nDm elements to receive DM identifier.
 * @param[in]  ...   Serial names (char*) of each DM.
 *
 * @return On success, acecsSUCCESS is returned. Otherwise, acecsFAILURE is
 * returned and acecsErrGetStatus() can be used to get error description.
 */
ACEDEV5_API acecsCOMPL_STAT acedev5Init(int nDm, int *dmIds, /* const char * serialName1 */ ...);

/**
 * Release DMs.
 *
 * @param[in]  nDm   Number of DM to release.
 * @param[in]  dmIds Array of nDm element with DMs identifier.
 *
 * @return On success, acecsSUCCESS is returned. Otherwise, acecsFAILURE is
 * returned and acecsErrGetStatus() can be used to get error description.
 */
ACEDEV5_API acecsCOMPL_STAT acedev5Release(int nDm, const int *dmIds);

/* Send Intensity */
/**
 * Send arrays to electronics.
 *
 * @param[in]  nDm   Number of DM to drive.
 * @param[in]  dmIds Array of nDm element with DMs identifier.
 * @param[in]  value Array of nAct*nDm with values to send for each DM,
 *                   valid range = [-1:1].
 *
 * @return On success, acecsSUCCESS is returned. Otherwise, acecsFAILURE is
 * returned and acecsErrGetStatus() can be used to get error description.
 */
ACEDEV5_API acecsCOMPL_STAT acedev5Send(int nDm, const int *dmIds, const double *value);

/* Send Pattern */
/**
 * Send Asynchronous Patterns.
 * The same pattern is send to all DMs.
 *
 * @param[in] nDm      Number of DM to drive.
 * @param[in] dmIds    Array of nDm elements with DM identifier.
 * @param[in] pattern  Array of nAct * nPattern elements with patterns to send.
 * @param[in] nPattern Number of patterns to send.
 * @param[in] nRepeat  Number of times the data in the buffer is to be sent. A value
 *                     of 0 means that digital output operation proceeds indefinitely.
 * 
 * @warning If you use pattern generation with several DMs, but only a single DAQ card,
 *          a DM will be queued until the end of execution of the preceding DM.
 *
 * @return On success, acecsSUCCESS is returned. Otherwise, acecsFAILURE is
 * returned and acecsErrGetStatus() can be used to get an error description.
 */
ACEDEV5_API acecsCOMPL_STAT acedev5StartPattern(int nDm, const int *dmIds, const double *pattern, int nPattern, int nRepeat);

/**
 * Send multiple patterns to DAC (one per DM).
 *
 * @param[in] nDm      Number of DMs to drive.
 * @param[in] dmIds    Array of nDm elements with DM identifier.
 * @param[in] patterns List of patterns array per DM: nDm * (nAct * nPattern)
 * @param[in] nPattern Number of pattern to send per DM.
 * @param[in] nRepeat  Number of times the data in the Buffer is to be sent. A value
 *                     of 0 means that digital output operation proceeds indefinitely.
 *
 * @par Example
 * For example, using several DMs with the same number of actuators:
 *   double patterns[nDm][ (nAct)*nPattern ];
 * If the mirror models are not identical:
 *   double * patterns[nDm];
 *   for ( int idx; idx < nDm; i++ )
 *     patterns[ idx ] = (double*) calloc( nAct[ idx ] * nPattern, sizeof(double) );
 *   
 * @warning All DMs should be on the same electronic box. 
 *
 * @return On success, acecsSUCCESS is returned. Otherwise, acecsFAILURE is
 * returned and acecsErrGetStatus() can be used to get an error description.
 */
ACEDEV5_API acecsCOMPL_STAT acedev5StartMultiPattern(int nDm, const int *dmIds, double const* const* patterns, int nPattern, int nRepeat);

/**
 * Stop Patterns.
 *
 * @param[in] nDm   Number of DMs to drive.
 * @param[in] dmIds Array of nDm elements with DM identifier.
 *
 * @warning This function is dependent on the DAQ interface, all DMs on the same
 *          interface will be stopped.
 *
 * @return On success, acecsSUCCESS is returned. Otherwise, acecsFAILURE is
 * returned and acecsErrGetStatus() can be used to get an error description.
 */
ACEDEV5_API acecsCOMPL_STAT acedev5StopPattern(int nDm, const int *dmIds);


/* Alias for compatibility with SDK v2.x */

/**
 * Logging level (to stdout)
 */
typedef enum
{
    acecsLOG_ERROR = -1, /**< Error messages */
    acecsLOG_QUIET,      /**< No echo */
    acecsLOG_WARNING,    /**< Abnormal events for application */
    acecsLOG_INFO,       /**< Major events (e.g when command is received).*/
    acecsLOG_TEST,       /**< @deprecated */
    acecsLOG_TRACE       /**< @deprecated */
} acecsLOG_LEVEL;

/**
 * Boolean
 */
typedef enum
{
    acecsFALSE = 0, /**< False */
    acecsTRUE       /**< True */
} acecsBOOLEAN;

/**  Maximum size of the error message */
#define acecsERR_MSG_MAX_LEN 1024

/** Error message definition */
typedef char acecsERR_MSG[acecsERR_MSG_MAX_LEN];

/**
 * @brief Configure logging service.
 *
 * It set the verbose level of logging service.
 *
 * @param[in] level         Lowest level of messages to be printed out.
 * @param[in] enabled       Not used (compatibility purpose)
 * @param[in] printDetails  Not used (compatibility purpose)
 */
ACEDEV5_API void acecsLogSet(int enabled, int level, int printDetails);

/**
 * @brief Displays the lastest error in stack.
 *
 * It display on standard output (stdout or stderr), the latest error currently
 * stored in the error stack.
 */
ACEDEV5_API void acecsErrDisplay();

/**
 * @brief Get the last error status.
 *
 * It gets the last message.
 *
 * @param[out] errMsg  Pointer to the error message.
 * @param[out] errorId Error Id (IDs returned correspond to SDK 3).
 *
 * @return On success, acecsSUCCESS is returned. Otherwise, if stack is empty,
 * acecsFAILURE is returned.
 */
ACEDEV5_API acecsCOMPL_STAT acecsErrGetStatus(int *errorId, acecsERR_MSG errMsg);

/**
 * Get number Of actuators.
 *
 * @param[in]  nDm         Number of DM to drive.
 * @param[in]  dmIds       Array of nDm element with DM identifier.
 * @param[out] nbActuator  Array of nDm with number of actuators for each DM.
 *
 * @return Always return acecsSUCCESS.
 */
ACEDEV5_API acecsCOMPL_STAT acedev5GetNbActuator(int nDm, const int *dmIds, int *nbActuator);

/**
 * Get offsets.
 *
 * @param[in]  nDm     Number of to drive.
 * @param[in]  dmIds   Array of nDm elements with DM identifier.
 * @param[out] offset  Array of nAct*nDm filled with offsets for each DM.
 *
 * @return Always return acecsSUCCESS.
 */
ACEDEV5_API acecsCOMPL_STAT acedev5GetOffset(int nDm, const int *dmIds, double *offset);

/**
 * Check electronic cards.
 *
 * @param[in] nDm   Number of DM to check.
 * @param[in] dmIds Array of nDm element with DMs identifier.
 *
 * @return On success, acecsSUCCESS is returned. Otherwise, acecsFAILURE is
 * returned.
 */
ACEDEV5_API acecsCOMPL_STAT acedev5CheckElectronic(int nDm, const int *dmIds);

/**
 * Reset DACs (Digital to Analogue Converters).
 *
 * @param[in] nDm   Number of DM to drive.
 * @param[in] dmIds Array of nDm elements with DM identifier.
 *
 * @return On success, acecsSUCCESS is returned. Otherwise, acecsFAILURE is
 * returned and acecsErrGetStatus() can be used to get error description.
 */
ACEDEV5_API acecsCOMPL_STAT acedev5SoftwareDACReset(int nDm, const int *dmIds);

/**
 * Query Pattern generation.
 *
 * @param[in]  nDm    Number of DMs to drive.
 * @param[in]  dmIds  Array of nDm elements with DM identifier.
 * @param[out] status Array of nDm elements to return status.
 *                    The value is 0 if the pattern generation has finished.
 *
 * @warning This function is dependent on the DAQ interface, all DMs on the same
 *          interface will return the same status.
 *
 * @return On success, acecsSUCCESS is returned. Otherwise, acecsFAILURE is
 * returned and acecsErrGetStatus() can be used to get error description.
 */
ACEDEV5_API acecsCOMPL_STAT acedev5QueryPattern(int nDm, const int *dmIds, int *status);

/* Trigger for pattern (the electronic trigger output is optional) */
/**
 * Enable trigger signal on electronics.
 *
 * @param[in] nDm   Number of DMs to drive.
 * @param[in] dmIds Array of nDm elements with DM identifier.
 *
 * @warning Yellow LED (data) will blink faster.
 *
 * @return On success, acecsSUCCESS is returned. Otherwise, acecsFAILURE is
 * returned and acecsErrGetStatus() can be used to get an error description.
 */
ACEDEV5_API acecsCOMPL_STAT acedev5EnableTrig(int nDm,  const int *dmIds);

/**
 * Disable trigger signal.
 *
 * @param[in] nDm   Number of DMs to drive.
 * @param[in] dmIds Array of nDm elements with DM identifier.
 *
 * @return On success, acecsSUCCESS is returned. Otherwise, acecsFAILURE is
 * returned and acecsErrGetStatus() can be used to get error and description.
 */
ACEDEV5_API acecsCOMPL_STAT acedev5DisableTrig(int nDm, const int *dmIds);

/**
 * Enable trigger signal on electronic for strobo, TTL signal will be sent
 * before each pattern repetition.
 *
 * @param[in] nDm   Number of DMs to drive.
 * @param[in] dmIds Array of nDm elements with DM identifier.
 *
 * @warning Yellow LED (data) will blink only before pattern repetition.
 *
 * @return On success, acecsSUCCESS is returned. Otherwise, acecsFAILURE is
 * returned and acecsErrGetStatus() can be used to get an error description.
 */
ACEDEV5_API acecsCOMPL_STAT acedev5EnableTrigStrobo(int nDm, const int *dmIds);

#undef ACEDEV5_API

#ifdef __cplusplus
}
#endif

#endif /* ASDK_ACE_DEV5_H_ */
