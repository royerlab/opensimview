
#include <mirao52e.h>
#include <stdio.h>
#include <conio.h>
#include <windows.h>

#include "MiraoMessages.h"
#include "CommandApplying.h"
#include "CommandStock.h"
#include "DeviceInformation.h"
#include "Monitoring.h"
#include "CommandFile.h"


void mainMenuLoop();


/**
 * Example program entry point.
 * 
 * - Show some information about the program,
 * - opens the mirao 52-e device,
 * - call the menu,
 * - closes the mirao 52-e device.
 *
 * Use the mirao 52-e functions:
 * MroBoolean mro_getVersion( char* version, int* status )
 * MroBoolean mro_open( int* status )
 * MroBoolean mro_close( int* status )
 */
int main() {

	// Integer to store the mirao DLL functions calls status
	int status;

	// Character string used to store the DLL version.
	char dllVersion[ 32 ];

	// Print the header
	printf( "------------------------------\n" );
	printf( "| Mirao 52-e example program |\n" );
	printf( "------------------------------\n\n" );


	// Retrieves the mirao 52-e DLL version...
	if( mro_getVersion( dllVersion, &status ) == MRO_FALSE ) {
		//... an error occured, print the error message and return.
		printf( "<!> DLL Version read error: %s\n", getMiraoErrorMessage( status ) );
		return 0;
	}

	// ... and display it.
	printf( "DLL version %s\n\n\n", dllVersion );


	// Opens the mirao 52-e device...
	if( mro_open( &status ) == MRO_FALSE ) {
		//... an error occured, print the error message and exit.
		printf( "<!> Open error: %s\n", getMiraoErrorMessage( status ) );
	}
	else {
		//... device opened
		printf( "mirao 52-e device opened.\n" );

		// Launch the main menu loop.
		mainMenuLoop();

		// Closes the mirao 52-e device...
		printf("\nClosing mirao 52-e device...\n");
		if( mro_close( &status ) == MRO_FALSE ) {
			//... an error occured, print the error message.
			printf( "<!> Close error: %s\n", getMiraoErrorMessage( status ) );
		}
		else {
			//... OK, mirao closed.
			printf( "mirao 52-e device closed.\n" );
		}
	}


	// Program terminated, just print a message and exit.
	printf( "\n\nProgram terminated. Press any key to exit." );
	getchar();
	return 0;
}



/**
 * Show the main menu.
 */
void mainMenuLoop() {

	// Variable to contain the user key
	char option = 0;

	printf( "\n\n" );

	while( 1 ) {

		// Print the menu
		printf( ">\n>\n> Main menu:\n>\n" );
		printf( ">\t1. Command applying ...\n" );
		printf( ">\t2. Stock management ...\n" );
		printf( ">\t3. Command file utils ...\n" );
		printf( ">\t4. Toggle monitoring\n" );
		printf( ">\t5. Print device information\n" );
		printf( ">\tQ. Quit\n" );
		printf( ">\n>\n> Choice: " );

		// Get the option choosed by the user
		option = getch();
		printf( "%c\n>\n", option );

		// Performs an action according to the choice of the user.
		switch( option ) {

			// Option "Command applying", see CommandApplying.cpp
			case '1':	commandApplyingMenu();		break;

			// Option "Stock management", see CommandStock.cpp
			case '2':	commandStockMenu();			break;

			// Option "Command file utils", see CommandFile.cpp
			case '3':	commandFileMenu();			break;

			// Option "Toggle monitoring", see Monitoring.cpp
			case '4':	
				printf( "\n" );
				toggleMonitoring();
				printf( "\n\n" );
				break;

			// Option "Print device information", see DeviceInformation.cpp
			case '5':	printDeviceInformation();	break;

			// Option "Quit", return;
			case 'q': case 'Q':						return;

		}

	}

}

