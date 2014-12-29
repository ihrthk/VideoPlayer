package zhangls.dlink;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;

public class VideoParseThread extends Thread {

    private final static int CONTENT_LOST_COUNT_CONENT_LIMIT = 60;

    private InputStream videoStream;
    private MediaFrameHolder frameHolder;

    private boolean StateRuning = true;

    private byte[] mjpegBuffer = new byte[2 * 1024];
    private byte[] h264Buffer = new byte[1024];

    private static final byte[] locker = new byte[0];
    private FrameHeader mFrameHeader;

    private int parserType;
    private int resolutionWidth;
    private int resolutionHeight;

    public VideoParseThread(InputStream in, MediaFrameHolder videoFrameHolder, int parserType, int width, int height) {
        // this.videoStream = new DataInputStream(in);
        this.videoStream = in;
        this.parserType = parserType;
        this.resolutionWidth = width;
        this.resolutionHeight = height;
        this.frameHolder = videoFrameHolder;
        mFrameHeader = new FrameHeader();
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
        try {
            Thread.sleep(50); // ensure refresh thread run
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        synchronized (locker) {
            StreamParser streamParser = StreamParserFactory
                    .getStreamParser(parserType);
            H264Decoder.InitDecoder(resolutionWidth, resolutionHeight);

            byte[] mPixel = new byte[resolutionWidth * resolutionHeight * 2];
            int lostFrameCount = 0;


            long countStart = System.currentTimeMillis();
            long countEnd;
            int count = 0;
            DecimalFormat df = new DecimalFormat("0.0");

            while (StateRuning) {
                try {
                    lostFrameCount = 0;
                    MediaFrame mframe = frameHolder.dequeue_free();
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
            }

            try {
                if (null != videoStream) {
                    videoStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            H264Decoder.UninitDecoder();
        }
    }

    private void getH264() {
        try {
            Thread.sleep(50); // ensure refresh thread run
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        synchronized (locker) {
            StreamParser streamParser = StreamParserFactory
                    .getStreamParser(parserType);
            H264Decoder.InitDecoder(resolutionWidth, resolutionHeight);
            byte[] mPixel = new byte[resolutionWidth * resolutionHeight * 2];
            int readFailCount = 0;
            int lostFrameCount = 0;
            boolean showLog = true;

            long countStart = System.currentTimeMillis();
            long countEnd;
            int count = 0;
            DecimalFormat df = new DecimalFormat("0.0");

            while (StateRuning) {
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
                        MediaFrame mframe = frameHolder.dequeue_free();
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
            }

            try {
                if (null != videoStream) {
                    videoStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            H264Decoder.UninitDecoder();
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
