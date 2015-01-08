package com.dlink.mydlinkbase.parser;


import com.dlink.mydlinkbase.Loger;
import com.dlink.mydlinkbase.StreamParser;

public class StreamParserFactory {
    // videoParser
    public static final int MJPEG_93X = 0x0001;
    public static final int MJPEG_11X = 0x0002;
    public static final int H264_940 = 0x0003;
    public static final int MJPEG_940 = 0x0004;
    public static final int H264_942 = 0x0005;// 5211 5222
    public static final int MJPEG_942 = 0x0006;
    public static final int MJPEG_5230 = 0x0007;
    public static final int H264_NVR = 0x0008;
    public static final int MJPEG_NVR = 0x0009;
    public static final int H264_93X = 0x000A;

    public static final int H264_2132L = 0x00A0;// 2310 6010 7010
    public static final int MJPEG_2132 = 0x00A1;

    // AudioParser
    public static final int PCM = 0x1000;
    public static final int ULAW_940 = 0x1001;
    public static final int ADPCM = 0x1002;
    public static final int PCM_93X = 0x1003;

    public static StreamParser getStreamParser(int parserType) {

        Loger.d("parserType = " + parserType);

        switch (parserType) {
            case MJPEG_93X:
                return new DlinkMJPEG_93xParser();
            case H264_93X:
                return new DlinkH264_93xParser();
            case MJPEG_11X:
            case MJPEG_5230:
                return new DlinkMJPEG_11xParser();
            case H264_940:
                return new DLinkH264_940Parser();
            case MJPEG_940:
                return new DlinkMJPEG_940Parser();
            case H264_942:
            case H264_2132L:
                return new DLinkH264_942Parser();
            case MJPEG_942:
                return new DlinkMJPEG_942Parser();
            case MJPEG_2132:
                return new DlinkMJPEG_2132Parser();
//            case PCM:
//                return new DlinkPCMParser();
//            case ULAW_940:
//                return new DlinkULAW_940Parser();
            case ADPCM:
                return null;
            case PCM_93X:
                return new DlinkPCM_93xParser();
            default:
                return null;
        }

    }
}
