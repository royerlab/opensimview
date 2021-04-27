

//default image_render_2df vmin=0f
//default image_render_2df vmax=1f
//default image_render_2df gamma=1f
__kernel void image_render_2d(           __read_only  image2d_t  image,
                                __global  uchar*     rgbabuffer,
            		                                      float      vmin,
            		                                      float      vmax,
            		                                      float      gamma)
{
   const int width = get_image_width(image);
  const int height = get_image_height(image);
  
  int2 pos = {get_global_id(0),get_global_id(1)}; 
 
  #if defined FLOAT
   const float valueraw = read_imagef(image,  pos).x;
  #elif defined UINT
   const float valueraw = read_imageui(image, pos).x;
  #elif defined INT
   const float valueraw = read_imagei(image,  pos).x;
  #endif
     
  float value = clamp(native_powr((valueraw-vmin)/(vmax-vmin),gamma),0.0f,1.0f);
	
  uchar bytevalue = (uchar)(255*value);
	
  int i = (pos.x+ width*pos.y);
  
  vstore4((uchar4){bytevalue,bytevalue,bytevalue,255}, i, rgbabuffer);
}














//default image_render_2df vmin=0f
//default image_render_2df vmax=1f
//default image_render_2df gamma=1f
__kernel void buffer_render_2df(__global              float*     image,
                                __global uchar*     rgbabuffer,
                                                      float      vmin,
                                                      float      vmax,
                                                      float      gamma)
{
  const int width  = get_global_size(0);
  const int height = get_global_size(1);
  
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  const int i = x+ width*y;
  
  float value = clamp(native_powr((image[i]-vmin)/(vmax-vmin),gamma),0.0f,1.0f);
  
  uchar bytevalue = (uchar)(255*value);
  
  vstore4((uchar4){bytevalue,bytevalue,bytevalue,255}, i, rgbabuffer);
}