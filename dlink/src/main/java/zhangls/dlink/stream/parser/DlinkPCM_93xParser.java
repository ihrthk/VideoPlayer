package zhangls.dlink.stream.parser;

import zhangls.dlink.ByteArrayUtil;
import zhangls.dlink.FrameHeader;
import zhangls.dlink.StreamParser;

public class DlinkPCM_93xParser extends StreamParser {

    private static final byte[] frameheader = {(byte) 0x52, (byte) 0x49,
            (byte) 0x46, (byte) 0x46};

    @Override
    public void readHeader(FrameHeader header, byte[] buffer) {
        int pos;

        int length_header = ByteArrayUtil.byteArrayIndexof(buffer, frameheader,
                0);
        if (length_header == -1) {
            return;
        }

        if (buffer.length - length_header >= 44) {
            pos = length_header + 44;
            header.count = 1024;
            header.channel = (short) countChannel(buffer[length_header + 22],
                    buffer[length_header + 23]);
            header.rate = countRate(buffer[length_header + 24],
                    buffer[length_header + 25], buffer[length_header + 26],
                    buffer[length_header + 27]);
            header.bit = (short) countBit(buffer[length_header + 34],
                    buffer[length_header + 35]);
        } else {
            pos = -1;
        }
        header.pos = pos;
    }

    private int countChannel(byte b1, byte b2) {
        int a, b;
        a = byteToInt(b2) << 8;
        b = byteToInt(b1);
        return a + b;
    }

    private int countRate(byte b1, byte b2, byte b3, byte b4) {
        int a, b, c, d;
        a = byteToInt(b4) << 24;
        b = byteToInt(b3) << 16;
        c = byteToInt(b2) << 8;
        d = byteToInt(b1);
        return a + b + c + d;
    }

    private int countBit(byte b1, byte b2) {
        int a, b;
        a = byteToInt(b2) << 8;
        b = byteToInt(b1);
        return a + b;
    }

    private int byteToInt(byte b) {
        return Integer.parseInt(Integer.toBinaryString(b & 0xff), 2);
    }

}