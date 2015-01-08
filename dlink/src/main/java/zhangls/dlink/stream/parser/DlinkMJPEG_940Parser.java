package zhangls.dlink.stream.parser;


import zhangls.dlink.ByteArrayUtil;
import zhangls.dlink.FrameHeader;
import zhangls.dlink.Loger;
import zhangls.dlink.MediaFrame;
import zhangls.dlink.MediaFrameHolder;
import zhangls.dlink.StreamParser;

public class DlinkMJPEG_940Parser extends StreamParser {

    private byte[] tail = {(byte) 0xFF, (byte) 0xD9};
    private static final byte[] pre_header = {(byte) 0x33, (byte) 0x32,
            (byte) 0x39, (byte) 0x37, (byte) 0x14, (byte) 0x04};
    private static final byte[] pre_frameheader = {(byte) 0xFF, (byte) 0xD8};

    @Override
    public void readHeader(FrameHeader header, byte[] buffer) {

        int[] readerInfo = {-1, -1, -1, -1, -1, -1, -1};

        int start_header = ByteArrayUtil
                .byteArrayIndexof(buffer, pre_header, 0);
        if (start_header == -1) {
            return;
        }

        int start_frameheader = ByteArrayUtil.byteArrayIndexof(buffer,
                pre_frameheader, start_header);
        if (start_frameheader == -1) {
            return;
        }

        header.count = readCount(buffer[start_header + 12],
                buffer[start_header + 13], buffer[start_header + 14],
                buffer[start_header + 15]);

        header.pos = start_frameheader;
    }

    @Override
    public void verifyFrame(int len, byte[] buffer, MediaFrame mframe,
                            MediaFrameHolder frameHolder) {
        int real_length = ByteArrayUtil.byteArrayIndexof(buffer, tail, 0);
        if ((real_length + 2) != len) {
            mframe.clean();
            frameHolder.queue_free(mframe);
        } else {
            frameHolder.queue_filled(mframe);
        }
    }

    private int readCount(byte b1, byte b2, byte b3, byte b4) {
        int a, b, c;

        a = ((b3 & 0xFE) >> 1) + ((b4 & 0x1) << 7);
        a = a << 16;
        b = ((b2 & 0xFE) >> 1) + ((b3 & 0x1) << 7);
        b = b << 8;
        c = ((b1 & 0xFE) >> 1) + ((b2 & 0x1) << 7);

        int size = a + b + c;
        Loger.d("size = " + size);

        return size;
    }

}