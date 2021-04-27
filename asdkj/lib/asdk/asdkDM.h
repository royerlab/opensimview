/*************************************************************************
 * ALPAO - SDK - Driver
 * @brief Deformable mirror class
 ************************************************************************/

#ifndef ASDK_DM_H_
#define ASDK_DM_H_

/* Local Headers */
#include "asdkType.h"

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
 * @brief Class to drive Alpao deformable mirror
 */ 
class ASDK_API  DM
{
public:
    //! @name CONSTRUCTORS
    //--------------------------------------------------------------------
    
    /** Default constructor
     *  
     * @param[in] serialNumber Serial number of the mirror (eg: "BXXYYY" ).
     *
     * Use method Check() to determine the validity of that object.
     */
    DM( CStrConst serialNumber );
    
    /**
     * Default destructor
     */
    virtual ~DM();
    
    //! @name METHODS
    //--------------------------------------------------------------------
    
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

    /** @brief Send patterns to the mirror.
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
    COMPL_STAT Send( const Scalar * values, UInt nPattern, UInt nRepeat = 1 );
    
public:
    //! @name ACCESSORS
    //--------------------------------------------------------------------

    /** Get parameter value
     *
     * @param[in] command Parameter name, see the documentation for
     *            a list of allowed parameters.
     *
     * @return If found, return the scalar value of the parameter,
     *         you should cast that value to the wanted type. If not found,
     *         return 0 and Check() will return false.
     */
    Scalar Get( CStrConst command ) const;

    
    /** Set parameter value
     *
     * @param[in] command Parameter name, see the documentation for
     *            a list of allowed parameters.
     * @param[in] value   Parameter value (numeric).
     *
     * Use method Check() to determine if the parameter was set correctly.
     */    
    void   Set( CStrConst command, Scalar value );
    void   Set( CStrConst command, Int    value );
    
    /** Set parameter value
     *
     * @param[in] command Parameter name, see the documentation for
     *            a list of allowed parameters.
     * @param[in] str     Parameter value (null-terminated string).
     *
     * Use method Check() to determine if the parameter was set correctly.
     */    
    void   Set( CStrConst command, CStrConst str );

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

    /* Alias for Get / Set / Check */ 
    Scalar operator ()( CStrConst command ) const;
    void   operator ()( CStrConst command, Scalar    value );
    void   operator ()( CStrConst command, CStrConst value );
    operator Bool() const;

private:
    Packer* _packer;
};

//! Human readable status and message
ASDK_API std::ostream& operator<<( std::ostream&, const DM& );

/** @} */

}    // End namespace

#endif /* ASDK_DM_H_ */
