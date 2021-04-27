// Alpao SDK Header: All types and class are in the ACS namespace
#include "asdkDM.h"
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
	cin.clear();
    cin.ignore( std::numeric_limits <std::streamsize> ::max(), '\n' );
}

// Example using C++ API
int dmExample()
{
	String serial;

	// Get serial number
    cout << "Please enter the S/N within the following format: BXXYYY (see DM backside)" << endl;
	cin >> serial;
    cin.ignore( 10, '\n' );

    // Load configuration file
    DM dm( serial.c_str() );

    // Get the number of actuators
    UInt nbAct = (UInt) dm.Get( "NbOfActuator" );

    // Check errors
    if ( !dm.Check() )
    {
        return -1;
    }
    
    cout << "Number of actuators: " << nbAct << endl;

    // Initialize data
    Scalar *data = new Scalar[nbAct];
    for ( UInt i = 0 ; i < nbAct ; i++ )
    {
        data[i] = 0;
    }
	
    cout << "Send data on mirror (data LED should blink): " << endl;
    // Send value to the DM
    for ( UInt act = 0; act < nbAct && dm.Check(); act++ )
    {
		cout << ".";

        data[ act ] = 0.12;
        dm.Send( data );
		Sleep( 200 );
        data[ act ] = 0;
    }
    cout << "Done." << endl;
    
    // Release memory
    delete [] data;

    return 0;
}

// Main program
int main( int argc, char ** argv )
{
    int ret = dmExample();
    
    // Print last errors if any
    while ( !DM::Check() ) DM::PrintLastError();

    pause();
    return ret;
}