
                                
__kernel void flip_ui  (    __read_only   image3d_t  imagein,
                           __write_only   image3d_t  imageout,
                           const          int        flipx,
                           const          int        flipy,
                           const          int        flipz
                     )
{
  const sampler_t intsampler  = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_NONE | CLK_FILTER_NEAREST;
 
  const int x = get_global_id(0); 
  const int y = get_global_id(1);
  const int z = get_global_id(2);
  
  const int width = get_global_size(0); 
  const int height = get_global_size(1);
  const int depth = get_global_size(2);
  
  const int4 pos = (int4){flipx?(width-1-x):x,
                          flipy?(height-1-y):y,
                          flipz?(depth-1-z):z,0};

  const ushort value = read_imageui(imagein, intsampler, pos).x;

  write_imageui (imageout, (int4){x,y,z,0}, (uint4){value,0,0,0});
}

__kernel void flip_f   (    __read_only   image3d_t  imagein,
                           __write_only   image3d_t  imageout,
                           const          int        flipx,
                           const          int        flipy,
                           const          int        flipz
                     )
{
  const sampler_t intsampler  = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_NONE | CLK_FILTER_NEAREST;
 
  const int x = get_global_id(0); 
  const int y = get_global_id(1);
  const int z = get_global_id(2);
  
  const int width = get_global_size(0); 
  const int height = get_global_size(1);
  const int depth = get_global_size(2);
  
  const int4 pos = (int4){flipx?(width-1-x):x,
                          flipy?(height-1-y):y,
                          flipz?(depth-1-z):z,0};
                          
  printf(" %d %d \n", x, pos.x);

  const float value = read_imagef(imagein, intsampler, pos).x;

  write_imagef (imageout, (int4){x,y,z,0}, (float4){value,0,0,0});
}



