package com.zhangls.videoplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by BSDC-ZLS on 2015/1/7.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void vitamio(View view) {
        startActivity(new Intent(this, VideoViewBuffer.class));
    }

    public void exoplayer(View view) {

    }

    public void dlink(View view) {
        startActivity(new Intent(this, DlinkActivity.class));
    }
}
