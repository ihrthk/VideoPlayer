package zhangls.dlink;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;

import zhangls.dlink.stream.parser.StreamParserFactory;

public class VideoPlayThread extends Thread {
    private SurfaceView msurface;
    private MediaFrameHolder frameHolder;

    private boolean mRun = true;
    private int frameSizeLimit = 100 * 1024;

    private boolean snapshot;
    private boolean isWaiting;
    private Context context;
    private long mDrawStart, mDrawStop;

    private static Bitmap bmpCache;
    private int videoParserType;
    private int resolutionWidth;
    private int resolutionHeight;

    public synchronized static Bitmap getBmpCache() {
        return bmpCache;
    }

    public VideoPlayThread(SurfaceView surface,

                           MediaFrameHolder videoFrameHolder) {
        this.msurface = surface;
        this.frameHolder = videoFrameHolder;
    }

    public VideoPlayThread(VideoSurfaceView surface,

                           MediaFrameHolder videoFrameHolder, Context c) {
        this.msurface = surface;
        this.frameHolder = videoFrameHolder;
        this.context = c;
    }

    @Override
    public void run() {

        switch (videoParserType) {
            case StreamParserFactory.H264_940:
            case StreamParserFactory.H264_942:
            case StreamParserFactory.H264_NVR:
            case StreamParserFactory.H264_2132L:
            case StreamParserFactory.H264_93X:
//			Loger.d(LogTagConstant.VIDEO_DECODE_TAG, "Play the H264 video");
                playH264();
                break;
            default:
//			Loger.d(LogTagConstant.VIDEO_DECODE_TAG, "Play mjpeg video");
                playMJPEG();
                break;
        }
    }

    private void playH264() {
        int countNullTimes = 0;
        Loger.d("playH264");

        int countFrame = 0;
        long countStart, countEnd;

        int frameLen = 0;
        DecimalFormat df = new DecimalFormat("0.0");
        countStart = System.currentTimeMillis();
        boolean flag = true;
        boolean setEptz = true;

        while (mRun) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MediaFrame frame = frameHolder.dequeue_filled();
            if (frame == null) {
                Loger.d("frame == null");
                continue;
            }

            frameLen = frame.getlength();
            byte[] data = frame.getbytes();
            Bitmap showingBitmap = null;
            if (data != null) {
                SurfaceHolder holder = null;
                try {
                    showingBitmap = Bitmap.createBitmap(resolutionWidth, resolutionHeight, Config.RGB_565);
                    showingBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(data));
                    // updateThumbnail(showingBitmap);
//                    saveCurrentFrame(showingBitmap);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    showingBitmap = null;
                    continue;
                }
                Canvas c = null;
                try {

                    holder = msurface.getHolder();
                    c = holder.lockCanvas();
                    if (c != null) {
                        c.drawColor(Color.BLACK);
                    }
                    // Loger.d(LogTagConstant.VIDEO_DECODE_TAG,
                    // "H264 draw bitmap~~~");
                    if (mRun) {
                        if (c != null) {
                            c.drawBitmap(showingBitmap, null, null);
                        }
                    }

                    countNullTimes = 0;
                    countFrame++;

                } catch (NullPointerException e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    countNullTimes++;
                    if (countNullTimes > 10) {
                        mRun = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (c != null && holder != null) {
                        holder.unlockCanvasAndPost(c);
                    }
                }
            }
            frameHolder.queue_free(frame);
            // Loger.d("queue free frame into holder!");
            frame = null;

            countEnd = System.currentTimeMillis();
            if (countEnd - countStart >= 1000) {
                String mFPS = df.format((1000.0 * countFrame / (countEnd - countStart)));
//                camera.setFPS(mFPS);
                countFrame = 0;
                countStart = countEnd;
            }
            // if (showingBitmap != null) {
            // showingBitmap.recycle();
            // }
        }
        clearBackground();
    }

    private void playMJPEG() {
        int countNullTimes = 0;
        Loger.d("playMJPEG");
        int countFrame = 0;
        long countStart, countEnd;

        int frameLen = 0;
        DecimalFormat df = new DecimalFormat("0.0");
        countStart = System.currentTimeMillis();
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inTempStorage = new byte[16 * 1024];
        boolean flag = true;
        boolean setEptz = true;

        while (mRun) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MediaFrame frame = frameHolder.dequeue_filled();
            if (frame == null) {
                if (!mRun) {
                    Loger.d("MJPEG video get frame == null");
                }
                continue;
            }

            frameLen = frame.getlength();
            byte[] data = frame.getbytes();
            Bitmap showingBitmap = null;
            if (data != null) {

                int sampleSize;
                if (frameLen <= frameSizeLimit) {
                    sampleSize = 1;
                    opts.inSampleSize = sampleSize;
                } else {
                    double tmp = (double) frameLen / (double) frameSizeLimit;
                    tmp = Math.sqrt(tmp);
                    sampleSize = (int) Math.ceil(tmp);
                    opts.inSampleSize = sampleSize;
                }
                try {
                    showingBitmap = BitmapFactory.decodeByteArray(data, 0,
                            frameLen, opts);
                    // updateThumbnail(showingBitmap);
//                    saveCurrentFrame(showingBitmap);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    showingBitmap = null;
                }
                SurfaceHolder holder = null;
                Canvas c = null;
                try {


                    // Matrix matrix = getMatrix(msurface, showingBitmap, zoom);

                    holder = msurface.getHolder();
                    c = holder.lockCanvas();
                    c.drawColor(Color.BLACK);
                    if (mRun) {
                        c.drawBitmap(showingBitmap, new Matrix(), null);
                    }

                    countNullTimes = 0;
                    countFrame++;

                } catch (NullPointerException e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    countNullTimes++;
                    if (countNullTimes > 10) {
                        mRun = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (c != null && holder != null) {
                        holder.unlockCanvasAndPost(c);
                    }
                }
            }
            frameHolder.queue_free(frame);
            frame = null;

            countEnd = System.currentTimeMillis();
            if (countEnd - countStart >= 1000) {
                String mFPS = df
                        .format((1000.0 * countFrame / (countEnd - countStart)));
//                camera.setFPS(mFPS);
                countFrame = 0;
                countStart = countEnd;
            }
            // if (showingBitmap != null) {
            // showingBitmap.recycle();
            // }
        }
        clearBackground();
    }

    private void clearBackground() {
        try {
            SurfaceHolder holder = msurface.getHolder();
            Canvas c = holder.lockCanvas();
            c.drawColor(Color.BLACK);
            holder.unlockCanvasAndPost(c);
        } catch (Exception e) {
        }
    }

    public void changeSurface(SurfaceView surface) {
        this.msurface = surface;
    }

    public void changeSurface(VideoSurfaceView surface) {
        this.msurface = surface;
    }

    public void startSnapshot() {
        snapshot = true;
    }

    private Matrix getMatrix(SurfaceView msurface, Bitmap showingBitmap,
                             double zoom) {
        if (msurface == null || showingBitmap == null) {
            return null;
        }
        int targetWidth = msurface.getWidth();
        int targetHeight = msurface.getHeight();
        // int targetWidth = msurface.getMeasuredWidth();
        // int targetHeight = msurface.getMeasuredHeight();
        int currentWidth = showingBitmap.getWidth();
        int currentHeight = showingBitmap.getHeight();

        if (targetWidth <= 0 || targetHeight <= 0 || currentWidth <= 0
                || currentHeight <= 0) {
            return null;
        }
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) targetWidth) / currentWidth;
        float scaleHeight = ((float) targetHeight) / currentHeight;
        boolean isTranslateX;
        float translateX;
        float translateY;
        if (scaleWidth > scaleHeight) {
            isTranslateX = true;
            matrix.postScale(scaleHeight, scaleHeight);
            translateX = (targetWidth - scaleHeight * currentWidth) / 2;
            matrix.postTranslate(translateX, 0);
        } else {
            isTranslateX = false;
            matrix.postScale(scaleWidth, scaleWidth);
            translateY = (targetHeight - scaleWidth * currentHeight) / 2;
            matrix.postTranslate(0, translateY);
        }

        if (zoom != 1) {
            float scale = scaleWidth;
            if (isTranslateX) {
                scale = scaleHeight;
            }
            matrix.reset();
            matrix.postScale((float) zoom * scale, (float) zoom * scale);
            translateX = (targetWidth - (float) zoom * scale * currentWidth) / 2;
            translateY = (targetHeight - (float) zoom * scale * currentHeight) / 2;
            matrix.postTranslate(translateX, translateY);
        }
        return matrix;
    }

    public void pauseVideo() {
        isWaiting = true;
    }

    private void saveCurrentFrame(Bitmap showingBitmap) {
        if (showingBitmap != null) {
            if (bmpCache == null) {
                bmpCache = showingBitmap;
            } else {
                synchronized (bmpCache) {
                    bmpCache.recycle();
                    bmpCache = showingBitmap;
                }
            }
        }
    }
}
