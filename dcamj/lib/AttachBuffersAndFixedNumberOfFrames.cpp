// console/c44e_snap_attach.cpp
//	Modified from c44b_live_attach.cpp
//

#include	"../console.h"
#include	"../misc/common.h"

#include	<iostream>
using namespace std;

// calculate average center point
double calc_average( const void* buf, int32 rowbytes, DCAM_PIXELTYPE type, int32 width, int32 height )
{
	if( type != DCAM_PIXELTYPE_MONO16  )
	{
		// this only supports B/W 16
		return -1;
	}

	int32	cy = width  / 10;
	int32	cx = height / 10;
	if( cx < 10 )	cx = 10;
	if( cy < 10 )	cy = 10;
	if( cx > width || cy > height )
	{
		// frame is too small
		return -1;
	}

	int32	ox = (width-cx) / 2;
	int32	oy = (height-cy) / 2;

	const char*	src = (const char*)buf + rowbytes * oy;
	double	total = 0;
	
	int32	x, y;
	for( y = 0; y < cy; y++ )
	{
		const unsigned short*	s = (const unsigned short*)src + ox;
		for( x = 0; x < cx; x++ )
		{
			total += *s++;
		}
		src += rowbytes;
	}
	
	return total / cx / cy;
}

int my_dcamprop_getvalue( HDCAM hdcam, int32 idprop, int32& lValue )
{
	DCAMERR	err;
	double	value;
	err = dcamprop_getvalue( hdcam, idprop, &value );
	if( failed( err ) )
		return false;

	lValue = (int32)value;
	return true;
}

void wait_and_calc( HDCAM hdcam, HDCAMWAIT hwait, void** buffer )
{
	DCAMERR	err;
	int32	width, height, rowbytes, type;
	
	my_dcamprop_getvalue( hdcam, DCAM_IDPROP_IMAGE_WIDTH,      width );
	my_dcamprop_getvalue( hdcam, DCAM_IDPROP_IMAGE_HEIGHT,     height );
	my_dcamprop_getvalue( hdcam, DCAM_IDPROP_BUFFER_ROWBYTES,  rowbytes );
	my_dcamprop_getvalue( hdcam, DCAM_IDPROP_BUFFER_PIXELTYPE, type );

	DCAMWAIT_START	paramwait;
	memset( &paramwait, 0, sizeof(paramwait) );
	paramwait.size		= sizeof(paramwait);
	paramwait.eventmask	= DCAMCAP_EVENT_FRAMEREADY | DCAMCAP_EVENT_STOPPED;
	paramwait.timeout	= 1000;

	for( ;; )
	{
		err = dcamwait_start( hwait, &paramwait );
		if( err == DCAMERR_TIMEOUT || err == DCAMERR_LOSTFRAME )
		{
			printf( "." );
			continue;
		}

		if( failed( err ) )
		{
			dcamcon_show_dcamerr( err, "dcamwait_start()" );
			return;
		}

		// success
		if( paramwait.eventhappened == DCAMCAP_EVENT_STOPPED )
		{
			printf( " Capture end\n" );
			break;
		}

		if( paramwait.eventhappened == DCAMCAP_EVENT_FRAMEREADY )
			printf( "F" );
	}

	DCAMCAP_TRANSFERINFO	transferinfo;
	memset( &transferinfo, 0, sizeof(transferinfo) );
	transferinfo.size	= sizeof(transferinfo);
	err = dcamcap_transferinfo( hdcam, &transferinfo );
	if( failed( err ) )
	{
		dcamcon_show_dcamerr( err, "dcamcap_transferinfo()" );
		return;
	}

	int32	i;
	for( i = 0; i < transferinfo.nFrameCount; i++ )
	{
		void*	buf = buffer[ i ];
		double	v = calc_average( buf, rowbytes, (DCAM_PIXELTYPE)type, width, height );
		cout << v << "\n";
	}
}

int main( int argc, char * const argv[])
{
	// Initialize DCAM-API ver 4.0

	DCAMERR	err;
	HDCAM	hdcam;
	hdcam = dcamcon_init_open();
	if( hdcam != NULL )
	{
		dcamcon_cout_dcamdev_info( hdcam );

		DCAMWAIT_OPEN	waitopen;
		memset( &waitopen, 0, sizeof(waitopen) );
		waitopen.size = sizeof(waitopen);
		waitopen.hdcam	= hdcam;
		err = dcamwait_open( &waitopen );
		if( failed( err ) )
			dcamcon_show_dcamerr( err, "dcamwait_open" );
		else
		{
			HDCAMWAIT	hwait = waitopen.hwait;
			int32	number_of_buffer = 10;
			DCAMBUF_ATTACH	paramattach;
			memset( &paramattach, 0, sizeof(paramattach) );
			paramattach.size		= sizeof(paramattach);
			paramattach.buffer		= new void*[number_of_buffer];
			paramattach.buffercount	= number_of_buffer;

			int32	bufferbytes;
			my_dcamprop_getvalue( hdcam, DCAM_IDPROP_BUFFER_FRAMEBYTES, bufferbytes );
			int32	i;
			for( i = 0; i < number_of_buffer; i++ )
				paramattach.buffer[i] = new char[bufferbytes];

			err = dcambuf_attach( hdcam, &paramattach );
			if( failed( err ) )
				dcamcon_show_dcamerr( err, "dcambuf_alloc" );
			else
			{
				err = dcamcap_start( hdcam, DCAMCAP_START_SNAP );
				if( failed( err ) )
					dcamcon_show_dcamerr( err, "dcamcap_start" );
				else
				{
					wait_and_calc( hdcam, hwait, paramattach.buffer );
					
					dcamcap_stop( hdcam );
				}
				dcambuf_release( hdcam );
			}

			for( i = 0; i < number_of_buffer; i++ )
				delete (char*)paramattach.buffer[i];
			delete paramattach.buffer;

			dcamwait_close( hwait );
		}

		dcamdev_close( hdcam );
		dcamapi_uninit();
	}

    return 0;
}
