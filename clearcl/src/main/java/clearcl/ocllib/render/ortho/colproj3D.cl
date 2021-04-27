#include [OCLlib] "rgb/rgbhsv.cl" 


//default image_render_colorproj_3d vmin=0f
//default image_render_colorproj_3d vmax=1f
//default image_render_colorproj_3d gamma=1f
//default image_render_colorproj_3d zmin=0i
//default image_render_colorproj_3d zmax=16000i
//default image_render_colorproj_3d zstep=1i
__kernel void image_render_colorproj_3d(          __read_only  image3d_t  image,
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
  
  const float idepth = 1.0f/depth;
  
  zmin = max(0,zmin);
  zmax = min(depth,zmax);
  
  int4 pos = {x,y,0,0};
  float acc = 0.0f;
  int posmax = 0;
  for(pos.z=zmin; pos.z<zmax;pos.z+=zstep)
  {
    #if defined FLOAT
     const float value = read_imagef(image, pos).x;
    #elif defined UINT
     const float value = read_imageui(image, pos).x;
    #elif defined INT
     const float value = read_imagei(image, pos).x;
    #endif
  
    if(value>acc)
    {
      acc = value;
      posmax = pos.z;
    }
  }
  
  const float cvalue = clamp(native_powr(((acc)-vmin)/(vmax-vmin),gamma), 0.0f, 1.0f);
  const float h = idepth*posmax; 
    
  const float4 frgba = hsv2rgb((float4){h,1,0.5*cvalue,1});
   
  const int i = x+ width*y;
  
  uchar4 rgba = convert_uchar4(255*frgba);
  
  //if(x==0 & y==0)
  //  printf("acc = %v4f %#d \n", acc, rgba.x);
  
  vstore4(rgba, i, rgbabuffer);
}





//default image_render_colorproj_3df vmin=0f
//default image_render_colorproj_3df vmax=1f
//default image_render_colorproj_3df gamma=1f
//default image_render_colorproj_3df zmin=0i
//default image_render_colorproj_3df zmax=16000i
//default image_render_colorproj_3df zstep=1i
__kernel void buffer_render_colorproj_3df(__global              float*     image,
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
  
  const float idepth = 1.0f/depth;
  
  zmin = max(0,zmin);
  zmax = min(depth,zmax);
  
  float acc = 0.0f;
  uint posmax = 0;
  for(int z=zmin; z<zmax; z+=zstep)
  {
    const uint i = x + width*y + width*height*z;
    const float value = image[i];
    
    if(value>acc)
    {
      acc = value;
      posmax = z;
    }
  }
  
  const float cvalue = clamp(native_powr(((acc)-vmin)/(vmax-vmin),gamma), 0.0f, 1.0f);
  const float h = idepth*posmax; 
  
  const float4 frgba = hsv2rgb((float4){h,1,0.5*cvalue,1});
  const uchar4 rgba = convert_uchar4(255*frgba);
  
  const int i = x+ width*y;
  
  vstore4(rgba, i, rgbabuffer);
}

