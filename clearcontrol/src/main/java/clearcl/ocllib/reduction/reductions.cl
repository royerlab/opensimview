__kernel
void reduce_min_buffer( __global float* buffer,
                                   long    length,
                          __global float* result) 
{
  int index  = get_global_id(0);
  int stride = get_global_size(0);
  
  float min = INFINITY;
  float max = -INFINITY;
  
  while(index<length)
  {
    float value = buffer[index];
    min = fmin(min, value);
    max = fmax(max, value);
    index += stride;
  }

  result[2*get_global_id(0)+0] = min;
  result[2*get_global_id(0)+1] = max;
}




__kernel
void reduce_min_image_1d(__read_only image1d_t  image,
                         __global    float*     result) 
{
  const int width = get_image_width(image);
  
  int x  = get_global_id(0);
  int stridex = get_global_size(0);
  
  float min = INFINITY;
  float max = -INFINITY;
  
  for(int lx=x; lx<width; lx+=stridex)
  {
    #if defined FLOAT
     float value = (float)(read_imagef(image, lx)).x;
    #elif defined UINT
     float value = (float)(read_imageui(image, lx)).x;
    #elif defined INT
     float value = (float)(read_imagei(image, lx)).x;
    #endif
    
    min = fmin(min, value);
    max = fmax(max, value);
  }

  int index = 2*get_global_id(0);

  result[index+0] = min;
  result[index+1] = max;
}

    

__kernel
void reduce_min_image_2d( __read_only image2d_t  image,
                          __global    float*     result) 
{
  const int width = get_image_width(image);
  const int height = get_image_height(image);
  
  const int x  = get_global_id(0);
  const int y  = get_global_id(1);
  
  const int stridex = get_global_size(0);
  const int stridey = get_global_size(1);
  
  float min = INFINITY;
  float max = -INFINITY;
  
  for(int ly=y; ly<height; ly+=stridey)
  {
    for(int lx=x; lx<width; lx+=stridex)
    {
      const int2 pos = {lx,ly};
   
      #if defined FLOAT
       float value = (float)(read_imagef(image, pos)).x;
      #elif defined UINT
       float value = (float)(read_imageui(image, pos)).x;
      #elif defined INT
       float value = (float)(read_imagei(image, pos)).x;
      #endif
      
      min = fmin(min, value);
      max = fmax(max, value);
    }
  }
  
  int index = 2*(x+stridex*y);
  
  result[index+0] = min;
  result[index+1] = max;
}



    
__kernel
void reduce_min_image_3d (__read_only image3d_t  image,
                          __global    float*     result) 
{
  const int width   = get_image_width(image);
  const int height  = get_image_height(image);
  const int depth   = get_image_depth(image);
  
  const int x       = get_global_id(0);
  const int y       = get_global_id(1);
  const int z       = get_global_id(2);
  
  const int stridex = get_global_size(0);
  const int stridey = get_global_size(1);
  const int stridez = get_global_size(2);
  
  float min = INFINITY;
  float max = -INFINITY;
  
  for(int lz=z; lz<depth; lz+=stridez)
  {
    for(int ly=y; ly<height; ly+=stridey)
    {
      for(int lx=x; lx<width; lx+=stridex)
      {
        const int4 pos = {lx,ly,lz,0};
     
        #if defined FLOAT
         float value = (float)(read_imagef(image, pos)).x;
        #elif defined UINT
         float value = (float)(read_imageui(image, pos)).x;
        #elif defined INT
         float value = (float)(read_imagei(image, pos)).x;
        #endif
        
        min = fmin(min, value);
        max = fmax(max, value);
      }
    }
  }

  int index = 2*(x+stridex*y+stridex*stridey*z);

  result[index+0] = min;
  result[index+1] = max;
}


