package com.zhangls.videoplayer;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;


public class VideoViewBuffer extends Activity implements
        io.vov.vitamio.MediaPlayer.OnInfoListener, io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener {

    /**
     * TODO: Set the h264 variable to a streaming video URL or a local media file
     * h264.
     */
    private String h264 = "http://172.18.195.49:80/video/ACVS-H264.cgi?profileid=2";
    private String mjpeg = "http://172.18.195.26:80/mjpeg.cgi";
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

        auth();

        downloadRateView = (TextView) findViewById(R.id.download_rate);
        loadRateView = (TextView) findViewById(R.id.load_rate);
        if (h264 == "") {
            // Tell the user to provide a media file URL/h264.
            Toast.makeText(
                    VideoViewBuffer.this,
                    "Please edit VideoBuffer Activity, and set h264"
                            + " variable to your media file URL/h264", Toast.LENGTH_LONG).show();
            return;
        } else {
      /*
       * Alternatively,for streaming media you can use
       * mVideoView.setVideoURI(Uri.parse(URLstring));
       */
            uri = Uri.parse(mjpeg);
            mVideoView.setVideoURI(uri,getHeaders());
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

    public Map<String, String> getHeaders() {
        Header admin = BasicScheme.authenticate(
                new UsernamePasswordCredentials("admin", "111111"),
                "UTF-8", false);
        Map<String, String> map = new HashMap<String, String>();
        map.put(admin.getName(), admin.getValue());
        return map;
    }

    private void auth() {
        new Thread() {
            @Override
            public void run() {
                //第一步，创建HttpGet对象
                HttpGet httpGet = new HttpGet("http://172.18.195.49:80/video/ACVS-H264.cgi?profileid=2");

                //第二步，使用execute方法发送HTTP GET请求，并返回HttpResponse对象
                HttpResponse httpResponse = null;
                try {


//                    String userpass = "admin" + ":" + "111111";
//                    String basicAuth = "basic  " + Base64.encodeToString(userpass.getBytes("UTF-8"), Base64.DEFAULT);
//                    httpGet.addHeader("Authorization", basicAuth);
                    httpGet.addHeader(BasicScheme.authenticate(
                            new UsernamePasswordCredentials("admin", "111111"),
                            "UTF-8", false));
                    httpResponse = new DefaultHttpClient().execute(httpGet);
                    StatusLine statusLine = httpResponse.getStatusLine();
                    if (statusLine.getStatusCode() == 200) {
                        //第三步，使用getEntity方法活得返回结果
                        String result = EntityUtils.toString(httpResponse.getEntity());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
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
