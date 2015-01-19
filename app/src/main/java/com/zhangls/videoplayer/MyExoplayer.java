package com.zhangls.videoplayer;

import android.app.Activity;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.SampleSource;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.chunk.ChunkSampleSource;
import com.google.android.exoplayer.chunk.ChunkSource;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.chunk.FormatEvaluator;
import com.google.android.exoplayer.dash.DashChunkSource;
import com.google.android.exoplayer.dash.mpd.Representation;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.HttpDataSource;
import com.google.android.exoplayer.util.MimeTypes;

import org.apache.http.Header;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.auth.BasicScheme;


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

    public MediaCodecVideoTrackRenderer getTrackRenderer() {
        HttpDataSource videoDataSource = new HttpDataSource("us",
                HttpDataSource.REJECT_PAYWALL_TYPES, new DefaultBandwidthMeter());
        Header header = BasicScheme.authenticate(new UsernamePasswordCredentials("admin", "111111"),
                "UTF-8", false);
        videoDataSource.setRequestProperty(header.getName(), header.getValue());
        Format format = new Format("1", MimeTypes.VIDEO_H264, 320, 240, 1, 1, 1);
        String uriString = "http://192.168.0.110/video/ACVS-H264.cgi?profileid=3";
        ChunkSource videoChunkSource = new DashChunkSource(videoDataSource,
                new FormatEvaluator.AdaptiveEvaluator(new DefaultBandwidthMeter()),
                Representation.SingleSegmentRepresentation.newInstance(0L, -1L, "", -1L, format,
                        Uri.parse(uriString)
                        , 0L, 0L, 0L, 0L, -1L));


        SampleSource videoSampleSource = new ChunkSampleSource(videoChunkSource,
                new LoadControl() {
                    @Override
                    public void register(Object loader, int bufferSizeContribution) {

                    }

                    @Override
                    public void unregister(Object loader) {

                    }

                    @Override
                    public Allocator getAllocator() {
                        return null;
                    }

                    @Override
                    public void trimAllocator() {

                    }

                    @Override
                    public boolean update(Object loader, long playbackPositionUs, long nextLoadPositionUs, boolean loading, boolean failed) {
                        return false;
                    }
                }, VIDEO_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, true);
        return new MediaCodecVideoTrackRenderer(
                videoSampleSource, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
    }


}
