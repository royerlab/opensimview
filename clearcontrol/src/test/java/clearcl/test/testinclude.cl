
// Some useless function
void thiscoulddosomething(const __global uint* pSrc, __global uint* pDst)
{
    const int x = get_global_id(0);
    const int y = get_global_id(1);

    const int width = get_global_size(0);
    const int iOffset = y * width;
    const int iPrev = iOffset - width;
    const int iNext = iOffset + width;
}

