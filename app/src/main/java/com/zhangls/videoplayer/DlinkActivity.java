package com.zhangls.videoplayer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.android.exoplayer.VideoSurfaceView;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;

import zhangls.dlink.MediaFrameHolder;
import zhangls.dlink.VideoParseThread;
import zhangls.dlink.VideoPlayThread;

/**
 * Created by BSDC-ZLS on 2015/1/7.
 */
public class DlinkActivity extends Activity {

    VideoSurfaceView videoSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dlink_view);

        videoSurfaceView = (VideoSurfaceView) findViewById(R.id.video);
        connect("http://172.18.195.49:80/video/ACVS-H264.cgi?profileid=2", 0, 2, 2);
    }

    private void connect(final String uri, final int parserType, final int width, final int height) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    InputStream inputStream = (InputStream) msg.obj;
                    MediaFrameHolder videoFrameHolder = new MediaFrameHolder();
                    VideoParseThread videoParseThread = new VideoParseThread(inputStream, videoFrameHolder,
                            parserType, width, height);
                    videoParseThread.start();
                    VideoPlayThread videoPlayThread = new VideoPlayThread(videoSurfaceView, videoFrameHolder);
                    videoPlayThread.start();
                }
            }
        };


        new Thread() {
            @Override
            public void run() {
                Message message = Message.obtain();
                //第一步，创建HttpGet对象
                HttpGet httpGet = new HttpGet(uri);
                //第二步，使用execute方法发送HTTP GET请求，并返回HttpResponse对象
                HttpResponse httpResponse = null;
                try {
                    message.what = 1;

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
                        message.obj = httpResponse.getEntity().getContent();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    message.what = 0;
                    message.obj = null;
                }
                handler.sendMessage(message);
            }
        }.start();
    }
}
