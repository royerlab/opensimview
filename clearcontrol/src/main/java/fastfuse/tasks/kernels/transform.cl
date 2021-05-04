
#include [OCLlib] "linear/matrix.cl"
                                
__kernel void transform(    __read_only    image3d_t  imagein,
                           __constant     float*     matrix,
                           __write_only   image3d_t  imagedest,
                     )
{
  const sampler_t intsampler  = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_NONE | CLK_FILTER_LINEAR;
 
  const int x = get_global_id(0); 
  const int y = get_global_id(1);
  const int z = get_global_id(2);
  
  const int4 pos = (int4){x,y,z,0};

  const float16 matrix16  = matrix_load(0, matrix);

  const ushort valuea = trans_read_imagui(imagein, intsampler, matrix16, pos).x;

  write_imageui (imagedest, pos, (uint4){(value,0,0,0});
}



