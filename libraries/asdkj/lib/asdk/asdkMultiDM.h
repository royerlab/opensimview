/*****************************************************************************
 * ALPAO - SDK - Driver
 * @brief Handle multiple deformable mirror using the same hardware interface
 *****************************************************************************/

#ifndef ASDK_MULTI_DM_H_
#define ASDK_MULTI_DM_H_

/* Local Headers */
#include "asdkType.h"
#include <vector>

//! Import/Export macro
#if defined(ASDK_EXPORTS)
# define ASDK_API ACS_API_EXPORTS
#else
# define ASDK_API ACS_API_IMPORTS
#endif


/**
 * Namespace: Alpao Common Software
 */
namespace acs
{

/**
 * @addtogroup adr
 * @{
 */
    
/* Class prototype */
class Packer;

/**
 * Type of DM list
 */
typedef std::vector<Packer*> DM_LIST_T;
#if IS_WIN32
// Disable warning: class 'type' needs to have dll-interface to be used by clients of class 'type2'
# pragma warning( disable : 4251 )
#endif

/**
 * @brief Class to drive deformable mirror
 *
 *  This class is only useful if all the mirrors are on the same electronics,
 *  it allows to reduce the latency between each mirror. Especially in the context
 *  of pattern generation.
 */ 
class ASDK_API MultiDM
{

public:

    //! @name CONSTRUCTORS
    //--------------------------------------------------------------------
    /** Default constructor
     */
    MultiDM();
    
    /** Default destructor
     */
    virtual ~MultiDM();
    
    //! @name METHODS
    //--------------------------------------------------------------------
    
    /** Add one mirror to the list, all mirror should be one the same electronics.
     *
     * @param[in] serialNumber Serial number of the mirror (eg: "BXXYYY" ).
     */
    COMPL_STAT Add( CStrConst serialNumber );
    
    /** Remove one mirror from the list.
     */
    COMPL_STAT Remove( Size_T index );

    /** Send value to the mirror, values are normalized between -1 and 1
     * 
     * @param[in] values Array of values to be sent to the mirror,
     *                   the number of element should be equal to
     *                   the number of actuator. 
     *
     * @return FAILURE in case of failure, SUCCESS otherwise.
     */
    COMPL_STAT Send( const Scalar * values );
    
    /** Set all actuators to the value zero.
     *
     * @return FAILURE in case of failure, SUCCESS otherwise.
     */
    COMPL_STAT Reset();
    
    /** Stops all current transfer (send, pattern ...)
     *
     * @return FAILURE in case of failure, SUCCESS otherwise.
     */
    COMPL_STAT Stop();

    /** @brief Send one pattern to all mirrors.
     * Mirrors should have the same number of actuators.
     * Patterns are a set of precalculated values sent with greater speed.
     * 
     * @param[in] values   Array of values to be sent to the mirror,
     *                   the number of element should be equal to the number
     *                   of actuator multiplied by the number of patterns.
     * @param[in] nPattern Number of patterns to be sent.
     * @param[in] nRepeat  Number of time to repeat that pattern (some interface
     *                   does not allow you to use this feature).
     *
     * @return FAILURE in case of failure, SUCCESS otherwise.
     */
    COMPL_STAT Send( const Scalar * patterns, UInt nPattern, UInt nRepeat = 1 );
    
    /** @brief Send one pattern per mirror.
     * Patterns are a set of precalculated values sent with greater speed.
     * 
     * @param[in] values   Array of arrays of values to be sent to each mirror,
     *                   the number of element should be equal to the number
     *                   of actuator multiplied by the number of patterns, index
     *                   by the mirror id ( e.g.: values[ nDm ][ nAct * nPattern ] ).
     * @param[in] nPattern Number of patterns to be sent.
     * @param[in] nRepeat  Number of time to repeat that pattern (some interface
     *                   does not allow you to use this feature).
     *
     * @return FAILURE in case of failure, SUCCESS otherwise.
     */
    COMPL_STAT Send( Scalar const* const* patterns, UInt nPattern, UInt nRepeat = 1 );
    
    //! Value for multi-mirror handling
    /** Get the total number of actuators.
     */
    UInt   GetNbOfActuator() const;
    
    /** Get the number of mirrors. 
     */
    UInt   GetNbOfDM() const;

    /** Get parameter value
     *
     * @param[in] index   Index of the mirror, from 0 to nDm-1.
     * @param[in] command Parameter name, see the documentation for
     *            a list of allowed parameters.
     *
     * @return If found, return the scalar value of the parameter,
     *         you should cast that value to the wanted type. If not found,
     *         return 0 and Check() will return false.
     */
    Scalar Get( Size_T index, CStrConst command ) const;

    /** Set parameter value
     *
     * @param[in] index   Index of the mirror, from 0 to nDm-1.
     * @param[in] command Parameter name, see the documentation for
     *            a list of allowed parameters.
     * @param[in] value   Parameter value (numeric).
     *
     * Use method Check() to determine if the parameter was set correctly.
     */
    void   Set( Size_T index, CStrConst command, Scalar value );
    void   Set( Size_T index, CStrConst command, Int    value );
    
    /** Set parameter value
     *
     * @param[in] index   Index of the mirror, from 0 to nDm-1.
     * @param[in] command Parameter name, see the documentation for
     *            a list of allowed parameters.
     * @param[in] str     Parameter value (null-terminated string).
     *
     * Use method Check() to determine if the parameter was set correctly.
     */   
    void   Set( Size_T index, CStrConst command, CStrConst str );

    //! @name ERROR HANDLER
    /** Global error status of Alpao SDK
     *
     * @return false if one of function for the SDK fail, true otherwise.
     */
    static Bool Check();

    /** Print last message to the standard output (stdout or stderr)
     */
    static UInt PrintLastError();
    
    /** Get the last message
     *
     * @param[in|out] message Description of the error (null-terminated string).
     * @param[in]     size    Size of buffer for the error description.
     *
     * To retrieve only the error code, you can call the function by
     * specifying NULL message and size zero.
     *
     * @return Error unique number.
     */
    static UInt GetLastError( CString message, Size_T size );

private:
    //! @name ATTRIBUTES
    //--------------------------------------------------------------------
    DM_LIST_T _list;
};

//! Human readable status and message
ASDK_API std::ostream& operator<<( std::ostream&, const MultiDM& );

#if IS_WIN32
# pragma warning( default : 4251 )
#endif

/** @} */

}    // End namespace

#endif /* ASDK_MULTI_DM_H_ */
