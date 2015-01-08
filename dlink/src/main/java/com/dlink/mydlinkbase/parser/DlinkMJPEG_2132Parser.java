package com.dlink.mydlinkbase.parser;

import com.dlink.mydlinkbase.ByteArrayUtil;
import com.dlink.mydlinkbase.FrameHeader;
import com.dlink.mydlinkbase.MediaFrame;
import com.dlink.mydlinkbase.MediaFrameHolder;
import com.dlink.mydlinkbase.StreamParser;

public class DlinkMJPEG_2132Parser extends StreamParser {
    private static final byte[] tail = {(byte) 0xFF, (byte) 0xD9};

    @Override
    public void readHeader(FrameHeader header, byte[] buffer) {
        header.reset();
        String size = "";
        int pos = -1;

        int length_header = ByteArrayUtil.byteArrayIndexof(buffer,
                "Content-Length: ".getBytes(), 0);
        if (length_header == -1) {
            return;
        }
        length_header += "Content-Length: ".length();
        int length_tail = ByteArrayUtil.byteArrayIndexof(buffer,
                "\r\n".getBytes(), length_header);
        if (length_tail != -1) {
            pos = ByteArrayUtil.byteArrayIndexof(buffer, "\r\n\r\n".getBytes(),
                    length_tail) + 4;
        }
        header.pos = pos;

        if ((length_header >= 0) && (pos >= 0)) {
            size = new String(buffer, length_header, length_tail
                    - length_header);
            try {
                header.count = Integer.parseInt(size);
            } catch (Exception e) {
                e.printStackTrace();
                header.count = -1;
            }
        }
    }

    @Override
    public void verifyFrame(int len, byte[] buffer, MediaFrame mframe,
                            MediaFrameHolder frameHolder) {

        int real_length = ByteArrayUtil.byteArrayIndexof(buffer, tail, 0);
        if ((real_length + 2) != len && (real_length + 3) != len) {
            mframe.clean();
            frameHolder.queue_free(mframe);
            return;
        }
        frameHolder.queue_filled(mframe);
    }

}