package com.zhangls.videoplayer;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceView;

import com.dlink.mydlinkbase.MediaFrameHolder;
import com.dlink.mydlinkbase.VideoParseThread;
import com.dlink.mydlinkbase.VideoPlayThread;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;

/**
 * TODO
 * 1.兼容dlink h264格式
 * 2.ptz
 * 3.横竖屏切换
 * 4.视频流切换
 * 5.截屏/拍照
 * 6.音频
 * 7.eptz
 * 8.google统计
 * Created by BSDC-ZLS on 2015/1/7.
 */
public class DlinkActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dlink_view);

        SurfaceView videoSurfaceView = (SurfaceView) findViewById(R.id.video);
//        connect("http://192.168.0.110:80/video/ACVS-H264.cgi?profileid=3", "admin", "111111",
//                160, 320, 240, videoSurfaceView);
        connect("http://192.168.0.110:80/video/mjpg.cgi?profileid=4", "admin", "111111",
                161, 640, 480, videoSurfaceView);

        makeSurfaceTexture();
    }

    private SurfaceTexture makeSurfaceTexture() {
        SurfaceTexture mTexture;
        int[] mTextureId;
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        mTextureId = textures;
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId[0]);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        mTexture = new SurfaceTexture(mTextureId[0]);
        return mTexture;
    }

    private void connect(final String uri, final String admin, final String password,
                         final int parserType, final int width, final int height, final SurfaceView videoSurfaceView1) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    InputStream inputStream = (InputStream) msg.obj;
                    MediaFrameHolder videoFrameHolder = new MediaFrameHolder();
                    VideoParseThread videoParseThread = new VideoParseThread(inputStream, videoFrameHolder,
                            parserType, width, height, makeSurfaceTexture());
                    videoParseThread.start();
                    VideoPlayThread videoPlayThread = new VideoPlayThread(videoSurfaceView1, videoFrameHolder);
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
                            new UsernamePasswordCredentials(admin, password),
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
