package com.dlink.mydlinkbase.media;

public class H264Decoder {

    static {
        System.loadLibrary("H264Android");
    }

    public static native int InitDecoder(int width, int height);

    public static native int UninitDecoder();

    public static native int DecoderNal(byte[] in, int insize, byte[] out);

}
