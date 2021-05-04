__constant sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

__constant float hx[] = {-1,-2,-1,-2,-4,-2,-1,-2,-1,0,0,0,0,0,0,0,0,0,1,2,1,2,4,2,1,2,1};
__constant float hy[] = {-1,-2,-1,0,0,0,1,2,1,-2,-4,-2,0,0,0,2,4,2,-1,-2,-1,0,0,0,1,2,1};
__constant float hz[] = {-1,0,1,-2,0,2,-1,0,1,-2,0,2,-4,0,4,-2,0,2,-1,0,1,-2,0,2,-1,0,1};


inline float sobel_magnitude_squared(read_only image3d_t src, const int i0, const int j0, const int k0) {
  float Gx = 0.0f, Gy = 0.0f, Gz = 0.0f;
  for (int i = 0; i < 3; ++i) for (int j = 0; j < 3; ++j) for (int k = 0; k < 3; ++k) {
    const int dx = i-1, dy = j-1, dz = k-1;
    const int ind = i + 3*j + 3*3*k;
    const float pix = (float)READ_IMAGE(src,sampler,(int4)(i0+dx,j0+dy,k0+dz,0)).x;
    Gx += hx[ind]*pix;
    Gy += hy[ind]*pix;
    Gz += hz[ind]*pix;
  }
  return Gx*Gx + Gy*Gy + Gz*Gz;
}


__kernel void tenengrad_fusion_4_images(write_only image3d_t dst, read_only image3d_t src1, read_only image3d_t src2, read_only image3d_t src3, read_only image3d_t src4) {

  const int i = get_global_id(0), j = get_global_id(1), k = get_global_id(2);
  const int4 coord = (int4)(i,j,k,0);

  float w1 = sobel_magnitude_squared(src1,i,j,k);
  float w2 = sobel_magnitude_squared(src2,i,j,k);
  float w3 = sobel_magnitude_squared(src3,i,j,k);
  float w4 = sobel_magnitude_squared(src4,i,j,k);

  const float wsum = w1 + w2 + w3 + w4 + 1e-30f; // add small epsilon to avoid wsum = 0
  w1 /= wsum;  w2 /= wsum;  w3 /= wsum;  w4 /= wsum;

  const float  v1 = (float)READ_IMAGE(src1,sampler,coord).x;
  const float  v2 = (float)READ_IMAGE(src2,sampler,coord).x;
  const float  v3 = (float)READ_IMAGE(src3,sampler,coord).x;
  const float  v4 = (float)READ_IMAGE(src4,sampler,coord).x;
  const float res = w1*v1 + w2*v2 + w3*v3 + w4*v4;

  WRITE_IMAGE(dst,coord,(DTYPE_OUT)res);
}


__kernel void tenengrad_fusion_2_images(write_only image3d_t dst, read_only image3d_t src1, read_only image3d_t src2) {

  const int i = get_global_id(0), j = get_global_id(1), k = get_global_id(2);
  const int4 coord = (int4)(i,j,k,0);

  float w1 = sobel_magnitude_squared(src1,i,j,k);
  float w2 = sobel_magnitude_squared(src2,i,j,k);

  const float wsum = w1 + w2 + 1e-30f; // add small epsilon to avoid wsum = 0
  w1 /= wsum;  w2 /= wsum;

  const float  v1 = (float)READ_IMAGE(src1,sampler,coord).x;
  const float  v2 = (float)READ_IMAGE(src2,sampler,coord).x;
  const float res = w1*v1 + w2*v2;

  WRITE_IMAGE(dst,coord,(DTYPE_OUT)res);
}


__kernel void tenengrad_weight_unnormalized(write_only image3d_t dst, read_only image3d_t src) {
  const int i = get_global_id(0), j = get_global_id(1), k = get_global_id(2);
  const int4 coord = (int4)(i,j,k,0);
  float w = sobel_magnitude_squared(src,i,j,k);
  // w = w*w;
  WRITE_IMAGE(dst,coord,(DTYPE_OUT)w);
}


__kernel void tenengrad_fusion_with_provided_weights_2_images(
  write_only image3d_t dst, const int factor,
  read_only image3d_t src1, read_only image3d_t src2,
  read_only image3d_t weight1, read_only image3d_t weight2
)
{
  const int i = get_global_id(0), j = get_global_id(1), k = get_global_id(2);
  const int4 coord = (int4)(i,j,k,0);

  const float4 coord_weight = (float4)((i+0.5f)/factor,(j+0.5f)/factor,k+0.5f,0);
  const sampler_t sampler_weight = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;

  float w1 = read_imagef(weight1,sampler_weight,coord_weight).x;
  float w2 = read_imagef(weight2,sampler_weight,coord_weight).x;

  const float wsum = w1 + w2 + 1e-30f; // add small epsilon to avoid wsum = 0
  w1 /= wsum;  w2 /= wsum;

  const float  v1 = (float)READ_IMAGE(src1,sampler,coord).x;
  const float  v2 = (float)READ_IMAGE(src2,sampler,coord).x;
  const float res = w1*v1 + w2*v2;

  WRITE_IMAGE(dst,coord,(DTYPE_OUT)res);
}


__kernel void tenengrad_fusion_with_provided_weights_4_images(
  write_only image3d_t dst, const int factor,
  read_only image3d_t src1, read_only image3d_t src2, read_only image3d_t src3, read_only image3d_t src4,
  read_only image3d_t weight1, read_only image3d_t weight2, read_only image3d_t weight3, read_only image3d_t weight4
)
{
  const int i = get_global_id(0), j = get_global_id(1), k = get_global_id(2);
  const int4 coord = (int4)(i,j,k,0);
  
  const float4 coord_weight = (float4)((i+0.5f)/factor,(j+0.5f)/factor,k+0.5f,0);
  const sampler_t sampler_weight = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_LINEAR;

  float w1 = read_imagef(weight1,sampler_weight,coord_weight).x;
  float w2 = read_imagef(weight2,sampler_weight,coord_weight).x;
  float w3 = read_imagef(weight3,sampler_weight,coord_weight).x;
  float w4 = read_imagef(weight4,sampler_weight,coord_weight).x;

  const float wsum = w1 + w2 + w3 + w4 + 1e-30f; // add small epsilon to avoid wsum = 0
  w1 /= wsum;  w2 /= wsum;  w3 /= wsum;  w4 /= wsum;

  const float  v1 = (float)READ_IMAGE(src1,sampler,coord).x;
  const float  v2 = (float)READ_IMAGE(src2,sampler,coord).x;
  const float  v3 = (float)READ_IMAGE(src3,sampler,coord).x;
  const float  v4 = (float)READ_IMAGE(src4,sampler,coord).x;
  const float res = w1*v1 + w2*v2 + w3*v3 + w4*v4;

  WRITE_IMAGE(dst,coord,(DTYPE_OUT)res);
}