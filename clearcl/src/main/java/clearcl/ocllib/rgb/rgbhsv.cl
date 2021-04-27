// RGB to and from HSV routines



inline float4 rgb2hsv(const float4 RGB)
{
  float4 HSV;

  float minv = fmin(RGB.x, fmin(RGB.y, RGB.z));
  float maxv = fmax(RGB.x, fmax(RGB.y, RGB.z));
  float delta = maxv - minv;

  HSV.z = maxv;
  HSV.w = RGB.w;

  if (fabs(maxv) > 1e-6f && fabs(delta) > 1e-6f)
  { 
    HSV.y = delta / maxv;
  }
  else
  {
    HSV.x = 0.0f;
    HSV.y = 0.0f;
    return HSV;
  }

  if (RGB.x == maxv)
   HSV.x = (RGB.y - RGB.z) / delta;
  else if (RGB.y == maxv)
   HSV.x = 2.0f + (RGB.z - RGB.x) / delta;
  else
   HSV.x = 4.0f + (RGB.x - RGB.y) / delta;

  HSV.x /= 6.0f;

  if(HSV.x < 0)
    HSV.x += 1.0f;

  return HSV;
}

inline float4 hsv2rgb(const float4 HSV)
{
  float4 RGB;

  if (fabs(HSV.y) < 1e-6f)
  {
    RGB.x = RGB.y = RGB.z = HSV.z;
    RGB.w = HSV.w;
    return RGB;
  }

  int i = floor(6.0f*HSV.x);
  float v = HSV.z;
  float w = HSV.w;
  float p = v * (1.0f - HSV.y);
  float q = v * (1.0f - HSV.y * (6.0f*HSV.x - i));
  float t = v * (1.0f - HSV.y * (1.0f - (6.0f*HSV.x - i)));

  switch (i)
  {
    case 0:
      RGB = (float4){v, t, p, w};
      break;
    case 1:
      RGB = (float4){q, v, p, w};
      break;
    case 2:
      RGB = (float4){p, v, t, w};
      break;
    case 3:
      RGB = (float4){p, q, v, w};
      break;
    case 4:
      RGB = (float4){t, p, v, w};
      break;
    case 5:
    default:
      RGB = (float4){v, p, q, w};
      break;
  }
  return RGB;
}


inline float4 hv2rgb(const float4 HSV)
{
  float4 RGB;

  const int i = floor(6.0f*HSV.x);
  const float v = HSV.z;
  const float w = HSV.w;
  const float p = 0.0f;
  const float tt = 6.0f*HSV.x - i;
  const float q = v * (1.0f - tt);
  const float t = v * (tt - i);

  switch (i)
  {
    case 0:
      RGB = (float4){v, t, p, w};
      break;
    case 1:
      RGB = (float4){q, v, p, w};
      break;
    case 2:
      RGB = (float4){p, v, t, w};
      break;
    case 3:
      RGB = (float4){p, q, v, w};
      break;
    case 4:
      RGB = (float4){t, p, v, w};
      break;
    case 5:
    default:
      RGB = (float4){v, p, q, w};
      break;
  }
  return RGB;
}










































// from: http://lolengine.net/blog/2013/07/27/rgb-to-hsv-in-glsl


/*
inline float4 rgb2hsv(float4 c)
{
    float4 K = float4(0.0f, -1.0f / 3.0f, 2.0f / 3.0f, -1.0f);
    float4 p = (c.g < c.b) ? float4(c.b, c.g, K.w, K.z) : float4(c.g, c.b, K.x, K.y);
    float4 q = (c.r < p.x) ? float4(p.x, p.y, p.w, c.r) : float4(c.r, p.y, p.z, p.x);
    
    //float4 p = mix(float4(c.b, c.g, K.w, K.z), float4(c.g,c.b, K.x, K.y), step(c.b, c.g));
    //float4 q = mix(float4(p.x, p.y, p.w, c.r), float4(c.r, p.y, p.z, p.x), step(p.x, c.r));


    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10f;
    return float4(fabs(q.z + (q.w - q.y) / (6.0f * d + e)), d / (q.x + e), q.x, c.a);
}

inline float4 hsv2rgb(float4 c)
{
    float3 dummy;
    float4 K = float4(1.0f, 2.0f / 3.0f, 1.0f / 3.0f, 3.0f);
    float3 p = fabs(6.0f*fract(c.xyz + K.xyz,&dummy) - K.www);
    float3 res = c.z * mix(K.xyz, clamp(p - K.xyz, 0.0f, 1.0f), c.y);
    return float4(res.r, res.g, res.b, c.a);
}






/*
inline float3 rgb2hsv(float3 c)
{
    float4 K = float4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    float4 p = (c.g < c.b) ? float4(c.bg, K.wz) : float4(c.gb, K.xy);
    float4 q = (c.r < p.x) ? float4(p.xyw, c.r) : float4(c.r, p.yzx);
    
    //float4 p = mix(float4(c.bg, K.wz), float4(c.gb, K.xy), step(c.b, c.g));
    //float4 q = mix(float4(p.xyw, c.r), float4(c.r, p.yzx), step(p.x, c.r));


    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return float3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

inline float3 hsv2rgb(float3 c)
{
    float4 K = float4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    float3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}
/**/
