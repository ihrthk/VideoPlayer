package com.dlink.mydlinkbase.parser;

import com.dlink.mydlinkbase.ByteArrayUtil;
import com.dlink.mydlinkbase.FrameHeader;
import com.dlink.mydlinkbase.Loger;
import com.dlink.mydlinkbase.MediaFrame;
import com.dlink.mydlinkbase.MediaFrameHolder;
import com.dlink.mydlinkbase.StreamParser;

public class DlinkMJPEG_93xParser extends StreamParser {
    @SuppressWarnings("unused")
    private static final byte[] tail = {(byte) 0xFF, (byte) 0xD9};

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
        if (length_tail == -1) {
            return;
        }

        int frame_header = ByteArrayUtil.byteArrayIndexof(buffer,
                "jpeg".getBytes(), 0);
        if (frame_header == -1) {
            return;
        }

        int frame_tail = ByteArrayUtil.byteArrayIndexof(buffer,
                "\r\n".getBytes(), frame_header);
        if (frame_tail == -1) {
            pos = -1;
        } else {
            pos = frame_tail + 4;
        }
        header.pos = pos;

        if ((length_header >= 0) && (pos >= 0)) {
            size = new String(buffer, length_header + 4, length_tail
                    - length_header - 4);
            Loger.d("size = " + size);
            try {
                header.count = Integer.parseInt(size);
            } catch (Exception e) {
                header.count = -1;
            }
        }
    }

    @Override
    public void verifyFrame(int len, byte[] buffer, MediaFrame mframe,
                            MediaFrameHolder frameHolder) {
        // TODO 932LB1 no tail
        // int real_length = ByteArrayUtil.byteArrayIndexof(buffer, tail, 0);
        // if ((real_length + 2) != len) {
        // mframe.clean();
        // frameHolder.queue_filled(mframe);
        // } else {
        frameHolder.queue_filled(mframe);
        // }
    }

}