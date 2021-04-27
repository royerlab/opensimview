
//default image_render_slice_3d z=0i
//default image_render_slice_3d vmin=0f
//default image_render_slice_3d vmax=1f
//default image_render_slice_3d gamma=1f
__kernel void image_render_slice_3d(          __read_only   image3d_t  image,
                                      __global  uchar*     rgbabuffer,
                                                            float      vmin,
                                                            float      vmax,
                                                            float      gamma,
                                                            int        z
                                                            )
{
  
  const int width = get_image_width(image);
  const int height = get_image_height(image);
  
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  
  const int4 pos = {x,y,z,0};
  
  #if defined FLOAT
   const float valueraw = read_imagef(image,  pos).x;
  #elif defined UINT
   const float valueraw = read_imageui(image, pos).x;
  #elif defined INT
   const float valueraw = read_imagei(image,  pos).x;
  #endif
  
  const float value = clamp(native_powr((valueraw-vmin)/(vmax-vmin),gamma),0.0f,1.0f);
  
  const uchar bytevalue = (uchar)(255*value);
  
  const int i = (pos.x+ width*pos.y);
  
  vstore4((uchar4){bytevalue,bytevalue,bytevalue,255}, i, rgbabuffer);
}


//default image_render_slice_3df z=0i
//default image_render_slice_3df vmin=0f
//default image_render_slice_3df vmax=1f
//default image_render_slice_3df gamma=1f
__kernel void buffer_render_slice_3df(__global              float*     image,
                                      __global  uchar*     rgbabuffer,
                                                            float      vmin,
                                                            float      vmax,
                                                            float      gamma,
                                                            int        z
                                                            )
{
  const int width  = get_global_size(0);
  const int height = get_global_size(1);
  const int depth  = get_global_size(2);
  
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  
  const int ri = x+ width*y;
  const int i = ri + width*height*z;
  
  const float value = clamp(native_powr((image[i]-vmin)/(vmax-vmin),gamma),0.0f,1.0f);
  
  const uchar bytevalue = (uchar)(255*value);

  vstore4((uchar4){bytevalue,bytevalue,bytevalue,255}, ri, rgbabuffer);
}



