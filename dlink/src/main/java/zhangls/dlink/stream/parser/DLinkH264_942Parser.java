package zhangls.dlink.stream.parser;


import zhangls.dlink.ByteArrayUtil;
import zhangls.dlink.FrameHeader;
import zhangls.dlink.StreamParser;

public class DLinkH264_942Parser extends StreamParser {

    private static final byte[] frameheader = {(byte) 0x00, (byte) 0x00,
            (byte) 0x01, (byte) 0xF5};

    @Override
    public void readHeader(FrameHeader header, byte[] buffer, int width, int height) {
        header.reset();
        int pos;

        int length_header = ByteArrayUtil.byteArrayIndexof(buffer, frameheader,
                0);

        if (length_header == -1) {
            return;
        }

        if (buffer.length - length_header >= 40) {
            pos = length_header + 40;
            header.count = countLength(buffer[length_header + 8],
                    buffer[length_header + 9], buffer[length_header + 10],
                    buffer[length_header + 11], width, height);
            header.remain = buffer.length - header.count - pos;
            if (header.remain > 0) {
                System.arraycopy(buffer, pos + header.count, header.buffer, 0,
                        header.remain);
            }
            header.second = countSec(buffer[length_header + 16],
                    buffer[length_header + 17], buffer[length_header + 18],
                    buffer[length_header + 19]);
            header.microSecond = countUSec(buffer[length_header + 20],
                    buffer[length_header + 21], buffer[length_header + 22],
                    buffer[length_header + 23]);
            // readerInfo[4] = countShowPara(buffer[length_header + 30],
            // buffer[length_header + 31]);
            // readerInfo[5] = countShowPara(buffer[length_header + 32],
            // buffer[length_header + 33]);
            // readerInfo[6] = countShowPara(buffer[length_header + 34],
            // buffer[length_header + 35]);
        } else {
            pos = -1;
        }

        header.pos = pos;
    }

    private int countSec(byte b1, byte b2, byte b3, byte b4) {
        int a, b, c, d;
        a = byteToInt(b4) << 24;
        b = byteToInt(b3) << 16;
        c = byteToInt(b2) << 8;
        d = byteToInt(b1);
        return a + b + c + d;
    }

    private int countUSec(byte b1, byte b2, byte b3, byte b4) {
        int a, b, c, d;
        a = byteToInt(b4) << 24;
        b = byteToInt(b3) << 16;
        c = byteToInt(b2) << 8;
        d = byteToInt(b1);
        return a + b + c + d;
    }

    private int countLength(byte b1, byte b2, byte b3, byte b4, int width, int height) {
        int a, b, c, d;
        a = byteToInt(b4) << 24;
        b = byteToInt(b3) << 16;
        c = byteToInt(b2) << 8;
        d = byteToInt(b1);
        int length = a + b + c + d;
        if (length > (width * height * 2)) {
            return -1;
        }
        // Loger.d("length = " + length);
        return length;
    }

    private int countShowPara(byte b1, byte b2) {
        int a, b;
        a = byteToInt(b2) << 8;
        b = byteToInt(b1);
        return a + b;
    }

    private int byteToInt(byte b) {
        return Integer.parseInt(Integer.toBinaryString(b & 0xff), 2);
    }
}