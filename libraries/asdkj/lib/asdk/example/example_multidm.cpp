// Alpao SDK Header: All types and class are in the ACS namespace
#include "asdkMultiDM.h"
using namespace acs;

// System Headers
#include <iostream>
#include <windows.h>
using namespace std;

// Wait for human action
void pause()
{
    cout << endl << "Press ENTER to exit... " << endl << flush;
#undef max
    cin.ignore( std::numeric_limits <std::streamsize> ::max(), '\n' );
}

// Example using multiple mirrors
int multiDmExample( )
{
    // Load configuration file
    MultiDM     dms;
	const UInt  nbOfDM = 2;
	String      serial;

	// Get serial number
    cout << "Please enter the S/N within the following format: BXXYYY (see DM backside)" << endl;
	cin >> serial;
    cin.ignore( 10, '\n' );

    // Add new mirrors
	/* NOTE: For this example, we simulate the control of two mirrors using the same serial number,
	   you should use one serial number per mirror. */
	cout << "Add " << nbOfDM << " mirrors." << endl;
    for (UInt i = 0; i < nbOfDM && MultiDM::Check(); i++)
        dms.Add( serial.c_str() );
    
    // Get the number of actuators
    UInt nbAct = (UInt) dms.GetNbOfActuator();

    // Check errors
    if ( !dms.Check() )
    {
        return -1;
    }
    
    cout << "Total number of actuators: " << nbAct << endl;

    // Initialize data
    Scalar *data = new Scalar[nbAct];
    for ( UInt i = 0 ; i < nbAct ; i++ )
    {
        data[i] = 0;
    }

    // Send value to the DM
    cout << "Send data on mirrors (data LED should blink): " << endl;
    UInt act = 0;
    for ( UInt act = 0; act < nbAct && dms.Check(); act++ )
    {
		cout << ".";

        data[ act ] = 0.10;
        dms.Send( data );
		Sleep( 200 );
        data[ act ] = 0;
    }
    cout << "Done." <<  endl;

    // Reset mirror values
    dms.Reset( );

    // Release memory
    delete [] data;
    
    return 0;
}

// Main program
int main( int argc, char ** argv )
{
    int ret = multiDmExample();
    
    // Print last errors if any
    while ( !MultiDM::Check() ) MultiDM::PrintLastError();

    pause();
    return ret;
}