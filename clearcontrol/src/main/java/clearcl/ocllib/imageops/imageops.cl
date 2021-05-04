
inline void add2(         __read_only    image3d_t  imagea,
                           __read_only    image3d_t  imageb,
                                 const    float      fa, 
                                 const    float      fb,    
                           __write_only   image3d_t  imagedest
                     )
{
  const int x = get_global_id(0); 
  const int y = get_global_id(1);
  const int z = get_global_id(2);
  
  const int4 pos = (int4){x,y,z,0};

  const float2 factor2 = (float2){fa,fb};

  const float a = read_imagef(imagea, pos).x;
  const float b = read_imagef(imageb, pos).x;
  
  const float2 value2 = (float2){a,b};
  
  const float value = dot(factor2,value2);
  
  write_imagef (imagedest, pos, (float4){value,0.0f,0.0f,0.0f});
}

inline void add4(          __read_only    image3d_t  imagea,
                           __read_only    image3d_t  imageb,
                           __read_only    image3d_t  imagec,
                           __read_only    image3d_t  imaged,
                                 const    float      fa, 
                                 const    float      fb, 
                                 const    float      fc, 
                                 const    float      fd, 
                           __write_only   image3d_t  imagedest
                     )
{
  const int x = get_global_id(0); 
  const int y = get_global_id(1);
  const int z = get_global_id(2);
  
  const int4 pos = (int4){x,y,z,0};

  const float4 factor4 = (float4){fa,fb,fc,fd};

  const float a = read_imagef(imagea, pos).x;
  const float b = read_imagef(imageb, pos).x;
  const float c = read_imagef(imagec, pos).x;
  const float d = read_imagef(imaged, pos).x;
  
  const float4 value4 = (float4){a,b,c,d};
  
  const float value = dot(factor4,value4);
  
  write_imagef (imagedest, pos, (float4){value,0.0f,0.0f,0.0f});
}

