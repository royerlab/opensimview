#pragma OPENCL EXTENSION cl_khr_3d_image_writes : enable

#pragma OPENCL EXTENSION cl_amd_printf : enable
#pragma OPENCL EXTENSION cl_intel_printf : enable

#ifndef M_PI
    #define   M_PI 3.14159265358979323846f /* pi */
#endif

#ifndef M_LOG2E
    #define   M_LOG2E   1.4426950408889634074f /* log_2 e */
#endif
 
#ifndef M_LOG10E
    #define   M_LOG10E   0.43429448190325182765f /* log_10 e */
#endif
 
#ifndef M_LN2
    #define   M_LN2   0.69314718055994530942f  /* log_e 2 */
#endif

#ifndef M_LN10
    #define   M_LN10   2.30258509299404568402f /* log_e 10 */
#endif

#ifndef BUFFER_READ_WRITE
    #define BUFFER_READ_WRITE 1
inline __attribute__((overloadable)) ushort2 read_bufferui(int read_buffer_width, int read_buffer_height, __global ushort * buffer, sampler_t sampler, int4 pos )
{
    return (ushort2){buffer[pos.x + pos.y * read_buffer_width + pos.z * read_buffer_width * read_buffer_height],0};
}

inline __attribute__((overloadable)) float2 read_bufferf(int read_buffer_width, int read_buffer_height, __global float* buffer, sampler_t sampler, int4 pos )
{
    return (float2){buffer[pos.x + pos.y * read_buffer_width + pos.z * read_buffer_width * read_buffer_height],0};
}

inline __attribute__((overloadable)) void write_bufferui(int write_buffer_width, int write_buffer_height, __global ushort * buffer, int4 pos, ushort value )
{
    buffer[pos.x + pos.y * write_buffer_width + pos.z * write_buffer_width * write_buffer_height] = value;
}

inline __attribute__((overloadable)) void write_bufferf(int write_buffer_width, int write_buffer_height, __global float* buffer, int4 pos, float value )
{
    buffer[pos.x + pos.y * write_buffer_width + pos.z * write_buffer_width * write_buffer_height] = value;
}

inline __attribute__((overloadable)) ushort2 read_bufferui(int read_buffer_width, int read_buffer_height, __global ushort * buffer, sampler_t sampler, int2 pos )
{
    return (ushort2){buffer[pos.x + pos.y * read_buffer_width ],0};
}

inline __attribute__((overloadable)) float2 read_bufferf(int read_buffer_width, int read_buffer_height, __global float* buffer, sampler_t sampler, int2 pos )
{
    return (float2){buffer[pos.x + pos.y * read_buffer_width ],0};
}

inline __attribute__((overloadable)) void write_bufferui(int write_buffer_width, int write_buffer_height, __global ushort * buffer, int2 pos, ushort value )
{
    buffer[pos.x + pos.y * write_buffer_width ] = value;
}

inline __attribute__((overloadable)) void write_bufferf(int write_buffer_width, int write_buffer_height, __global float* buffer, int2 pos, float value )
{
    buffer[pos.x + pos.y * write_buffer_width ] = value;
}
#endif