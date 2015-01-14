package com.zhangls.videoplayer;

import android.app.Activity;
import android.media.MediaCodec;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.chunk.ChunkSampleSource;
import com.google.android.exoplayer.chunk.ChunkSource;
import com.google.android.exoplayer.chunk.FormatEvaluator;
import com.google.android.exoplayer.dash.DashChunkSource;
import com.google.android.exoplayer.upstream.BufferPool;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;

import java.io.IOException;


public class MyExoplayer extends Activity {

    private static final int VIDEO_BUFFER_SEGMENTS = 200;
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exoplayer);

        ChunkSource videoChunkSource = new DashChunkSource(new DataSource() {
            @Override
            public long open(DataSpec dataSpec) throws IOException {
                return 0;
            }

            @Override
            public void close() throws IOException {

            }

            @Override
            public int read(byte[] buffer, int offset, int readLength) throws IOException {
                return 0;
            }
        }, new FormatEvaluator.FixedEvaluator());
        LoadControl loadControl = new DefaultLoadControl(new BufferPool(BUFFER_SEGMENT_SIZE));
        ChunkSampleSource videoSampleSource = new ChunkSampleSource(videoChunkSource, loadControl,
                VIDEO_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, true, new Handler(), new ChunkSampleSource.EventListener() {
            @Override
            public void onLoadStarted(int sourceId, String formatId, int trigger, boolean isInitialization, int mediaStartTimeMs, int mediaEndTimeMs, long length) {

            }

            @Override
            public void onLoadCompleted(int sourceId, long bytesLoaded) {

            }

            @Override
            public void onLoadCanceled(int sourceId, long bytesLoaded) {

            }

            @Override
            public void onUpstreamDiscarded(int sourceId, int mediaStartTimeMs, int mediaEndTimeMs, long bytesDiscarded) {

            }

            @Override
            public void onUpstreamError(int sourceId, IOException e) {

            }

            @Override
            public void onConsumptionError(int sourceId, IOException e) {

            }

            @Override
            public void onDownstreamDiscarded(int sourceId, int mediaStartTimeMs, int mediaEndTimeMs, long bytesDiscarded) {

            }

            @Override
            public void onDownstreamFormatChanged(int sourceId, String formatId, int trigger, int mediaTimeMs) {

            }
        }, 1);
        ExoPlayer exoplayer = ExoPlayer.Factory.newInstance(1);
        TrackRenderer videoRender = new MediaCodecVideoTrackRenderer(videoSampleSource, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        exoplayer.prepare(videoRender);
    }


}
