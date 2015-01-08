package zhangls.dlink;

import java.io.IOException;
import java.io.InputStream;

public abstract class StreamParser {

    public int skipFrame(byte[] buffer, int len, int pos, int count,
                         InputStream dis) throws IOException {
        int itrans = count - (len - pos);
        while (itrans > 0) {
            if (itrans > buffer.length) {
                len = dis.read(buffer);
            } else {
                len = dis.read(buffer, 0, itrans);
            }
            itrans -= len;
        }
        return len;
    }

    public synchronized int readFrame(MediaFrame mframe, byte[] buffer, int len, int pos,
                                      int count, InputStream dis) throws IOException {
        int itrans = -1;
        if (mframe == null)
            return 0;
        if (count + pos < len) {
            len = count + pos;
        }
        if (len > pos) {
            mframe.append(buffer, pos, len - pos);
            itrans = count - (len - pos);
        } else {
            if (pos - len > 0) {
                skipFrame(buffer, 0, 0, (pos - len), dis);
            }
            itrans = count;
        }

        while (itrans > 0) {
            if (itrans > buffer.length) {
                len = dis.read(buffer);
            } else {
                len = dis.read(buffer, 0, itrans);
            }
            mframe.append(buffer, 0, len);
            itrans = itrans - len;
        }
        return len;
    }

    public void readHeader(FrameHeader header, byte[] buffer) {
    }

    public void readHeader(FrameHeader header, byte[] buffer, int width, int height) {

    }


    public boolean readSystemHeader(InputStream audioStream, FrameHeader header)
            throws IOException {
        return false;
    }

    public void verifyFrame(int len, byte[] buffer, MediaFrame mframe,
                            MediaFrameHolder frameHolder) {

    }

    /**
     * Decode the stream stored in the media frame.
     *
     * @param frame The frame to be decode.
     */
    protected void decodeStream(MediaFrame frame) {
    }

    /**
     * Release the decoder.
     */
    protected void releaseDecoder() {
    }

    /**
     * Convert the short array to byte array.
     *
     * @param input The short array.
     */
    public byte[] convertShortArrToByteArr(short[] input) {
        byte[] result = null;
        if (null != input) {
            int shortLength = input.length;
            result = new byte[shortLength * 2];
            for (int i = 0; i < shortLength; i++) {
                result[i * 2] = (byte) (input[i] & 0xFF);
                result[i * 2 + 1] = (byte) (input[i] >> 8 & 0xFF);
            }
        }
        return result;
    }
}
