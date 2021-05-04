

__kernel void buffer(__global const float *a, __global float *b)
{
	int w = get_global_size(0);
	int h = get_global_size(1);
	int x = get_global_id(0);
	int y = get_global_id(1);

  float acc=0;
  
  for(int i=0; i<8; i++)
  {
	 int is =  ((i*x)^(y+i)) % (w*h);
	 acc += a[is];
	}
	
	acc = acc/64;

  int id = (w-1-x)+w*(h-1-y);
	b[id] = acc;
}


// Render function,
// performs max projection and then uses the transfer function to obtain a color per pixel:
__kernel void image(	__read_only image3d_t image,
                          const uint  imageW,
                         global uint *output						
						            )
{
  // thread int coordinates:
  const uint x = get_global_id(0);
  const uint y = get_global_id(1);

	// samplers:
  const sampler_t volumeSampler   =   CLK_NORMALIZED_COORDS_TRUE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;

  // precompute vectors: 
  const float4 vecstep = 0.5f*(float4)(1.f,1.f,1.f,0.f);
  float4 pos = (float4)(0.f,0.f,0.f,0.f);

  // raycasting loop:
  float maxp = 0.0f;
	for(int i=0; i<64; i++) 
	{
	  	maxp = fmax(maxp,read_imagef(image, volumeSampler, pos).x);
	  	pos+=vecstep;
	}

	// lookup in transfer function texture:
  const float4 color = (float4)(maxp,maxp,maxp,1.f);
  
  // write output color:
  output[x + y*imageW] = (uint)color.x;

}

