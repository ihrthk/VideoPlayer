package com.zhangls.videoplayer;

import android.app.Activity;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;


public class MyExoplayer extends Activity implements SurfaceHolder.Callback {

    private static final int VIDEO_BUFFER_SEGMENTS = 200;
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;


    SurfaceView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exoplayer);

        videoView = (SurfaceView) findViewById(R.id.video);
        videoView.getHolder().addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        // 1. Instantiate the player.
        ExoPlayer exoplayer = ExoPlayer.Factory.newInstance(1);
        // 2. Construct renderers.
        String string = "http://mofunsky-video.qiniudn.com/90/193/2014082012541850633.webm";
//        String string = "http://live.3gv.ifeng.com/zixun.m3u8";
        FrameworkSampleSource frameworkSampleSource = new FrameworkSampleSource(this, Uri.parse(string), null, 1);
        TrackRenderer videoRender = new MediaCodecVideoTrackRenderer(frameworkSampleSource, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        // 3. Inject the renderers through prepare
        exoplayer.prepare(videoRender);
        // 4. Pass the surface to the video renderer.
        exoplayer.sendMessage(videoRender, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE,
                holder.getSurface());
        // 5. Start playback.
        exoplayer.setPlayWhenReady(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


}
