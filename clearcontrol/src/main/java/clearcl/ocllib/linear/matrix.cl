
// Loads a matrix from a buffer with offset
inline float16 matrix_load(int offset, __constant float* pointer) 
{
    float16 matrix = vload16(offset, pointer);
    return matrix;
}


// 4x4 matrix multiplication:
inline float4 matrix_mult(float16 matrix, float4 vector) 
{
  float4 result;

  result.x = dot(vector, ((float4)(matrix.s0,matrix.s1,matrix.s2,matrix.s3)));
  result.y = dot(vector, ((float4)(matrix.s4,matrix.s5,matrix.s6,matrix.s7)));
  result.z = dot(vector, ((float4)(matrix.s8,matrix.s9,matrix.sa,matrix.sb)));
  result.w = dot(vector, ((float4)(matrix.sc,matrix.sd,matrix.se,matrix.sf)));    

  return result;             
}

// 4x4 matrix transpose 
inline float16 matrix_transpose(float16 matrix) 
{
  return matrix.s048C159D26AE37BF;
}

/*
// read_imagef with matrix multiplication, int4 version:
inline float4 trans_read_imagef( image3d_t image,
                           sampler_t sampler,
                           float16 matrix,
                           int4 vector)
{
  const int4 transvector = convert_int4(matrix_mult(matrix,convert_float4(vector))); 
  return read_imagef(image, sampler, transvector);
}
/**/

// read_imagef with matrix multiplication, float4 version:
inline float4 trans_read_imagef( image3d_t image,
                                 sampler_t sampler,
                                 float16 matrix,
                                 float4 vector)
{
  vector.w=1;
  const float4 transvector = matrix_mult(matrix,vector); 
  return read_imagef(image, sampler, transvector);
}

