package gsao64;


import gsao64.exceptions.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GSSplitterBuffer
{
    public List<GSBuffer> mData;
    private int mCurrentBufferIndex;

    public GSSplitterBuffer(int maxTP) throws BufferTooLargeException, BoardInitializeException
    {
        // Initialize everything
        this.mData = new ArrayList<>(); // Should be ArrayList as we do add and get where both O(1) with ArrayList
        mCurrentBufferIndex = 0;

        for (int i = 0; i < Math.floor(maxTP/2999); i++)
            this.mData.add(new GSBuffer(2999));

        this.mData.add(new GSBuffer(maxTP%2999));
    }

    public void appendValue(double value, int i)
    {
        GSBuffer lCurrentBuffer = this.mData.get(mCurrentBufferIndex);
        if (lCurrentBuffer.getValsWritten() * 4 == lCurrentBuffer.getMaxSizeInBytes())
            mCurrentBufferIndex++;

        try {
            this.mData.get(mCurrentBufferIndex).appendValue(value, i);
        } catch (ActiveChanException | VoltageRangeException e) {
            e.printStackTrace();
        }
    }

    public void appendEndofTP()
    {
            this.mData.get(mCurrentBufferIndex).appendEndofTP();
    }

    public void appendEndofFunction()
    {
        try {
            this.mData.get(mCurrentBufferIndex).appendEndofFunction();
        } catch (FlagException e) {
            e.printStackTrace();
        }
    }

    public ArrayDeque<GSBuffer> getData() { return new ArrayDeque<>(this.mData); }

    public int getmCurrentBufferIndex() { return mCurrentBufferIndex; }
}
