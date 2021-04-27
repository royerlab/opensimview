

//default image_render_avgproj_3d vmin=0f
//default image_render_avgproj_3d vmax=1f
//default image_render_avgproj_3d gamma=1f
//default image_render_avgproj_3d zmin=0i
//default image_render_avgproj_3d zmax=16000i
//default image_render_avgproj_3d zstep=1i
__kernel void image_render_avgproj_3d(          __read_only  image3d_t  image,
                                        __global  uchar*     rgbabuffer,
                                                              float      vmin,
                                                              float      vmax,
                                                              float      gamma,
                                                              int        zmin,
                                                              int        zmax,
                                                              int        zstep
                                                              )
{
  const int width  = get_image_width(image);
  const int height = get_image_height(image);
  const int depth  = get_image_depth(image);
  
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  
  zmin = max(0,zmin);
  zmax = min(depth,zmax);
  
  int4 pos = {x,y,0,0};
  float acc = 0;
  for(pos.z=zmin; pos.z<zmax;)
  {
    for(int i=0; i<8; i++)
    {
      #if defined FLOAT
       const float value = read_imagef(image, pos).x;
      #elif defined UINT
       const float value = read_imageui(image, pos).x;
      #elif defined INT
       const float value = read_imagei(image, pos).x;
      #endif
      
      acc += value;
      pos.z+=zstep;
    }
  }
  
  const float gcvalue = clamp(native_powr(((acc/depth)-vmin)/(vmax-vmin),gamma),0.0f, 1.0f);
  
  const uchar bytevalue = (uchar)(255*gcvalue);
  
  const int i = x+ width*y;
  
  vstore4((uchar4){bytevalue,bytevalue,bytevalue,255}, i, rgbabuffer);
}


//default image_render_avgproj_3df vmin=0f
//default image_render_avgproj_3df vmax=1f
//default image_render_avgproj_3df gamma=1f
//default image_render_avgproj_3df zmin=0i
//default image_render_avgproj_3df zmax=16000i
//default image_render_avgproj_3df zstep=1i
__kernel void buffer_render_avgproj_3df(__global              float*     image,
                                        __global  uchar*     rgbabuffer,
                                                              float      vmin,
                                                              float      vmax,
                                                              float      gamma,
                                                              int        zmin,
                                                              int        zmax,
                                                              int        zstep
                                                              )
{
  const int width  = get_global_size(0);
  const int height = get_global_size(1);
  const int depth = get_global_size(2);
  
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  
  zmin = max(0,zmin);
  zmax = min(depth,zmax);
  
  float acc = 0;
  for(int z=zmin; z<zmax;)
  {
    for(int i=0; i<8; i++)
    {
      const uint i = x + width*y + width*height*z;
      const float value = image[i];
      acc += value;
      z+=zstep;
    }
  }
  
  const float gcvalue = clamp(native_powr(((acc/depth)-vmin)/(vmax-vmin),gamma),0.0f, 1.0f);
  
  const int i = x+ width*y;
  const uchar bytevalue = (uchar)(255*gcvalue);

  vstore4((uchar4){bytevalue,bytevalue,bytevalue,255}, i, rgbabuffer);
}


