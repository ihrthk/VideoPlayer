package com.dlink.mydlinkbase;

/**
 * Audio & Video Frame Header
 *
 * @author mydlink
 */
public class FrameHeader {
    public int count;
    public int second;
    public int microSecond;
    public int pos;
    public short channel;
    public int rate;
    public short bit;
    public int remain;
    public byte[] buffer;

    public void reset() {
        count = -1;
        second = -1;
        microSecond = -1;
        pos = -1;
        channel = -1;
        rate = -1;
        bit = -1;
        remain = 0;
        buffer = new byte[2048];
    }

    public FrameHeader() {
        reset();
    }
}
