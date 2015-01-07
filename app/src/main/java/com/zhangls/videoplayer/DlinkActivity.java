package com.zhangls.videoplayer;

import android.app.Activity;
import android.os.Bundle;

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

/**
 * Created by BSDC-ZLS on 2015/1/7.
 */
public class DlinkActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dlink_view);
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
}
