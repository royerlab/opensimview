/*************************************************************************
 * ALPAO - SDK - Driver
 * @brief Common types used in Alpao SDK
 ************************************************************************/

#ifndef ASDK_TYPE_H_
#define ASDK_TYPE_H_

/* Compiler informations */
#define IS_WIN32 1


#if __WORDSIZE == 64
# define IS_64B 1
#else
# define IS_64B 0
#endif

/* System Headers */
#include <stdint.h>


#define ACS_API_EXPORTS __declspec(dllexport)
#define ACS_API_IMPORTS __declspec(dllimport)


/**
 * @addtogroup asdk
 * @{
 */

typedef char        Char;       /**< signed char (8b)*/
typedef uint8_t     UChar;      /**< unsigned char (8b)*/

typedef int16_t     Short;      /**< signed short integer (16b)*/
typedef uint16_t    UShort;     /**< unsigned short integer (16b)*/

typedef int32_t     Int;        /**< signed integer (32b)*/
typedef uint32_t    UInt;       /**< unsigned integer (32b)*/

typedef int64_t     Long;       /**< signed long integer (64b)*/
typedef uint64_t    ULong;      /**< unsigned long integer (64b)*/

typedef size_t      Size_T;     /**< size type (target dependent)*/
typedef double      Scalar;     /**< Only one floating point type: standard scalar value*/

typedef enum {FALSE, TRUE}  Bool; /**< boolean*/

typedef char*        CString;     /**< C style string type*/
typedef char const * CStrConst;   /**< C style const string type*/

/**
 * @brief Completion status for any function
 */
typedef enum
{
    SUCCESS = 0,         /**< Function returns successfully*/
    FAILURE = -1         /**< Function returns with error*/
} COMPL_STAT;

/** @} */



#endif /* ASDK_TYPE_H_ */
