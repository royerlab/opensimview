 
#include "linear/matrix.cl"
 
 //default:
__kernel void transform3d( __read_only 	image3d_t input, 
												 __write_only 	image3d_t output,
												 __global 			float4* 	matrix )
{
	size_t x =  get_global_id(0);
	size_t y =  get_global_id(1);
 
	int2 coords = {x,y};
	uint4 inputPixel = read_imageui( inputImage, sampler, coords );
 
	int2 transCoords = { coords.x*transformationMatrix[0].x + coords.y*transformationMatrix[0].y + transformationMatrix[0].z, 
	                     coords.x*transformationMatrix[1].x + coords.y*transformationMatrix[1].y + transformationMatrix[1].z };
 
	write_imageui( transformedImage, transCoords, inputPixel );
}