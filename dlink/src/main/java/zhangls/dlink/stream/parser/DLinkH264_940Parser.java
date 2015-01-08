package zhangls.dlink.stream.parser;


import zhangls.dlink.ByteArrayUtil;
import zhangls.dlink.FrameHeader;
import zhangls.dlink.Loger;
import zhangls.dlink.StreamParser;

public class DLinkH264_940Parser extends StreamParser {

    private static final byte[] frameheader = {(byte) 0x33, (byte) 0x32,
            (byte) 0x39, (byte) 0x37, (byte) 0x14,};

    @Override
    public void readHeader(FrameHeader header, byte[] buffer, int width, int height) {
        header.reset();

        int start_header = ByteArrayUtil.byteArrayIndexof(buffer, frameheader,
                0);

        if (start_header == -1) {
            return;
        }
        if (buffer.length - start_header >= 20) {
            header.count = readCount(buffer[start_header + 12],
                    buffer[start_header + 13], buffer[start_header + 14],
                    buffer[start_header + 15], width, height);

            header.pos = start_header + 20;

            header.second = readTimeSec(buffer[start_header + 6],
                    buffer[start_header + 7], buffer[start_header + 8],
                    buffer[start_header + 9], buffer[start_header + 10],
                    buffer[start_header + 11]);

            header.microSecond = readTimeMSec(buffer[start_header + 6],
                    buffer[start_header + 7]);
        }
    }

    private int readCount(byte b1, byte b2, byte b3, byte b4,
                          int width, int height) {
        int a, b, c;
        a = ((b3 & 0xFE) >> 1) + ((b4 & 0x1) << 7);
        a = a << 16;
        b = ((b2 & 0xFE) >> 1) + ((b3 & 0x1) << 7);
        b = b << 8;
        c = ((b1 & 0xFE) >> 1) + ((b2 & 0x1) << 7);
        int length = a + b + c;
        Loger.d("lenght = " + length);
        if (length > (width * height * 2)) {
            return -1;
        }
        return length;
    }

    private int readTimeSec(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6) {
        byte[] temp = new byte[4];
        int total = 0;

        temp[0] = (byte) (((b3 & 0x80) >> 7) + ((b4 & 0x7F) << 1));
        temp[1] = (byte) (((b2 & 0x80) >> 7) + ((b3 & 0x7F) << 1));
        temp[2] = b5;
        temp[3] = b6;

        for (int i = 0; i < temp.length; i++) {
            total = total + byteToInt(temp[i]) << (3 - i) * 8;
        }

        return total;
    }

    private int readTimeMSec(byte b1, byte b2) {
        int a, b;
        a = ((b1 & 0xF8) >> 2) + ((b2 & 0x07) << 5);
        a = a << 2;
        b = (b1 & 06) >> 8;
        return (a + b);
    }

    private int byteToInt(byte b) {
        return Integer.parseInt(Integer.toBinaryString(b & 0xff), 2);
    }
}