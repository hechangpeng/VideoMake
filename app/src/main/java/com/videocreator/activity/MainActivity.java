package com.videocreator.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.videocreator.MyProgressDialog;
import com.videocreator.OnFinishListener;
import com.videocreator.VideoMaker;
import com.videocreator.example.R;

import java.util.concurrent.Executors;

/**
 * Date：2018/4/19
 * Author：HeChangPeng
 */

public class MainActivity extends Activity {
    private MyProgressDialog mProgressDialog;
    private VideoMaker videoMaker;

    private static class InterHandler extends Handler {

    }

    public static final InterHandler MHANDLER = new InterHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressDialog = new MyProgressDialog(this);
        videoMaker = new VideoMaker(new OnFinishListener() {
            @Override
            public void onVideoMakeStart() {
                MHANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.show();
                    }
                });
            }

            @Override
            public void onVideoMakeFinish(final boolean isSuccess) {
                MHANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        Toast.makeText(MainActivity.this, isSuccess ? "success" : "fail", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onProgressIn(final int percent) {
                MHANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.setProgress(percent);
                    }
                });
            }
        });

        findViewById(R.id.record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ScreenRecordActivity.class));
            }
        });

        findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CameraRecordActivity.class));
            }
        });

        findViewById(R.id.make).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        videoMaker.makeVideo(MainActivity.this);
                    }
                });
            }
        });

        findViewById(R.id.exoplayer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ExoPlayerActivity.class));
            }
        });
    }
}
