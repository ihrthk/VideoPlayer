package com.dlink.mydlinkbase;


import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import com.dlink.mydlinkbase.media.H264Decoder;
import com.dlink.mydlinkbase.parser.StreamParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;

public class VideoParseThread extends Thread {

    private final static int CONTENT_LOST_COUNT_CONENT_LIMIT = 60;

    private InputStream videoStream;
    private MediaFrameHolder frameHolder;

    private SurfaceTexture surfaceTexture;
    private Surface surface;
    private MediaCodec videoDecoder;

    private boolean StateRuning = true;

    private byte[] mjpegBuffer = new byte[2 * 1024];
    private byte[] h264Buffer = new byte[1024];

    private static final byte[] locker = new byte[0];
    private FrameHeader mFrameHeader;

    private int parserType;
    private int resolutionWidth;
    private int resolutionHeight;

    public VideoParseThread(InputStream in, MediaFrameHolder videoFrameHolder, int parserType, int width, int height, SurfaceTexture texture) {
        // this.videoStream = new DataInputStream(in);
        this.videoStream = in;
        this.parserType = parserType;
        this.resolutionWidth = width;
        this.resolutionHeight = height;
        this.frameHolder = videoFrameHolder;
        mFrameHeader = new FrameHeader();
        this.surface = new Surface(texture);
    }


    @Override
    public void run() {
        switch (parserType) {
            case StreamParserFactory.H264_940:
            case StreamParserFactory.H264_942:
            case StreamParserFactory.H264_2132L:
                getH264();
                break;
            case StreamParserFactory.H264_93X:
                get93xH264();
                break;
            default:
                getMjpeg();
                break;
        }
    }

    private void get93xH264() {

        ByteBuffer[] inputBuffers = null;
        ByteBuffer[] outputBuffers = null;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        byte[] mPixel = null;
        try {
            Thread.sleep(50); // ensure refresh thread run
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        synchronized (locker) {
            StreamParser streamParser = StreamParserFactory
                    .getStreamParser(parserType);

            try {
                videoDecoder = MediaCodec.createDecoderByType("video/avc");
                videoDecoder.configure(MediaFormat.createVideoFormat("video/avc", resolutionWidth, resolutionHeight), surface, null, 0);
                videoDecoder.start();
                Loger.i("[[video/avc]] decoder started");
                inputBuffers = videoDecoder.getInputBuffers();
                outputBuffers = videoDecoder.getOutputBuffers();
                mPixel = new byte[resolutionWidth * resolutionHeight * 2];
            } catch (IOException e) {
                e.printStackTrace();
            }

            int lostFrameCount = 0;


            long countStart = System.currentTimeMillis();
            long countEnd;
            int count = 0;
            DecimalFormat df = new DecimalFormat("0.0");
            MediaFrame mframe;
            while (StateRuning) {
                try {
                    lostFrameCount = 0;
                    mframe = frameHolder.dequeue_free();
                    if (mframe == null) {
                        Loger.d("dequeue_free frame is " + mframe);
                        continue;
                    }
                    int readCount = streamParser.readFrame(mframe, null, 0, 0,
                            0, videoStream);

                    count += mframe.getlength();
                    countEnd = System.currentTimeMillis();
                    if (countEnd - countStart > 1000) {
                        String mBPS = df
                                .format((1000.0 * count * 8 / (countEnd - countStart)) / 1024);
//                        camera.setKBPS(mBPS);
                        countStart = countEnd;
                        count = 0;
                    }

                    Loger.d("Read count is " + readCount);
                    if (readCount >= 0) {
                        H264Decoder.DecoderNal(mframe.getbytes(),
                                mframe.getlength(), mPixel);

                        mframe.clean();
                        mframe.append(mPixel, 0, mPixel.length);
                        frameHolder.queue_filled(mframe);
                    } else {
                        Loger.d("VideoParseThread", "93x lostFrameCount is "
                                + lostFrameCount);
                        lostFrameCount++;
//                        if (lostFrameCount >= CONTENT_LOST_COUNT_CONENT_LIMIT) {
//                            notifyObserver(AppEnum.Stream_Video_Unexpected_Header);
//                        }
                    }
                } catch (SocketTimeoutException e) {
                    Loger.d("HttpVideoStream socketTimeoutException");
//                    notifyObserver(AppEnum.Stream_Video_Time_Out);
                    e.printStackTrace();
                    break;
                } catch (SocketException e) {
//                    notifyObserver(AppEnum.Stream_Video_Time_Out);
                    Loger.d("HttpVideoStream in SocketException");
                    e.printStackTrace();
                    break;
                } catch (IOException e) {
//                    notifyObserver(AppEnum.Stream_Video_Time_Out);
                    Loger.d("other Exception in HttpVideoStream");
                    e.printStackTrace();
                    break;
                }

                if (mframe.getlength() > 0) {
                    int inIdx = videoDecoder.dequeueInputBuffer(10000);
                    if (inIdx >= 0) {
                        ByteBuffer buffer = inputBuffers[inIdx];
                        buffer.clear();
                        buffer.put(mframe.getbytes(), 0, mframe.getlength());
                        buffer.flip();
                        videoDecoder.queueInputBuffer(inIdx, 0, mframe.getlength(), 0, 0);
                    }

                    int outIdx = videoDecoder.dequeueOutputBuffer(info, 10000);
                    switch (outIdx) {
                        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                            outputBuffers = videoDecoder.getOutputBuffers();
                            frameHolder.queue_free(mframe);
                            break;
                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                            frameHolder.queue_free(mframe);
                            break;
                        case MediaCodec.INFO_TRY_AGAIN_LATER:
                            frameHolder.queue_free(mframe);
                            break;
                        default:
                            ByteBuffer buffer = outputBuffers[outIdx];
                            videoDecoder.releaseOutputBuffer(outIdx, true); /* this will render to surface directly */
                            /* Just a dummy push to video play thread so that it can perform all
                               other functions other than rendering to surface */
                            frameHolder.queue_filled(mframe);
                            break;
                    }
                }
            }

            try {
                if (null != videoStream) {
                    videoStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            videoDecoder.stop();
            videoDecoder.release();
        }
    }

    private void getH264() {

        ByteBuffer[] inputBuffers = null;
        ByteBuffer[] outputBuffers = null;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

        try {
            Thread.sleep(50); // ensure refresh thread run
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        synchronized (locker) {
            StreamParser streamParser = StreamParserFactory
                    .getStreamParser(parserType);
            try {
                videoDecoder = MediaCodec.createDecoderByType("video/avc");
                videoDecoder.configure(MediaFormat.createVideoFormat("video/avc", resolutionWidth, resolutionHeight), surface, null, 0);
                videoDecoder.start();
                Loger.i("[[video/avc]] decoder started");
                inputBuffers = videoDecoder.getInputBuffers();
                outputBuffers = videoDecoder.getOutputBuffers();
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] mPixel = new byte[resolutionWidth * resolutionHeight * 2];
            int readFailCount = 0;
            int lostFrameCount = 0;
            boolean showLog = true;

            long countStart = System.currentTimeMillis();
            long countEnd;
            int count = 0;
            DecimalFormat df = new DecimalFormat("0.0");

            while (StateRuning) {
                MediaFrame mframe = null;
                try {
                    if (mFrameHeader.remain > 0) {
                        System.arraycopy(mFrameHeader.buffer, 0, h264Buffer, 0,
                                mFrameHeader.remain);
                    } else {
                        mFrameHeader.remain = 0;
                    }
                    int readCount = Utils.readDataToVideoBuffer(videoStream,
                            h264Buffer, mFrameHeader.remain, h264Buffer.length
                                    - mFrameHeader.remain);
                    readCount += mFrameHeader.remain;

//                    count += readCount;
                    // int readCount = Utils.readDataToBuffer(videoStream,
                    // h264Buffer);
                    if (readCount == -1) {
                        Loger.d("VideoParseThread", "H264 readFailCount is "
                                + readFailCount);
                        readFailCount++;
                        if (readFailCount > CONTENT_LOST_COUNT_CONENT_LIMIT) {
//                            notifyObserver(AppEnum.Stream_Video_Time_Out);
                            break;
                        } else {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            continue;
                        }
                    }
                    readFailCount = 0;
                    streamParser.readHeader(mFrameHeader, h264Buffer);

                    if (mFrameHeader.count > 0) {
                        lostFrameCount = 0;
                        mframe = frameHolder.dequeue_free();
                        if (mframe == null) {
                            Loger.d("dequeue_free frame is " + mframe);
                            streamParser.skipFrame(h264Buffer, readCount,
                                    mFrameHeader.pos, mFrameHeader.count,
                                    videoStream);
                            continue;
                        }
                        mframe.setSecond(mFrameHeader.second);
                        mframe.setMicrosecond(mFrameHeader.microSecond);
                        readCount = streamParser.readFrame(mframe, h264Buffer,
                                readCount, mFrameHeader.pos,
                                mFrameHeader.count, videoStream);
                        count += mframe.getlength();
                        countEnd = System.currentTimeMillis();
                        if (countEnd - countStart > 1000) {
                            String mBPS = df
                                    .format((1000.0 * count * 8 / (countEnd - countStart)) / 1024);
//                            camera.setKBPS(mBPS);
                            countStart = countEnd;
                            count = 0;
                        }
                        if (readCount >= 0) {
                            H264Decoder.DecoderNal(mframe.getbytes(),
                                    mframe.getlength(), mPixel);
                            mframe.clean();
                            mframe.append(mPixel, 0, mPixel.length);
                            frameHolder.queue_filled(mframe);
                            if (showLog) {
                                showLog = false;
                                Loger.d("have decode the first frame!");
                            }
                        }

                    } else {
                        Loger.d("VideoParseThread", "H264 lostFrameCount is " + lostFrameCount);
                        lostFrameCount++;
                        if (lostFrameCount >= CONTENT_LOST_COUNT_CONENT_LIMIT) {
//                            notifyObserver(AppEnum.Stream_Video_Unexpected_Header);
                            break;
                        }
                    }
                } catch (SocketTimeoutException e) {
//                    notifyObserver(AppEnum.Stream_Video_Time_Out);
                    Loger.d("HttpVideoStream socketTimeoutException");
                    e.printStackTrace();
                    break;
                } catch (SocketException e) {
//                    notifyObserver(AppEnum.Stream_Video_Time_Out);
                    Loger.d("HttpVideoStream in SocketException");
                    e.printStackTrace();
                    break;
                } catch (IOException e) {
//                    notifyObserver(AppEnum.Stream_Video_Time_Out);
                    Loger.d("other Exception in HttpVideoStream");
                    e.printStackTrace();
                    break;
                }
                if ((mframe != null) && (mframe.getlength() > 0)) {
                    int inIdx = videoDecoder.dequeueInputBuffer(10000);
                    if (inIdx >= 0) {
                        //Loger.w ("QueueInputBuffer with length = " + mframe.getlength());
                        ByteBuffer buffer = inputBuffers[inIdx];
                        buffer.clear();
                        buffer.put(mframe.getbytes(), 0, mframe.getlength());
                        buffer.flip();
                        videoDecoder.queueInputBuffer(inIdx, 0, mframe.getlength(), 0, 0);
                    }

                    int outIdx = videoDecoder.dequeueOutputBuffer(info, 10000);
                    //Loger.w ("Dequeue output buffer, idx = " + outIdx);
                    switch (outIdx) {
                        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                            outputBuffers = videoDecoder.getOutputBuffers();
                            frameHolder.queue_free(mframe);
                            break;
                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                            frameHolder.queue_free(mframe);
                            break;
                        case MediaCodec.INFO_TRY_AGAIN_LATER:
                            frameHolder.queue_free(mframe);
                            break;
                        default:
                            ByteBuffer buffer = outputBuffers[outIdx];
                            videoDecoder.releaseOutputBuffer(outIdx, true); /* this will render to surface directly */
                            /* Just a dummy push to video play thread so that it can perform all
                               other functions other than rendering to surface */
                            frameHolder.queue_filled(mframe);
                            break;
                    }
                }
            }


            try {
                if (null != videoStream) {
                    videoStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            videoDecoder.stop();
            videoDecoder.release();
        }
    }

    private void getMjpeg() {
        try {
            Thread.sleep(50); // ensure refresh thread run
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        synchronized (locker) {
            StreamParser streamParser = StreamParserFactory
                    .getStreamParser(parserType);
            int readFailCount = 0;
            int lostFrameCount = 0;

            long countStart = System.currentTimeMillis();
            long countEnd;
            int count = 0;
            DecimalFormat df = new DecimalFormat("0.0");

            while (StateRuning) {
                try {
                    int size = mjpegBuffer.length;
                    mjpegBuffer = new byte[size];
                    int readCount = Utils.readDataToVideoBuffer(videoStream,
                            mjpegBuffer);
                    if (readCount == -1) {
//                        Loger.d("VideoParseThread", "Mjpeg readFailCount is "
//                                + readFailCount);
                        readFailCount++;
                        if (readFailCount > CONTENT_LOST_COUNT_CONENT_LIMIT) {
//                            notifyObserver(AppEnum.Stream_Video_Time_Out);
                            break;
                        } else {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            continue;
                        }
                    }
                    readFailCount = 0;
                    streamParser.readHeader(mFrameHeader, mjpegBuffer);

                    if (mFrameHeader.count > 0) {
                        lostFrameCount = 0;
                        MediaFrame mframe = frameHolder.dequeue_free();
                        if (mframe == null) {
                            Loger.d("dequeue_free frame is " + mframe);
                            streamParser.skipFrame(mjpegBuffer, readCount,
                                    mFrameHeader.pos, mFrameHeader.count,
                                    videoStream);
                            continue;
                        }
                        mframe.setSecond(mFrameHeader.second);
                        mframe.setMicrosecond(mFrameHeader.count);
                        readCount = streamParser.readFrame(mframe, mjpegBuffer,
                                readCount, mFrameHeader.pos,
                                mFrameHeader.count, videoStream);

                        count += mframe.getlength();
                        countEnd = System.currentTimeMillis();
                        if (countEnd - countStart > 1000) {
                            String mBPS = df
                                    .format((1000.0 * count * 8 / (countEnd - countStart)) / 1024);
//                            camera.setKBPS(mBPS);
                            countStart = countEnd;
                            count = 0;
                        }

                        if (readCount >= 0) {
                            streamParser.verifyFrame(readCount, mjpegBuffer,
                                    mframe, frameHolder);
                        }

                    } else {
                        lostFrameCount++;
                        Loger.d("VideoParseThread", "Mjpeg lostFrameCount is "
                                + lostFrameCount);
                        if (lostFrameCount >= CONTENT_LOST_COUNT_CONENT_LIMIT) {
//                            notifyObserver(AppEnum.Stream_Video_Unexpected_Header);
                            break;
                        }

                    }
                } catch (SocketTimeoutException e) {
                    Loger.d("HttpVideoStream socketTimeoutException");
//                    notifyObserver(AppEnum.Stream_Video_Time_Out);
                    e.printStackTrace();
                    break;
                } catch (SocketException e) {
//                    notifyObserver(AppEnum.Stream_Video_Time_Out);
                    Loger.d("HttpVideoStream in SocketException");
                    e.printStackTrace();
                    break;
                } catch (IOException e) {
//                    notifyObserver(AppEnum.Stream_Video_Time_Out);
                    Loger.d("other Exception in HttpVideoStream");
                    e.printStackTrace();
                    break;
                }
            }

            try {
                if (null != videoStream) {
                    videoStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setState(boolean state) {
        Loger.d("Set HttpVideoStream  State = " + state);
        this.StateRuning = state;
    }

//    private void notifyObserver(AppEnum ERROR_MSG) {
//        try {
//            mListener.onListener(ERROR_MSG);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
