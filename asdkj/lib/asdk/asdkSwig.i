/*************************************************************************
 * ALPAO - SDK - SWIG Interface file
 *
 * This file help you to interface ASDK v3 libraries with other languages.
 * It was originally written to build Python module.
 *
 * SWIG (Simplified Wrapper and Interface Generator) is a software development
 * tool for building scripting language interfaces to C and C++ programs.
 * In a nutshell, SWIG is a compiler that takes C declarations and creates
 * the wrappers needed to access those declarations from other languages
 * including Perl, Python, Tcl, Ruby, Guile, and Java.
 *
 * @see http://www.swig.org/
 ************************************************************************/
 
%module (docstring="Alpao SDK module") asdk

// Includes
%include "windows.i"
%include "typemaps.i"
%include "carrays.i"
%include "cstring.i"
%include "stdint.i"
%include "constraints.i"
%include "exception.i"

%{
#include "asdkDM.h"
%}

// Ignore the default constructor
%ignore *::operator();
%ignore *::operator acs::Bool;
%ignore *::operator Bool;
%ignore operator<<;
%ignore IS_WIN32;
%ignore IS_64B;
%nodefaultctor DM;
%ignore *::PrintLastError;

// Parse types
%import "asdkType.h"

%apply unsigned int NONZERO  {acs::UInt nPattern};
%apply unsigned int NONZERO  {acs::UInt nRepeat};


#if SWIGPYTHON
%feature("autodoc");

%typemap(typecheck) acs::Scalar const *values
{
    if (PySequence_Check($input))
    {
         $1 = true;
     } else {
         SWIG_exception(SWIG_TypeError, "sequence expected");
     }
}

%typemap(in) acs::Scalar const *values  {
  /* Check if is a list */
  if (!PySequence_Check($input))
  {
    PyErr_SetString(PyExc_ValueError,"Expected a sequence");
    return NULL;
  }
  else
  {
    size_t ln = PySequence_Length($input);
    $1 = (acs::Scalar*) malloc((ln)*sizeof(acs::Scalar));
    
    for (size_t i = 0; i < ln; i++)
    {        
        PyObject *o = PySequence_GetItem($input,i);
        if (PyNumber_Check(o)) {
          $1[i] = (double) PyFloat_AsDouble(o);
        } else {
          PyErr_SetString(PyExc_ValueError,"Sequence elements must be numbers");      
          return NULL;
        }
    }
  }
}

// This cleans up
%typemap(freearg) acs::Scalar const *values
{
  free((acs::Scalar *) $1);
}
#endif

/* Define string returns */
%cstring_bounded_output(acs::CString message, 1024);
// Don't use size parameter
%typemap(in, numinputs=0) acs::Size_T size {
    $1 = 1024;
}

%exception {
    $action
    if (!acs::DM::Check())
    {
        static char mess[1024] = "";
        acs::DM::GetLastError(mess, 1024);
        SWIG_exception(SWIG_SystemError, mess);
    }
}

// Parse the original header file
%include "asdkDM.h"

#if SWIGPYTHON
%extend acs::DM {
    char *__str__() {
			static char temp[1024];
			char mess[1024];
			acs::UInt code;
            code = $self->GetLastError(mess, 1024);
			snprintf(temp, 1024, "[ErrId: %u - %s]", code, mess);
			return &temp[0];
		}
}
#endif

// Always put an new line at EOF
