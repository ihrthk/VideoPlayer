package com.zhangls.videoplayer;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;


public class VideoViewBuffer extends Activity implements
        io.vov.vitamio.MediaPlayer.OnInfoListener, io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener {

    /**
     * TODO: Set the path variable to a streaming video URL or a local media file
     * path.
     */
    private String path = "http://live.3gv.ifeng.com/zixun.m3u8";
    private Uri uri;
    private VideoView mVideoView;
    private ProgressBar pb;
    private TextView downloadRateView, loadRateView;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (!LibsChecker.checkVitamioLibs(this))
            return;
        setContentView(R.layout.activity_main);
        mVideoView = (VideoView) findViewById(R.id.buffer);
        pb = (ProgressBar) findViewById(R.id.probar);

        downloadRateView = (TextView) findViewById(R.id.download_rate);
        loadRateView = (TextView) findViewById(R.id.load_rate);
        if (path == "") {
            // Tell the user to provide a media file URL/path.
            Toast.makeText(
                    VideoViewBuffer.this,
                    "Please edit VideoBuffer Activity, and set path"
                            + " variable to your media file URL/path", Toast.LENGTH_LONG).show();
            return;
        } else {
      /*
       * Alternatively,for streaming media you can use
       * mVideoView.setVideoURI(Uri.parse(URLstring));
       */
            uri = Uri.parse(path);
            mVideoView.setVideoURI(uri);
            mVideoView.setMediaController(new MediaController(this));
            mVideoView.requestFocus();
            mVideoView.setOnInfoListener(this);
            mVideoView.setOnBufferingUpdateListener(this);
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    // optional need Vitamio 4.0
                    mediaPlayer.setPlaybackSpeed(1.0f);
                }
            });
        }

    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    pb.setVisibility(View.VISIBLE);
                    downloadRateView.setText("");
                    loadRateView.setText("");
                    downloadRateView.setVisibility(View.VISIBLE);
                    loadRateView.setVisibility(View.VISIBLE);

                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                mVideoView.start();
                pb.setVisibility(View.GONE);
                downloadRateView.setVisibility(View.GONE);
                loadRateView.setVisibility(View.GONE);
                break;
            case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                downloadRateView.setText("" + extra + "kb/s" + "  ");
                break;
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        loadRateView.setText(percent + "%");
    }

}