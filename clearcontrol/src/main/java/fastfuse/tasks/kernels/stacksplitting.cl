__constant sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;



__kernel void convert_interleaved_to_stacks_4(read_only image3d_t src, write_only image3d_t dst0,write_only image3d_t dst1,write_only image3d_t dst2,write_only image3d_t dst3){

   const int i = get_global_id(0), j = get_global_id(1), k = get_global_id(2);

   WRITE_IMAGE(dst0,(int4)(i,j,k,0),(DTYPE_OUT)READ_IMAGE(src,sampler,(const int4)(i,j,4*k,0)).x);
   WRITE_IMAGE(dst1,(int4)(i,j,k,0),(DTYPE_OUT)READ_IMAGE(src,sampler,(int4)(i,j,4*k+1,0)).x);
   WRITE_IMAGE(dst2,(int4)(i,j,k,0),(DTYPE_OUT)READ_IMAGE(src,sampler,(int4)(i,j,4*k+2,0)).x);
   WRITE_IMAGE(dst3,(int4)(i,j,k,0),(DTYPE_OUT)READ_IMAGE(src,sampler,(int4)(i,j,4*k+3,0)).x);

}


__kernel void convert_interleaved_to_stacks_4_and_downsample_xy_by_half_nearest(read_only image3d_t src, write_only image3d_t dst0,write_only image3d_t dst1,write_only image3d_t dst2,write_only image3d_t dst3){

   const int i = get_global_id(0), j = get_global_id(1), k = get_global_id(2);

   // this is to make Robert happy
   const int i2 = 2*i, j2 = 2*j, k2 = 4*k;

   WRITE_IMAGE(dst0,(int4)(i,j,k,0),(DTYPE_OUT)READ_IMAGE(src,sampler,(const int4)(i2,j2,k2,0)).x);
   WRITE_IMAGE(dst1,(int4)(i,j,k,0),(DTYPE_OUT)READ_IMAGE(src,sampler,(int4)(i2,j2,k2+1,0)).x);
   WRITE_IMAGE(dst2,(int4)(i,j,k,0),(DTYPE_OUT)READ_IMAGE(src,sampler,(int4)(i2,j2,k2+2,0)).x);
   WRITE_IMAGE(dst3,(int4)(i,j,k,0),(DTYPE_OUT)READ_IMAGE(src,sampler,(int4)(i2,j2,k2+3,0)).x);

}



__kernel void convert_interleaved_to_stacks_5(read_only image3d_t src, write_only image3d_t dst0,write_only image3d_t dst1,write_only image3d_t dst2,write_only image3d_t dst3,write_only image3d_t dst4){

   const int i = get_global_id(0), j = get_global_id(1), k = get_global_id(2);

   WRITE_IMAGE(dst0,(int4)(i,j,k,0),(DTYPE_OUT)READ_IMAGE(src,sampler,(const int4)(i,j,5*k,0)).x);
   WRITE_IMAGE(dst1,(int4)(i,j,k,0),(DTYPE_OUT)READ_IMAGE(src,sampler,(int4)(i,j,5*k+1,0)).x);
   WRITE_IMAGE(dst2,(int4)(i,j,k,0),(DTYPE_OUT)READ_IMAGE(src,sampler,(int4)(i,j,5*k+2,0)).x);
   WRITE_IMAGE(dst3,(int4)(i,j,k,0),(DTYPE_OUT)READ_IMAGE(src,sampler,(int4)(i,j,5*k+3,0)).x);
   WRITE_IMAGE(dst4,(int4)(i,j,k,0),(DTYPE_OUT)READ_IMAGE(src,sampler,(int4)(i,j,5*k+3,0)).x);

}


__kernel void convert_interleaved_to_stacks_5_and_downsample_xy_by_half_nearest(read_only image3d_t src, write_only image3d_t dst0,write_only image3d_t dst1,write_only image3d_t dst2,write_only image3d_t dst3,write_only image3d_t dst4){

   const int i = get_global_id(0), j = get_global_id(1), k = get_global_id(2);

   // this is to make Robert happy
   const int i2 = 2*i, j2 = 2*j, k2 = 5*k;

   WRITE_IMAGE(dst0,(int4)(i,j,k,0),(DTYPE_OUT)READ_IMAGE(src,sampler,(const int4)(i2,j2,k2,0)).x);
   WRITE_IMAGE(dst1,(int4)(i,j,k,0),(DTYPE_OUT)READ_IMAGE(src,sampler,(int4)(i2,j2,k2+1,0)).x);
   WRITE_IMAGE(dst2,(int4)(i,j,k,0),(DTYPE_OUT)READ_IMAGE(src,sampler,(int4)(i2,j2,k2+2,0)).x);
   WRITE_IMAGE(dst3,(int4)(i,j,k,0),(DTYPE_OUT)READ_IMAGE(src,sampler,(int4)(i2,j2,k2+3,0)).x);
   WRITE_IMAGE(dst4,(int4)(i,j,k,0),(DTYPE_OUT)READ_IMAGE(src,sampler,(int4)(i2,j2,k2+3,0)).x);
}

