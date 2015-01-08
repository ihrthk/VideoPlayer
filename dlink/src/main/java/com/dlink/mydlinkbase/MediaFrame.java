package com.dlink.mydlinkbase;

import java.nio.ByteBuffer;

public class MediaFrame {
    private ByteBuffer buff;
    private int microsecond;
    private int second;

    public MediaFrame(ByteBuffer buff) {
        this.buff = buff;
    }

    public void append(byte[] data, int start, int count) {
        if (start == -1 || count == -1) {
            return;
        }
        if ((buff.position() + count) < buff.capacity()) {
            buff.put(data, start, count);
        } else if ((buff.position() + count) >= buff.capacity()) {
            ByteBuffer buff_temp = ByteBuffer
                    .allocate((buff.position() + count));
            buff_temp.put(buff.array(), 0, buff.position());
            buff_temp.put(data, start, count);
            buff = buff_temp;
        }
    }

    public void clean() {
        buff.clear();
    }

    public ByteBuffer getBuff() {
        return buff;
    }

    public byte[] getbytes() {
        return buff.array();
    }

    public int getlength() {
        return this.buff.position();
    }

    public int getMicrosecond() {
        return microsecond;
    }

    public int getSecond() {
        return second;
    }

    public void setBuff(ByteBuffer buff) {
        this.buff = buff;
    }

    public void setMicrosecond(int usec) {
        this.microsecond = usec;
    }

    public void setSecond(int sec) {
        this.second = sec;
    }

    /**
     * Set the byte to the media frame.
     *
     * @param array The specified byte array.
     */
    public void setByteArray(byte[] array) {
        buff.clear();
        buff.put(array);
    }

    /**
     * Get the available data of the buffer.
     *
     * @return The available data of the buffer.
     */
    public int getAvaiDataLength() {
        return buff.position();
    }
}
