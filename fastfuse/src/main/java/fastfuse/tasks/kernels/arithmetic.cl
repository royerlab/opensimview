__kernel void subtract_constant(write_only image3d_t dst, read_only image3d_t src, const float c, const float threshold) {

  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

  const int i = get_global_id(0), j = get_global_id(1), k = get_global_id(2);
  const int4 coord = (int4)(i,j,k,0);

  const float pix = (float) READ_IMAGE(src,sampler,coord).x;
  float out = pix - c;
  out = out < threshold ? threshold : out;
  WRITE_IMAGE(dst,coord,(DTYPE_OUT)out);
}


__kernel void subtract_image(write_only image3d_t dst, read_only image3d_t src1, read_only image3d_t src2, const float threshold) {

  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;

  const int i = get_global_id(0), j = get_global_id(1), k = get_global_id(2);
  const int4 coord = (int4)(i,j,k,0);

  const float pix1 = (float) READ_IMAGE(src1,sampler,coord).x;
  const float pix2 = (float) READ_IMAGE(src2,sampler,coord).x;
  float out = pix1 - pix2;
  out = out < threshold ? threshold : out;
  WRITE_IMAGE(dst,coord,(DTYPE_OUT)out);
}
