/*
volume and iso surface rendering

		volume rendering adapted from the Nvidia sdk sample
  		http://developer.download.nvidia.com/compute/cuda/4_2/rel/sdk/website/OpenCL/html/samples.html
 

  Author: Martin Weigert (mweigert@mpi-cbg.de)
    	  Loic Royer		 (royer@mpi-cbg.de)
*/

#include [OCLlib] "linear/matrix.cl" 
#include [OCLlib] "geometry/boundingbox.cl" 

// Loop unrolling length:
#define LOOPUNROLL 16



// Render function,
__kernel void image_render_maxproj_3d( 
                                    __read_only image3d_t    image,	
                          __global  uchar*       rgbabuffer,
                                                float        vmin,
                                                float        vmax,
                                                float        gamma,
										        float        alpha,
										     	int          maxsteps,
									 __constant float* 	     invProjectionMatrix,
									 __constant float* 	     invModelViewMatrix
									 )
{

  // samplers:
  const sampler_t volumeSampler   =   CLK_NORMALIZED_COORDS_TRUE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR ;

    // convert range bounds to linear map:
  const float ta = 1.f/(vmax-vmin);
  const float tb = -vmin/(vmax-vmin); 

  // box bounds using the clipping box
  const float4 boxMin = (float4){0.0f,0.0f,0.0f,1.f};
  const float4 boxMax = (float4){1.0f,1.0f,1.0f,1.f};

  // global sizes:
  const int width  = get_global_size(0);
  const int height = get_global_size(1);

  // thread int coordinates:
  const uint x = get_global_id(0);
  const uint y = get_global_id(1);
  
  // thread float coordinates:
  const float u = ((x*2.0f) / (float) width)-1.0f;
  const float v = ((y*2.0f) / (float) height)-1.0f;

  // front and back:
  const float4 front = (float4)(u,v,-1.f,1.f);
  const float4 back = (float4)(u,v,1.f,1.f);
  
  //load matrices:
  const float16 iPMatrix  = matrix_load(0, invProjectionMatrix);
  const float16 iMVMatrix = matrix_load(0, invModelViewMatrix);
  
  // calculate eye ray in world space
  float4 orig0, orig;
  float4 direc0, direc;
  
  orig0  = matrix_mult(iPMatrix, front);
  orig0 *= 1.f/orig0.w; 

  orig  = matrix_mult(iMVMatrix, orig0);
  orig *= 1.f/orig.w;
  
  direc0 = matrix_mult(iPMatrix, back);
  direc0 *= 1.f/direc0.w;
  direc0 = normalize(direc0-orig0);

  direc = matrix_mult(iMVMatrix, direc0);
  direc.w = 0.0f;
 
  // find intersection with box
  float tnear, tfar;
  const int hit = intersectBox(orig,direc, boxMin, boxMax, &tnear, &tfar);
  if (!hit || tfar<=0) 
  {
  	vstore4((uchar4){0,0,0,0}, x+ width*y, rgbabuffer);
  	return;
  }
  
  // clamp to near plane:
  if (tnear < 0.0f) tnear = 0.0f;     

  // compute step size:
  const float tstep = fabs(tnear-tfar)/((maxsteps/LOOPUNROLL)*LOOPUNROLL);
  	
  // precompute vectors: 
  const float4 vecstep = 0.5f*tstep*direc;
  float4 pos = orig*0.5f+0.5f + tnear*0.5f*direc;

  // Loop unrolling setup: 
  const int unrolledmaxsteps = (maxsteps/LOOPUNROLL);
  
  // raycasting loop:
  float maxp = 0.0f;
  
  float mappedVal;
  
  if (alpha<=0.f)
  {
    // No alpha blending:  
	  for(int i=0; i<unrolledmaxsteps; i++) 
	  {
			for(int j=0; j<LOOPUNROLL; j++)
			{
	  		maxp = fmax(maxp,read_imagef(image, volumeSampler, pos).x);
	  		pos+=vecstep;
			}
		}
		
		// Mapping to transfer function range and gamma correction: 
		mappedVal = clamp(pow(mad(ta,maxp,tb),gamma),0.f,1.f);
	}
	else
	{
	  // alpha blending:  
	  float cumsum = 1.f;
	  float decay_rate = alpha*tstep;
  	
		for(int i=0; i<unrolledmaxsteps; i++) 
		{
			for(int j=0; j<LOOPUNROLL; j++)
			{
		  	float new_val = read_imagef(image, volumeSampler, pos).x;
		  
		  	//normalize to 0...1
		  	float normalized_val = mad(ta,new_val,tb);
		  	maxp = fmax(maxp,cumsum*normalized_val);
		  	cumsum  *= native_exp(-decay_rate*normalized_val);
	  		pos+=vecstep;
			}
		}
		
		// Mapping to transfer function range and gamma correction: 
      mappedVal = clamp(pow(maxp,gamma),0.f,1.f);
	}
  
  float4 colorf = (float4){mappedVal, mappedVal, mappedVal, 1.0f};
  
  
  uchar4 coloruc = convert_uchar4(255*colorf);
  vstore4(coloruc, x+ width*y, rgbabuffer);
}

