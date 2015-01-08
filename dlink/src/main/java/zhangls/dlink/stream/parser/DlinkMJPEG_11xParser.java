package zhangls.dlink.stream.parser;


import zhangls.dlink.ByteArrayUtil;
import zhangls.dlink.FrameHeader;
import zhangls.dlink.Loger;
import zhangls.dlink.MediaFrame;
import zhangls.dlink.MediaFrameHolder;
import zhangls.dlink.StreamParser;

public class DlinkMJPEG_11xParser extends StreamParser {

    private static final byte[] frametime = {(byte) 0x0D, (byte) 0x0A};
    private static final byte[] tail = {(byte) 0xFF, (byte) 0xD9, (byte) 0xFF,
            (byte) 0xD9};

    @Override
    public void readHeader(FrameHeader header, byte[] buffer) {
        header.reset();
        String size = "";
        int pos = -1;

        int length_header = ByteArrayUtil.byteArrayIndexof(buffer,
                "th:".getBytes(), 0);
        if (length_header == -1) {
            return;
        }
        int length_tail = ByteArrayUtil.byteArrayIndexof(buffer,
                "\r\n".getBytes(), length_header);
        if (length_tail != -1) {
            pos = length_tail + 4;
        }

        header.pos = pos;

        if ((length_header >= 0) && (pos >= 0)) {
            size = new String(buffer, length_header + 4, length_tail
                    - length_header - 4);
            try {
                header.count = Integer.parseInt(size);
                Loger.d("Length = " + header.count);
            } catch (Exception e) {
                header.count = -1;
            }
        }

        int videoSec_start = ByteArrayUtil.byteArrayIndexof(buffer,
                "-sec:".getBytes(), 0);
        if (videoSec_start == -1) {
            return;
        }

        int videoSec_end = ByteArrayUtil.byteArrayIndexof(buffer, frametime,
                videoSec_start);
        if (videoSec_end == -1) {
            return;
        }

        size = new String(buffer, videoSec_start + 6, videoSec_end
                - videoSec_start - 6);
        try {
            header.second = Integer.parseInt(size);
        } catch (Exception e) {
            header.second = -1;
        }

        int videoUsec_start = ByteArrayUtil.byteArrayIndexof(buffer,
                "usec:".getBytes(), 0);
        if (videoUsec_start == -1) {
            return;
        }

        int videoUsec_end = ByteArrayUtil.byteArrayIndexof(buffer, frametime,
                videoUsec_start);
        if (videoUsec_end == -1) {
            return;
        }

        size = new String(buffer, videoUsec_start + 6, videoUsec_end
                - videoUsec_start - 6);
        try {
            header.microSecond = Integer.parseInt(size);
        } catch (Exception e) {
            header.microSecond = -1;
        }
    }

    @Override
    public void verifyFrame(int len, byte[] buffer, MediaFrame mframe,
                            MediaFrameHolder frameHolder) {
        int real_length = ByteArrayUtil.byteArrayIndexof(buffer, tail, 0);
        if ((real_length + 4) != len) {
            mframe.clean();
            frameHolder.queue_free(mframe);
            return;
        }
        frameHolder.queue_filled(mframe);
    }

}