package zhangls.dlink.stream.parser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import zhangls.dlink.MediaFrame;
import zhangls.dlink.StreamParser;
import zhangls.dlink.Utils;

public class DlinkH264_93xParser extends StreamParser {
    // little endian
    // private static final int START_CODE = 0x000001;
    private static final int START_CODE = 0x00000001;
    private static final int BUF_SIZE = 1024;
    private static final int MAX_NALU_SIZE = 100 * 1024;
    private byte[] mRemainBuf;
    private int mRemainLength;

    public DlinkH264_93xParser() {
        mRemainBuf = new byte[BUF_SIZE * 2];
        mRemainLength = -1;
    }

    private int findNextStartCode(MediaFrame mframe, int offset) {
        ByteBuffer buf = mframe.getBuff();
        int position = buf.position();
        int limit = buf.limit();
        if (position > 4 && (position - offset) >= 4) {
            for (int i = offset; i < position && limit - i >= 4; i++) {
                buf.position(i);
                if (buf.getInt() == START_CODE) {
                    buf.position(position);
                    return i;
                }
            }
        }
        buf.position(position);
        return -1;
    }

    @Override
    public int readFrame(MediaFrame mframe, byte[] buffer, int len, int pos,
                         int count, InputStream dis) throws IOException {
        int startOffset = -1;
        int endOffset = -1;
        int offset = 0;

        while (mframe.getlength() < MAX_NALU_SIZE) {

            if (mRemainLength > 0) {// read remain data
                mframe.append(mRemainBuf, 0, mRemainLength);
            } else {// read data from camera
                int iLength = Utils.readDataToVideoBuffer(dis, mRemainBuf, 0,
                        BUF_SIZE);
                if (iLength < 0) {
                    return -1;
                }

                mframe.append(mRemainBuf, 0, iLength);
            }

            // find first start code
            if (startOffset == -1) {
                startOffset = findNextStartCode(mframe, offset);
                if (mframe.getlength() > 4) {
                    offset = mframe.getlength() - 4;
                }
            }

            // If we have found first start code,
            // then try to find next start code
            if (startOffset != -1 && (startOffset + 4) < mframe.getlength()
                    && endOffset == -1) {
                endOffset = findNextStartCode(mframe, startOffset + 4);
                offset = mframe.getlength() - 4;
            }

            mRemainLength = -1;
            if (startOffset != -1 && endOffset != -1) {
                mRemainLength = (mframe.getlength() - endOffset);
                if (mRemainLength > 0) {
                    System.arraycopy(mframe.getBuff().array(), endOffset,
                            mRemainBuf, 0, mRemainLength);
                }
                mframe.getBuff().position(endOffset);
                return mframe.getlength();
            }

        }
        return -1;
    }

}
